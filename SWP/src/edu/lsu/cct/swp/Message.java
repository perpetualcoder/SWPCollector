package edu.lsu.cct.swp;

public class Message {
	private MessageType type;
	private int src;
	private int dest;
	private int which;

	static Master master;

	Message(MessageType type, int src, int dest) {
		this.type = type;
		this.src = src;
		this.dest = dest;
	}

	public boolean send() {
		Node dst = master.getNode(this.dest);
		assert (dst != null);
		dst.getQu().add(this);
		return true;
	}
	
	public void setWhich(int w) {
		which = w;
	}
	
	public int getWhich() {
		return which;
	}
	
	public MessageType getType(){
		return type;
	}
	
	public int getSrc(){
		return src;
	}
}
