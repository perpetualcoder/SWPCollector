package edu.lsu.cct.swp;

public class SimpleDoublyLinkedListTest extends junit.framework.TestCase {

	private Node node[] = new Node[5];
	private Node root;

	static Master master = new Master();

	public void testSimpleDoubleLLDelete() {
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
		for (int i = 4; i > 0; i--) {
			node[i].createLink(node[i - 1].getNodeId());
			node[i - 1].processQueue();
			node[i].processQueue();
		}
		// Verifying if the nodes are all healthy and
		// count as expected.
		for (int i = 0; i < 5; i++) {
			assertTrue("The Nodes are not strong", node[i].getSRC() > 0);
			if (i < 4) {
				assertTrue("The Nodes does not have weak link",
						node[i].getWRC() > 0);
			}
			assertTrue("The nodes are healthy",
					node[i].getState() == NodeState.Healthy);
		}
		// Severe the root link.
		root.deleteLink(node[0].getNodeId());
		// Process all the messages in the system.
		master.processAllMessages();
		// Verify if all the nodes are dead by now.
		for (int i = 0; i < 5; i++) {
			assertTrue("Node is not dead",
					node[i].getState() == NodeState.Dead);
		}

	}

	public void testSimpleDoubleLLRecover() {
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
		for (int i = 4; i > 0; i--) {
			node[i].createLink(node[i - 1].getNodeId());
			node[i - 1].processQueue();
			node[i].processQueue();
		}
		root.createLink(node[4].getNodeId());
		node[4].processQueue();
		root.processQueue();
		// Verifying if the nodes are all healthy and
		// count as expected.
		for (int i = 0; i < 5; i++) {
			assertTrue("The Nodes are not strong", node[i].getSRC() > 0);
			assertTrue("The Nodes does not have weak link",
					node[i].getWRC() > 0);
			assertTrue("The nodes are healthy",
					node[i].getState() == NodeState.Healthy);
		}
		// Severe the root link.
		root.deleteLink(node[0].getNodeId());
		// Process all the messages in the system.
		master.processAllMessages();
		// Verify if all the nodes are dead by now.
		for (int i = 0; i < 5; i++) {
			assertTrue("Node is not dead",
					node[i].getState() == NodeState.Healthy);
		}

	}
}