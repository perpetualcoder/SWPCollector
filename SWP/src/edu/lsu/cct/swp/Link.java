package edu.lsu.cct.swp;

public class Link {
	private int src;
	private int dest;
	private int which;
	private boolean phantom;
	
	Link(int source, int dst, int which){
		src = source;
		dest = dst;
		this.which = 0;
		phantom = false;
		Message msg = new Message(MessageType.CreateLink,src, dst);
		msg.send();
	}
	
	public void setWhich(int w) {
		which = w;
	}
	
	public void delete() {
		Message msg = new Message(MessageType.Delete, src, dest);
		msg.setWhich(which);
		msg.send();
		
	}
	

}
