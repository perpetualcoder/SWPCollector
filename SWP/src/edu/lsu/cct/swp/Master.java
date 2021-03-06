package edu.lsu.cct.swp;

import java.util.HashMap;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class Master {
  public final Random RAND = new Random();

	private HashMap<Integer, Node> nodeMap;
	
	Master(){
		nodeMap = new HashMap<Integer, Node>();
	}
	
	public Node getNode(int nodeId){
		return nodeMap.get(nodeId);
	}
	
	public Node createNode(){
		Node n = new Node();
		nodeMap.put(n.getNodeId(), n);
		return n;
	}

  public void processAllMessages() {
    List<Node> nodes = new ArrayList<>();
    for(Node n : nodeMap.values()) {
      if(n.getQu().size() > 0) {
        nodes.add(n);
      }
    }
    while(nodes.size() > 0) {
      int n = RAND.nextInt(nodes.size());
      Node node = nodes.get(n);
      node.processQueue();
      if(node.getQu().size()==0)
        nodes.remove(node);
    }
  }

}
