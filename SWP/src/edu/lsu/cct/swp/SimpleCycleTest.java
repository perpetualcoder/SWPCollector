package edu.lsu.cct.swp;

public class SimpleCycleTest extends junit.framework.TestCase {

	private Node node[] = new Node[5];
	private Node root;

	static Master master = new Master();

	public void testSimpleCyleDelete() {
		Message.master = master;
		master.no = node;
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
			node[i].printNode();
		}
		System.out.println("Ehllo!");
		// Severe the root link.
		root.deleteLink(node[0].getNodeId());
		master.mark(root);
		// Process all the messages in the system.
		
		master.processAllMessages();
		// Verify if all the nodes are dead by now.
		for (int i = 0; i < 5; i++) {
			 node[i].printNode();
			assertTrue("Node is not dead",
					node[i].getState() == NodeState.Dead);
			assertTrue("Something went wrong", 
					(node[i].getState() == NodeState.Dead 
					&& node[i].mark == false) ||
					(node[i].getState() == NodeState.Healthy
					&& node[i].mark == true));
		}
		System.out.println("-----------------------------------------------------------------------------------");

	}

	public void testSimpleCyleRecover() {
		Message.master = master;
		master.no = node;
		Node root1 = master.createNode();
		Node prev = root1;
		// Creating five nodes and then creating a
		// cycle.
		for (int i = 0; i < 5; i++) {
			node[i] = master.createNode();
			prev.createLink(node[i].getNodeId());
			master.processAllMessages();
			prev = node[i];
		}
		node[4].createLink(node[0].getNodeId());
		master.processAllMessages();
		root = master.createNode();
		root.myWeight = 7;
		root.createLink(node[2].getNodeId());
		master.processAllMessages();
		// Verifying if the nodes are all healthy and
		// count as expected.
		for (int i = 0; i < 5; i++) {
			assertTrue("The Nodes are not strong", node[i].getSRC() > 0);
			assertTrue("The nodes are healthy",
					node[i].getState() == NodeState.Healthy);
			node[i].printNode();
		}
		// Verifying if the weak counts are as expected.
		assertTrue("The node 0 weak count is not right", node[0].getWRC() == 1);
		assertTrue(" The node 1 weak count is not right",
				node[2].getWRC() == 1);
		// Severe the root link.
		root1.deleteLink(node[0].getNodeId());
		master.mark(root);
		// Process all the messages in the system.
		master.processAllMessages();
		// Verify if all the nodes are healthy by now because we have a root
		// node connected to one of the nodes.
		for (int i = 0; i < 5; i++) {
			assertTrue("Node is not dead",
					node[i].getState() == NodeState.Healthy);
			assertTrue("Something went wrong", 
					(node[i].getState() == NodeState.Dead 
					&& node[i].mark == false) ||
					(node[i].getState() == NodeState.Healthy
					&& node[i].mark == true));
			node[i].printNode();
		}
		System.out.println("-----------------------------------------------------------------------------------");


	}

}
