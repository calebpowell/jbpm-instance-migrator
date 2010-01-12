package org.jbpm.instance.migration.handler;

import org.jbpm.graph.exe.ProcessInstance;

public interface MigrationHandler {

	public void migrateInstance(ProcessInstance oldProcessInstance, ProcessInstance newProcessInstance);
}
