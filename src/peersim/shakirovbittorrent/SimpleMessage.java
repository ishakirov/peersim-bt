package peersim.shakirovbittorrent;

import peersim.core.Node;

public class SimpleMessage extends SimpleEvent {
	
	protected Node sender;
	
	public SimpleMessage() {
		this.sender = null;
		this.type = 0;
	}
	
	public SimpleMessage(int type, Node sender) {
		this.sender = sender;
		this.type = type;
	}
	
	public Node getSender() {
		return this.sender;	
	}
}
