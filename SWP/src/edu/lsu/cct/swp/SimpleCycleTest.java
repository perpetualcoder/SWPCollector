package edu.lsu.cct.swp;

public class SimpleCycleTest extends junit.framework.TestCase {

	private Node node[] = new Node[5];
	private Node root;

	static Master master = new Master();

	public void testSimpleCyleDelete() {
		Message.master = master;
		Node root = master.createNode();
		Node prev = root;
		// Creating five nodes and then creating a 
		// cycle.
		for (int i = 0; i < 5; i++) {
			node[i] = master.createNode();
			prev.createLink(node[i].getNodeId());
			node[i].processQueue();
			prev.processQueue();
			prev = node[i];
		}
		node[4].createLink(node[0].getNodeId());
		node[0].processQueue();
		node[4].processQueue();
		// Verifying if the nodes are all healthy and 
		// count as expected.
		for (int i = 0; i < 5; i++) {
			assertTrue("The Nodes are not strong", node[i].getSRC() > 0);
			assertTrue("The nodes are healthy",
					node[i].getState() == NodeState.Healthy);
		}
		// Severe the root link.
		root.deleteLink(node[0].getNodeId());
		// Process all the messages in the system.
		while (true) {
			boolean exit = true;
			for (int i = 0; i < 5; i++) {
				if (master.getNode(node[i].getNodeId()).getQu().size() > 0) {
					exit = false;
					master.getNode(node[i].getNodeId()).processQueue();
				}
			}
			if (exit)
				break;
		}
		// Verify if all the nodes are dead by now.
		for (int i = 0; i < 5; i++) {
			assertTrue("Node is not dead",
					node[i].getState() == NodeState.Dead);
		}

	}
	
	public void testSimpleCyleRecover() {
		Message.master = master;
		Node root = master.createNode();
		Node prev = root;
		// Creating five nodes and then creating a 
		// cycle.
		for (int i = 0; i < 5; i++) {
			node[i] = master.createNode();
			prev.createLink(node[i].getNodeId());
			node[i].processQueue();
			prev.processQueue();
			prev = node[i];
		}
		node[4].createLink(node[0].getNodeId());
		node[0].processQueue();
		node[4].processQueue();
		root.createLink(node[2].getNodeId());
		node[2].processQueue();
		root.processQueue();
		// Verifying if the nodes are all healthy and 
		// count as expected.
		for (int i = 0; i < 5; i++) {
			assertTrue("The Nodes are not strong", node[i].getSRC() == 1);
			assertTrue("The nodes are healthy",
					node[i].getState() == NodeState.Healthy);
		}
		// Verifying if the weak counts are as expected.
		assertTrue("The node 0 weak count is not right", node[0].getWRC() == 1);
		assertTrue(" The node 1 weak count is not right", node[2].getWRC() == 1);
		// Severe the root link.
		root.deleteLink(node[0].getNodeId());
		// Process all the messages in the system.
		while (true) {
			boolean exit = true;
			for (int i = 0; i < 5; i++) {
				if (master.getNode(node[i].getNodeId()).getQu().size() > 0) {
					exit = false;
					master.getNode(node[i].getNodeId()).processQueue();
				}
			}
			if (exit)
				break;
		}
		// Verify if all the nodes are healthy by now because we have a root 
		// node connected to one of the nodes.
		for (int i = 0; i < 5; i++) {
			assertTrue("Node is not dead",
					node[i].getState() == NodeState.Healthy);
		}

	}


}
