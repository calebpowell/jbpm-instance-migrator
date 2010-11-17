package org.jbpm.instance.migration;

import org.jbpm.graph.exe.ProcessInstance;

public class NoDefaultConstructorDynamicMigrationForTesting implements DynamicMigration {

	
	public NoDefaultConstructorDynamicMigrationForTesting(String s) {
		super();
	}

	public String map(String deprecatedNodeName, ProcessInstance oldProcessInstance) {
		return null;
	}

}
