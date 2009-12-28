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
import org.jbpm.instance.migration.InvalidMigrationException;
import org.jbpm.instance.migration.Migration;
import org.jbpm.instance.migration.MigrationUtils;
import org.jbpm.instance.migration.Migrator;
import org.jbpm.instance.migration.StateNodeMap;

/**
 * 
 * @author Caleb Powell <caleb.powell@intelliware.ca> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class SubprocessMigratorTest extends BaseTestCase {

	private static final String SUB_PROCESS_NAME = "simpleSubProcess";

	private static final String SUPER_PROCESS_NAME = "simpleSuperProcess";

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testTheProcessInstanceSignals() throws IOException, InvalidMigrationException {
		deployV1Definitions();

		ProcessDefinition processDefinitionV1 = findLatestProcessDefinition(SUPER_PROCESS_NAME);
		ProcessInstance processInstanceV1 = processDefinitionV1.createProcessInstance();

		processInstanceV1.signal();
		assertEquals("A", processInstanceV1.getRootToken().getNode().getName());

		processInstanceV1.signal();
		assertEquals("Fork1", processInstanceV1.getRootToken().getNode().getName());
		assertEquals("forkNode1", processInstanceV1.getRootToken().getChild("to_forkNode1").getNode().getName());
		assertEquals("forkNode2", processInstanceV1.getRootToken().getChild("to_forkNode2").getNode().getName());
		assertEquals("forkNode3", processInstanceV1.getRootToken().getChild("to_forkNode3").getNode().getName());
		assertEquals("SubA", processInstanceV1.getRootToken().getChild("to_forkNode3").getSubProcessInstance().getRootToken().getNode().getName());

		processInstanceV1.getRootToken().getChild("to_forkNode1").signal();
		processInstanceV1.getRootToken().getChild("to_forkNode2").signal();
		processInstanceV1.getRootToken().getChild("to_forkNode3").getSubProcessInstance().signal();
		assertEquals("SubB", processInstanceV1.getRootToken().getChild("to_forkNode3").getSubProcessInstance().getRootToken().getNode().getName());
	}
	
	public void testForSubProcesses() throws Exception {
		deployV1Definitions();
		
		ProcessDefinition processDefinitionV1 = findLatestProcessDefinition(SUPER_PROCESS_NAME);
		ProcessInstance processInstanceV1 = processDefinitionV1.createProcessInstance();
		
		processInstanceV1.signal();
		processInstanceV1.signal();
		assertNull(processInstanceV1.getRootToken().getChild("to_forkNode1").getSubProcessInstance());
		assertNotNull(processInstanceV1.getRootToken().getChild("to_forkNode3").getSubProcessInstance());
	}
	
	public void testSubprocessCreationAcrossVersions() throws Exception {
		deployV1Definitions();
		ProcessDefinition processDefinitionV1 = findLatestProcessDefinition(SUPER_PROCESS_NAME);
		assertEquals(1, processDefinitionV1.getVersion());
		
		ProcessInstance instanceV1 = processDefinitionV1.createProcessInstance();
		instanceV1.signal();
		instanceV1.signal();
		assertEquals("Fork1", instanceV1.getRootToken().getNode().getName());
		assertEquals("forkNode1", instanceV1.getRootToken().getChild("to_forkNode1").getNode().getName());
		assertEquals("forkNode2", instanceV1.getRootToken().getChild("to_forkNode2").getNode().getName());
		assertEquals("forkNode3", instanceV1.getRootToken().getChild("to_forkNode3").getNode().getName());
		assertEquals("SubA", instanceV1.getRootToken().getChild("to_forkNode3").getSubProcessInstance().getRootToken().getNode().getName());
		
		deployV2Definitions();
		assertEquals(2, findLatestProcessDefinition(SUPER_PROCESS_NAME).getVersion());
		
		Migrator subProcessMigrator = new Migrator(SUB_PROCESS_NAME, this.jbpmContext, new Migration[] {new SimpleSubProcessDefinitionMigration001()}, null );
		Migrator superProcessMigrator = new Migrator(SUPER_PROCESS_NAME, this.jbpmContext, new Migration[] {}, new Migrator[]{subProcessMigrator});
		ProcessInstance instanceV2 = superProcessMigrator.migrate(instanceV1);
		assertNull(instanceV2.getRootToken().getChild("to_forkNode1").getSubProcessInstance());
		assertNotNull(instanceV2.getRootToken().getChild("to_forkNode3").getSubProcessInstance());
		assertEquals("Sub1", instanceV2.getRootToken().getChild("to_forkNode3").getSubProcessInstance().getRootToken().getNode().getName());
		
		instanceV2.getRootToken().getChild("to_forkNode3").getSubProcessInstance().signal();
		assertEquals("SubB", instanceV2.getRootToken().getChild("to_forkNode3").getSubProcessInstance().getRootToken().getNode().getName());

		instanceV2.getRootToken().getChild("to_forkNode3").getSubProcessInstance().signal();
		assertNull(instanceV2.getRootToken().getChild("to_forkNode3").getSubProcessInstance());
		assertEquals("Join1", instanceV2.getRootToken().getChild("to_forkNode3").getNode().getName());

		instanceV2.getRootToken().getChild("to_forkNode1").signal();
		instanceV2.getRootToken().getChild("to_forkNode2").signal();
		assertEquals("B", instanceV2.getRootToken().getNode().getName());
		
		instanceV2.signal();
		assertEquals("end", instanceV2.getRootToken().getNode().getName());
	}

	public void testThatSubprocessIsMigratedWhenSuperprocessIsMigrated() throws Exception {
		deployV1Definitions();
		ProcessDefinition processDefinitionV1 = findLatestProcessDefinition(SUPER_PROCESS_NAME);
		assertEquals(1, processDefinitionV1.getVersion());
		
		ProcessInstance instanceV1 = processDefinitionV1.createProcessInstance();
		instanceV1.signal();
		instanceV1.signal();
		assertEquals("Fork1", instanceV1.getRootToken().getNode().getName());
		assertEquals("forkNode1", instanceV1.getRootToken().getChild("to_forkNode1").getNode().getName());
		assertEquals("forkNode2", instanceV1.getRootToken().getChild("to_forkNode2").getNode().getName());
		assertEquals("forkNode3", instanceV1.getRootToken().getChild("to_forkNode3").getNode().getName());
		assertEquals("SubA", instanceV1.getRootToken().getChild("to_forkNode3").getSubProcessInstance().getRootToken().getNode().getName());
		
		deployV2SuperprocessOnly();
		assertEquals(2, findLatestProcessDefinition(SUPER_PROCESS_NAME).getVersion());
		
		Migrator subProcessMigrator = new Migrator(SUB_PROCESS_NAME, this.jbpmContext, new Migration[] {}, null );
		Migrator superProcessMigrator = new Migrator(SUPER_PROCESS_NAME, this.jbpmContext, new Migration[] {}, new Migrator[]{subProcessMigrator});
		ProcessInstance instanceV2 = superProcessMigrator.migrate(instanceV1);
		
		assertNotSame(instanceV2.getRootToken().getChild("to_forkNode3").getSubProcessInstance(), instanceV1.getRootToken().getChild("to_forkNode3").getSubProcessInstance());
	}

	private void deployV1Definitions() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleSubProcessDefinition_001.xml"));
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleSuperProcessDefinition_001.xml"));
	}

	private void deployV2Definitions() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleSubProcessDefinition_002.xml"));
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleSuperProcessDefinition_002.xml"));
	}

	private void deployV2SuperprocessOnly() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleSuperProcessDefinition_002.xml"));
	}
	
	private static class SimpleSubProcessDefinitionMigration001 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"SubA", "Sub1"}});
		}
	}
}
