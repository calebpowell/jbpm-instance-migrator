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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.instance.migration.util.JbpmInstanceMigratorLogger;


/**
 * Provides utility methods to the Migrator class.
 * 
 * @see Migrator
 * @author Caleb Powell <caleb.powell@intelliware.ca> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class MigrationUtils {
	
	private static Logger logger = Logger.getLogger(JbpmInstanceMigratorLogger.class);
	
	/**
	 * Convert an xml file into a valid jBPM ProcessDefinition class
	 *  
	 * @param filename The xml file containing the ProcessDefinition definition
	 * @return A ProcessDefinition
	 * @throws IOException
	 */
	public static ProcessDefinition getProcessDefinition(String filename) throws IOException {
		String processDefinition = FileUtils.readFileToString(new File(MigrationUtils.class.getResource(filename).getPath()), "UTF-8");
		return ProcessDefinition.parseXmlString(processDefinition);
	}

	/**
	 * Taking a base class name, this method will search the Classpath for all Migrations that begin with that name. The migrations will be returned
	 * in an array. For example, given the base Class name 'com.foo.BarMigration', this method will attempt to load classes named 
	 * 'com.foo.BarMigration001', 'com.foo.BarMigration002', 'com.foo.BarMigration003', etc. It will stop loading when it catches a {@link ClassNotFoundException}.
	 *  
	 * Valid Migration classes must implement the {@link Migration} interface (but not be an interface themselves), have a default constructor, and cannot be abstract.
	 * 
	 * @param baseClassName
	 * @return 
	 */
	public static Migration[] lookupMigrationsFor(String baseClassName) {
		int revision = 0;
		List migrations = new ArrayList();
		while (true) {
			String migrationName = constructMigrationClassName(baseClassName, ++revision);
			try {
				Class migrationClass = Class.forName(migrationName);
				if (isValidMigration(migrationClass)) {
					migrations.add(migrationClass.newInstance());
				}
			} catch (ClassNotFoundException e) {
				if(CollectionUtils.isEmpty(migrations)) {
					logger.info("Could not locate any migrations for the base Class name [" + baseClassName + "].");
				}
				break;
			} catch (InstantiationException e) {
				String errorMessage = "The " + migrationName + " migration could not be instantiated. Please ensure it has a default constructor.";
				logger.error(errorMessage, e);
				throw new InvalidMigrationException(errorMessage, e);
			} catch (IllegalAccessException e) {
				String errorMessage = "The " + migrationName + " migration could not be instantiated.";
				logger.error(errorMessage, e);
				throw new InvalidMigrationException(errorMessage, e);
			}
		}
		return (Migration[]) migrations.toArray(new Migration[migrations.size()]);
	}

	/**
	 * The method joins a base Class name and a revision number. It will pad the revision number if necessary.
	 * @param baseClassName
	 * @param revision
	 * @return
	 */
	private static String constructMigrationClassName(String baseClassName, int revision) {
		return baseClassName + StringUtils.leftPad(Integer.toString(revision), 3, "0");
	}

	/**
	 * 
	 * @param migrationClass
	 * @return true is this class is an assignable to {@link Migration}, is not an interface, and is not abstract.
	 */
	private static boolean isValidMigration(Class migrationClass) {
		return Migration.class.isAssignableFrom(migrationClass) && !migrationClass.isInterface() && !Modifier.isAbstract(migrationClass.getModifiers());
	}

	/**
	 * Used to determine whether a ProcessInstance requires migration.
	 * @param processInstance
	 * @return true if the processInstance (or any of it's subProcessInstance's) belongs to an outdated processDefinition.
	 */
	public static boolean requiresMigration(ProcessInstance processInstance, JbpmContext jbpmContext) {
		ProcessDefinition latestProcessDefinition = MigrationUtils.findLatestProcessDefinition(processInstance.getProcessDefinition().getName(), jbpmContext);
		//1. look at the given process definition
		boolean processInstanceRequiresMigration = processInstance.getProcessDefinition().getVersion() != latestProcessDefinition.getVersion(); 
		
		//2. search for sub process definitions
		List subProcesses = findSubProcesses(processInstance.getRootToken());
		for (Iterator iter = subProcesses.iterator(); processInstanceRequiresMigration == false && iter.hasNext();) {
			ProcessInstance subProcessInstance = (ProcessInstance) iter.next();
			processInstanceRequiresMigration = requiresMigration(subProcessInstance, jbpmContext);
		}
		logger.info("Checking whether process instance[@id='" + processInstance.getId()+"'] requires migration? => " + processInstanceRequiresMigration);
		return processInstanceRequiresMigration ;
	}

	private static List findSubProcesses(Token token) {
		ArrayList processInstances = new ArrayList();
		ProcessInstance processInstance = token.getSubProcessInstance();
		if(processInstance != null){
			processInstances.add(processInstance);
			processInstances.addAll(findSubProcesses(processInstance.getRootToken()));
		}
		
		Map children = token.getChildren();
		if(children != null){
			for (Iterator iter = children.values().iterator(); iter.hasNext();) {
				Token childToken = (Token) iter.next();
				processInstances.addAll(findSubProcesses(childToken));
			}
		}
		return processInstances;
	}

	/**
	 * Used to find a {@link ProcessDefinition} by it's name.
	 * 
	 * @param processName The name of the process definition.
	 * @param jbpmContext
	 * @return
	 */
	public static ProcessDefinition findLatestProcessDefinition(String processName, JbpmContext jbpmContext) {
		return jbpmContext.getGraphSession().findLatestProcessDefinition(processName);
	}
}
