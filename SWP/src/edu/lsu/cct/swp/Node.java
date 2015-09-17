package edu.lsu.cct.swp;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.BasicConfigurator;

public class Node {
	private int nodeId = -1;
	private int rc[];
	private int which;
	private HashMap<Integer, Link> outgoingLinks;
	private Queue qu;
	private boolean deleted;

	static int phantom = 2;
	static AtomicInteger gCounter = new AtomicInteger();
	static Logger logger = Logger.getLogger(Node.class);

	Node() {
		nodeId = gCounter.getAndIncrement();
		outgoingLinks = new HashMap<Integer, Link>();
		qu = new LinkedList<Message>();
		rc = new int[3];
		rc[0] = rc[1] = rc[2] = 0;
		which = 0;
		deleted = false;
	}

	public boolean createLink(int dest) {
		Link lnk = new Link(nodeId, dest, 0);
		outgoingLinks.put(dest, lnk);
		return true;
	}
	
	public boolean deleteLink(int dest) {
		Link destn = outgoingLinks.get(dest);
		if (destn != null) {
			destn.delete();
			outgoingLinks.remove(dest);
		}
		return true;		
	}

	public String toReturn() {
		return "Hello!";
	}

	public int getNodeId() {
		return nodeId;
	}

	public int getSRC() {
		return rc[which];
	}

	public int getWRC() {
		return rc[1 - which];
	}

	public void processQueue() {
		if (qu.size() > 0) {
			Message m = (Message) qu.poll();
			switch (m.getType()) {
			case CreateLink:
				createLinkMessage(m);
				break;
			case CreateLinkReturn:
				createLinkReturnMessage(m);
				break;
			case Delete:
				deleteMessage(m);
				break;
			}
		}
	}

	private void deleteMessage(Message m) {
		int src = m.getSrc();
		if (which == m.getWhich()) {
			rc[which]--;
		}
		else {
			rc[1-which]--;
		}
		if (rc[which] ==  0 && rc[1-which] == 0 && rc[2] ==0) {
			deleted = true;
			for (Link l : outgoingLinks.values())  {
				deleteLink(l.)
			}
		}
		}

	private void createLinkReturnMessage(Message m) {
		int src = m.getSrc();
		Link lnk = outgoingLinks.get(src);
		lnk.setWhich(m.getWhich());
	}

	public Queue getQu() {
		return qu;
	}

	private void createLinkMessage(Message msg) {
		if (rc[phantom] == 0 && outgoingLinks.size() == 0) {
			rc[which]++;
			Message m = new Message(MessageType.CreateLinkReturn, this.nodeId,
					msg.getSrc());
			m.setWhich(which);
			m.send();
		} else if (rc[phantom] == 0 && outgoingLinks.size() > 0) {
			rc[1 - which]++;
			Message m = new Message(MessageType.CreateLinkReturn, this.nodeId,
					msg.getSrc());
			m.setWhich(1 - which);
			m.send();
		}
		else {
			assert(false);
		}

	}
	
	public int numOutgoingLinks() {
		return outgoingLinks.size();
	}

	public static void main(String args[]) {
		BasicConfigurator.configure();
		logger.info(" Result is " + new Node().toReturn());
	}
}
