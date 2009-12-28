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


import org.jbpm.instance.migration.InvalidMigrationException;
import org.jbpm.instance.migration.Migration;
import org.jbpm.instance.migration.Migrator;
import org.jbpm.instance.migration.StateNodeMap;


/**
 * 
 * @author Caleb Powell <caleb.powell@intelliware.ca> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class MigratorAggregationTest extends BaseTestCase {
	
	public void testThatTheMigratorCanAggregateACollectionOfMigrations() throws Exception{
		Migration migration1 = new MigratorTestMigration001();
		Migration migration2 = new MigratorTestMigration002();
		Migration migration3 = new MigratorTestMigration003();
		Migrator migrator = new Migrator("", null, new Migration[]{migration1, migration2, migration3}, null);
		
		assertTrue(migrator.getStateNodeMap().containsDeprecatedNodeName("a"));
		assertExistenseNodeMapping("a", "d", migrator.getStateNodeMap());
		assertExistenseNodeMapping("x", "z", migrator.getStateNodeMap());
		assertExistenseNodeMapping("b", "d", migrator.getStateNodeMap());
		assertExistenseNodeMapping("c", "d", migrator.getStateNodeMap());
	}
	
	public void testIllegalMigrationThrowsException() throws Exception{
		Migration migration1 = new MigratorTestMigration001();
		Migration migration2 = new MigratorTestMigration002();
		Migration migration3 = new MigratorTestMigration003();
		Migration migration4 = new MigratorTestMigration004();
		try {
			new Migrator("", null, new Migration[]{migration1, migration2, migration3, migration4}, null);
			fail();
		} catch (InvalidMigrationException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void assertExistenseNodeMapping(String deprecatedNodeName, String currentNodeName, StateNodeMap mappings) {
		assertTrue("Mapping does not include the key '"+deprecatedNodeName+"'", mappings.containsDeprecatedNodeName(deprecatedNodeName));
		assertTrue("Mapping does not include the value '"+currentNodeName+"'", mappings.containsCurrentNodeName(currentNodeName));
		assertEquals(deprecatedNodeName+"==>"+currentNodeName+" mapping does not exist!", currentNodeName, mappings.getCurrentNodeName(deprecatedNodeName));
	}

	private static class MigratorTestMigration001 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][]{{"a", "b"}, {"x", "y"}});
		}
	}
	private static class MigratorTestMigration002 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][]{{"b", "c"}, {"y", "z"}});
		}
	}
	private static class MigratorTestMigration003 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][]{{"c", "d"}});
		}
	}
	private static class MigratorTestMigration004 implements Migration{
		public StateNodeMap createNodeMap() {
			return new StateNodeMap(new String[][]{{"d", "a"}});
		}
	}
}
