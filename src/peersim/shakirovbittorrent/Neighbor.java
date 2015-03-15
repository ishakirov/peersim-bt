package peersim.shakirovbittorrent;

import java.util.BitSet;

import peersim.core.CommonState;
import peersim.core.Node;

public class Neighbor implements Comparable<Neighbor> {
	
	public Node node;
	public BitSet pieces;
	public boolean choked;
	public int interested;
	public long lastSeen;
	public long lastSent;
	public int maxBandwidth;
	public int fouls;
	
	public void Alive() {
		this.lastSeen = CommonState.getTime();
	}
	
	public void Sent() {
		this.lastSent = CommonState.getTime();
	}
	
	public Neighbor() {	}
	
	public Neighbor(Node node) {
		this.node = node;
		this.choked = true;
		this.lastSeen = CommonState.getTime();
		this.lastSent = CommonState.getTime();
		this.fouls = 0;
	}
	
	public void setChoke(boolean choked) {
		this.choked = choked;
	}
	
	public void setInterested(int interested) {
		this.interested = interested;
	}
	
	public void setPieces(BitSet pieces) {
		this.pieces = (BitSet) pieces.clone();
	}
	
	public boolean isSeeder() {
		for (int i = 0; i < this.pieces.size(); i++) {
			if (!this.pieces.get(i)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int compareTo(Neighbor compareNeighbor) {
		return compareNeighbor.maxBandwidth - this.maxBandwidth; //desc
	}
	
}
