package edu.lsu.cct.swp;

public class BenzeneTest extends junit.framework.TestCase {

	public static int count = 4;
	public static int NumBenzene = 2;
	public static int totalNodes = count + (NumBenzene - 1) * (count - 2);
	private Node node[] = new Node[totalNodes];
	private Node root;

	static Master master = new Master();

	public void testTwoRingOneDel() {
		Message.master = master;
		master.no = node;
		Node root = master.createNode();
		Node prev = root;
		int countDone = 0;
		for (int i = 0; i < NumBenzene; i++) {
			if (i == 0) {
				creatFirstBenzene(root);
				countDone = count;
			} else {
				Node high = master.getNode(countDone);
				Node low = master.getNode(countDone - 1);
				creatBenzene(root, high, low, countDone);
				countDone = countDone + (count - 2);
			}
		}

		// // Creating five nodes and then creating a
		// // cycle.
		// for (int i = 0; i < count; i++) {
		// node[i] = master.createNode();
		// prev.createLink(node[i].getNodeId());
		// master.processAllMessages();
		// prev = node[i];
		// }
		// node[5].createLink(node[0].getNodeId());
		// master.processAllMessages();
		// node[0].createLink(node[5].getNodeId());
		// master.processAllMessages();
		// prev = node[5];
		// for (int i = 4; i >= 0; i--) {
		// prev.createLink(node[i].getNodeId());
		// master.processAllMessages();
		// prev = node[i];
		// }
		//
		// Verifying if the nodes are all healthy and
		// count as expected.
		for (int i = 0; i < totalNodes; i++) {
			assertTrue("The Nodes are not strong", node[i].getSRC() > 0);
			assertTrue("The nodes are healthy",
					node[i].getState() == NodeState.Healthy);
			node[i].printNode();
		}
		System.out.println("Ehllo!");
		// Severe the root link.
		int link = 1;
		for (int i = 0; i < NumBenzene; i++) {
			root.deleteLink(link);
			if (i == 0) {
				link = 5;
			} else {
				link = link + 4;
			}
		}
		 master.mark(root);
		// // Process all the messages in the system.
		 master.processAllMessages();
		 // Verify if all the nodes are dead by now.
		 for (int i = 0; i < totalNodes; i++) {
		 node[i].printNode();
		 assertTrue("Something went wrong",
		 (node[i].getState() == NodeState.Dead
		 && node[i].mark == false) ||
		 (node[i].getState() == NodeState.Healthy
		 && node[i].mark == true));
		 }
		 System.out.println("Hello!!!");

	}

	private void creatBenzene(Node root2, Node high, Node low, int countDone) {
		// TODO Auto-generated method stub
		Node prev = root2;
		Node first = null;
		for (int i = countDone; i < (countDone + count - 2); i++) {
			node[i] = master.createNode();
			prev.createLink(node[i].getNodeId());
			master.processAllMessages();
			if (i == countDone) {
				first = node[i];
				node[i].createLink(high.getNodeId());
				master.processAllMessages();
				high.createLink(node[i].getNodeId());
				master.processAllMessages();
				prev = low;
			} else if (i >= countDone + 1) {
				node[i].createLink(prev.getNodeId());
				master.processAllMessages();
				prev = node[i];
			}
		}
		node[countDone + count - 3].createLink(first.getNodeId());
		master.processAllMessages();
		first.createLink(node[countDone + count - 3].getNodeId());
		master.processAllMessages();

	}

	private void creatFirstBenzene(Node root2) {
		// TODO Auto-generated method stub
		Node prev = root2;
		for (int i = 0; i < count; i++) {
			node[i] = master.createNode();
			prev.createLink(node[i].getNodeId());
			master.processAllMessages();
			prev = node[i];
		}
		node[count - 1].createLink(node[0].getNodeId());
		master.processAllMessages();
		node[0].createLink(node[count - 1].getNodeId());
		master.processAllMessages();
		prev = node[count - 1];
		for (int i = count - 2; i >= 0; i--) {
			prev.createLink(node[i].getNodeId());
			master.processAllMessages();
			prev = node[i];
		}
	}

	// public void testSimpleCyleRecover() {
	// Message.master = master;
	// Node root = master.createNode();
	// Node prev = root;
	// // Creating five nodes and then creating a
	// // cycle.
	// for (int i = 0; i < 5; i++) {
	// node[i] = master.createNode();
	// prev.createLink(node[i].getNodeId());
	// node[i].processQueue();
	// prev.processQueue();
	// prev = node[i];
	// }
	// node[4].createLink(node[0].getNodeId());
	// node[0].processQueue();
	// node[4].processQueue();
	// root.createLink(node[2].getNodeId());
	// node[2].processQueue();
	// root.processQueue();
	// // Verifying if the nodes are all healthy and
	// // count as expected.
	// for (int i = 0; i < 5; i++) {
	// assertTrue("The Nodes are not strong", node[i].getSRC() > 0);
	// assertTrue("The nodes are healthy",
	// node[i].getState() == NodeState.Healthy);
	// node[i].printNode();
	// }
	// // Verifying if the weak counts are as expected.
	// assertTrue("The node 0 weak count is not right", node[0].getWRC() == 1);
	// assertTrue(" The node 1 weak count is not right",
	// node[2].getWRC() == 1);
	// // Severe the root link.
	// root.deleteLink(node[0].getNodeId());
	// master.mark(root);
	// // Process all the messages in the system.
	// master.processAllMessages();
	// // Verify if all the nodes are healthy by now because we have a root
	// // node connected to one of the nodes.
	// for (int i = 0; i < 5; i++) {
	// assertTrue("Node is not dead",
	// node[i].getState() == NodeState.Healthy);
	// assertTrue("Something went wrong",
	// (node[i].getState() == NodeState.Dead
	// && node[i].mark == false) ||
	// (node[i].getState() == NodeState.Healthy
	// && node[i].mark == true));
	// node[i].printNode();
	// }
	//
	// }

}
