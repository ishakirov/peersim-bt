package peersim.shakirovbittorrent;

import peersim.core.*;
import peersim.config.*;
import peersim.edsim.*;
import peersim.transport.*;

import java.util.*;

public class BitTorrent implements EDProtocol {
	
	private BitSet pieces;
	public ArrayList<Neighbor> neighbors;
	private Node tracker;
	private int tid;
	private long id;
	private boolean malicious;
	private boolean seeder;
	private int fileSize;
	private int swarmSize;
	private int maxBandwidth;
	private int lastInterested;
	private ArrayList<Integer> pendingRequests;
	private int fstHeur = 0;
	private int sndHeur = 0;
	
	public void setBandwidth(int bandwidth) {
		this.maxBandwidth = bandwidth;
	}
	
	public int getBandwidth() {
		return this.maxBandwidth;
	}
	
	public Node getTracker() {
		return this.tracker;
	}
	
	public void setTracker(Node tracker) {
		this.tracker = tracker;
	}
	
	public boolean getMalicious() {
		return this.malicious;
	}
	
	public void setMalicious(boolean malicious) {
		this.malicious = malicious;
	}
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public boolean getSeeder() {
		return this.seeder;
	}
	
	public void setSeeder(boolean seeder) {
		this.seeder = seeder;
		if (this.seeder) {
			for (int i = 0; i < this.fileSize; i++) {
				this.pieces.set(i);
			}
		}
	}
	
	public int getNeighborsSize() {
		return this.neighbors.size();
	}
	
	Neighbor findNeighborById(long id) {
		for (Neighbor neighbor : neighbors) {
			if (neighbor.node.getID() == id) {
				return neighbor;
			}
		}
		return null;
	}
	
	public BitSet getFileStatus(){
		return this.pieces;
	}
	
	public BitTorrent(String prefix) {
		
		this.tid = Configuration.getPid(prefix + "." + Constants.PAR_TRANSPORT);
		this.swarmSize = (int)Configuration.getInt(prefix + "." + Constants.PAR_SWARM);
		this.fileSize = (int)Configuration.getInt(prefix + "." + Constants.PAR_SIZE);
		this.fstHeur = (int)Configuration.getInt(prefix + "." + Constants.PAR_FIRST_HEUR);
		this.sndHeur = (int)Configuration.getInt(prefix + "." + Constants.PAR_SECOND_HEUR);
		this.neighbors = new ArrayList<Neighbor>();
		this.pieces = new BitSet(this.fileSize);
		if (!this.seeder) {
			for (int i = 0; i < fileSize; i++) {
				this.pieces.clear(i);
			}
		}
		this.pendingRequests = new ArrayList<Integer>();
	}
	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		
		SimpleEvent e = (SimpleEvent)event;
		long now = CommonState.getTime();
		Node sender;
		long latency;
		
