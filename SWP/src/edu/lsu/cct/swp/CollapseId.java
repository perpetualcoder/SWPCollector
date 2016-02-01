package edu.lsu.cct.swp;

import java.util.concurrent.atomic.AtomicInteger;

public class CollapseId {
	static AtomicInteger uid = new AtomicInteger();
	int cid;
	int in;
	public int originator;
	public CollapseId(int nodeId) {
		cid = uid.incrementAndGet();
		in = 0;
		originator = nodeId;
	}
	
	public void newCollapse() {
		in++;
	}
	
	public boolean equalTo(CollapseId c) {
		return c != null && cid == c.cid && c.in == in;
		
	}
	public boolean lessThan(CollapseId c) {
		return c != null && cid < c.cid;
	}
	
	public boolean lessThanOrEqualTo(CollapseId c) {
		return lessThan(c) || equalTo(c);
	}
	
	public boolean partialLessThan(CollapseId c) {
		return c !=  null && cid == c.cid && in < c.in;
	}
	public String toString() {
		return "<"+cid+","+in+">";
	}
}
