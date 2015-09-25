package edu.lsu.cct.swp;

public class Link {
	private int src;
	private int dest;
	private boolean phantom;
	
	Link(int source, int dst, int which){
		src = source;
		dest = dst;
		phantom = false;
		Message msg = new Message(MessageType.CreateLink,src, dst);
		msg.send();
	}
	
	public int getDest() {
		return dest;
	}
	
	public void setPhantom() {
		phantom = true;
	}
	
	public void delete(int collapseId) {
		Message msg = new Message(MessageType.Delete, src, dest);
		msg.setWhich();
		msg.setPhantom();
		msg.setCollapseId(collapseId);
		msg.send();
	}
	
	public void phantomize(int collapseId) {
		Message msg = new Message(MessageType.Phantomize, src, dest);
		msg.setWhich();
		msg.setPhantom();
		setPhantom();
		msg.setCollapseId(collapseId);
		msg.send();
	}
	
	public void tryRecover(int collapseId) {
		Message msg = new Message(MessageType.Recover, src, dest);
		msg.setCollapseId(collapseId);
		msg.send();
	}

	public void build(int collapseId) {
		Message msg = new Message(MessageType.Build, src, dest);
		msg.setCollapseId(collapseId);
		msg.setWhich();
		msg.send();
	}
	

}
