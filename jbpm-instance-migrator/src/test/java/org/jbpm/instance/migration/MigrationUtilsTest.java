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
import org.jbpm.instance.migration.MigrationUtils;


/**
 * 
 * @author Caleb Powell <caleb.powell@gmail.com> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class MigrationUtilsTest extends BaseTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testThatASuperProcessWillRequireMigrationIfItsOutOfDate() throws IOException{
		deployV1Definitions();
		ProcessDefinition definition = findLatestProcessDefinition("simpleSuperProcess");
		ProcessInstance processInstance = definition.createProcessInstance();
		
		processInstance.signal();
		deployV2Definitions();
		
		assertTrue(MigrationUtils.requiresMigration(processInstance, jbpmContext));
	}

	public void testThatASuperProcessWillRequireMigrationIfANewSubProcessWasDeployed() throws IOException{
		deployV1Definitions();
		ProcessDefinition definition = findLatestProcessDefinition("simpleSuperProcess");
		ProcessInstance processInstance = definition.createProcessInstance();
		
		processInstance.signal();
		processInstance.signal();
		assertEquals("Fork1", processInstance.getRootToken().getNode().getName());
		assertEquals("forkNode1", processInstance.getRootToken().getChild("to_forkNode1").getNode().getName());
		assertEquals("forkNode2", processInstance.getRootToken().getChild("to_forkNode2").getNode().getName());
		assertEquals("forkNode3", processInstance.getRootToken().getChild("to_forkNode3").getNode().getName());
		assertEquals("SubA", processInstance.getRootToken().getChild("to_forkNode3").getSubProcessInstance().getRootToken().getNode().getName());
		assertFalse(MigrationUtils.requiresMigration(processInstance, jbpmContext));
		
		deploySubProcessV2Definition();
		
		assertTrue(MigrationUtils.requiresMigration(processInstance, jbpmContext));
		assertTrue(MigrationUtils.requiresMigration(processInstance.getRootToken().getChild("to_forkNode3").getSubProcessInstance(), jbpmContext));
		
	}
	
	public void testThatThe_lookupDynamicMigration_methodReturnsTheCorrectClassName(){
		DynamicMigration dynamicMigration = 
			MigrationUtils.lookupDynamicMigration("org.jbpm.instance.migration.DynamicMigrationForTesting");
		assertNotNull(dynamicMigration);
		assertEquals(DynamicMigrationForTesting.class, dynamicMigration.getClass());
	}

	//TODO: Would be better if you could generate these various classes at runtime. Look into a class generation library.
	public void testThatThe_lookupDynamicMigration_throwsAnInvalidMigrationExceptionForInvalidScenarios(){
		try {
			MigrationUtils.lookupDynamicMigration("org.jbpm.instance.migration.NonExistentDynamicMigration");
			fail("Non-existent Class type. Expected an '" + InvalidMigrationException.class.getName() + "'");
		} catch (InvalidMigrationException e) {
			//expected
		}

		try {
			MigrationUtils.lookupDynamicMigration("org.jbpm.instance.migration.DynamicMigration");
			fail("Interface is not a valid type. Expected an '" + InvalidMigrationException.class.getName() + "'");
		} catch (InvalidMigrationException e) {
			//expected
		}

		try {
			MigrationUtils.lookupDynamicMigration("org.jbpm.instance.migration.AbstractDynamicMigrationForTesting");
			fail("Abstract class is not a valid type. Expected an '" + InvalidMigrationException.class.getName() + "'");
		} catch (InvalidMigrationException e) {
			//expected
		}

		try {
			MigrationUtils.lookupDynamicMigration("org.jbpm.instance.migration.NoDefaultConstructorDynamicMigrationForTesting");
			fail("A DynamicMigration inplementation must have a default constructor. Expected an '" + InvalidMigrationException.class.getName() + "'");
		} catch (InvalidMigrationException e) {
			//expected
		}
		
	}
	
	public void testThatThe_isDynamicNodeName_methodIsCorrect(){
		assertTrue(MigrationUtils.isDynamicNodeName("java://org.jbpm.instance.migration.DynamicMigrationForTesting"));
		
		assertFalse(MigrationUtils.isDynamicNodeName("org.jbpm.instance.migration.DynamicMigrationForTesting"));
		assertFalse(MigrationUtils.isDynamicNodeName(null));
		assertFalse(MigrationUtils.isDynamicNodeName(""));
		assertFalse(MigrationUtils.isDynamicNodeName(" "));
	}

	public void testThatThe_parseClassNameFromDynamicNode_methodIsCorrect(){
		assertEquals("org.jbpm.instance.migration.DynamicMigrationForTesting",
				MigrationUtils.parseClassNameFromDynamicNode("java://org.jbpm.instance.migration.DynamicMigrationForTesting"));
		
		assertEquals("",
				MigrationUtils.parseClassNameFromDynamicNode("org.jbpm.instance.migration.DynamicMigrationForTesting"));
		
		assertEquals("",
				MigrationUtils.parseClassNameFromDynamicNode(""));

		assertEquals(null,
				MigrationUtils.parseClassNameFromDynamicNode(null));
		
	}
	
	private void deployV1Definitions() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleSubProcessDefinition_001.xml"));
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleSuperProcessDefinition_001.xml"));
	}

	private void deployV2Definitions() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleSubProcessDefinition_002.xml"));
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleSuperProcessDefinition_002.xml"));
	}

	private void deploySubProcessV2Definition() throws IOException {
		jbpmContext.deployProcessDefinition(MigrationUtils.getProcessDefinition("simpleSubProcessDefinition_002.xml"));
	}

}
