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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A utility class to provide Hibernate support in unit tests.
 * @author Caleb Powell <caleb.powell@gmail.com> 
 * @author David Harcombe <david.harcombe@intelliware.ca> 
 */
public class HibernateTestSupport {
	private static XmlBeanFactory factory;

	private void setUpTransactionManager() {
		Session session = getSessionFactory().openSession();
		TransactionSynchronizationManager.bindResource(getSessionFactory(), new SessionHolder(session));
	}

	public void setUp() {
		if (factory == null) {
			factory = new XmlBeanFactory(new ClassPathResource("/org/jbpm/instance/migration/mock-hibernate-spring.xml"));
		}
		setUpTransactionManager();
	}

	public void tearDown() throws Exception {
		SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(getSessionFactory());
		Session session = holder.getSession();
		TransactionSynchronizationManager.unbindResource(getSessionFactory());
		session.close();
	}

	public SessionFactory getSessionFactory() {
		return (SessionFactory) getFactory().getBean(SessionFactory.class.getName());
	}

	public BeanFactory getFactory() {
		return factory;
	}

	public void runInTransaction(Runnable runnable) {
		Transaction transaction = getSessionFactory().getCurrentSession().beginTransaction();
		runnable.run();
		transaction.commit();
	}
}
