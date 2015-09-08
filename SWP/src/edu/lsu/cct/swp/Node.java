package edu.lsu.cct.swp;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

public class Node {
	static Logger logger = Logger.getLogger(Node.class);
	public String toReturn(){
		return "Hello!";
	}
	public static void main(String args[]) {
		BasicConfigurator.configure();
		logger.info(" Result is "+new Node().toReturn());
	}

}
