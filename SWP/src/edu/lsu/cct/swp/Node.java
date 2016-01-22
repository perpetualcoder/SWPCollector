package edu.lsu.cct.swp;

import org.apache.log4j.Logger;

import java.nio.file.LinkOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.BasicConfigurator;

public class Node {
	private int nodeId = -1;
	private int rc[];
	private int pc, rcc;
	private int which;
	private boolean startover;
	public HashMap<Integer, Link> outgoingLinks;
	private Queue qu;
	private CollapseId collapseId;
	private int waitMsg;
	private NodeState state;
	private int nodeParent;
	private boolean phantomized;
	
	public boolean mark;

	static AtomicInteger gCounter = new AtomicInteger();
	static Logger logger = Logger.getLogger(Node.class);

	Node() {
		nodeId = gCounter.getAndIncrement();
		outgoingLinks = new HashMap<Integer, Link>();
		qu = new LinkedList<Message>();
		rc = new int[2];
		rc[0] = rc[1] = 0;
		pc = rcc = 0;
		which = 0;
		startover = phantomized = false;
		state = NodeState.Dead;
		collapseId = null;
		waitMsg = 0;
		nodeParent = -1;
	}

	public boolean isHealthy() {
		return rc[which] > 0 && pc == 0 && rcc == 0 && phantomized == false
				&& collapseId == null && waitMsg == 0;
	}

	public boolean isOriginator() {
		return nodeParent == nodeId;
	}

	public boolean isLinkBuilt() {
		for (Link l : outgoingLinks.values()) {
			if (l.getPhantom())
				return false;
		}
		return true;
	}

	public boolean isWeaklySupported() {
		return getWRC() > 0 && getSRC() == 0 && pc == 0 && rcc == 0
				&& phantomized == false && collapseId == null && waitMsg == 0;
	}

	public boolean isPhantomWeaklySupported() {
		return getWRC() > 0 && getSRC() == 0 && pc > 0 && rcc == 0
				&& phantomized == false && collapseId != null && waitMsg == 0;
	}

	public boolean isPhantomizing() {
		return pc > 0 && rcc == 0 && phantomized == true && collapseId != null
				&& waitMsg > 0;
	}

	public boolean isPhantomized() {
		return (pc > 0) && (rcc == 0) && (phantomized == true)
				&& (collapseId != null) && (waitMsg == 0);
	}

	public boolean isRecovering() {
		return pc > 0 && rcc > 0 && phantomized == true && collapseId != null
				&& waitMsg > 0;
	}

	public boolean isRecovered() {
		return pc > 0 && rcc > 0 && phantomized == true && collapseId != null
				&& waitMsg == 0 && getSRC() == 0;
	}

	public boolean isOrginatorBuilt() {
		return isOriginator() && rcc > 0 && rc[which] > 0 && collapseId != null;
	}

	public boolean isMayBeDelete() {
		return pc == rcc && rc[which] == rc[1 - which] && rc[which] == 0
				&& pc > 0 && waitMsg == 0 && collapseId != null;
	}

	public boolean isSimplyDead() {
		return pc == rcc && rc[which] == rc[1 - which] && rc[which] == 0
				&& pc == 0 && waitMsg == 0;

	}

	public boolean isGarbage() {
		return (isSimplyDead() || (isMayBeDelete() && isOriginator()));
	}

	public boolean isPhantomLive() {
		return rc[which] > 0 && collapseId != null && waitMsg == 0;
	}

	public boolean isBuilding() {
		return rc[which] > 0 && collapseId != null && waitMsg > 0;
	}

	public void setNodeParent(int id) {
		nodeParent = id;
	}

	public int getPhantomCount() {
		return pc;
	}

	public int getParentNodeId() {
		return nodeParent;
	}

	public void incWaitMsg() {
		waitMsg++;
	}

	public CollapseId getCollapseId() {
		return collapseId;
	}

