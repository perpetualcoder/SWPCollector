package edu.lsu.cct.swp;

import java.util.concurrent.atomic.AtomicInteger;

public class CollapseId {
	static AtomicInteger uid = new AtomicInteger();
	int cid;
	int in;
	public CollapseId() {
		cid = uid.incrementAndGet();
		in = 0;
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
	
	public boolean partialLessThan(CollapseId c) {
		return c !=  null && cid == c.cid && in < c.in;
	}
}
