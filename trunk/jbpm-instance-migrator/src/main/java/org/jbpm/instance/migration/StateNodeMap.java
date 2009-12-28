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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This class is used to compile a list of node name mappings for one or more Migrations.
 * It is is backed by a synchronized {@link HashMap}
 * @author Caleb Powell <caleb.powell@intelliware.ca> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class StateNodeMap {

	private final Map backingMap = MapUtils.synchronizedMap(new HashMap());

	public StateNodeMap() {
	}
	
	/**
	 * Take a String[][] containing the node conversion mappings to be performed and convert them
	 * into a Map we can use elsewhere.
	 * 
	 * @param nodeMap Array of conversion mappings 
	 * @return Map of node conversion mappings
	 */
	public StateNodeMap(String[][] nodeMap) {
		for (int i = 0; i < nodeMap.length; i++) {
			String from = nodeMap[i][0];
			String to = nodeMap[i][1];
			if(StringUtils.isBlank(from)) {
				continue;
			}
			addNodeMapping(from, to);
		}
	}

	/**
	 * Given a deprecated node name, this method will return the name of the node which has superseded  
	 * it (the <i>current</i> node).   
	 * @param deprecatedNodeName The name of a deprecated node.
	 * @return The name of the node that supersedes the deprecated node.
	 */
	public String getCurrentNodeName(String deprecatedNodeName){
		return (String) backingMap.get(deprecatedNodeName);
	}

	/**
	 * 
	 * @param nodeName
	 * @return true if this map contains the deprecated node.
	 */
	public boolean containsDeprecatedNodeName(String nodeName){
		return backingMap.containsKey(nodeName);
	}

	/**
	 * 
	 * @param nodeName
	 * @return true if this map contains the current node.
	 */
	public boolean containsCurrentNodeName(String currentNodeName) {
		return backingMap.containsValue(currentNodeName);
	}

	/**
	 * 
	 * @return a Set of deprecated node names.
	 */
	public Set deprecatedNodeNames() {
		return backingMap.keySet();
	}
	
	/**
	 * 
	 * @return a {@link Collection} of current node names.
	 */
	public Collection currentNodeNames() {
		return backingMap.values();
	}

	/**
	 * Adds the deprecated and current nodes to this map.
	 * @param deprecatedNodeName
	 * @param currentNodeName
	 * @throws {@link InvalidMigrationException} if the currentNodeName was previously added as a deprecated node.
	 */
	public void addNodeMapping(String deprecatedNodeName, String currentNodeName) {
		if(this.containsDeprecatedNodeName(currentNodeName)) {
			throw new InvalidMigrationException("Invalid Node Mapping ['"+deprecatedNodeName+"' => '"+currentNodeName+"']! The '"+currentNodeName+"' node was deprecated by a previous migration.");
		}
		updateCurrentNodeNames(deprecatedNodeName, currentNodeName);
		backingMap.put(deprecatedNodeName, currentNodeName);
	}

	/**
	 * Searches the entries in the backingMap. If an Entry contains a value equal to the 
	 * deprecatedNodeName, the value will be replaced with the currentNodeName. 
	 * @param deprecatedNodeName The value that will be replaced
	 * @param currentNodeName The replacement value
	 */
	private void updateCurrentNodeNames(String deprecatedNodeName, String currentNodeName) {
		Iterator allEntries = this.backingMap.entrySet().iterator();
		while (allEntries.hasNext()) {
			Map.Entry entry = (Map.Entry) allEntries.next();
			if (entry.getValue().equals(deprecatedNodeName)) {
				entry.setValue(currentNodeName);
			}
		}
	}

	/**
	 * Adds the {@link StateNodeMap} from the provided Migration to this map.
	 * @param migration
	 * @throws {@link InvalidMigrationException} if the migration paramater contains a <i>current</i> node that is in this map's <i>deprecated</i> node list.
	 */
	public void addMigration(Migration migration) {
		addStateNodeMap(migration.createNodeMap());
	}

	/**
	 * Adds each entry in the provided stateNodeMap to this nodeMap.
	 * @param stateNodeMap
	 * @throws {@link InvalidMigrationException} if the stateNodeMap paramater contains a <i>current</i> node that is 
	 * in this map's <i>deprecated</i> node list.
	 */
	private void addStateNodeMap(StateNodeMap stateNodeMap) {
		Iterator deprecatedNodes = stateNodeMap.deprecatedNodeNames().iterator();
		while (deprecatedNodes.hasNext()) {
			String deprecatedNodeName = (String) deprecatedNodes.next();
			String currentNodeName = stateNodeMap.getCurrentNodeName(deprecatedNodeName);
			addNodeMapping(deprecatedNodeName, currentNodeName);
		}
	}

	
}
