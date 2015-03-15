package peersim.shakirovbittorrent;

public class SimpleEvent {
	
	protected int type;
	
	public SimpleEvent() {
		this.type = 0;
	}
	
	public SimpleEvent(int type) {
		this.type = type;
	}
	
	public int getType() {
		return this.type;
	}
	
}
