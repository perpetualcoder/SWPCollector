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

}
