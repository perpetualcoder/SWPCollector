package edu.lsu.cct.swp;

import org.apache.log4j.Logger;

import java.nio.file.LinkOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;

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
	public long myWeight;
	private long maxWeight;
	private boolean recoverysent;

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
		startover = phantomized = recoverysent = false;
		state = NodeState.Healthy;
		collapseId = null;
		waitMsg = 0;
		nodeParent = -1;
		rephantomize = false;
		rerecover = false;
	}

	public boolean isHealthy() {
		return state == NodeState.Healthy;
	}

	public boolean isOriginator() {
		return nodeParent == nodeId && collapseId != null
				&& collapseId.originator == nodeId;
	}

	public boolean isWeaklySupported() {
		return getWRC() > 0 && getSRC() == 0 && pc == 0 && rcc == 0
				&& phantomized == false && collapseId == null && waitMsg == 0;
	}

	public boolean isPhantomlySupported() {
		return getWRC() == 0 && getSRC() == 0 && pc > 0 && rcc == 0
				&& phantomized == false && collapseId == null && waitMsg == 0;
	}

	public boolean isPhantomizing() {
		return state == NodeState.Phantomizing;
	}

	public boolean isPhantomized() {
		return state == NodeState.Phantomized;
	}

	public boolean isRecovering() {
		return state == NodeState.Recovering;
	}

	public boolean isRecovered() {
		return state == NodeState.Recovered;
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

	public boolean isBuilding() {
		return state == NodeState.Building;
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
		if (waitMsg == 0 && isPhantomizing()) {
			state = NodeState.Phantomized;
			if (isOriginator() || rerecover) {
				rerecover = false;
				if (getSRC() > 0) {
					state = NodeState.Building;
					for (Link l : outgoingLinks.values()) {
						incWaitMsg();
						l.build(collapseId, myWeight, myWeight, recoverysent);
					}
					if (outgoingLinks.isEmpty()) {
						if(isOriginator())
						incWaitMsg();
						sendReturnMessage(false);
					}
					if (recoverysent) {
						recoverysent = false;
					}
				} else {
					state = NodeState.Recovering;
					recoverysent = true;
					for (Link l : outgoingLinks.values()) {
						incWaitMsg();
						l.recover(collapseId, myWeight);
					}
					if (outgoingLinks.isEmpty()) {
						if(isOriginator())
						incWaitMsg();
						sendReturnMessage(false);
					}
				}
			} else {
				sendReturnMessage(false);
			}
		}
		if (waitMsg == 0 && isBuilding()) {
			if (getSRC()>0 || startover ) {
				if (!isOriginator()) {
					sendReturnMessage(startover);
				}
				cleanUp();
			} else {
				rephantomize = false;
//				rerecover = true;
				state = NodeState.Phantomizing;
				recoverysent = false;
				for (Link l : outgoingLinks.values()) {
					incWaitMsg();
					l.phantomize(collapseId, null, myWeight, myWeight);
				}
				if (outgoingLinks.isEmpty()) {
					if(isOriginator())
					incWaitMsg();
					sendReturnMessage(false);
				}
			}
		}
		if (waitMsg == 0 && isRecovering()) {
			if (rcc == pc) {
				if(isOriginator()) {
					Here.here("Hello!"+startover);
					printNode();
					if(startover) {
						rerecover = true;
						startover = false;
					} else if(rerecover || getSRC() > 0) {
						Here.here("This is not exectured");
						rerecover = false;
						rcc = 0;
						recoverOrBuild();
					} else if(isGarbage()) {
						sendPlagueOrDelMsg();
						outgoingLinks.clear();
					}
				} else {
					if (startover) {
						state = NodeState.Recovered;
						sendReturnMessage(true);
						startover = false;
					} else if(rerecover || getSRC() > 0) {
						rerecover = false;
						recoverOrBuild();
					}else if(isMayBeDelete()){
						state = NodeState.Recovered;
						sendReturnMessage(false);
					}
				}
			} else {
				if (rerecover) {
					rerecover = false;
					if (isOriginator()) {
						rcc = 0;
					}
					recoverOrBuild();
				}
			}
		}
	}

	private void recoverOrBuild() {
		if (getSRC() == 0) {
			state = NodeState.Recovering;
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.recover(collapseId, myWeight);
				recoverysent = true;
			}
			if (outgoingLinks.isEmpty()) {
				if(isOriginator())
				incWaitMsg();
				sendReturnMessage(false);
				if (!isOriginator())
					state = NodeState.Recovered;
			}
		} else if(getSRC() > 0) {
			state = NodeState.Building;
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.build(collapseId, myWeight, myWeight, recoverysent);
			}
			if (outgoingLinks.isEmpty()) {
				if(isOriginator())
				incWaitMsg();
				sendReturnMessage(false);
				if (!isOriginator())
					cleanUp();
			}
			if (recoverysent){
				recoverysent = false;
			}
		}
	}

	private void cleanUp() {
		rephantomize = false;
		rerecover = false;
		recoverysent = false;
		startover = false;
		collapseId = null;
		nodeParent = -1;
		rcc = 0;
		state = NodeState.Healthy;
		phantomized = false;
	}

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

	private void linkBuild(long old, long noob, boolean recov) {
		pc--;
		if (!isHealthy() && recov) rcc--;
		maxWeight = maxWeight < noob ? noob : maxWeight;
		if (getSRC() == 0) {
			myWeight = maxWeight + 1;
			rc[which]++;
		} else {
			rc[1 - which]++;
		}
	}

	private void buildMessage(Message m) {
		if (isBuilding() || isHealthy()) {
			linkBuild(m.getOld(), m.getNew(), m.getDecrcc());
			sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
		} else if(isPhantomizing()) {
			if (collapseId == null || m.getCollapseId().lessThanOrEqualTo(collapseId)) {
				linkBuild(m.getOld(), m.getNew(), m.getDecrcc());
				sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
			} else {
				if (!isOriginator()) {
					sendReturnMessage(true);
				}
				linkBuild(m.getOld(), m.getNew(),m.getDecrcc());
				nodeParent = m.getSrc();
				collapseId = m.getCollapseId();
				rerecover = true;
			}
		} else if(isPhantomized()) {
			linkBuild(m.getOld(), m.getNew(), m.getDecrcc());
			nodeParent = m.getSrc();
			collapseId = m.getCollapseId();;
			if (phantomized) {
				state = NodeState.Building;
				for (Link l : outgoingLinks.values()) {
					incWaitMsg();
					l.build(collapseId, myWeight, myWeight, recoverysent);
				}
				if (outgoingLinks.isEmpty()) {
					if (isOriginator())
						incWaitMsg();
					sendReturnMessage(false);
					if (!isOriginator())
						cleanUp();
				}
			} else {
				sendReturnMessage(false);
				cleanUp();
			}
		} else if(isRecovering() && waitMsg > 0) {
			if (m.getCollapseId().lessThanOrEqualTo(collapseId)) {
				linkBuild(m.getOld(), m.getNew(), m.getDecrcc());
				sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
			} else  {
				if (!isOriginator()) {
					sendReturnMessage(true);
				}
				collapseId = m.getCollapseId();
				nodeParent = m.getSrc();
				linkBuild(m.getOld(), m.getNew(), m.getDecrcc());
			}
		} else if(isRecovering() && waitMsg == 0 && rcc != pc) {
			if (m.getCollapseId().lessThanOrEqualTo(collapseId)) {
				linkBuild(m.getOld(), m.getNew(), m.getDecrcc());
				sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
				rerecover = true;
				incWaitMsg();
				sendReturnMessageMyself();
				
			} else {
				linkBuild(m.getOld(), m.getNew(), m.getDecrcc());
				if (!isOriginator()) {
					sendReturnMessage(true);
				}
				collapseId = m.getCollapseId();
				nodeParent = m.getSrc();;
				rerecover = true;
				incWaitMsg();
				sendReturnMessageMyself();
			}
		} else if (isRecovered()) {
			linkBuild(m.getOld(), m.getNew(), m.getDecrcc());
			collapseId = m.getCollapseId();
			nodeParent = m.getSrc();
			state = NodeState.Building;
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.build(collapseId, myWeight, myWeight, recoverysent);
			}
			if (outgoingLinks.isEmpty()) {
				if (isOriginator())
					incWaitMsg();
				sendReturnMessage(false);
				if (!isOriginator())
					cleanUp();
			}
			
		}
	}
	
	private void recoverMessage(Message m) {
		if (isHealthy()) {
			sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
		} else if (isPhantomizing()) {
			if(!isOriginator()) {
				sendReturnMessage(true);
				
			}
			rerecover = true;
			collapseId = m.getCollapseId();;
			nodeParent = m.getSrc();
			
		} else if(isPhantomized()) {
			rcc++;
			state = NodeState.Recovering;
			collapseId = m.getCollapseId();
			nodeParent = m.getSrc();
			recoverOrBuild();
		} else if(isBuilding()) {
			if (m.getCollapseId().lessThan(collapseId)) {
				rephantomize = true;
				rerecover = true;
				sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
			} else if (m.getCollapseId().equalTo(collapseId)) {
				sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
			} else  {
				if (!isOriginator()) {
					sendReturnMessage(true);
				}
				collapseId = m.getCollapseId();
				rerecover = true;
				nodeParent = m.getSrc();;
				rephantomize = true;
			}
		} else if (isRecovering()) {
			if (m.getCollapseId().lessThan(collapseId)) {
				sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
			} else if (m.getCollapseId().equalTo(collapseId)) {
				rcc++;
				sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
				if (!isOriginator() && rcc==pc && waitMsg ==0) {
					sendReturnMessage(false);
				}
			} else {
				if (!isOriginator()) {
					sendReturnMessage(true);
					
				}
				collapseId = m.getCollapseId();
				nodeParent = m.getSrc();
				rcc = 1;
				if (waitMsg > 0) {
					rerecover = true;
				} else {
					state = NodeState.Recovering;
					for (Link l : outgoingLinks.values()) {
						incWaitMsg();
						l.recover(collapseId, myWeight);
						recoverysent = true;
					}
					if (outgoingLinks.isEmpty()) {
						if(isOriginator())
						incWaitMsg();
						sendReturnMessage(false);
						if (!isOriginator())
							state = NodeState.Recovered;
					}
				}
			}
		} else if(isRecovered()) {
			rcc = 1;
			collapseId = m.getCollapseId();;
			nodeParent = m.getSrc();;
			state = NodeState.Recovering;
			for (Link l : outgoingLinks.values()) {
				incWaitMsg();
				l.recover(collapseId, myWeight);
				recoverysent = true;
			}
			if (outgoingLinks.isEmpty()) {
				if(isOriginator())
				incWaitMsg();
				sendReturnMessage(false);
				if (!isOriginator())
					state = NodeState.Recovered;
			}
			
		}
	}

	private void returnMessage(Message m) {
		startover = startover | m.isStartOver();
		decWaitMsg();
//		startover = m.isStartOver();
	}

	private void sendReturnMessage(boolean startover) {
		Message msg = new Message(MessageType.Return, nodeId, getParentNodeId(),
				myWeight, myWeight);
		msg.setStartOver(startover);
		msg.setCollapseId(collapseId);
		msg.send();
		msg.printMsg("receiving");
	}
	
	private void sendReturnMessageMyself() {
		Message msg = new Message(MessageType.Return, nodeId, nodeId,
				myWeight, myWeight);
		msg.setStartOver(false);
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
	
	
	/***
	 * Decrement SC/WC and Increment PC.
	 * if (phantomizing)
	 * 		if (initiator)
	 * 			if (msg.CID <= CID)
	 * 				<R, Node Id, msg.sender, CID>
	 * 			else
	 * 				parent = msg.sender
	 * 				CID = null
	 * 		else
	 * 			<R, Node Id, msg.sender, CID>
	 * else if (healthy or phantomized)
	 * 		parent = msg.sender
	 * 		<R, Node Id, msg.sender, CID>
	 * else if (Weakly Supported  or SC = 0)
	 * 		Toggle SC and WC
	 * 		parent = msg.sender
	 * 		state = phantomizing & Phantomized = true
	 * 		<PH, Node Id, Gamma out, CID>
	 * 		if (Gamma out is empty)
	 * 			state = phantomized
	 * 			<R, Node Id, parent, CID>
	 * else if (building)
	 * 		if ( SC > 0)
	 * 			<R, Node Id, msg.sender, CID>
	 * 		else 
	 * 			if (not initiator)
	 * 				<R, Node Id, parent, CID, True>
	 * 				Rephantomize = true
	 * 				parent = msg.sender
	 * 			else 
	 * 				Rephantomize = true
	 * 				if (msg.CID > CID)
	 * 					CID = null & parent = msg.sender
	 * 				else
	 * 					<R, Node ID, msg sender, CID>
	 * 					Rerecover = true
	 * @param m
	 */

	private void phantomizeMessage(Message m) {
		decRCIncPC(m);
		if (isPhantomizing() || isHealthy() || isPhantomized()) {
			if (isOriginator()) {
				if (m.getCollapseId().lessThanOrEqualTo(collapseId)) {
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
					phantomized = true;
					if (outgoingLinks.isEmpty()) {
						state = NodeState.Phantomized;
						sendReturnMessage(false);
					}
				} else {
					nodeParent = m.getSrc();
					sendReturnMessageSender(false, m.getSrc(),
							m.getCollapseId());
				}
			}
		} else if (isBuilding()) {
			if (collapseId.lessThan(m.getCollapseId())) {
				if (!isOriginator()) {
					sendReturnMessage(true);
				}
				collapseId = m.getCollapseId();
				;
				nodeParent = m.getSrc();
				rephantomize = true;
			} else {
				sendReturnMessageSender(false, m.getSrc(), m.getCollapseId());
				if (getSRC() == 0) {
					rephantomize = true;
				}
			}
		}
	}

	
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

	private void plagueDeleteMessage(Message m) {
		Here.here("Received this");
		if (m.getPhantom()) {
			pc--;
			if (m.getCollapseId().equalTo(collapseId) && rcc > 0) {
				rcc--;
			}
		}
		for (Link l : outgoingLinks.values()) {
			l.plagueDelete(collapseId, myWeight);
		}
		outgoingLinks.clear();
		if (isSimplyDead()) {
			collapseId = null;
			state = NodeState.Dead;
		}
	}
	
	/***
	 * Decrement SC/WC/PC/RCC
	 * If (Garbage State)
	 * 		If(Phantomized)
	 * 			Send PD to Gamma
	 * 		else
	 * 			send D to Gamma
	 * 			Delete the node.
	 * else if (Weakly supported)
	 * 		Toggle Counts
	 * 		CID = new CID
	 * 		<PH, Node Id, Gamma, CID>
	 * 		state = Phantomizing
	 * else if (pc $>$ 0 & SC = 0)
	 * 		Toggle Counts.
	 * 		if (not initiator)
	 * 				<R, Node Id, parent, CID, true>
	 * 		if (W > 0)
	 * 			RR = true & RP = true 
	 * 			CID = new CID;
	 * 		else
	 * 			CID = new CID;
	 * 			RCC = 0.
	 * 	 		<RC,Node Id, Gamma, CID>
	 * 		 
	 * @param m
	 */

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
		} else if (isWeaklySupported() || isPhantomlySupported()) {
			startPhantomizing();
			state = NodeState.Phantomizing;
		} else {
			if (waitMsg > 0) {
				if (!isOriginator()) {
					sendReturnMessage(true);
				}
				rerecover = true;
				rephantomize = true;
				collapseId = new CollapseId(nodeId);
				nodeParent = nodeId;
			} else {
				if (!isOriginator() && isRecovering() && rcc == pc) {
					sendReturnMessage(true);
				}
				nodeParent = nodeId;
				collapseId = new CollapseId(nodeId);
				for (Link l : outgoingLinks.values()) {
					incWaitMsg();
					l.recover(collapseId, myWeight);
				}
				if (outgoingLinks.isEmpty()) {
					incWaitMsg();
					sendReturnMessage(false);
				}
			}
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
				Here.here(
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
		Here.here("Node " + nodeId + " : " + getSRC() + "," + getWRC()
				+ " ," + pc + ", " + rcc + ", " + state + " ph: " + phantomized
				+ " collapseID" + co + " wait " + waitMsg + "mark " + mark
				+ " parent = " + nodeParent + "weight " + myWeight + " max weight"
				+ maxWeight+" start over "+startover);
	}

	public static void main(String args[]) {
		BasicConfigurator.configure();
		logger.info(" Result is " + new Node().toReturn());
	}
}
