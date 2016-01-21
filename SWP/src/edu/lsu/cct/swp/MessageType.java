package edu.lsu.cct.swp;

public enum MessageType {
	CreateLink ("CreateLink"), 
	CreateLinkReturn ("CreateLinkReturn"),
	Phantomize ("phantomize"),
	Return ("Return"),
	Recover ("Recover"),
	Build ("Build"),
	Delete ("Delete"), 
	PlagueDelete ("PlagueDelete"),
	LinkBuildReturn ("LinkBuildReturn");
	
	private final String name;
	
	private MessageType(String s) {
		name = s;
	}
	
	public String toString() {
		return this.name();
	}
}
