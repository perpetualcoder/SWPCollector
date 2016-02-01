package edu.lsu.cct.swp;

import junit.framework.TestCase;

public class Message {
	private MessageType type;
	private int src;
	private int dest;
	private int which;
	private boolean phantom;
	private boolean already;
	public boolean isAlready() {
		return already;
	}
	private boolean decrcc;
	
	public void setDecRcc() {
		decrcc = true;
	}
	
	public boolean getDecrcc(){
		return decrcc;
	}
	

	public void setAlready(boolean already) {
		this.already = already;
	}

	private CollapseId collapseId;
	private boolean startOver;
	private CollapseId Override;
	private long myOldWeight;
	private long myNewWeight;
	
	static public Master master;

	Message(MessageType type, int src, int dest, long old, long noob) {
		this.type = type;
		this.src = src;
		this.dest = dest;
		this.phantom = false;
		this.myOldWeight = old;
		this.myNewWeight = noob;
	}
	
	public long getOld(){
		return myOldWeight;
	}
	
	public long getNew(){
		return myNewWeight;
	}
	
	public void printMsg() {
		String coll = collapseId!=null ?collapseId.toString():"null";
		String ov = Override!=null?Override.toString():"null";
		System.out.println(" Node "+dest+" is processing msg "+ type.toString()+
				" from "+src+ "which = "+which+" phantom = "+phantom+
				" collapseId" + coll +
				" override"+ov);
	}
	public void printMsg(String send) {
		String coll = collapseId!=null ?collapseId.toString():"null";
		String ov = Override!=null?Override.toString():"null";
		System.out.println(" Node "+dest+" is "+send+"  msg "+ type.toString()+
				" from "+src+ "which = "+which+" phantom = "+phantom+
				" collapseId" + coll +
				" override"+ov);
	}


	public boolean send() {
		assert(master != null);
		Node dst = master.getNode(this.dest);
		assert (dst != null);
		dst.getQu().add(this);
		return true;
	}
	
	public void setWhich() {
		which = master.getNode(src).getWhich();
	}
	
	public void setWhich(int which) {
		this.which = which;
	}
	
	public void setCollapseId(CollapseId id) {
		collapseId = id;
	}
	
	public CollapseId getCollapseId() {
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

	public boolean isStartOver() {
		return startOver;
	}

	public void setStartOver(boolean startOver) {
		this.startOver = startOver;
	}

	public CollapseId getOverride() {
		return Override;
	}

	public void setOverride(CollapseId override) {
		Override = override;
	}
}
