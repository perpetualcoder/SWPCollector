package edu.lsu.cct.swp;

import java.util.HashSet;
import java.util.Set;

public class CreationTest extends junit.framework.TestCase {

	private Node node[] = new Node[5];

	static Master master = new Master();

	protected void setUp() {
		for (int i = 0; i < 5; i++) {
			node[i] = master.createNode();
		}
		Message.master = master;
	}

	public void testNodeIds() {
		Set prev = new HashSet<Integer>();
		for (int i = 0; i < 5; i++) {
			assertTrue("Should not be -1", node[i].getNodeId() != -1);
			assertTrue("Already available",
					prev.contains(node[i].getNodeId()) == false);
		}
	}

	public void createStrongLinks() {
		for (int i = 0; i < 5; i++) {
			for (int j = i + 1; j < 5; j++) {
				node[i].createLink(j);
				node[j].processQueue();
				node[i].processQueue();
			}
		}
	}

	public void createWeakLinks() {
		for (int i = 4; i > 0; i--) {
			for (int j = 0; j < i; j++) {
				node[i].createLink(j);
				node[j].processQueue();
				node[i].processQueue();
			}
		}
	}

	public void testRC() {
		createStrongLinks();
		for (int i = 0; i < 5; i++) {
			assertEquals("Strong RC is not same", i, node[i].getSRC());
			assertEquals("Number of Outgoing Links is not right", 4 - i,
					node[i].numOutgoingLinks());
		}
		createWeakLinks();
		for (int i = 0; i < 5; i++) {
			assertEquals("Weak RC is not as same as expected", 4 - i,
					node[i].getWRC());
		}
	}
	
	
}
