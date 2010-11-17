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
import org.jbpm.instance.migration.Migration;
import org.jbpm.instance.migration.MigrationUtils;
import org.jbpm.instance.migration.Migrator;
import org.jbpm.instance.migration.StateNodeMap;


/**
 * 
 * @author Caleb Powell <caleb.powell@gmail.com> 
 */
public class DynamicMigrationTest extends BaseTestCase {

	private static final String SIMPLE_PROCESS_DEFINITION_NAME = "simple";

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testMigrationUsingADynamicMigration() throws Exception {
		deploySimpleProcessDefinition01();

		ProcessDefinition processDefinitionV1 = findLatestProcessDefinition(SIMPLE_PROCESS_DEFINITION_NAME);
		assertEquals(1, processDefinitionV1.getVersion());

		ProcessInstance instanceV1 = processDefinitionV1.createProcessInstance();
		instanceV1.signal();
		assertEquals("A", instanceV1.getRootToken().getNode().getName());

		deploySimpleProcessDefinition02();
		assertEquals(2, findLatestProcessDefinition(SIMPLE_PROCESS_DEFINITION_NAME).getVersion());
		
		Migrator migrator = new Migrator(SIMPLE_PROCESS_DEFINITION_NAME, 
				this.jbpmContext, new Migration[] {new Migration01()}, null);
		ProcessInstance instanceV2 = migrator.migrate(instanceV1);
		
		assertEquals("first", instanceV2.getRootToken().getNode().getName());
	}

	private void deploySimpleProcessDefinition01() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleProcessDefinition_001.xml"));
	}

	private void deploySimpleProcessDefinition02() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleProcessDefinition_002.xml"));
	}

	private static class Migration01 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"A", "java://org.jbpm.instance.migration.DynamicMigrationForTheSimpleProcessANode"}});
		}
	}

}
