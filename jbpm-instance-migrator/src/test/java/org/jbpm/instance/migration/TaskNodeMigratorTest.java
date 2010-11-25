/*
 * *##% 
 * jBPM Instance Migrator
 * Copyright (C) null - 2010 JBoss Inc.
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
import org.jbpm.graph.node.TaskNode;

/**
 * 
 * @author Caleb Powell <caleb.powell@gmail.com> 
 */
public class TaskNodeMigratorTest extends BaseTestCase {

	private static final String PROCESS_NAME = "taskNodeProcess";

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testTheProcessInstanceSignals() throws IOException, InvalidMigrationException {
		deployV1Definition();

		ProcessDefinition processDefinitionV1 = findLatestProcessDefinition(PROCESS_NAME);
		ProcessInstance processInstanceV1 = processDefinitionV1.createProcessInstance();

		processInstanceV1.signal();
		assertEquals("ATaskNode", processInstanceV1.getRootToken().getNode().getName());
		assertEquals(TaskNode.class, processInstanceV1.getRootToken().getNode().getClass());
		
		processInstanceV1.signal();
		assertEquals("end", processInstanceV1.getRootToken().getNode().getName());
	}
	
	public void testSubprocessCreationAcrossVersions() throws Exception {
		deployV1Definition();
		ProcessDefinition processDefinitionV1 = findLatestProcessDefinition(PROCESS_NAME);
		assertEquals(1, processDefinitionV1.getVersion());
		
		ProcessInstance instanceV1 = processDefinitionV1.createProcessInstance();
		instanceV1.signal();
		assertEquals("ATaskNode", instanceV1.getRootToken().getNode().getName());
		assertEquals(TaskNode.class, instanceV1.getRootToken().getNode().getClass());
		
		deployV2Definition();
		assertEquals(2, findLatestProcessDefinition(PROCESS_NAME).getVersion());
		
		Migrator migrator = new Migrator(PROCESS_NAME, this.jbpmContext, new Migration[] {}, new Migrator[]{});
		ProcessInstance instanceV2 = migrator.migrate(instanceV1);
		assertEquals("ATaskNode", instanceV2.getRootToken().getNode().getName());
		assertEquals(TaskNode.class, instanceV2.getRootToken().getNode().getClass());
		
	}

	private void deployV1Definition() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("taskNodeProcessDefinition_001.xml"));
	}

	private void deployV2Definition() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("taskNodeProcessDefinition_002.xml"));
	}

}
