package edu.lsu.cct.swp;

public class TwoRingTest extends junit.framework.TestCase {

	private Node node[] = new Node[13];
	private Node root;

	static Master master = new Master();

//	public void testTwoRingOneDel() {
//		Message.master = master;
//		Node root = master.createNode();
//		Node prev = root;
//		// Creating five nodes and then creating a
//		// cycle.
//		for (int i = 0; i < 3; i++) {
//			node[i] = master.createNode();
//			prev.createLink(node[i].getNodeId());
//			master.processAllMessages();
//			prev = node[i];
//		}
//		node[2].createLink(node[0].getNodeId());
//		master.processAllMessages();
//		
//		node[3] = master.createNode();
//		node[4] = master.createNode();
//		node[1].createLink(node[3].getNodeId());
//		master.processAllMessages();
//		node[3].createLink(node[4].getNodeId());
//		master.processAllMessages();
//		
//		prev = root;
//		for (int i = 5; i < 8; i++) {
//			node[i] = master.createNode();
//			prev.createLink(node[i].getNodeId());
//			master.processAllMessages();
//			prev = node[i];
//		}
//		node[7].createLink(node[5].getNodeId());
//		master.processAllMessages();
//		
//		node[8] = master.createNode();
//		node[9] = master.createNode();
//		node[6].createLink(node[8].getNodeId());
//		master.processAllMessages();
//		node[8].createLink(node[9].getNodeId());
//		master.processAllMessages();
//		
//		node[10] = master.createNode();
//		node[9].createLink(node[10].getNodeId());
//		master.processAllMessages();
//		node[4].createLink(node[10].getNodeId());
//		master.processAllMessages();
//		node[11] = master.createNode();
//		node[10].createLink(node[11].getNodeId());
//		master.processAllMessages();
//		node[12] = master.createNode();
//		node[11].createLink(node[12].getNodeId());
//		master.processAllMessages();
//		
//		// Verifying if the nodes are all healthy and
//		// count as expected.
//		for (int i = 0; i < 13; i++) {
//			assertTrue("The Nodes are not strong", node[i].getSRC() > 0);
//			assertTrue("The nodes are healthy",
//					node[i].getState() == NodeState.Healthy);
//			node[i].printNode();
//		}
//		System.out.println("Ehllo!");
//		// Severe the root link.
//		root.deleteLink(node[0].getNodeId());
//		master.mark(root);
//		// Process all the messages in the system.
//		master.processAllMessages();
//		// Verify if all the nodes are dead by now.
//		for (int i = 0; i < 13; i++) {
//			 node[i].printNode();
//			assertTrue("Something went wrong", 
//					(node[i].getState() == NodeState.Dead 
//					&& node[i].mark == false) ||
//					(node[i].getState() == NodeState.Healthy
//					&& node[i].mark == true));
//		}
//		System.out.println("Hello!!!");
//
//	}
	
	public void testTwoRingOneDel() {
		Message.master = master;
		Node root = master.createNode();
		Node prev = root;
		master.no = node;

		// Creating five nodes and then creating a
		// cycle.
		for (int i = 0; i < 6; i++) {
			node[i] = master.createNode();
			prev.createLink(node[i].getNodeId());
			master.processAllMessages();
			prev = node[i];
		}
		prev = root;
		for (int i = 6; i < 13; i++) {
			node[i] = master.createNode();
			prev.createLink(node[i].getNodeId());
			master.processAllMessages();
			prev = node[i];
		}
		node[5].createLink(node[6].getNodeId());
		master.processAllMessages();
		node[12].createLink(node[0].getNodeId());
		master.processAllMessages();
		
		node[0].createLink(node[6].getNodeId());
		master.processAllMessages();
		node[6].createLink(node[0].getNodeId());
		master.processAllMessages();
		
		// Verifying if the nodes are all healthy and
		// count as expected.
		for (int i = 0; i < 13; i++) {
			assertTrue("The Nodes are not strong", node[i].getSRC() > 0);
			assertTrue("The nodes are healthy",
					node[i].getState() == NodeState.Healthy);
			node[i].printNode();
		}
		System.out.println("Ehllo!");
		// Severe the root link.
//		root.deleteLink(node[0].getNodeId());
//		root.deleteLink(node[6].getNodeId());
		master.mark(root);
		// Process all the messages in the system.
		master.processAllMessages();
		// Verify if all the nodes are dead by now.
		for (int i = 0; i < 13; i++) {
			 node[i].printNode();
			assertTrue("Something went wrong", 
					(node[i].getState() == NodeState.Dead 
					&& node[i].mark == false) ||
					(node[i].getState() == NodeState.Healthy
					&& node[i].mark == true));
		}
		System.out.println("Hello!!!");

	}

}
