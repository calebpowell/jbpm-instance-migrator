package org.jbpm.instance.migration.handler;

import org.jbpm.graph.exe.ProcessInstance;

/**
 * This handler can be used to end the oldProcessInstance.
 * @author Caleb Powell <caleb.powell@gmail.com>
 *
 */
public class EndProcessMigrationHandler implements MigrationHandler {

	/**
	 * This implementation will invoke the end() method on the oldProcessInstance parameter.
	 * @param oldProcessInstance 
	 * @param newProcessInstance
	 */
	public void migrateInstance(ProcessInstance oldProcessInstance, ProcessInstance newProcessInstance) {
		oldProcessInstance.end();
	}

}
