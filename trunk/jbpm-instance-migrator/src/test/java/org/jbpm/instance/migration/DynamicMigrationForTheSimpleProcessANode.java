package org.jbpm.instance.migration;

import org.jbpm.graph.exe.ProcessInstance;

/**
 * Used for testing. 
 * 
 * @see DynamicMigrationTest
 * @author caleb powell
 *
 */
public class DynamicMigrationForTheSimpleProcessANode implements DynamicMigration {

	public String map(String deprecatedNodeName, ProcessInstance oldProcessInstance) {
		return "first";
	}

}
