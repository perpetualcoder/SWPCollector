package edu.lsu.cct.swp;

public class NodeTest extends junit.framework.TestCase {
	public void testNothing() {
	}

	public void testWillAlwaysFail() {
		assertEquals(" to Return must return hello","Hello!",new Node().toReturn());
	}
}
