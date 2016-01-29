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
	private boolean rephantomize;
	private boolean rerecover;
	private long myWeight;
	private long maxWeight;

	public boolean mark;

	static AtomicInteger gCounter = new AtomicInteger();
	static Logger logger = Logger.getLogger(Node.class);

	Node() {
		nodeId = gCounter.getAndIncrement();
		outgoingLinks = new HashMap<Integer, Link>();
		qu = new LinkedList<Message>();
		rc = new int[2];
		rc[0] = rc[1] = 0;
		myWeight = maxWeight = 0;
		pc = rcc = 0;
		which = 0;
		startover = phantomized = false;
		state = NodeState.Healthy;
		collapseId = null;
		waitMsg = 0;
		nodeParent = -1;
		rephantomize = false;
		rerecover = false;
	}

	public boolean isHealthy() {
		// return rc[which] > 0 && pc == 0 && rcc == 0 && phantomized == false
		// && collapseId == null && waitMsg == 0;
		return state == NodeState.Healthy;
	}

	public boolean isOriginator() {
		return nodeParent == nodeId && collapseId != null
				&& collapseId.originator == nodeId;
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
		// return pc > 0 && rcc == 0 && phantomized == true && collapseId !=
		// null
		// && waitMsg > 0;
		return state == NodeState.Phantomizing;
	}

	public boolean isPhantomized() {
		// return (pc > 0) && (rcc == 0) && (phantomized == true)
		// && (collapseId != null) && (waitMsg == 0);
		return state == NodeState.Phantomized;
	}

	public boolean isPhantomizedNow() {
		return getSRC() == 0 && getWRC() == 0 && (pc > 0) && (rcc == 0)
				&& (phantomized == false) && (collapseId != null)
				&& (waitMsg == 0);
	}

	public boolean isRecovering() {
		// return pc > 0 && rcc > 0 && phantomized == true && collapseId != null
		// && waitMsg > 0;
		return state == NodeState.Recovering;
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
		// return rc[which] > 0 && collapseId != null && waitMsg > 0;
		return state == NodeState.Building;
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
			if (isPhantomizing()) {
				state = NodeState.Phantomized;
				if (isOriginator()) {
					if (getSRC() > 0)
						System.out.println("Build Children");
					else {
						for (Link l : outgoingLinks.values()) {
							incWaitMsg();
							l.recover(collapseId, myWeight);
						}
						if (outgoingLinks.isEmpty()) {
							incWaitMsg();
							sendReturnMessage(false);
						}
					}						
				} else {
					sendReturnMessage(false);
				}
			}
		}
	}

	// private void nonOriginatorRecoveryReceivedAction() {
	// if (startover) {
	// sendReturnMessage(true);
	// startover = false;
	// } else if (isPhantomLive()) {
	// state = NodeState.Building;
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.build(collapseId);
	// }
	// if (outgoingLinks.isEmpty() && isMayBeDelete()) {
	// sendReturnMessage(false);
	// }
	// } else if (isMayBeDelete()) {
	// sendReturnMessage(false);
	// }
	// }

	// private void originatorRecoveryReceivedAction() {
	// if (startover) {
	// startover = false;
	// collapseId.newCollapse();
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.phantomize(collapseId, null);
	// }
	// phantomized = true;
	// if (outgoingLinks.isEmpty()) {
	// incWaitMsg();
	// sendReturnMessage(false);
	// }
	// } else if (isPhantomLive()) {
	// state = NodeState.Building;
	// if (outgoingLinks.size() > 0) {
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.build(collapseId);
	// }
	// } else {
	// incWaitMsg();
	// sendReturnMessage(false);
	// }
	// } else if (isMayBeDelete()) {
	// if (outgoingLinks.size() > 0) {
	// for (Link l : outgoingLinks.values()) {
	// l.plagueDelete(collapseId);
	// }
	// outgoingLinks.clear();
	// }
	// if (isSimplyDead()) {
	// collapseId = null;
	// state = NodeState.Dead;
	// }
	// }
	// }

	// private void originatorPhatomReturnAction() {
	// if (startover) {
	// startover = false;
	// collapseId.newCollapse();
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.phantomize(collapseId, null);
	// }
	// phantomized = true;
	// if (outgoingLinks.isEmpty()) {
	// incWaitMsg();
	// sendReturnMessage(false);
	// }
	// } else if (isPhantomLive()) {
	// System.out.println("Building started by originator");
	// state = NodeState.Building;
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.build(collapseId);
	// }
	// if (outgoingLinks.isEmpty()) {
	// incWaitMsg();
	// sendReturnMessage(false);
	// }
	// } else {
	// System.out.println("Recovery initiated");
	// if (outgoingLinks.size() > 0) {
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.recover(collapseId);
	// }
	// } else {
	// incWaitMsg();
	// sendReturnMessage(false);
	// }
	// }
	// }

	public boolean createLink(int dest) {
		Link lnk = new Link(nodeId, dest, which, myWeight);
		outgoingLinks.put(dest, lnk);
		return true;
	}

	public boolean deleteLink(int dest) {
		Link destn = outgoingLinks.get(dest);
		if (destn != null) {
			destn.delete(collapseId, myWeight);
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
		assert state != NodeState.Dead;
		if (qu.size() > 0) {
			Message m = (Message) qu.poll();
			m.printMsg();
			switch (m.getType()) {
			case CreateLink:
				createLinkMessage(m);
				break;
			// case LinkBuild:
			// linkBuildMessage(m);
			// break;
			// case LinkBuildReturn:
			// linkBuildReturnMessage(m);
			// break;
			case Delete:
				deleteMessage(m);
				break;
			case Phantomize:
				phantomizeMessage(m);
				break;
			// case PlagueDelete:
			// plagueDeleteMessage(m);
			// break;
			case Return:
				returnMessage(m);
				break;
			case Recover:
				recoverMessage(m);
				break;
			// case Build:
			// buildMessage(m);
			// break;
			}
		}
	}

	// private void linkBuildReturnMessage(Message m) {
	// if (!m.getCollapseId().equals(collapseId)) {
	// System.out.println(" ignored");
	// return;
	// }
	// int src = m.getSrc();
	// Link lnk = outgoingLinks.get(src);
	// lnk.setWhich(m.getWhich());
	// lnk.unsetPhantom();
	// Message msg = new Message(MessageType.LinkBuild, nodeId, m.getSrc());
	// msg.setWhich(m.getWhich());
	// msg.send();
	// msg.printMsg("receiving");
	// }
	//
	// private void linkBuildMessage(Message m) {
	// rc[m.getWhich()]++;
	// pc--;
	// if (pc == 0) {
	// state = NodeState.Healthy;
	// rcc = 0;
	// }
	// }
	//
	// private void buildMessage(Message m) {
	// if (isPhantomizing()) {
	// sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
	// } else if (isPhantomized()) {
	// Message msg = new Message(MessageType.LinkBuildReturn, nodeId,
	// m.getSrc());
	// msg.setCollapseId(m.getCollapseId());
	// if (state == NodeState.Building)
	// msg.setWhich(1 - which);
	// else
	// msg.setWhich(which);
	// msg.send();
	// }
	// }
	//
	// private void oldbuildMessage(Message m) {
	// if (isHealthy()) {
	// sendReturnMessage(false);
	// return;
	// }
	// Message msg = new Message(MessageType.LinkBuildReturn, nodeId,
	// m.getSrc());
	// // if (getSRC() > 0) {
	// // rc[1 - which]++;
	// // pc--;
	// // } else {
	// // rc[which]++;
	// // pc--;
	// // }
	// msg.setCollapseId(m.getCollapseId());
	// if (state == NodeState.Building)
	// msg.setWhich(1 - which);
	// else
	// msg.setWhich(which);
	// msg.send();
	// state = NodeState.Building;
	// if (isBuilding() || !collapseId.equalTo(m.getCollapseId())) {
	// sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
	// return;
	// }
	// if (!isOriginator() && !isLinkBuilt()) {
	// nodeParent = m.getSrc();
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.build(collapseId);
	// }
	// if (outgoingLinks.isEmpty()) {
	// sendReturnMessage(false);
	// rcc = 0;
	// state = NodeState.Healthy;
	// nodeParent = -1;
	// collapseId = null;
	// phantomized = false;
	// }
	// } else {
	// sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
	// if (!isOriginator() && pc == 0) {
	// rcc = 0;
	// state = NodeState.Healthy;
	// nodeParent = -1;
	// collapseId = null;
	// phantomized = false;
	// startover = false;
	// }
	// }
	// }
	//

	private void recoverMessage(Message m) {
		if (isPhantomizing()) {
			if (isOriginator()) {
				rerecover = true;
				collapseId = m.getCollapseId();
				nodeParent = m.getSrc();
			} else {
				sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
				rerecover = true;
				collapseId = m.getCollapseId();
				nodeParent = m.getSrc();
				// Phantom Receive should do case for rerecover or change this
				// to recovery state.
			}
		} else if (isPhantomized()) {
			collapseId = m.getCollapseId();
			state = NodeState.Recovering;
			nodeParent = m.getSrc();
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.recover(collapseId, myWeight);
			}
			if (outgoingLinks.isEmpty()) {
				sendReturnMessage(false);
			}
		} else if (isBuilding()) {
			if (isOriginator()) {
				if (m.getCollapseId().lessThan(collapseId)) {
					sendReturnMessageSender(false, m.getSrc(),
							m.getCollapseId());
				} else if (m.getCollapseId().equalTo(collapseId)) {
					rcc++;
					sendReturnMessageSender(false, m.getSrc(), collapseId);
					if (rcc == pc) {
						// Clean up is not clear yet.
					}
				} else {
					collapseId = m.getCollapseId();
					rerecover = true;
					nodeParent = m.getSrc();
				}
			} else {
				if (m.getCollapseId().lessThan(collapseId)) {
					if (getSRC() == 0) {
						rephantomize = true;
						rerecover = true;
					}
					sendReturnMessageSender(false, m.getSrc(), collapseId);
				} else if (m.getCollapseId().equalTo(collapseId)) {
					rcc++;
					sendReturnMessageSender(false, m.getSrc(), collapseId);
					if (rcc == pc && waitMsg == 0) {
						sendReturnMessage(false);
					}
				} else {
					sendReturnMessage(true);
					collapseId = m.getCollapseId();
					rerecover = true;
					nodeParent = m.getSrc();
				}
			}
		} else if (isRecovering()) {
			if (isOriginator()) {
				if (m.getCollapseId().lessThan(collapseId)) {
					sendReturnMessageSender(false, m.getSrc(), collapseId);
				} else if (m.getCollapseId().equalTo(collapseId)) {
					rcc++;
					sendReturnMessageSender(false, m.getSrc(), collapseId);
				} else {
					collapseId = m.getCollapseId();
					rcc = 1;
					nodeParent = m.getSrc();
					if (waitMsg > 0) {
						rerecover = true;
					} else {
						for (Link l : outgoingLinks.values()) {
							incWaitMsg();
							l.recover(collapseId, myWeight);
						}
						if (outgoingLinks.isEmpty()) {
							sendReturnMessage(false);
						}
					}
				}
			} else {
				if (m.getCollapseId().lessThan(collapseId)) {
					sendReturnMessageSender(false, m.getSrc(), collapseId);
				} else if (m.getCollapseId().equalTo(collapseId)) {
					rcc++;
					if (rcc == pc && waitMsg == 0) {
						rcc = 0;
						sendReturnMessage(false);
					}
					sendReturnMessageSender(false, m.getSrc(), collapseId);
				} else {
					sendReturnMessage(true);
					collapseId = m.getCollapseId();
					rcc = 1;
					nodeParent = m.getSrc();
					// SO much to be done.
					if (waitMsg > 0) {
						rerecover = true;
					} else {
						for (Link l : outgoingLinks.values()) {
							incWaitMsg();
							l.recover(collapseId, myWeight);
						}
						if (outgoingLinks.isEmpty()) {
							sendReturnMessage(false);
						}
					}
				}
			}
		} else if (isRecovered()) {
			rcc = 1;
			collapseId = m.getCollapseId();
			collapseId = m.getCollapseId();
			nodeParent = m.getSrc();
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.recover(collapseId, myWeight);
			}
			if (outgoingLinks.isEmpty()) {
				sendReturnMessage(false);
			}

		}
	}

	// private void oldrecoverMessage(Message m) {
	// printNode();
	// if (m.getCollapseId().lessThan(collapseId)) {
	// sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
	// return;
	// } else if (collapseId.equalTo(m.getCollapseId())) {
	// rcc++;
	// if (isRecovering() || m.getSrc() != nodeParent) {
	// sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
	// } else if (isPhantomLive() && phantomized) {
	// state = NodeState.Building;
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.build(collapseId);
	// }
	// if (outgoingLinks.isEmpty()) {
	// sendReturnMessage(false);
	// }
	// } else if (isPhantomLive() && !phantomized) {
	// rcc--;
	// sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
	// } else {
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.recover(collapseId);
	// }
	// if (outgoingLinks.isEmpty() && isMayBeDelete()) {
	// sendReturnMessage(false);
	// }
	// }
	// }
	// }
	//
	private void returnMessage(Message m) {
		decWaitMsg();
		startover = m.isStartOver();
	}

	private void sendReturnMessage(boolean startover) {
		Message msg = new Message(MessageType.Return, nodeId, getParentNodeId(),
				myWeight, myWeight);
		msg.setStartOver(startover);
		msg.setCollapseId(collapseId);
		msg.send();
		msg.printMsg("receiving");
	}

	private void sendReturnMessageSender(boolean startover, int sender,
			CollapseId c) {
		Message msg = new Message(MessageType.Return, nodeId, sender, myWeight,
				myWeight);
		msg.setStartOver(startover);
		msg.setCollapseId(c);
		msg.send();
		msg.printMsg("receiving");
	}

	private void phantomizeMessage(Message m) {
		decRCIncPC(m);
		if (isPhantomizing() || isHealthy()) {
			if (isOriginator()) {
				if (m.getCollapseId().lessThan(collapseId)
						|| m.getCollapseId().equalTo(collapseId)) {
					sendReturnMessageSender(false, m.getSrc(), collapseId);
				} else {
					nodeParent = m.getSrc();
					collapseId = null;
				}
			} else {
				if (phantomized) {
					sendReturnMessageSender(false, m.getSrc(),
							m.getCollapseId());
				} else if (getSRC() == 0) {
					long old = myWeight;
					myWeight = maxWeight + 1;
					rc[which] = rc[1 - which];
					rc[1 - which] = 0;
					nodeParent = m.getSrc();
					state = NodeState.Phantomizing;
					for (Link l : outgoingLinks.values()) {
						l.phantomize(m.getCollapseId(), null, old, myWeight);
						incWaitMsg();
					}
					if (outgoingLinks.isEmpty()) {
						state = NodeState.Phantomized;
						sendReturnMessage(false);
					} else {
						phantomized = true;
					}
				} else {
					nodeParent = m.getSrc();
					sendReturnMessage(false);
				}
			}
		} else if (isBuilding()) {
			if (isOriginator()) {
				if (collapseId.lessThan(m.getCollapseId())) {
					collapseId = m.getCollapseId();
					nodeParent = m.getSrc();
					rephantomize = true;
				} else {
					sendReturnMessageSender(false, m.getSrc(),
							m.getCollapseId());
					if (getSRC() == 0) {
						rephantomize = true;
					}
				}
			} else {
				if (collapseId == null || m.getCollapseId().lessThan(collapseId)
						|| m.getCollapseId().equalTo(collapseId)) {
					sendReturnMessageSender(false, m.getSrc(),
							m.getCollapseId());
					if (getSRC() == 0) {
						rephantomize = true;
						rerecover = true;
					}
				} else {
					collapseId = m.getCollapseId();
					rephantomize = true;
					sendReturnMessage(true);
					nodeParent = m.getSrc();
				}
			}
		}
	}

	//
	// private void oldphantomizeMessage(Message m) {
	// System.out.println("Before processing ph message");
	// printNode();
	// if (!m.isAlready()) {
	// decRCIncPC(m);
	// }
	// assert (getSRC() >= 0 && getWRC() >= 0);
	// if (collapseId != null && collapseId.partialLessThan(m.getCollapseId())
	// || (m.getOverride() != null
	// && m.getOverride().equals(collapseId))) {
	// printNode();
	// System.out.println(" Partial less phantom or override");
	// remarkingPhantomizing(m);
	// } else if (m.getOverride() != null
	// && m.getCollapseId().equals(collapseId)) {
	// sendReturnMessageSender(false, m.getSrc(), collapseId);
	// } else {
	//
	// if (collapseId == null) {
	// System.out.println("null collapse");
	// collapseAction(m);
	// } else if (collapseId.equals(m.getCollapseId())) {
	// System.out.println("Same Collapse");
	// sameCollapseAction(m);
	// } else if (collapseId.lessThan(m.getCollapseId())) {
	// System.out.println("Override collapse due to more than");
	// collapseOverrideAction(m);
	// } else {
	// System.out.println("Less than collapse");
	// if (isPhantomizedNow()) {
	// rcc = 0;
	// System.out.println("New collapse due to all inc ph");
	// spawnNewCollapseHere();
	// } else if (isPhantomWeaklySupported()) {
	// sendReturnMessageSender(false, m.getSrc(),
	// m.getCollapseId());
	// rcc = 0;
	// which = 1 - which;
	// System.out.println("New collapse due to all inc ph");
	// spawnNewCollapseHere();
	// } else {
	// System.out.println("return message");
	// sendReturnMessageSender(false, m.getSrc(),
	// m.getCollapseId());
	//
	// }
	// }
	// }
	// System.out.println("After processing ph message");
	// printNode();
	// assert (getSRC() >= 0 && getWRC() >= 0);
	// }
	//
	// private void collapseOverrideAction(Message m) {
	// if (isPhantomizing() || isRecovering() || isBuilding()) {
	// sendReturnMessage(true);
	// }
	// CollapseId old = collapseId;
	// collapseId = m.getCollapseId();
	// nodeParent = m.getSrc();
	// rcc = 0;
	// waitMsg = 0;
	// if (!phantomized) {
	// sendReturnMessage(false);
	// return;
	// }
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.phantomize(collapseId, old);
	// }
	// phantomized = true;
	// if (outgoingLinks.isEmpty()) {
	// sendReturnMessage(false);
	// }
	// }
	//
	// private void sameCollapseAction(Message m) {
	// if (isBuilding() || isRecovering()) {
	// System.out.println("Return due to building or recovering");
	// sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
	// } else if (isPhantomLive() || phantomized) {
	// System.out.println(" Ph live or phed");
	// sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
	// } else {
	// if (isPhantomWeaklySupported()) {
	// which = 1 - which;
	// }
	// nodeParent = m.getSrc();
	// System.out.println("Spreading pH");
	// phantomSpreadOrReturn();
	// }
	// }
	//
	// private void collapseAction(Message m) {
	// System.out.println("Before collapse");
	// printNode();
	// collapseId = m.getCollapseId();
	// nodeParent = m.getSrc();
	// if (isPhantomLive()) {
	// printNode();
	// System.out.println("Phantom Live state");
	// sendReturnMessage(false);
	// } else if (outgoingLinks.isEmpty()) {
	// if (getSRC() == 0 && getWRC() > 0) {
	// which = 1 - which;
	// }
	// phantomized = true;
	// System.out.println(" no outgoing link");
	// sendReturnMessage(false);
	// } else {
	// if (isPhantomWeaklySupported()) {
	// System.out.println("WEAKLY SUPPORTED NODE");
	// printNode();
	// which = 1 - which;
	// printNode();
	// }
	// System.out.println("Phantom Spread");
	// phantomSpreadOrReturn();
	// }
	// System.out.println("After Collapse");
	// printNode();
	// }
	//
	// private void phantomSpreadOrReturn() {
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.phantomize(collapseId, null);
	// }
	// phantomized = true;
	// if (outgoingLinks.isEmpty()) {
	// sendReturnMessage(false);
	// }
	// }
	//
	private void decRCIncPC(Message m) {

		if (m.getOld() < myWeight) {
			rc[which]--;
			pc++;
		} else {
			rc[1 - which]--;
			pc++;
		}
		if (maxWeight < m.getNew()) {
			maxWeight = m.getNew();
		}
	}

	//
	// private void remarkingPhantomizing(Message m) {
	// collapseId = m.getCollapseId();
	// waitMsg = 0;
	// nodeParent = m.getSrc();
	// if (phantomized) {
	// for (Link l : outgoingLinks.values()) {
	// incWaitMsg();
	// l.phantomize(collapseId, m.getOverride());
	// }
	// if (outgoingLinks.isEmpty()) {
	// sendReturnMessage(false);
	// }
	// } else {
	// sendReturnMessageSender(startover, m.getSrc(), m.getCollapseId());
	// }
	// }
	//
	// private void plagueDeleteMessage(Message m) {
	// System.out.println("Received this");
	// if (m.getPhantom()) {
	// pc--;
	// if (m.getCollapseId().equalTo(collapseId) && rcc > 0) {
	// rcc--;
	// }
	// }
	// if (phantomized == true && m.getCollapseId().equalTo(collapseId)) {
	// for (Link l : outgoingLinks.values()) {
	// l.plagueDelete(collapseId);
	// }
	// outgoingLinks.clear();
	// if (isSimplyDead()) {
	// collapseId = null;
	// state = NodeState.Dead;
	// }
	// }
	// }
	//
	private void deleteMessage(Message m) {
		decrementCountInDelMsg(m);
		if (isGarbage()) {
			sendPlagueOrDelMsg();
		}
		if (isSimplyDead()) {
			collapseId = null;
			state = NodeState.Dead;
			return;
		} else if (isGarbage() || getSRC() > 0) {
			return;
		} else if (isWeaklySupported()) {
			startPhantomizing();
			state = NodeState.Phantomizing;
		} else {
			if (isPhantomizing() || isRecovering() || isBuilding()) {
				sendReturnMessage(true);
				waitMsg = 0;
			}
			rcc = 0;
			rephantomize = true;
			rerecover = true;
			collapseId = new CollapseId(nodeId);
			// long oldWeight = myWeight;
			// myWeight = maxWeight + 1;
			// rc[which] = rc[1-which];
			// rc[1-which] = 0;
			// spawnNewCollapseHere(oldWeight);
		}
	}

	private void spawnNewCollapseHere(long oldWeight) {
		collapseId = new CollapseId(nodeId);
		nodeParent = nodeId;
		for (Link l : outgoingLinks.values()) {
			incWaitMsg();
			l.phantomize(collapseId, null, oldWeight, myWeight);
		}
		phantomized = true;
		if (outgoingLinks.isEmpty()) {
			incWaitMsg();
			sendReturnMessage(false);
		}
	}

	private void startPhantomizing() {
		long oldWeight = myWeight;
		myWeight = maxWeight + 1;
		rc[which] = rc[1 - which];
		rc[1 - which] = 0;
		spawnNewCollapseHere(oldWeight);
	}

	private void sendPlagueOrDelMsg() {
		if (phantomized) {
			for (Link l : outgoingLinks.values()) {
				l.plagueDelete(collapseId, myWeight);
			}
		} else if (isSimplyDead()) {
			for (Link l : outgoingLinks.values()) {
				l.delete(collapseId, myWeight);
			}
		}
	}

	private void decrementCountInDelMsg(Message m) {
		if (m.getPhantom()) {
			pc--;
			if ((isRecovering() || isBuilding())
					&& m.getCollapseId().equalTo(collapseId) && rcc > 0) {
				rcc--;
			}
		} else if (m.getNew() < myWeight)
			rc[which]--;
		else
			rc[1 - which]--;
	}

	public Queue getQu() {
		return qu;
	}

	public int getWhich() {
		return which;
	}

	private void createLinkMessage(Message msg) {
		if (getSRC() == 0 && getWRC() == 0 && pc == 0) {
			myWeight = msg.getNew() + 1;
			maxWeight = msg.getNew();
			rc[which]++;
		} else if (getSRC() > 0) {
			if (msg.getNew() < myWeight) {
				rc[which]++;
			} else {
				rc[1 - which]++;
			}
			if (maxWeight < msg.getNew()) {
				maxWeight = msg.getNew();
			}
		} else if (pc > 0) {
			if (!msg.getPhantom()) {
				if (msg.getNew() < myWeight) {
					rc[which]++;
				} else {
					myWeight = msg.getNew() + 1;
					rc[which]++;
				}
			} else {
				System.out.println(
						"Node is creating phantom from node that is already phantomized");
				assert false;
			}
		}
		if (maxWeight < msg.getNew()) {
			maxWeight = msg.getNew();
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
				+ " collapseID" + co + " wait " + waitMsg + "mark " + mark
				+ " which = " + which + "weight " + myWeight + " max weight"
				+ maxWeight);
	}

	public static void main(String args[]) {
		BasicConfigurator.configure();
		logger.info(" Result is " + new Node().toReturn());
	}
}
