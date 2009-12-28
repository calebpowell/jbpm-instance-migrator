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


/**
 * Defines the interface for an individual Migration Class. Classes that implement this interface can 
 * be read from a Migrator.
 * @author Caleb Powell <caleb.powell@intelliware.ca> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public interface Migration {

	/**
	 * 
	 * @return A Map of nodes. 
	 */
	public StateNodeMap createNodeMap();
	
}
