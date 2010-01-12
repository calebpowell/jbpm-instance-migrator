package org.jbpm.instance.migration.handler;

import java.io.IOException;

import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.instance.migration.BaseTestCase;
import org.jbpm.instance.migration.InvalidMigrationException;
import org.jbpm.instance.migration.Migration;
import org.jbpm.instance.migration.MigrationUtils;
import org.jbpm.instance.migration.Migrator;
import org.jbpm.instance.migration.StateNodeMap;

public class EndProcessMigrationHandlerTest extends BaseTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test_that_the_migrateInstance_method_will_end_the_old_ProcessInstance() throws IOException {
		deployV1Definitions();
		ProcessInstance originalProcessInstance = findLatestProcessDefinition("simple").createProcessInstance();
		deployV2Definitions();
		
		Migrator migrator = createSimpleProcessDefinitionMigrator();
		migrator.addMigrationHandler(new EndProcessMigrationHandler());
		ProcessInstance newProcessInstance = migrator.migrate(originalProcessInstance);
		
		assertTrue(originalProcessInstance.hasEnded());
		assertFalse(newProcessInstance.hasEnded());
	}
	
	private void deployV1Definitions() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleProcessDefinition_001.xml"));
	}
	
	private void deployV2Definitions() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleProcessDefinition_002.xml"));
	}
	
	private Migrator createSimpleProcessDefinitionMigrator() throws InvalidMigrationException {
		return new Migrator("simple", this.jbpmContext, new Migration[]{new SimpleProcessDefinitionMigration001(), new SimpleProcessDefinitionMigration002()}, null);
	}
	
	private static class SimpleProcessDefinitionMigration001 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"A","first"}, {"B","second"}, });
		}
	}
	private static class SimpleProcessDefinitionMigration002 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"first","third"}, {"forkNode1","forkNodeOne"}, {"forkNode2","forkNodeTwo"}, {"forkNode3","forkNodeThree"}, });
		}
	}


}
