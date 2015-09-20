package edu.lsu.cct.swp;

import junit.framework.TestCase;

public class Message {
	private MessageType type;
	private int src;
	private int dest;
	private int which;
	private boolean phantom;
	private int collapseId;
	
	static public Master master;

	Message(MessageType type, int src, int dest) {
		this.type = type;
		this.src = src;
		this.dest = dest;
		this.phantom = false;
	}

	public boolean send() {
		assert(master != null);
		Node dst = master.getNode(this.dest);
		assert (dst != null);
		dst.getQu().add(this);
		return true;
	}
	
	public void setWhich(int w) {
		which = w;
	}
	
	public void setCollapseId(int id) {
		collapseId = id;
	}
	
	public int getCollapseId() {
		return collapseId;
	}
	
	public int getWhich() {
		return which;
	}
	
	public void setPhantom() {
		phantom =  true;
	}
	
	public boolean getPhantom() {
		return phantom;
	}
	
	public MessageType getType(){
		return type;
	}
	
	public int getSrc(){
		return src;
	}
}
