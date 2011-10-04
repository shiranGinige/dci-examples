/*
 * Copyright (c) 2010 Ant Kutschera, maxant
 * 
 * This file is part of Ant Kutschera's blog.
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 * You should have received a copy of the Lesser GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.maxant.oopsoadci_common.bankingexample.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import ch.maxant.oopsoadci_common.bankingexample.data.BankAccount;
import ch.maxant.oopsoadci_common.bankingexample.data.Party;

/**
 * a helper for hiding a JPA ORM impl.
 */
public class DBHelper {

	private static final String FIND_PARTY_BY_LOGIN_QL = "select p from Party p where p.login = :login";
	private static final String SELECT_ACCOUNT_BY_ID = "select a from BankAccount a where a.uid = :id";

	private EntityManager em;

	public DBHelper(){
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("test_local");
		em = factory.createEntityManager();
	}
	
	public EntityTransaction begin(){
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		return tx;
	}
	
	public Party getPartyByLogin(String name){
		TypedQuery<Party> q = em.createQuery(FIND_PARTY_BY_LOGIN_QL, Party.class);
		q.setParameter("login", name);
		return q.getSingleResult();
	}
	
	public BankAccount getBankAccount(Integer accountNumber){
		TypedQuery<BankAccount> q = em.createQuery(SELECT_ACCOUNT_BY_ID, BankAccount.class);
		q.setParameter("id", accountNumber);
		BankAccount account = q.getSingleResult();
		return account;
	}
	
	public void persist(Object o){
		em.persist(o);
	}
	
	public EntityManager getEntityManager(){
		return em;
	}
	
}
