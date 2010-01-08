/*
 * *##% 
 * jBPM Instance Migrator
 * Copyright (C) null - 2009 JBoss Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * ##%*
 */
package org.jbpm.instance.migration;

import java.io.IOException;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jmock.Mock;


/**
 * 
 * @author Caleb Powell <caleb.powell@intelliware.ca> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class BasicMigrationTest extends BaseTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testTheProcessInstanceSignals() throws IOException, InvalidMigrationException{
		deployV1Definitions();

		ProcessInstance processInstanceV1 = findLatestProcessDefinition("simple").createProcessInstance();
		processInstanceV1.signal();
		assertEquals("A", processInstanceV1.getRootToken().getNode().getName());

		processInstanceV1.signal();
		assertEquals("Fork1", processInstanceV1.getRootToken().getNode().getName());
		assertEquals("forkNode1", processInstanceV1.getRootToken().getChild("to_forkNode1").getNode().getName());
		assertEquals("forkNode2", processInstanceV1.getRootToken().getChild("to_forkNode2").getNode().getName());
		assertEquals("forkNode3", processInstanceV1.getRootToken().getChild("to_forkNode3").getNode().getName());
		
		processInstanceV1.getRootToken().getChild("to_forkNode1").signal();
		processInstanceV1.getRootToken().getChild("to_forkNode2").signal();
		processInstanceV1.getRootToken().getChild("to_forkNode3").signal();
		assertEquals("B", processInstanceV1.getRootToken().getNode().getName());
	}

	public void testThatTheMigratorWillRejectInvalidProcessInstances() throws IOException {
		try{
			deployV1Definitions();
			Migrator fooMigrator = new Migrator("foo", this.jbpmContext, (Migration[])null, (Migrator[])null);
			fooMigrator.migrate(findLatestProcessDefinition("simple").createProcessInstance());
			fail();
		}catch(IllegalArgumentException e){
			
		} catch (InvalidMigrationException e) {
			fail();
		}
	}
	
	public void testThatTheMigratorCanMigrateAProcessInstance() throws IOException, InvalidMigrationException{
		deployV1Definitions();
		ProcessInstance processInstanceV1 = findLatestProcessDefinition("simple").createProcessInstance();

		processInstanceV1.signal();
		assertEquals("A", processInstanceV1.getRootToken().getNode().getName());
		
		deployV2Definitions();
		deployV3Definitions();

		Migrator migrator = createSimpleProcessDefinitionMigrator();
		ProcessDefinition processDefinitionV3 = findLatestProcessDefinition("simple");
		assertNotNull(processDefinitionV3);
		assertEquals(3, processDefinitionV3.getVersion());
		ProcessInstance processInstanceV3 = migrator.migrate(processInstanceV1);
		
		assertEquals("third", processInstanceV3.getRootToken().getNode().getName());
	}

	public void testThatTheMigratorCanMigrateAMidProcessInstance() throws IOException, InvalidMigrationException{
		deployV1Definitions();
		deployV2Definitions();
		ProcessInstance instance1 = findLatestProcessDefinition("simple").createProcessInstance();
		instance1.signal();

		deployV3Definitions();
		
		Migrator migrator = createSimpleProcessDefinitionMigrator();
		ProcessDefinition definition3 = findLatestProcessDefinition("simple");
		assertNotNull(definition3);
		assertEquals(3, definition3.getVersion());
		ProcessInstance instance3 = migrator.migrate(instance1);
		
		assertEquals("third", instance3.getRootToken().getNode().getName());
	}
	
	public void testThatTheMigratorCanMigrateAProcessInstanceWithAFork() throws IOException, InvalidMigrationException{
		deployV1Definitions();
		ProcessInstance processInstanceV1 = findLatestProcessDefinition("simple").createProcessInstance();
		processInstanceV1.signal();
		assertEquals("A", processInstanceV1.getRootToken().getNode().getName());

		processInstanceV1.signal();
		assertEquals("Fork1", processInstanceV1.getRootToken().getNode().getName());
		assertEquals("forkNode1", processInstanceV1.getRootToken().getChild("to_forkNode1").getNode().getName());
		assertEquals("forkNode2", processInstanceV1.getRootToken().getChild("to_forkNode2").getNode().getName());
		assertEquals("forkNode3", processInstanceV1.getRootToken().getChild("to_forkNode3").getNode().getName());

		deployV2Definitions();
		deployV3Definitions();

		Migrator migrator = createSimpleProcessDefinitionMigrator();
		ProcessInstance processInstanceV3 = migrator.migrate(processInstanceV1);
		assertEquals("Fork1", processInstanceV3.getRootToken().getNode().getName());
		assertEquals("forkNodeOne", processInstanceV3.getRootToken().getChild("to_forkNode1").getNode().getName());
		assertEquals("forkNodeTwo", processInstanceV3.getRootToken().getChild("to_forkNode2").getNode().getName());
		assertEquals("forkNodeThree", processInstanceV3.getRootToken().getChild("to_forkNode3").getNode().getName());
		
		processInstanceV3.getRootToken().getChild("to_forkNode1").signal();
		processInstanceV3.getRootToken().getChild("to_forkNode2").signal();
		processInstanceV3.getRootToken().getChild("to_forkNode3").signal();
		assertEquals("second", processInstanceV3.getRootToken().getNode().getName());
	}
	
	public void testThatTheContextInstanceIsTransferredToTheNewProcess() throws Exception{
		deployV1Definitions();
		ProcessInstance processInstanceV1 = findLatestProcessDefinition("simple").createProcessInstance();
		processInstanceV1.getContextInstance().setVariable("Foo", "Bar");

		Migrator migrator = createSimpleProcessDefinitionMigrator();

		deployV2Definitions();
		deployV3Definitions();
		
		ProcessInstance processInstanceV3 = migrator.migrate(processInstanceV1);
		assertEquals("Bar", processInstanceV3.getContextInstance().getVariable("Foo"));
		assertNull(processInstanceV1.getContextInstance().getVariable("migrationMemo"));
		assertNotNull(processInstanceV3.getContextInstance().getVariable("migrationMemo"));
		System.out.println(processInstanceV3.getContextInstance().getVariable("migrationMemo"));
	}
	
	public void testNoMigrationOfCurrentInstance() throws Exception {
		deployV1Definitions();
		deployV2Definitions();
		deployV3Definitions();
		
		ProcessInstance processInstance1 = findLatestProcessDefinition("simple").createProcessInstance();
		Migrator migrator = createSimpleProcessDefinitionMigrator();
		ProcessInstance processInstance2 = migrator.migrate(processInstance1);
		assertEquals(processInstance1, processInstance2);
	}

	public void testNoMigrations() throws Exception {
		new Migrator("", this.jbpmContext, (Migration[])null, (Migrator[])null);
	}
	
	public void test_that_all_of_the_register_handlers_are_invoked_by_the_migration_method() throws Exception {
		deployV1Definitions();
		ProcessInstance processInstance1 = findLatestProcessDefinition("simple").createProcessInstance();
		deployV2Definitions();
		
		Migrator migrator = createSimpleProcessDefinitionMigrator();
		Mock handler1 = mock(MigrationHandler.class);
		handler1.expects(once()).method("migrateInstance").with(same(processInstance1), isA(ProcessInstance.class));
		Mock handler2 = mock(MigrationHandler.class);
		handler2.expects(once()).method("migrateInstance").with(same(processInstance1), isA(ProcessInstance.class));
		
		migrator.addMigrationHandler((MigrationHandler)handler1.proxy());
		migrator.addMigrationHandler((MigrationHandler)handler2.proxy());
		migrator.migrate(processInstance1);
	}
	
	private Migrator createSimpleProcessDefinitionMigrator() throws InvalidMigrationException {
		Migrator migrator = new Migrator("simple", this.jbpmContext, new Migration[]{new SimpleProcessDefinitionMigration001(), new SimpleProcessDefinitionMigration002()}, null);
		return migrator;
	}
	
	private void deployV1Definitions() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleProcessDefinition_001.xml"));
	}

	private void deployV2Definitions() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleProcessDefinition_002.xml"));
	}
	
	private void deployV3Definitions() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleProcessDefinition_003.xml"));
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
