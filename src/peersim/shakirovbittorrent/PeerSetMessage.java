package peersim.shakirovbittorrent;

import java.util.ArrayList;

import peersim.core.Node;

public class PeerSetMessage extends SimpleMessage {
	
	private ArrayList<Neighbor> neighbors;
	
	public PeerSetMessage() {
		this.type = Constants.PEERSET;
		neighbors = null;
		sender = null;
	}
	
	public PeerSetMessage(Node sender, ArrayList<Neighbor> neighbors) {
		this.type = Constants.PEERSET;
		this.neighbors = neighbors;
		this.sender = sender;
	}
	
	public ArrayList<Neighbor> getPeerSet(){
		return this.neighbors;	
	}
}
