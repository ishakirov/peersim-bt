package peersim.shakirovbittorrent;

import peersim.core.Node;

public class IntMessage extends SimpleMessage {
	
	private int value;
	
	public int getValue() {
		return this.value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public IntMessage(int type, Node sender, int value) {
		this.type = type;
		this.sender = sender;
		this.value = value;
	}
}
