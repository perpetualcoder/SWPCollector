package edu.lsu.cct.swp;

public enum MessageType {
	CreateLink ("CreateLink"), 
	Phantomize ("phantomize"),
	Return ("Return"),
	Recover ("Recover"),
	Build ("Build"),
	Delete ("Delete"), 
	PlagueDelete ("PlagueDelete"),
	LinkBuild("LinkBuild"),
	LinkBuildReturn ("LinkBuildReturn");
	
	private final String name;
	
	private MessageType(String s) {
		name = s;
	}
	
	public String toString() {
		return this.name();
	}
}
