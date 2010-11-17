package org.jbpm.instance.migration;

import org.jbpm.graph.exe.ProcessInstance;

public class DynamicMigrationForTesting implements DynamicMigration {

	public String map(String deprecatedNodeName, ProcessInstance oldProcessInstance) {
		return null;
	}

}
