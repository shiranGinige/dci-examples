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
package ch.maxant.dci.util;


import org.junit.Test;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigDecimal;

import static org.junit.Assert.*;

public class BehaviourInjectorTest {

	@Test
	public void testPositive() {
		
		//force checks to ensure all interface methods exist somewhere
		System.setProperty(DCIHelper.SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST, "true");

		BankAccount account = new BankAccount();
		account.setUid(1);
		account.increaseBalance(new BigDecimal(1000.0));

		BehaviourInjector bi = new BehaviourInjector(null); //can pass null, as no recursion is done here
		
		//we add a resource, from which the test role will append the #toString() call. 
		bi.addResource("asdf", new Object(){
			@Override
			public String toString() {
				return "DB";
			}
		});

		//inject the role into the domain model object
		ITest_Role role = bi.assignRole(account, Test_Role.class, ITest_Role.class);

		//check a call to the underlying domain model object
		assertEquals(1, role.getUid());
		
		//start the interaction
		String s = role.doSomething();
		assertEquals("I am account # 1 and have access to the DB", s);
		
		assertFalse(role.equals("thisAintEqual!")); 

		assertTrue(role.equals(account)); //hey, no object schizophrenia!
		
		assertFalse(account.equals(role)); //hmm, ok, some schizophrenia - we can 
		//never get over this because equals tends to use "instanceof" which we cannot override

		assertFalse(account == role); //hmm, ok, some more schizophrenia - we can 
		//never get over this because the proxy is *not* the domain model object
	}
	
	@Test
	public void testNegative1() {
		
		//force checks to ensure all interface methods exist somewhere
		System.setProperty(DCIHelper.SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST, "true");
		
		BankAccount account = new BankAccount();
		account.setUid(1);
		account.increaseBalance(new BigDecimal(1000.0));

		BehaviourInjector bi = new BehaviourInjector(null); //can pass null, as no recursion is done here

		try{
			//inject the role into the domain model object
			bi.assignRole(account, Test_Role.class, ITest_Role_Error.class);
			fail("should throw an UnsupportedOperationException, because ITest_Role_Error " +
					"contains methods that are not implemented anywhere!");
		}catch(UnsupportedOperationException e){
			//yay, we expected this, because we have set the sys prop
		}
	}
	
	@Test
	public void testNegative2() {
		
		//DO NOT force checks to ensure all interface methods exist somewhere
		System.clearProperty(DCIHelper.SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST);
		
		BankAccount account = new BankAccount();
		account.setUid(1);
		account.increaseBalance(new BigDecimal(1000.0));

		BehaviourInjector bi = new BehaviourInjector(null); //can pass null, as no recursion is done here

		try{
			//inject the role into the domain model object
			ITest_Role_Error role = bi.assignRole(account, Test_Role.class, ITest_Role_Error.class);
			//yay, we expected this, because we DONT have set the sys prop
			
			//now use some unimplemented method and watch it fail
			role.doIExist();
			fail("should throw a InvocationTargetException");
		}catch(UnsupportedOperationException e){
			fail("shouldnt fail, because we are not doing checks");
		}catch(Exception e){
			if(e instanceof UndeclaredThrowableException){
				UndeclaredThrowableException ute = (UndeclaredThrowableException)e;
				if(ute.getUndeclaredThrowable() instanceof InvocationTargetException){
					//yay!
				}else{
					e.printStackTrace();
					fail("expected an InvocationTargetException!");
				}
			}else{
				e.printStackTrace();
				fail("expected an UndeclaredThrowableException!");
			}
		}
	}
	
	/** an example of a role interface */ 
	static interface ITest_Role {
		String doSomething();
		int getUid();
	}
	
	/** an example of a role interface, with methods which DONT exist in the domain */ 
	static interface ITest_Role_Error extends ITest_Role {
		boolean doIExist();
	}
	
	/** an example of the role implementation */
	static class Test_Role {
		
		@Resource(name="asdf")
		private Object resource;
		
		@Self
		private ITest_Role self;
		
		public String doSomething(){
			return "I am account # " + self.getUid() + " and have access to the " + resource;
		}
	}

	/** an example of a domain model class */
	static class BankAccount {
		private double balance;
		private int uid;
		public void setUid(int uid) {
			this.uid = uid;
		}
		public int getUid() {
			return uid;
		}
		public void increaseBalance(BigDecimal amount) {
			this.balance += amount.doubleValue();
		}
	}

}
