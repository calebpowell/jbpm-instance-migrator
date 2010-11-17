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

import org.jbpm.graph.exe.ProcessInstance;


/**
 * <p>
 * Defines the interface for a dynamic Migration Class. Classes that implement this interface are 
 * responsible for determining the destination node in the new process. Instances of this class
 * are invoked at runtime by the {@link Migrator} class. 
 * </p>
 * <p>
 * You declare a dynamic migration in the {@link StateNodeMap} returned by your {@link Migration}
 * implementation. The class name is declared using the syntax 
 * <i>java://com.some.DynamicMigrationImplementation</i>.
 * </p>
 * For example:
 * <p>
 * <blockquote>
 * public class SomeProcessInstanceMigration implements org.jbpm.instance.migration.Migration{
 *	public StateNodeMap createNodeMap() {
 *			return new StateNodeMap(
 *			new String[][] {{"deprecatedNode1", "java://com.some.DynamicMigrationImplementation"}});
 *		}
 *	}
 * </blockquote>
 * </p>
 * @author Caleb Powell <caleb.powell@gmail.com> 
 */
public interface DynamicMigration {

	/**
	 * Given the deprecatedNodeName and the oldProcessInstance, this method is responsible for returning the currentNodeName.
	 * 
	 * @param deprecatedNodeName
	 * @param oldProcessInstance
	 * @return currentNodeName
	 */
	public String map(String deprecatedNodeName, ProcessInstance oldProcessInstance);
	
}
