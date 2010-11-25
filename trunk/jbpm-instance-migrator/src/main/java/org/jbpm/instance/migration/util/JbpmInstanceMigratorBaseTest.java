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

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.instance.migration.Migrator;


import junit.framework.TestCase;

/**
 * 
 * @author Caleb Powell <caleb.powell@gmail.com> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public abstract class JbpmInstanceMigratorBaseTest extends TestCase {

	public static void assertMigratorValidity(Migrator migrator, ProcessDefinition processDefinition){
		assertThatTheMigratorWillMigrateTheProvidedProcessDefinition(processDefinition, migrator);
		assertTheValidityOfTheMigratorsDeprecatedNodes(processDefinition, migrator);
		assertThatCurrentNodesAreValidWaitStateNodes(processDefinition, migrator);
		assertThatCurrentNodesExistInTheProcessDefinition(processDefinition, migrator);
	}
	
	protected ProcessDefinition createProcessDefinition(String processDefinitionFileName) {
		InputStream processDefinitionStream = getClass().getResourceAsStream(processDefinitionFileName);
		ProcessDefinition processDefinition = ProcessDefinition.parseXmlInputStream(processDefinitionStream);
		return processDefinition;
	}
	
	private static void assertThatTheMigratorWillMigrateTheProvidedProcessDefinition(ProcessDefinition processDefinition, Migrator migrator) {
		assertTrue("The provided migrator is not capable of migrating the '"+processDefinition.getName()+"' ProcessDefinition!", migrator.willMigrate(processDefinition));
	}

	private static void assertTheValidityOfTheMigratorsDeprecatedNodes(ProcessDefinition processDefinition, Migrator migrator){
		String[] deprecatedNodes = MigratorValiditionUtil.findDeprecatedNodesInProcessDefinition(processDefinition, migrator);
		if(deprecatedNodes != null && deprecatedNodes.length > 0){
			String errorMessage = "The '"+processDefinition.getName()+"' ProcessDefinition contains the following nodes that were deprecated in the migrator ["+StringUtils.join(deprecatedNodes, ", ")+"]. You cannot re-introduce a deprecated node to a ProcessDefinition!";
			fail(errorMessage);
		}
	}

	private static void assertThatCurrentNodesExistInTheProcessDefinition(ProcessDefinition processDefinition, Migrator migrator) {
		String[] missingCurrentNodes = MigratorValiditionUtil.findMissingCurrentNodesInTheProcessDefinition(processDefinition, migrator);
		if(missingCurrentNodes != null && missingCurrentNodes.length > 0){
			String errorMessage = "The '"+processDefinition.getName()+"' ProcessDefinition is missing the following current nodes declared in the migrator ["+StringUtils.join(missingCurrentNodes, ", ")+"]. If these nodes were removed, you must create a Migration that deprecates them!";
			fail(errorMessage);
		}
	}

	private static void assertThatCurrentNodesAreValidWaitStateNodes(ProcessDefinition processDefinition, Migrator migrator) {
		String[] invalidWaitStateNodes = MigratorValiditionUtil.findCurrentNodesThatAreInvalidWaitStates(processDefinition, migrator);
		if(invalidWaitStateNodes != null && invalidWaitStateNodes.length > 0){
			String errorMessage = "The '"+processDefinition.getName()+"' Migrator contains current nodes that are not valid wait states ["+StringUtils.join(invalidWaitStateNodes, ", ")+"]";
			fail(errorMessage);
		}
	}

}
