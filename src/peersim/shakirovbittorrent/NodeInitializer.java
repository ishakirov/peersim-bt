package peersim.shakirovbittorrent;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;

public class NodeInitializer {
	
	private int seederDistr;
	private int maliciousDistr;
	private int pid;
	
	public NodeInitializer(String prefix){
		pid = Configuration.getPid(prefix + "." + Constants.PAR_PROT);
		seederDistr = Configuration.getInt(prefix + "." + Constants.PAR_SEEDER_DISTR);
		maliciousDistr = Configuration.getInt(prefix + "." + Constants.PAR_MALICIOUS_DISTR);
	}
	
	public void initialize(Node n) {
		Node tracker = Network.get(0);
		BitTorrent p;
		p = (BitTorrent)n.getProtocol(pid);
		p.setTracker(tracker);
		p.setId(n.getID());
		boolean v = setSeederStatus(p);
		if (!v) setMalicious(p);
		setBandwidth(p);
	}
	
	public boolean setSeederStatus(BitTorrent p) {
		int value = CommonState.r.nextInt(100);
		if (value + 1 < seederDistr) {
			System.out.println("Seeder setted ("+p.getId()+")");
			p.setSeeder(true);
			return true;
		} else {
			p.setSeeder(false);
			return false;
		}
	}
	
	public void setMalicious(BitTorrent p) {
		int value = CommonState.r.nextInt(100);
		if (value + 1 < maliciousDistr) {
			p.setMalicious(true);
		} else {
			p.setMalicious(false);
		}
	}
	
	public void setBandwidth(BitTorrent p) {
		int value = CommonState.r.nextInt(8);
		switch (value) {
		case 0:
			p.setBandwidth(128);
			break;
		case 1:
			p.setBandwidth(256);
			break;
		case 2:
			p.setBandwidth(512);
			break;
		case 3:
			p.setBandwidth(1024);
			break;
		case 4:
			p.setBandwidth(2048);
			break;
		case 5:
			p.setBandwidth(4096);
			break;
		case 6:
			p.setBandwidth(8192);
			break;
		case 7:
			p.setBandwidth(16384);
			break;
		}
		return;
	}
	
}
