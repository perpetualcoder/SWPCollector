package edu.lsu.cct.swp;

public class Link {
	private int src;
	private int dest;
	private boolean phantom;
	private int which;
	
	Link(int source, int dst, int which){
		src = source;
		dest = dst;
		phantom = false;
		this.which = which;
		Message msg = new Message(MessageType.CreateLink,src, dst);
		msg.send();
	}
	
	public int getDest() {
		return dest;
	}
	
	public void setWhich(int which) {
		this.which = which;
	}
	
	public int getWhich() {
		return which;
	}
	
	public void setPhantom() {
		phantom = true;
	}
	
	public void unsetPhantom() {
		phantom = false;
	}
	
	public boolean getPhantom() {
		return phantom;
	}
	
	public void delete(CollapseId collapseId) {
		Message msg = new Message(MessageType.Delete, src, dest);
		msg.setWhich(which);
		if (phantom) msg.setPhantom();
		msg.setCollapseId(collapseId);
		msg.send();
		msg.printMsg("receiving");
	}
	
	public void plagueDelete(CollapseId collapseId) {
		Message msg = new Message(MessageType.PlagueDelete, src, dest);
		msg.setWhich(which);
		msg.setPhantom();
		msg.setCollapseId(collapseId);
		msg.send();
		msg.printMsg("receiving");
	}
	
	public void phantomize(CollapseId collapseId, CollapseId Override) {
		Message msg = new Message(MessageType.Phantomize, src, dest);
		msg.setWhich(which);
		if (getPhantom()) {
			msg.setAlready(true);
		}
		else
			msg.setAlready(false);
		msg.setPhantom();
		setPhantom();
		msg.setCollapseId(collapseId);
		msg.setOverride(Override);
		msg.send();
		msg.printMsg("receiving");
	}
	
	public void recover(CollapseId collapseId) {
		Message msg = new Message(MessageType.Recover, src, dest);
		msg.setCollapseId(collapseId);
		msg.send();
		msg.printMsg("receiving");
	}

	public void build(CollapseId collapseId) {
		Message msg = new Message(MessageType.Build, src, dest);
		msg.setCollapseId(collapseId);
		msg.setWhich();
		msg.send();
		msg.printMsg();
		msg.printMsg("receiving");
	}
	

}
