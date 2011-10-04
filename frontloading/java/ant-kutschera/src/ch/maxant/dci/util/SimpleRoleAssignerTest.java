///*
// * Copyright (c) 2010 Ant Kutschera, maxant
// *
// * This file is part of Ant Kutschera's blog.
// *
// * This is free software: you can redistribute it and/or modify
// * it under the terms of the Lesser GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This software is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * Lesser GNU General Public License for more details.
// * You should have received a copy of the Lesser GNU General Public License
// * along with this software.  If not, see <http://www.gnu.org/licenses/>.
// */
//package ch.maxant.dci.util;
//
//
//import static org.junit.Assert.*;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.UndeclaredThrowableException;
//import java.math.BigDecimal;
//
//import org.junit.Test;
//
//public class SimpleRoleAssignerTest {
//
//	@Test
//	public void testPositive() {
//
//		//force checks to ensure all interface methods exist somewhere
//		System.setProperty(DCIHelper.SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST, "true");
//
//		BankAccount account = new BankAccount();
//		account.setUid(1);
//		account.increaseBalance(new BigDecimal(1000.0));
//
//		SimpleRoleAssigner ra = new SimpleRoleAssigner();
//
//		//inject the role into the domain model object
//		ITest_Role role = ra.assignRole(account, ITest_Role.class);
//
//		//can we call the domain object?
//		assertEquals(1, role.getUid());
//
//		assertFalse(role.equals("thisAintEqual!"));
//
//		assertTrue(role.equals(account)); //hey, no object schizophrenia!
//
//		assertFalse(account.equals(role)); //hmm, ok, some schizophrenia - we can
//		//never get over this because equals tends to use "instanceof" which we cannot override
//
//		assertFalse(account == role); //hmm, ok, some more schizophrenia - we can
//		//never get over this because the proxy is *not* the domain model object
//	}
//
//	@Test
//	public void testNegative1() {
//
//		//force checks to ensure all interface methods exist somewhere
//		System.setProperty(DCIHelper.SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST, "true");
//
//		BankAccount account = new BankAccount();
//		account.setUid(1);
//		account.increaseBalance(new BigDecimal(1000.0));
//
//		SimpleRoleAssigner ra = new SimpleRoleAssigner();
//
//		try{
//			//inject the role into the domain model object
//			ra.assignRole(account, ITest_Role_Error.class);
//			fail("should throw an UnsupportedOperationException!");
//		}catch(UnsupportedOperationException e){
//			//yay, we expected this, because we have set the sys prop
//		}
//	}
//
//	@Test
//	public void testNegative2() {
//
//		//DO NOT force checks to ensure all interface methods exist somewhere
//		System.clearProperty(DCIHelper.SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST);
//
//		BankAccount account = new BankAccount();
//		account.setUid(1);
//		account.increaseBalance(new BigDecimal(1000.0));
//
//		SimpleRoleAssigner ra = new SimpleRoleAssigner();
//
//		try{
//			//inject the role into the domain model object
//			ITest_Role_Error role = ra.assignRole(account, ITest_Role_Error.class);
//			//yay, we expected this, because we DONT have set the sys prop
//
//			//now use some unimplemented method and watch it fail
//			role.doIExist();
//			fail("should throw a InvocationTargetException");
//		}catch(UnsupportedOperationException e){
//			fail("shouldnt fail, because we are not doing checks");
//		}catch(Exception e){
//			if(e instanceof UndeclaredThrowableException){
//				UndeclaredThrowableException ute = (UndeclaredThrowableException)e;
//				if(ute.getUndeclaredThrowable() instanceof InvocationTargetException){
//					//yay!
//				}else{
//					e.printStackTrace();
//					fail("expected an InvocationTargetException!");
//				}
//			}else{
//				e.printStackTrace();
//				fail("expected an UndeclaredThrowableException!");
//			}
//		}
//	}
//
//	/** an example of a role interface */
//	static interface ITest_Role {
//		int getUid();
//	}
//
//	/** an example of a role interface, with methods which DONT exist in the domain */
//	static interface ITest_Role_Error extends ITest_Role {
//		boolean doIExist();
//	}
//
//	/** an example of a domain model class */
//	static class BankAccount {
//		private double balance;
//		private int uid;
//		public void setUid(int uid) {
//			this.uid = uid;
//		}
//		public int getUid() {
//			return uid;
//		}
//		public void increaseBalance(BigDecimal amount) {
//			this.balance += amount.doubleValue();
//		}
//	}
//
//}
