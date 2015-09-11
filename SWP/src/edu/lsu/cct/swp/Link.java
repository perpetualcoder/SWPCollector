package edu.lsu.cct.swp;

public class Link {
	private int src;
	private int dest;
	private boolean which;
	private boolean phantom;
	
	Link(int source, int dst, boolean which){
		src = source;
		dest = dst;
		this.which = which;
		phantom = false;
		Message msg = new Message(MessageType.CreateLink,src, dst);
	}

}
