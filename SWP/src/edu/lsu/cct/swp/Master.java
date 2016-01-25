package edu.lsu.cct.swp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;

public class Master {
	public final Random RAND = new Random();
	
	public Node[] no;

	private HashMap<Integer, Node> nodeMap;

	Master() {
		nodeMap = new HashMap<Integer, Node>();
	}

	public Node getNode(int nodeId) {
		return nodeMap.get(nodeId);
	}
	
	public void printAllNodes() {
		for (int i = 0; i < no.length; i++) {
			if (no[i] == null)
				continue;
			assert( no[i].getSRC() > 0);
			assert(	no[i].getState() == NodeState.Healthy);
			no[i].printNode();
		}
	}
	
	public Node createNode() {
		Node n = new Node();
		nodeMap.put(n.getNodeId(), n);
		return n;
	}
	
	public void mark(Node root) {
		Queue<Node> qu = new LinkedList<Node>();
		qu.add(root);
		for (Node n : nodeMap.values()) {
			n.mark = false;
		}
		while (!qu.isEmpty()) {
			Node n = qu.poll();
			if (n.mark)
				continue;
			n.mark = true;
			for (Link l : n.outgoingLinks.values()) {
				qu.add(nodeMap.get(l.getDest()));
			}
		}
	}
	
	public void processAllMessages() {
		while (true) {
			List<Node> nodes = new ArrayList<>();
			String r=" ";
			for (Node n : nodeMap.values()) {
				if (n.getQu().size() > 0) {
					r = r+ " , "+n.getNodeId();
					nodes.add(n);
				}
			}
			System.out.println("Mailbox has messages for "+r);
			if (nodes.size() == 0) {
				break;
			}
			while (nodes.size() > 0) {
				int n = RAND.nextInt(nodes.size());
				Node node = nodes.get(n);
				node.processQueue();
				if (node.getQu().size() == 0)
					nodes.remove(node);
			}
			printAllNodes();
			System.out.println("---Mailbox---");
		}
	}

}
