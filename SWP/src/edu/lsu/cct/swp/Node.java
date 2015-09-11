package edu.lsu.cct.swp;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.BasicConfigurator;

public class Node {
	private int nodeId = -1;
	private HashMap<Integer, Link> outgoingLinks;
	
	static AtomicInteger gCounter = new AtomicInteger();
	static Logger logger = Logger.getLogger(Node.class);
	
	Node() {
		nodeId = gCounter.getAndIncrement();
		outgoingLinks = new HashMap<Integer, Link>();
	}
	
	public boolean createLink(int dest) {
		Link lnk = new Link(nodeId, dest, false);
		outgoingLinks.put(dest, lnk);
		return true;
	}
	
	public String toReturn(){
		return "Hello!";
	}
	
	public static void main(String args[]) {
		BasicConfigurator.configure();
		logger.info(" Result is "+new Node().toReturn());
	}
}