		switch (e.getType()) {
		case Constants.PING:
			sender = ((SimpleMessage)e).getSender();
			SimpleMessage pong = new SimpleMessage(Constants.PONG, node);
			latency = ((Transport)node.getProtocol(tid)).getLatency(node, sender);
			EDSimulator.add(latency, pong, sender, pid);
			
			//System.out.println(String.format("%d : PONG %d", node.getID(), sender.getID()));
			
			if (findNeighborById(sender.getID()) != null) {
				findNeighborById(sender.getID()).Sent();
			}
			break;
			
		case Constants.PONG:
			sender = ((SimpleMessage)e).getSender();
			if (findNeighborById(sender.getID()) != null) {
				findNeighborById(sender.getID()).Alive();
			}
			break;
			
		case Constants.TRACKER:
			sender = ((SimpleMessage)e).getSender();
			ArrayList<Neighbor> temp = new ArrayList<Neighbor>();
			for (int i = 0; i < this.swarmSize; i++) {
				if (this.neighbors.get(i).node.getID() != sender.getID()) {
					temp.add(this.neighbors.get(i));
				}
			}
			PeerSetMessage peerset = new PeerSetMessage(node, temp);
			latency = ((Transport)node.getProtocol(tid)).getLatency(node, sender);
			EDSimulator.add(latency, peerset, sender, pid);
			break;
			
		case Constants.PEERSET:
			System.out.println("neighbors size:"+this.neighbors.size());
			sender = ((SimpleMessage)e).getSender();
			ArrayList<Neighbor> receivedPeerset = ((PeerSetMessage)e).getPeerSet();
			for (int i = 0; i < receivedPeerset.size(); i++) {
				latency = ((Transport)node.getProtocol(tid)).getLatency(node, sender);
				BitfieldMessage bitfield = new BitfieldMessage(node, this.pieces, false, true);
				EDSimulator.add(latency, bitfield, receivedPeerset.get(i).node, pid);
			}
			System.out.println("neighbors size:"+this.neighbors.size());
			break;
		
		case Constants.BITFIELD:
			sender = ((SimpleMessage)event).getSender();
			BitSet fileStatus = (BitSet) ((BitfieldMessage)event).getPieces().clone();
			boolean response = ((BitfieldMessage)event).getResponse();
			boolean ack = ((BitfieldMessage)event).getAck();
			if (response) {
				if (ack) {
					Neighbor neigh = new Neighbor(sender);
					neigh.Alive();
					this.neighbors.add(neigh);
					this.neighbors.get(this.neighbors.size() - 1).setPieces(fileStatus);
					int interestedPiece = this.getNextPiece();
					this.lastInterested = interestedPiece;
					if (interestedPiece != -1) {
						
						IntMessage interested = new IntMessage(Constants.INTERESTED, node, interestedPiece);
						for (int i = 0; i < this.neighbors.size(); i++) {
							latency = ((Transport)node.getProtocol(tid)).getLatency(node, this.neighbors.get(i).node);
							EDSimulator.add(latency, interested, this.neighbors.get(i).node, pid);	
						}
						
					}
				} else {
					// 
				}
			} else {
				if (ack) {
					if (this.neighbors.size() < this.swarmSize) {
						Neighbor neigh = new Neighbor(sender);
						neigh.Alive();
						this.neighbors.add(neigh);
						this.neighbors.get(this.neighbors.size() - 1).setPieces(fileStatus);
						latency = ((Transport)node.getProtocol(tid)).getLatency(node, sender);
						BitfieldMessage bitfield = new BitfieldMessage(node, this.pieces, true, true);
						EDSimulator.add(latency, bitfield, sender, pid);
					} else {
						latency = ((Transport)node.getProtocol(tid)).getLatency(node, sender);
						BitfieldMessage bitfield = new BitfieldMessage(node, this.pieces, true, false);
						EDSimulator.add(latency, bitfield, sender, pid);
					}
				} else {
					
				}
			}
			
			break;
			
		case Constants.INTERESTED:
			sender = ((SimpleMessage)event).getSender();
			if (this.findNeighborById(sender.getID()) != null) {
				this.findNeighborById(sender.getID()).interested = ((IntMessage)event).getValue();
			}
			//System.out.println(String.format("%d : interested %d from %d", this.id, ((IntMessage)event).getValue(), sender.getID()));
			break;
			
		case Constants.CHECKALIVE_TIME:
			SimpleMessage ping = new SimpleMessage(Constants.PING, node);
			Iterator<Neighbor> i = neighbors.iterator();
			
			while (i.hasNext()) {
				Neighbor neighbor = i.next();
				if (neighbor.lastSent < now - 121000) {
					latency = ((Transport)node.getProtocol(tid)).getLatency(node, neighbor.node);
					EDSimulator.add(latency, ping, neighbor.node, pid);
					//System.out.println(String.format("%d : PING %d", node.getID(), neighbor.node.getID()));
				} else if (neighbor.lastSeen < now - 121000) {
					i.remove();
				}
			}
			SimpleEvent checkalive = new SimpleEvent(Constants.CHECKALIVE_TIME);
			EDSimulator.add(120000, checkalive, node, pid);
			break;
			
		case Constants.CHOKE_TIME:
			for (int v = 0; v < this.pendingRequests.size(); v++) {
				Neighbor qqq = this.findNeighborById(this.pendingRequests.get(v));
				if (qqq != null) {
					qqq.fouls ++;
					if (qqq.fouls > 5) {
						IntMessage bad = new IntMessage(Constants.BAD_PEER, node, (int) qqq.node.getID());
						for (int rr = 0; rr < this.neighbors.size(); rr++) {
							latency = ((Transport)node.getProtocol(tid)).getLatency(node, this.neighbors.get(rr).node);
							EDSimulator.add(latency, bad, this.neighbors.get(rr).node, pid);
						}
					}
				}
			}
			Object chokeTime = new SimpleEvent(Constants.CHOKE_TIME);
			EDSimulator.add(10000, chokeTime, node, pid);
			
			ArrayList<Neighbor> list = new ArrayList<Neighbor>();
			
			for (int k = 0; k < this.neighbors.size(); k++) {
				if (this.neighbors.get(k).interested >= 0 && !this.neighbors.get(k).isSeeder() && this.neighbors.get(k).fouls < 6) {
					list.add(this.neighbors.get(k));
					// fst heur;
					if (this.fstHeur == 1) {
						list.get(list.size() - 1).maxBandwidth = 0;
						Node remote = list.get(list.size()-1).node;
						for (int m = 0; m < ((BitTorrent)remote.getProtocol(pid)).neighbors.size(); m++) {
							list.get(list.size() - 1).maxBandwidth += ((BitTorrent)remote.getProtocol(pid)).neighbors.get(m).maxBandwidth;
						}
					}
				}				
			}
			Collections.sort(list);
			if (this.sndHeur == 1 && this.neighbors.size() > 0) {
				long lucky = this.unchokeHeur2(pid);
				int ch = 0;
				for (int r = 0; r < this.neighbors.size(); r++) {
					if (lucky == this.neighbors.get(r).node.getID()) {
						ch = r;
						break;
					}
				}
				Neighbor a = this.neighbors.get(0);
				this.neighbors.set(0, this.neighbors.get(ch));
				this.neighbors.set(ch, a);
				
			}
			int k = 0;
			SimpleMessage choke = new SimpleMessage(Constants.CHOKE, node);
			SimpleMessage unchoke = new SimpleMessage(Constants.UNCHOKE, node);
			for (; k < Math.min(list.size(), 3); k++) {
				latency = ((Transport)node.getProtocol(tid)).getLatency(node, list.get(k).node);
				EDSimulator.add(latency, unchoke, list.get(k).node, pid);
			}
			for (;k < list.size(); k++) {
				latency = ((Transport)node.getProtocol(tid)).getLatency(node, list.get(k).node);
				EDSimulator.add(latency, choke, list.get(k).node, pid);
			}
			break;
		case Constants.BAD_PEER:
			sender = ((SimpleMessage)event).getSender();
			int vval = ((IntMessage)event).getValue();
			if (this.findNeighborById(vval) != null) {
				this.findNeighborById(vval).fouls += 5;
			}
			break;
		case Constants.CHOKE:
			sender = ((SimpleMessage)event).getSender();
			//System.out.println(String.format("%d : choke from %d", this.id, sender.getID()));
			if (this.findNeighborById(sender.getID()) != null) {
				this.findNeighborById(sender.getID()).Alive();
				this.findNeighborById(sender.getID()).setChoke(true);
				
			}
			break;
		case Constants.UNCHOKE:
			sender = ((SimpleMessage)event).getSender();
			//System.out.println(String.format("%d : unchoke from %d", this.id, sender.getID()));
			Neighbor q = this.findNeighborById(sender.getID()); 
			if (q != null) {
				q.Alive();
				q.setChoke(false);
				if (this.lastInterested != -1 && q.fouls < 6) {
					if (q.pieces.get(this.lastInterested)) {
						latency = ((Transport)node.getProtocol(tid)).getLatency(node, sender);
						IntMessage im = new IntMessage(Constants.REQUEST, node, this.lastInterested);
						EDSimulator.add(latency, im, sender, pid);
						this.pendingRequests.add(new Integer((int)sender.getID()));
					}
				}
			}
			break;
		case Constants.REQUEST:
			/// AHAHAHAHA IM THE MOST EVIL PERSON!
			if (this.malicious) return;
			sender = ((SimpleMessage)event).getSender();
			System.out.println(String.format("%d : REQUEST %d from %d", this.id, ((IntMessage)event).getValue(), sender.getID()));
			latency = ((Transport)node.getProtocol(tid)).getLatency(node, sender);
			IntMessage im = new IntMessage(Constants.PIECE, node, ((IntMessage)event).getValue());
			EDSimulator.add(latency, im, sender, pid);
			int time = (16384 / Math.min(((BitTorrent)sender.getProtocol(pid)).maxBandwidth, this.maxBandwidth)) * 1000;
			latency = ((Transport)node.getProtocol(tid)).getLatency(node, sender);
			IntMessage download = new IntMessage(Constants.DOWNLOAD_COMPLETED, node, ((IntMessage)event).getValue());
			EDSimulator.add(latency+time, download, sender, pid);
			break;
			
		case Constants.PIECE:
			sender = ((SimpleMessage)event).getSender();
			IntMessage notinterested = new IntMessage(Constants.NOT_INTERESTED, node, lastInterested);
			for (int j = 0; j < this.neighbors.size(); j++) {
				latency = ((Transport)node.getProtocol(tid)).getLatency(node, this.neighbors.get(j).node);
				EDSimulator.add(latency, notinterested, this.neighbors.get(j).node, pid);
			}
			for (int v = 0; v < this.pendingRequests.size(); v++) {
				if (this.pendingRequests.get(v) == sender.getID()) {
					this.pendingRequests.remove(v);
					break;
				}
			}
			break;
			
		case Constants.DOWNLOAD_COMPLETED:
			sender = ((SimpleMessage)event).getSender();
			int value = ((IntMessage)event).getValue();
			this.pieces.set(value);
			int interestedPiece = this.getNextPiece();
			this.lastInterested = interestedPiece;
			IntMessage haveMsg = new IntMessage(Constants.HAVE, node, value);
			for (int w = 0; w < this.neighbors.size(); w++) {
				latency = ((Transport)node.getProtocol(tid)).getLatency(node, this.neighbors.get(w).node);
				EDSimulator.add(latency, haveMsg, this.neighbors.get(w).node, pid);
			}
			if (interestedPiece != -1) {
				
				IntMessage interested = new IntMessage(Constants.INTERESTED, node, interestedPiece);
				for (int j = 0; j < this.neighbors.size(); j++) {
					latency = ((Transport)node.getProtocol(tid)).getLatency(node, this.neighbors.get(j).node);
					EDSimulator.add(latency, interested, this.neighbors.get(j).node, pid);	
				}
			}
			break;
		case Constants.HAVE:
			sender = ((SimpleMessage)event).getSender();
			int val = ((IntMessage)event).getValue();
			Neighbor n = this.findNeighborById(sender.getID());
			if (n!=null) n.pieces.set(val);
			break;
		}		
		return;
	}
	
	public boolean addNeighbor(Node neighbor) {
		if (this.findNeighborById(neighbor.getID()) != null) {
			return false;
		}
		if (this.tracker == null) {
			this.neighbors.add(new Neighbor(neighbor));
		}
		
		return true;
	}
	
	@Override
	public Object clone() {
		Object prot = null;
		try {
			prot = (BitTorrent)super.clone();
		}
		catch(CloneNotSupportedException e) {}
		
		((BitTorrent)prot).neighbors = new ArrayList<Neighbor>();
		((BitTorrent)prot).pendingRequests = new ArrayList<Integer>();
		((BitTorrent)prot).pieces = new BitSet(fileSize);
		
		return prot;
	}
	
	public int getNextPiece() {
		System.out.print("i m searching for next piece..");
		if (this.getNumCompletedPieces() < 4) { // random first piece
			System.out.println("randomly from 1 to " + fileSize);
			return CommonState.r.nextInt(this.fileSize);
		}
		if (this.getNumCompletedPieces() == fileSize) {
			System.out.println(this.id + " : COMPLETED AT " + CommonState.getTime());
			return -1;
		}
		//rarest first
		int rarest[] = new int[fileSize];
		for (int i = 0; i < fileSize; i++) {
			rarest[i] = 0;
		}
		for (int i = 0; i < this.neighbors.size(); i++) { 
			for (int j = 0; j < this.neighbors.get(i).pieces.size(); j++) {
				if (this.neighbors.get(i).pieces.get(j)) {
					rarest[j]++;
				}
			}
		}
		
		System.out.print(this.id+":rarest ");
		for (int q = 0; q < fileSize; q++) {
			System.out.print(rarest[q]+ "-"+this.pieces.get(q) + " ");
		}
		System.out.println(" :: " + this.getNumCompletedPieces());
		
		int min = 2000000000;
		int i_min = 0;
		for (int i = 0; i < fileSize; i++) {
			if (!this.pieces.get(i) && min > rarest[i]) {
				min = rarest[i];
				i_min = i;
			}
		}
		System.out.println("rarest");
		return i_min;
	}
	
	public int getNumCompletedPieces() {
		System.out.print("im " + this.id + ": ");
		int sum = 0;
		for (int i = 0; i < this.pieces.size(); i++) {
			if (this.pieces.get(i)) {
				sum++;
			}
		}
		System.out.println("("+this.pieces.get(5)+")"+"sum="+sum);
		return sum;
	}
	
	private int heur2_maxans = -1;
	private long heur2_unchoke = -1;
	
	public void dfs(BitTorrent v, long prev, int curdeep, int deep, int pid, int ans, long unchoke) {
		if (curdeep > deep) {
			return;
		}
		if (deep == 1) {
			unchoke = v.id; 
		}
		if (deep != 0) {
			for (int i = 0; i < v.neighbors.size(); i++) {
				if (v.neighbors.get(i).interested >= 0 && v.neighbors.get(i).node.getID() != prev) {
					ans ++;
				}
			}
		}
		if (deep != 0 && ans > this.heur2_maxans) {
			this.heur2_maxans = ans;
			this.heur2_unchoke = unchoke;
		}
		for (int i = 0; i < v.neighbors.size(); i++) {
			if (v.neighbors.get(i).interested >= 0 && v.neighbors.get(i).node.getID() != prev) {
				dfs((BitTorrent)v.neighbors.get(i).node.getProtocol(pid), v.id, curdeep + 1, deep, pid, ans, unchoke);
			}
		}
		
	}
	public long unchokeHeur2(int pid) {
		dfs(this, this.id, 0, 3, pid, 0, 0);
		return this.heur2_unchoke;
	}
}
