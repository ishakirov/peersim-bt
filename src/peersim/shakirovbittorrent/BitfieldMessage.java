package peersim.shakirovbittorrent;

import java.util.BitSet;

import peersim.core.Node;

public class BitfieldMessage extends SimpleMessage {
	private BitSet pieces;
	private boolean response;
	private boolean ack;
	
	public BitfieldMessage(Node sender, BitSet pieces, boolean response, boolean ack) {
		this.type = Constants.BITFIELD;
		this.sender = sender;
		this.pieces = pieces;
		this.response = response;
		this.ack = ack;
	}
	
	public BitSet getPieces() {
		return this.pieces;
	}
	
	public boolean getResponse() {
		return this.response;
	}
	
	public boolean getAck() {
		return this.ack;
	}
	
}
