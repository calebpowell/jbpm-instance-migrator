package org.jbpm.instance.migration;

import org.jbpm.graph.exe.ProcessInstance;

public abstract class AbstractDynamicMigrationForTesting implements DynamicMigration {

	public String map(String deprecatedNodeName, ProcessInstance oldProcessInstance) {
		return null;
	}

}
