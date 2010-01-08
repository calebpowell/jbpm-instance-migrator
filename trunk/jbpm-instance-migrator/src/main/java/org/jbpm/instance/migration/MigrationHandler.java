package org.jbpm.instance.migration;

import org.jbpm.graph.exe.ProcessInstance;

public interface MigrationHandler {

	public void migrateInstance(ProcessInstance oldProcessInstance, ProcessInstance newProcessInstance);
}
