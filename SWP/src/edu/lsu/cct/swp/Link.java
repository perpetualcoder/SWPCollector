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
	
	public int getDest() {
		return dest;
	}
	
	public void setWhich(int w) {
		which = w;
	}
	
	public void setPhantom() {
		phantom = true;
	}
	
	public void delete(int collapseId) {
		Message msg = new Message(MessageType.Delete, src, dest);
		msg.setWhich(which);
		msg.setPhantom();
		msg.setCollapseId(collapseId);
		msg.send();
	}
	
	public void phantomize(int collapseId) {
		Message msg = new Message(MessageType.Phantomize, src, dest);
		msg.setWhich(which);
		msg.setPhantom();
		setPhantom();
		msg.setCollapseId(collapseId);
		msg.send();
	}
	
	public void tryRecover(int collapseId) {
		Message msg = new Message(MessageType.TryRecovery, src, dest);
		msg.setCollapseId(collapseId);
		msg.send();
	}
	

}
