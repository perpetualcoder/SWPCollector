package edu.lsu.cct.swp;

import java.util.HashMap;

public class Master {
	private HashMap<Integer, Node> nodeMap;
	
	Master(){
		nodeMap = new HashMap<Integer, Node>();
	}
	
	public Node getNode(int nodeId){
		return nodeMap.get(nodeId);
	}

}
