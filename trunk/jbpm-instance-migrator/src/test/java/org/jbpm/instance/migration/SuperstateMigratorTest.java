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
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class SuperstateMigratorTest extends BaseTestCase {

	private static final String PROCESS_NAME = "superStateMigratorTestProcessDefinition";

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testMigrationToAProcessDefinitionWithASuperState() throws Exception {
		deployNonSuperStateDefinition();
		ProcessDefinition noSuperStateProcessDefinition = findLatestProcessDefinition(PROCESS_NAME);
		assertEquals(1, noSuperStateProcessDefinition.getVersion());
		
		ProcessInstance noSuperStateInstance = noSuperStateProcessDefinition.createProcessInstance();
		noSuperStateInstance.signal();
		assertEquals("Saved", noSuperStateInstance.getRootToken().getNode().getFullyQualifiedName());
		
		deploySuperStateDefinition01();
		assertEquals(2, findLatestProcessDefinition(PROCESS_NAME).getVersion());
		
		Migrator superProcessMigrator = new Migrator(PROCESS_NAME, this.jbpmContext, new Migration[] {new ToSuperStateProcessDefinitionMigration()}, null);
		ProcessInstance superStateInstance = superProcessMigrator.migrate(noSuperStateInstance);
		assertEquals("superStateNode/Saved", superStateInstance.getRootToken().getNode().getFullyQualifiedName());
		
		superStateInstance.signal();
		assertEquals("Deleted", superStateInstance.getRootToken().getNode().getFullyQualifiedName());
	}

	public void testMigrationToAProcessDefinitionWithNoSuperState() throws Exception {
		deploySuperStateDefinition01();
		ProcessDefinition superStateProcessDefinition = findLatestProcessDefinition(PROCESS_NAME);
		assertEquals(1, superStateProcessDefinition.getVersion());
		
		ProcessInstance superStateInstance = superStateProcessDefinition.createProcessInstance();
		superStateInstance.signal();
		assertEquals("superStateNode/Saved", superStateInstance.getRootToken().getNode().getFullyQualifiedName());
		
		deployNonSuperStateDefinition();
		assertEquals(2, findLatestProcessDefinition(PROCESS_NAME).getVersion());
		
		Migrator migrator = new Migrator(PROCESS_NAME, this.jbpmContext, new Migration[] {new FromSuperStateProcessDefinitionMigration()}, null);
		ProcessInstance nonSuperStateInstance = migrator.migrate(superStateInstance);
		assertEquals("Saved", nonSuperStateInstance.getRootToken().getNode().getFullyQualifiedName());
		
		nonSuperStateInstance.signal();
		assertEquals("Deleted", nonSuperStateInstance.getRootToken().getNode().getFullyQualifiedName());
	}

	public void testMigrationBetweenProcessDefinitionsWithSuperStates() throws Exception {
		deploySuperStateDefinition01();
		ProcessDefinition superStateProcessDefinition01 = findLatestProcessDefinition(PROCESS_NAME);
		assertEquals(1, superStateProcessDefinition01.getVersion());
		
		ProcessInstance superStateInstance01 = superStateProcessDefinition01.createProcessInstance();
		superStateInstance01.signal();
		assertEquals("superStateNode/Saved", superStateInstance01.getRootToken().getNode().getFullyQualifiedName());
		
		deploySuperStateDefinition02();
		assertEquals(2, findLatestProcessDefinition(PROCESS_NAME).getVersion());
		
		Migrator migrator = new Migrator(PROCESS_NAME, this.jbpmContext, new Migration[] {new BetweenSuperStateProcessDefinitionMigration()}, null);
		ProcessInstance superStateInstance02 = migrator.migrate(superStateInstance01);
		assertEquals("superStateNode/Saved2", superStateInstance02.getRootToken().getNode().getFullyQualifiedName());
		superStateInstance02.signal();
		assertEquals("Deleted", superStateInstance02.getRootToken().getNode().getFullyQualifiedName());
	}

	public void testMigrationToProcessDefinitionsWithNestedSuperStates() throws Exception {
		deploySuperStateDefinition02();
		ProcessDefinition superStateProcessDefinition01 = findLatestProcessDefinition(PROCESS_NAME);
		assertEquals(1, superStateProcessDefinition01.getVersion());
		
		ProcessInstance superStateInstance01 = superStateProcessDefinition01.createProcessInstance();
		superStateInstance01.signal();
		assertEquals("superStateNode/Saved2", superStateInstance01.getRootToken().getNode().getFullyQualifiedName());
		
		deployNestedSuperStateDefinition();
		assertEquals(2, findLatestProcessDefinition(PROCESS_NAME).getVersion());
		
		Migrator migrator = new Migrator(PROCESS_NAME, this.jbpmContext, new Migration[] {new NestedSuperStateProcessDefinitionMigration()}, null);
		ProcessInstance superStateInstance02 = migrator.migrate(superStateInstance01);
		assertEquals("superDuperStateNode/superStateNode/Saved2", superStateInstance02.getRootToken().getNode().getFullyQualifiedName());
		superStateInstance02.signal();
		assertEquals("Deleted", superStateInstance02.getRootToken().getNode().getFullyQualifiedName());
	}

	private void deployNonSuperStateDefinition() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("superstateMigratorTest_001.xml"));
	}

	private void deploySuperStateDefinition01() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("superstateMigratorTest_002.xml"));
	}

	private void deploySuperStateDefinition02() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("superstateMigratorTest_003.xml"));
	}

	private void deployNestedSuperStateDefinition() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("superstateMigratorTest_004.xml"));
	}
	
	private static class ToSuperStateProcessDefinitionMigration implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"Saved", "superStateNode/Saved"}});
		}
	}

	private static class FromSuperStateProcessDefinitionMigration implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"superStateNode/Saved", "Saved"}});
		}
	}

	private static class BetweenSuperStateProcessDefinitionMigration implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"superStateNode/Saved", "superStateNode/Saved2"}});
		}
	}

	private static class NestedSuperStateProcessDefinitionMigration implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"superStateNode/Saved2", "superDuperStateNode/superStateNode/Saved2"}});
		}
	}
}
