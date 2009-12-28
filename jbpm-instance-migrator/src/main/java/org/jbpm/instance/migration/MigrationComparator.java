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

import java.util.Comparator;

/**
 * A comparator that compares Migration instances based on their revision.
 * 
 * @see Migration
 * @author Caleb Powell <caleb.powell@intelliware.ca> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class MigrationComparator implements Comparator {

	public int compare(Object o1, Object o2) {
		Migration migration1 = (Migration) o1;
		Migration migration2 = (Migration) o2;
		return migration1.getClass().getName().compareTo(migration2.getClass().getName());
	}

}
