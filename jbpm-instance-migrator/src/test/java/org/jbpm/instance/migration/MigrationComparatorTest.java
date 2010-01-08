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

import org.jbpm.instance.migration.Migration;
import org.jbpm.instance.migration.MigrationComparator;
import org.jmock.cglib.MockObjectTestCase;

/**
 * 
 * @author Caleb Powell <caleb.powell@intelliware.ca> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class MigrationComparatorTest extends MockObjectTestCase {

	public void testCompare() {
		Migration migration1 = new TestMigration001();
		Migration migration2 = new TestMigration002();
		Migration migration3 = new TestMigration003();
		
		MigrationComparator migrationComparator = new MigrationComparator();
		assertEquals(0, migrationComparator.compare(migration1, migration1));
		assertTrue(migrationComparator.compare(migration1, migration2) < 0);
		assertTrue(migrationComparator.compare(migration2, migration3) < 0);
		assertTrue(migrationComparator.compare(migration3, migration1) > 0);
	}

}