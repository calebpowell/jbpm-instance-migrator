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
package org.jbpm.instance.migration.util;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.instance.migration.Migration;
import org.jbpm.instance.migration.MigrationUtils;
import org.jbpm.instance.migration.Migrator;
import org.jbpm.instance.migration.StateNodeMap;

/**
 * 
 * @author Caleb Powell <caleb.powell@gmail.com> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class MigratorValidityUtilTest extends TestCase {

	private static final String PROCESS_DEF_FILE_NAME = "/org/jbpm/instance/migration/util/migratorValidityTestProcessDefinition_001.xml";
	private static final String PROCESS_DEF_NAME = "migratorValidityTestProcessDefinition_001";

	public void testFindDeprecatedNodesInProcessDefinition() throws IOException {
		String[] deprecatedNodes = MigratorValiditionUtil.findDeprecatedNodesInProcessDefinition(createProcessDefinition(), createMigrator(migrations));
		
		assertEquals(2, deprecatedNodes.length);
		assertTrue(ArrayUtils.contains(deprecatedNodes, "firstSuperStateNode/first"));
		assertTrue(ArrayUtils.contains(deprecatedNodes, "forkNodeThree"));
	}

	public void testFindMissingCurrentNodesInTheProcessDefinition() throws IOException {
		String[] missingCurrentNodes = MigratorValiditionUtil.findMissingCurrentNodesInTheProcessDefinition(createProcessDefinition(), createMigrator(migrations));
		
		assertEquals(2, missingCurrentNodes.length);
		assertTrue(ArrayUtils.contains(missingCurrentNodes, "missingCurrentNode1"));
		assertTrue(ArrayUtils.contains(missingCurrentNodes, "missingCurrentNode2"));
	}

	public void testFindCurrentNodesThatAreInvalidWaitStates() throws IOException {
		String[] invalidNodes = MigratorValiditionUtil.findCurrentNodesThatAreInvalidWaitStates(createProcessDefinition(), createMigrator(migrations));
		
		assertEquals(1, invalidNodes.length);
		assertTrue(ArrayUtils.contains(invalidNodes, "nonWaitSuperStateNode/nonWaitStateNode"));
	}
	
	public void testFindNode() throws Exception {
		MigratorValiditionUtil.findNode(createProcessDefinition(), "nonexistent-superstate/anything");
	}
	
	private static class TestMigration001 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"firstSuperStateNode/first", "missingCurrentNode1"}});
		}
	}
	private static class TestMigration002 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"forkNodeThree","missingCurrentNode2"}});
		}
	}
	private static class TestMigration003 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"validDeprecatedNode","validSuperStateNode/validCurrentNode1"}});
		}
	}
	private static class TestMigration004 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][] {{"deprecatedNonWaitStateNode","nonWaitSuperStateNode/nonWaitStateNode"}});
		}
	}
	private Migration[] migrations = new Migration[]{new TestMigration001(), new TestMigration002(), new TestMigration003(), new TestMigration004()};

	private Migrator createMigrator(Migration[] migrations) {
		Migrator migrator = new Migrator(PROCESS_DEF_NAME, null, migrations, null);
		return migrator;
	}

	private ProcessDefinition createProcessDefinition() throws IOException {
		ProcessDefinition processDefinition = MigrationUtils.getProcessDefinition(PROCESS_DEF_FILE_NAME);
		return processDefinition;
	}
}
