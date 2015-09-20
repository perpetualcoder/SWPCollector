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
	private int collapseId;
	private int waitMsg;
	private NodeState state;
	private int nodeParent;

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
		state = NodeState.Dead;
		collapseId = -1;
		waitMsg = 0;
		nodeParent = -1;
	}

	public void setNodeParent(int id) {
		nodeParent = id;
	}

	public int getParentNodeId() {
		return nodeParent;
	}

	public void incWaitMsg() {
		waitMsg++;
	}

	public int getCollapseId() {
		return collapseId;
	}

	public void decWaitMsg() {
		waitMsg--;
		if (waitMsg == 0) {
			switch (state) {
			case Phantomized:
				if (collapseId == nodeId) {
					// Initiator of collapse. So go to next level.
					if (getSRC() > 0) {
						// Move to Recover State.

					} else {
						// Move to TryRecover State.
						state = NodeState.TryRecover;
						if (outgoingLinks.size() > 0) {
							for (Link l : outgoingLinks.values()) {
								incWaitMsg();
								l.tryRecover(collapseId);
							}
						} else {
							incWaitMsg();
							decWaitMsg();
						}
					}
				} else if (collapseId != nodeId) {
					// Send Reply message back.
					sendReturnMessage();
				}
				break;
			case TryRecover:
				if (collapseId == nodeId) {
					if (getSRC() > 0) {
						
					}
					else  {
						if (outgoingLinks.size() > 0) {
							for (Link l : outgoingLinks.values()) {
								l.delete(collapseId);
							}
						}						
					}
				}
				else {
					sendReturnMessage();
				}
				break;
			}
		}
	}

	public boolean createLink(int dest) {
		Link lnk = new Link(nodeId, dest, 0);
		outgoingLinks.put(dest, lnk);
		return true;
	}

	public boolean deleteLink(int dest) {
		Link destn = outgoingLinks.get(dest);
		if (destn != null) {
			destn.delete(collapseId);
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
			case Phantomize:
				phantomizeMessage(m);
				break;
			case Return:
				returnMessage(m);
				break;
			case TryRecovery:
				tryRecoverMessage(m);
				break;
			}
		}
	}

	private void tryRecoverMessage(Message m) {
		if (collapseId == m.getCollapseId() && state == NodeState.TryRecover) {
			// Return the message when the node is already visited by the 
			// TryRecover traversal.
			Message msg = new Message(MessageType.Return, nodeId, m.getSrc());
			msg.send();
			return;
		}
		if (rc[which] == 0 && rc[1 - which] == 0 && rc[2] != 0
				&& collapseId == m.getCollapseId()) {
			// Spread the TryRecover Traversal.
			state = NodeState.TryRecover;
			if (outgoingLinks.size() > 0) {
				for (Link l : outgoingLinks.values()) {
					incWaitMsg();
					l.tryRecover(collapseId);
				}
			} else {
				incWaitMsg();
				decWaitMsg();
			}
		}
	}

	private void returnMessage(Message m) {
		decWaitMsg();
	}

	private void sendReturnMessage() {
		Message msg = new Message(MessageType.Return, nodeId,
				getParentNodeId());
		msg.send();
	}

	private void phantomizeMessage(Message m) {
		if (which == m.getWhich()) {
			rc[which]--;
			rc[2]++;
		} else {
			rc[1 - which]--;
			rc[2]++;
		}
		if (collapseId == m.getCollapseId()) {
			// Node was already part of the phantomization.
			Message msg = new Message(MessageType.Return, nodeId,
					m.getSrc());
			msg.send();
		} else if (collapseId != m.getCollapseId() && collapseId != -1) {
			// Some other collapse is there and interferring with this collapse.
		} else {
			if (rc[which] == 0 && rc[1 - which] == 0 && rc[2] > 0) {
				// Continue phantomization.
				collapseId = m.getCollapseId();
				state = NodeState.Phantomized;
				setNodeParent(m.getSrc());
				if (outgoingLinks.size() > 0) {
					for (Link l : outgoingLinks.values()) {
						incWaitMsg();
						l.phantomize(collapseId);
					}
				} else {
					incWaitMsg();
					decWaitMsg();
				}
			} else if (rc[which] == 0 && rc[1 - which] > 0) {
				// Convert and proceed with phantomization.
				collapseId = m.getCollapseId();
				state = NodeState.Phantomized;
				setNodeParent(m.getSrc());
				which = 1- which;
				if (outgoingLinks.size() > 0) {
					for (Link l : outgoingLinks.values()) {
						incWaitMsg();
						l.phantomize(collapseId);
					}
				} else {
					incWaitMsg();
					decWaitMsg();
				}
			} else if (rc[which] > 0) {
				// Stop phantomization and reply back.
				collapseId = m.getCollapseId();
				setNodeParent(m.getSrc());
				incWaitMsg();
				decWaitMsg();

			}
		}
	}

	private void deleteMessage(Message m) {
		// Delete Message from the collapse traversal. So simply delete rather 
		// than creating new phantomization.
		if (m.getCollapseId() == collapseId && collapseId != -1 
				&& state != NodeState.Dead) {
			rc[2]--;
			if (rc[which] == 0 && rc[1 - which] == 0 && rc[2] == 0) {
				// Everything is zero. So starts deleting links and the node.
				state = NodeState.Dead;
				for (Link l : outgoingLinks.values()) {
					l.delete(collapseId);
				}
			}
			else if (rc[which] == 0 && rc[1 - which] == 0 && rc[2] != 0) {
				// Everything is zero but phantom. So starts deleting.
				for (Link l : outgoingLinks.values()) {
					l.delete(collapseId);
				}
			}
			return;
		}
		if (which == m.getWhich()) {
			rc[which]--;
		} else {
			rc[1 - which]--;
		}
		if (rc[which] == 0 && rc[1 - which] == 0 && rc[2] == 0) {
			// Everything is zero. So starts deleting.
			state = NodeState.Dead;
			for (Link l : outgoingLinks.values()) {
				deleteLink(l.getDest());
			}
		} else if (rc[which] == 0 && rc[2] == 0 && rc[1 - which] > 0) {
			// Phantomization starts here.
			collapseId = nodeId;
			state = NodeState.Phantomized;
			which = 1- which;
			if (outgoingLinks.size() > 0) {
				for (Link l : outgoingLinks.values()) {
					l.phantomize(collapseId);
					incWaitMsg();
				}
			} else {
				incWaitMsg();
				decWaitMsg();
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
			state = NodeState.Healthy;
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
		} else {
			assert(false);
		}

	}

	public int numOutgoingLinks() {
		return outgoingLinks.size();
	}
	
	public NodeState getState() {
		return state;
	}

	public static void main(String args[]) {
		BasicConfigurator.configure();
		logger.info(" Result is " + new Node().toReturn());
	}
}
