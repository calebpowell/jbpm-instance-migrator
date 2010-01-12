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

import org.hibernate.classic.Session;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jmock.cglib.MockObjectTestCase;

/**
 * Abstract base class for the jbpm-migrator project. Performs Hibernate and Log4J configurations.
 * @see HibernateTestSupport
 * @author Caleb Powell <caleb.powell@gmail.com> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public abstract class BaseTestCase extends MockObjectTestCase {

	private HibernateTestSupport hibernateSupport;

	protected JbpmContext jbpmContext;

	protected void setUp() throws Exception {
		super.setUp();
		
		this.hibernateSupport = new HibernateTestSupport();
		this.hibernateSupport.setUp();
		this.jbpmContext = JbpmConfiguration.getInstance().createJbpmContext();
		Session currentSession = this.hibernateSupport.getSessionFactory().getCurrentSession();
		currentSession.beginTransaction();
		jbpmContext.setSession(currentSession);
	}
	
	/**
	 * Rollback to stop jBPM from keeping multiple version of processes we don't want.
	 * support.tearDown() to reinitialise between tests. 
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		Session currentSession = this.hibernateSupport.getSessionFactory().getCurrentSession();
		currentSession.getTransaction().rollback();
		this.hibernateSupport.tearDown();
	}

	protected ProcessDefinition findLatestProcessDefinition(String processName) {
		return jbpmContext.getGraphSession().findLatestProcessDefinition(processName);
	}
}
