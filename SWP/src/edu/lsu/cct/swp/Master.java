package edu.lsu.cct.swp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;

public class Master {
	public final Random RAND = new Random();

	private HashMap<Integer, Node> nodeMap;

	Master() {
		nodeMap = new HashMap<Integer, Node>();
	}

	public Node getNode(int nodeId) {
		return nodeMap.get(nodeId);
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
			System.out.println("Mailbox has");
			for (Node n : nodeMap.values()) {
				if (n.getQu().size() > 0) {
					System.out.println("Node id"+n.getNodeId()+" in process");
					nodes.add(n);
				}
			}
			if (nodes.size() == 0) {
				break;
			}
			System.out.println("There are "+nodes.size()+" nodes have"
					+ "messages");
			while (nodes.size() > 0) {
				int n = RAND.nextInt(nodes.size());
				Node node = nodes.get(n);
				node.processQueue();
				if (node.getQu().size() == 0)
					nodes.remove(node);
			}
			System.out.println("---Mailbox---");
		}
	}

}
