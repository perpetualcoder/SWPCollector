package edu.lsu.cct.swp;

public class Link {
	private int src;
	private int dest;
	private boolean phantom;
	private int which;
	
	Link(int source, int dst, int which, long myWeight){
		src = source;
		dest = dst;
		phantom = false;
		this.which = which;
		Message msg = new Message(MessageType.CreateLink,src, dst, myWeight, myWeight);
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
	
	public void delete(CollapseId collapseId, long myWeight) {
		Message msg = new Message(MessageType.Delete, src, dest, myWeight, myWeight);
		msg.setWhich(which);
		if (phantom) msg.setPhantom();
		msg.setCollapseId(collapseId);
		msg.send();
		msg.printMsg("receiving");
	}
	
	public void plagueDelete(CollapseId collapseId, long myWeight) {
		Message msg = new Message(MessageType.PlagueDelete, src, dest, myWeight, myWeight);
		msg.setWhich(which);
		msg.setPhantom();
		msg.setCollapseId(collapseId);
		msg.send();
		msg.printMsg("receiving");
	}
	
	public void phantomize(CollapseId collapseId, CollapseId Override, long old, long noob) {
		Message msg = new Message(MessageType.Phantomize, src, dest, old, noob);
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
	
	public void recover(CollapseId collapseId, long old) {
		Message msg = new Message(MessageType.Recover, src, dest, old, old);
		msg.setCollapseId(collapseId);
		msg.send();
		msg.printMsg("receiving");
	}

	public void build(CollapseId collapseId, long old, long noob) {
		Message msg = new Message(MessageType.Build, src, dest, old, noob);
		msg.setCollapseId(collapseId);
		msg.setWhich();
		msg.send();
		msg.printMsg("receiving");
	}
	

}