	public void decWaitMsg() {
		waitMsg--;
		if (waitMsg == 0) {
			printNode();
			System.out.println("is phantomized" + isPhantomized());
			if (isPhantomized()) {
				if (isOriginator()) {
					originatorPhatomReturnAction();
				} else {
					// Send Reply message back.
					sendReturnMessage(startover);
					if (startover)
						startover = false;
				}
			} else if (isPhantomLive() && !isLinkBuilt()) {
				rcc = 0;
				if (outgoingLinks.size() > 0) {
					for (Link l : outgoingLinks.values()) {
						incWaitMsg();
						l.build(collapseId);
					}
				} else {
					incWaitMsg();
					sendReturnMessage(false);
				}
			} else if (isPhantomLive() && isLinkBuilt()) {
				rcc = 0;
				if (!isOriginator())
					sendReturnMessage(false);
				if (pc == 0) {
					collapseId = null;
					nodeParent = -1;
					rcc = 0;
					waitMsg = 0;
					phantomized = false;
					startover = false;
				}
			} else if (isRecovered()) {
				if (isOriginator()) {
					originatorRecoveryReceivedAction();
				} else {
					nonOriginatorRecoveryReceivedAction();
				}
			}
		}

	}

	private void nonOriginatorRecoveryReceivedAction() {
		if (startover) {
			sendReturnMessage(true);
			startover = false;
		} else if (isPhantomLive()) {
			state = NodeState.Built;
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.build(collapseId);
			}
			if (outgoingLinks.isEmpty() && isMayBeDelete()) {
				sendReturnMessage(false);
			}
		} else if (isMayBeDelete()) {
			sendReturnMessage(false);
		}
	}

	private void originatorRecoveryReceivedAction() {
		if (startover) {
			startover = false;
			collapseId.newCollapse();
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.phantomize(collapseId, null);
			}
			phantomized = true;
			if (outgoingLinks.isEmpty()) {
				incWaitMsg();
				sendReturnMessage(false);
			}
		} else if (isPhantomLive()) {
			state = NodeState.Built;
			if (outgoingLinks.size() > 0) {
				for (Link l : outgoingLinks.values()) {
					incWaitMsg();
					l.build(collapseId);
				}
			} else {
				incWaitMsg();
				sendReturnMessage(false);
			}
		} else if (isMayBeDelete()) {
			if (outgoingLinks.size() > 0) {
				for (Link l : outgoingLinks.values()) {
					l.plagueDelete(collapseId);
				}
				outgoingLinks.clear();
			}
			if (isSimplyDead()) {
				collapseId = null;
				state = NodeState.Dead;
			}
		}
	}

	private void originatorPhatomReturnAction() {
		if (startover) {
			startover = false;
			collapseId.newCollapse();
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.phantomize(collapseId, null);
			}
			phantomized = true;
			if (outgoingLinks.isEmpty()) {
				incWaitMsg();
				sendReturnMessage(false);
			}
		} else if (isPhantomLive()) {
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.build(collapseId);
			}
			if (outgoingLinks.isEmpty()) {
				incWaitMsg();
				sendReturnMessage(false);
			}
		} else {
			System.out.println("Recovery initiated");
			if (outgoingLinks.size() > 0) {
				for (Link l : outgoingLinks.values()) {
					incWaitMsg();
					l.recover(collapseId);
				}
			} else {
				incWaitMsg();
				sendReturnMessage(false);
			}
		}
	}

	public boolean createLink(int dest) {
		Link lnk = new Link(nodeId, dest, which);
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
			m.printMsg();
			switch (m.getType()) {
			case CreateLink:
				createLinkMessage(m);
				break;
			case CreateLinkReturn:
				createLinkReturnMessage(m);
				break;
			case LinkBuildReturn:
				linkBuildReturnMessage(m);
				break;
			case Delete:
				deleteMessage(m);
				break;
			case Phantomize:
				phantomizeMessage(m);
				break;
			case PlagueDelete:
				plagueDeleteMessage(m);
				break;
			case Return:
				returnMessage(m);
				break;
			case Recover:
				recoverMessage(m);
				break;
			case Build:
				buildMessage(m);
				break;
			}
		}
	}

	private void linkBuildReturnMessage(Message m) {
		int src = m.getSrc();
		Link lnk = outgoingLinks.get(src);
		lnk.setWhich(m.getWhich());
		lnk.unsetPhantom();
	}

	private void buildMessage(Message m) {
		if (isHealthy()) {
			sendReturnMessage(false);
			return;
		}
		Message msg = new Message(MessageType.LinkBuildReturn, nodeId,
				m.getSrc());
		if (getSRC() > 0) {
			rc[1 - which]++;
			pc--;
		} else {
			rc[which]++;
			pc--;
		}

		msg.setWhich(which);
		msg.send();
		if (isBuilding() || !collapseId.equalTo(m.getCollapseId())) {
			sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
			return;
		}
		if (!isOriginator() && !isLinkBuilt()) {
			nodeParent = m.getSrc();
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.build(collapseId);
			}
			if (outgoingLinks.isEmpty()) {
				sendReturnMessage(false);
				rcc = 0;
				state = NodeState.Healthy;
				nodeParent = -1;
				collapseId = null;
				phantomized = false;
			}
		} else {
			sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
			if (!isOriginator() && pc == 0) {
				rcc = 0;
				state = NodeState.Healthy;
				nodeParent = -1;
				collapseId = null;
				phantomized = false;
				startover = false;
			}
		}
	}

	private void recoverMessage(Message m) {
		printNode();
		if (m.getCollapseId().lessThan(collapseId)) {
			sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
			return;
		} else if (collapseId.equalTo(m.getCollapseId())) {
			rcc++;
			if (isRecovering() || m.getSrc() != nodeParent) {
				sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
			} else if (isPhantomLive()) {
				for (Link l : outgoingLinks.values()) {
					incWaitMsg();
					l.build(collapseId);
				}
				if (outgoingLinks.isEmpty()) {
					sendReturnMessage(false);
				}
			} else {
				for (Link l : outgoingLinks.values()) {
					incWaitMsg();
					l.recover(collapseId);
				}
				if (outgoingLinks.isEmpty() && isMayBeDelete()) {
					sendReturnMessage(false);
				}
			}
		}
	}

	private void returnMessage(Message m) {
		System.out.println("Received this message");
		if (collapseId.equalTo(m.getCollapseId())) {
			System.out.println("Running this message");
			decWaitMsg();
			startover = m.isStartOver();
		}
	}

	private void sendReturnMessage(boolean startover) {
		Message msg = new Message(MessageType.Return, nodeId,
				getParentNodeId());
		msg.setStartOver(startover);
		msg.setCollapseId(collapseId);
		msg.send();
	}

	private void sendReturnMessageSender(boolean startover, int sender,
			CollapseId c) {
		Message msg = new Message(MessageType.Return, nodeId, sender);
		msg.setStartOver(startover);
		msg.setCollapseId(c);
		msg.send();
	}

	private void phantomizeMessage(Message m) {
		if (collapseId != null && collapseId.partialLessThan(m.getCollapseId())
				|| (m.getOverride() != null
						&& m.getOverride().equals(collapseId))) {
			remarkingPhantomizing(m);
		} else {
			decRCIncPC(m);
			if (collapseId == null) {
				collapseAction(m);
			} else if (collapseId.equals(m.getCollapseId())) {
				sameCollapseAction(m);
			} else if (collapseId.lessThan(m.getCollapseId())) {
				collapseOverrideAction(m);
			} else {
				if (isPhantomized()) {
					rcc = 0;
					spawnNewCollapseHere();
				} else {
					sendReturnMessageSender(false, m.getSrc(),
							m.getCollapseId());
				}
			}
		}
	}

	private void collapseOverrideAction(Message m) {
		if (isPhantomizing() || isRecovering() || isBuilding()) {
			sendReturnMessage(true);
		}
		CollapseId old = collapseId;
		collapseId = m.getCollapseId();
		nodeParent = m.getSrc();
		rcc = 0;
		waitMsg = 0;
		for (Link l : outgoingLinks.values()) {
			incWaitMsg();
			l.phantomize(collapseId, old);
		}
		phantomized = true;
		if (outgoingLinks.isEmpty()) {
			sendReturnMessage(false);
		}
	}

	private void sameCollapseAction(Message m) {
		if (isBuilding() || isRecovering()) {
			sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
		} else if (isPhantomLive() || phantomized) {
			sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
		} else {
			if (isPhantomWeaklySupported()) {
				which = 1 - which;
			}
			nodeParent = m.getSrc();
			phantomSpreadOrReturn();
		}
	}

	private void collapseAction(Message m) {
		System.out.println("Before collapse");
		printNode();
		collapseId = m.getCollapseId();
		nodeParent = m.getSrc();
		if (isPhantomLive()) {
			sendReturnMessage(false);
		} else if (outgoingLinks.isEmpty()) {
			if (getSRC() == 0 && getWRC() > 0) {
				which = 1 - which;
			}
			phantomized = true;
			sendReturnMessage(false);
		} else {
			if (isPhantomWeaklySupported()) {
				System.out.println("WEAKLY SUPPORTED NODE");
				printNode();
				which = 1 - which;
				printNode();
			}
			phantomSpreadOrReturn();
		}
	}

	private void phantomSpreadOrReturn() {
		for (Link l : outgoingLinks.values()) {
			incWaitMsg();
			l.phantomize(collapseId, null);
		}
		phantomized = true;
		if (outgoingLinks.isEmpty()) {
			sendReturnMessage(false);
		}
	}

	private void decRCIncPC(Message m) {
		if (m.getWhich() == which) {
			rc[which]--;
			pc++;
		} else {
			rc[1 - which]--;
			pc++;
		}
	}

	private void remarkingPhantomizing(Message m) {
		collapseId = m.getCollapseId();
		waitMsg = 0;
		nodeParent = m.getSrc();
		if (phantomized) {
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.phantomize(collapseId, m.getOverride());
			}
			if (outgoingLinks.isEmpty()) {
				sendReturnMessage(false);
			}
		} else {
			sendReturnMessageSender(startover, m.getSrc(), m.getCollapseId());
		}
	}

	private void plagueDeleteMessage(Message m) {
		System.out.println("Received this");
		if (m.getPhantom()) {
			pc--;
			if (m.getCollapseId().equalTo(collapseId) && rcc > 0) {
				rcc--;
			}
		}
		if (phantomized == true && m.getCollapseId().equalTo(collapseId)) {
			for (Link l : outgoingLinks.values()) {
				l.plagueDelete(collapseId);
			}
			outgoingLinks.clear();
			if (isSimplyDead()) {
				collapseId = null;
				state = NodeState.Dead;
			}
		}
	}

	private void deleteMessage(Message m) {
		decrementCountInDelMsg(m);
		if (isGarbage()) {
			sendPlagueOrDelMsg();
		}
		if (isSimplyDead()) {
			collapseId = null;
			state = NodeState.Dead;
			return;
		} else if (isHealthy() || isGarbage()) {
			return;
		} else if (isWeaklySupported()) {
			startPhantomizing();
		} else {
			if (isPhantomizing() || isRecovering() || isBuilding()) {
				sendReturnMessage(true);
				waitMsg = 0;
			}
			if (!isPhantomLive()) {
				rcc = 0;
				spawnNewCollapseHere();
			}
		}
	}

	private void spawnNewCollapseHere() {
		collapseId = new CollapseId();
		nodeParent = nodeId;
		for (Link l : outgoingLinks.values()) {
			incWaitMsg();
			l.phantomize(collapseId, null);
		}
		phantomized = true;
		if (outgoingLinks.isEmpty()) {
			incWaitMsg();
			sendReturnMessage(false);
		}
	}

	private void startPhantomizing() {
		which = 1 - which;
		spawnNewCollapseHere();
	}

	private void sendPlagueOrDelMsg() {
		if (phantomized) {
			for (Link l : outgoingLinks.values()) {
				l.plagueDelete(collapseId);
			}
		} else if (isSimplyDead()) {
			for (Link l : outgoingLinks.values()) {
				l.delete(collapseId);
			}
		}
	}

	private void decrementCountInDelMsg(Message m) {
		if (m.getPhantom()) {
			pc--;
			if (m.getCollapseId() != null
					&& m.getCollapseId().equalTo(collapseId) && rcc > 0) {
				rcc--;
			}
		} else if (m.getWhich() == which)
			rc[which]--;
		else
			rc[1 - which]--;
	}

	private void createLinkReturnMessage(Message m) {
		int src = m.getSrc();
		Link lnk = outgoingLinks.get(src);
		lnk.setWhich(m.getWhich());
	}

	public Queue getQu() {
		return qu;
	}

	public int getWhich() {
		return which;
	}

	private void createLinkMessage(Message msg) {
		if (pc == 0 && outgoingLinks.size() == 0) {
			rc[which]++;
			state = NodeState.Healthy;
			Message m = new Message(MessageType.CreateLinkReturn, this.nodeId,
					msg.getSrc());
			m.setWhich();
			m.send();
		} else if (pc == 0 && outgoingLinks.size() > 0) {
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

	public void printNode() {
		String co = collapseId != null ? collapseId.toString() : "null";
		System.out.println("Node " + nodeId + " : " + getSRC() + "," + getWRC()
				+ " ," + pc + ", " + rcc + ", " + state + " ph: " + phantomized
				+ " collapseID" + co + " wait " + waitMsg+
				"mark "+mark);
	}

	public static void main(String args[]) {
		BasicConfigurator.configure();
		logger.info(" Result is " + new Node().toReturn());
	}
}
