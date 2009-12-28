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
package org.jbpm.instance.migration.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.instance.migration.Migrator;


/**
 * Contains utility methods used to validate a {@link Migrator}.
 * @author Caleb Powell <caleb.powell@intelliware.ca> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class MigratorValiditionUtil {

	/**
	 * Searches the processDefinition instance for any deprecated nodes specified in the migrator.
	 * 
	 * @param processDefinition
	 * @param migrator
	 * @return An array of deprecated nodes contained in the processDefinition. An empty array if none are found.  
	 */
	public static String[] findDeprecatedNodesInProcessDefinition(ProcessDefinition processDefinition, Migrator migrator) {
		List deprecatedNodes = new ArrayList();
		for (Iterator deprecatedNodeIterator = deprecatedNodeIterator(migrator);deprecatedNodeIterator.hasNext();) {
			String deprecatedNode = (String) deprecatedNodeIterator.next();
			if(findNode(processDefinition, deprecatedNode) != null){
				deprecatedNodes.add(deprecatedNode);
			}
		}
		
		return (String[]) deprecatedNodes.toArray(new String[deprecatedNodes.size()]);
	}

	/**
	 * Searches the processDefinition instance for missing current nodes. 
	 * @param processDefinition
	 * @param migrator
	 * @return An array of current nodes that are specified in the migrator and are missing in the processDefinition. 
	 * An empty array if no nodes are missing.
	 */
	public static String[] findMissingCurrentNodesInTheProcessDefinition(ProcessDefinition processDefinition, Migrator migrator) {
		Set missingNodes = new HashSet();
		for (Iterator currentNodeIterator = currentNodeIterator(migrator);currentNodeIterator.hasNext();) {
			String currentNode = (String) currentNodeIterator.next();
			if(findNode(processDefinition, currentNode) == null){
				missingNodes.add(currentNode);
			}
		}
		return (String[]) missingNodes.toArray(new String[missingNodes.size()]);
	}

	/**
	 * Searches the processDefinition instance for a node by name.   
	 * @param processDefinition
	 * @param nodeName
	 * @return the node, or null if no node of that name exists
	 */
	public static Node findNode(ProcessDefinition processDefinition, String nodeName) {
		try {
			return processDefinition.findNode(nodeName);
		} catch (NullPointerException e) {
			// IW/PP: jbpm method throws an NPE if the superstate referenced in a node name doesn't exist 
			return null;
		}
	}

	/**
	 * Searches the processDefinition instance for current nodes (as specified in the migrator) that are not supported wait states.
	 * See the {@link Migrator}'s SUPPORTED_WAIT_STATE_NODE_TYPES constant for the set of supported wait state nodes. 
	 * @param processDefinition
	 * @param migrator
	 * @return An array of current node names that are specified in the migrator as current nodes, but are not supported by the migrator.
	 */
	public static String[] findCurrentNodesThatAreInvalidWaitStates(ProcessDefinition processDefinition, Migrator migrator) {
		Set invalidNodes = new HashSet();
		for (Iterator currentNodeIterator = currentNodeIterator(migrator);currentNodeIterator.hasNext();) {
			String currentNode = (String) currentNodeIterator.next();
			Node node = findNode(processDefinition, currentNode);
			if(node != null && nodeIsNotASupportedWaitState(node)){
				invalidNodes.add(currentNode);
			}
		}
		
		return (String[]) invalidNodes.toArray(new String[invalidNodes.size()]);
	}

	private static Iterator deprecatedNodeIterator(Migrator migrator) {
		Iterator iterator = migrator.getStateNodeMap().deprecatedNodeNames().iterator();
		return iterator;
	}

	private static Iterator currentNodeIterator(Migrator migrator) {
		Iterator iterator = migrator.getStateNodeMap().currentNodeNames().iterator();
		return iterator;
	}

	private static boolean nodeIsNotASupportedWaitState(Node node) {
		return !Migrator.SUPPORTED_WAIT_STATE_NODE_TYPES.contains(node.getClass());
	}
}
