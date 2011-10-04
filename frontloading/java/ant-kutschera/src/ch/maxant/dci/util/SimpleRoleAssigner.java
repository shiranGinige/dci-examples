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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * using a dynamic proxy, calls the methodless role method on the domain object.
 * ie. assigns a methodless role to an object.
 * 
 * built, thanks to Sebastian Kübeck!
 */
/*package*/class SimpleRoleAssigner extends DCIHelper {

	/**
	 * casts the given objectToAssignRoleTo into a methodlessRoleInterface by using a dynamic proxy.
	 * This is used to simply narrow the interface of complex objects in order to make them easier to 
	 * understand during review.
	 * <br><br>
	 * @param <T>
	 * @param <U>
	 * @param objectToAssignRoleTo
	 * @param roleInterface
	 * @param doCheck if true, or if system property ch.maxant.dci.util.checkAllRoleMethodsExist is
	 * 			set to true, then this method checks that ALL methods in the interface exist in the 
	 * 			domain object too. throws a {@link UnsupportedOperationException} if a method cannot be 
	 * 			found.
	 * @return a dynamic proxy with the role interface.
	 */
	@SuppressWarnings("unchecked")
	public <T, U> U assignRole(
			T objectToAssignRoleTo, Class<? extends U> roleInterface) {

		ClassLoader classLoader = objectToAssignRoleTo.getClass().getClassLoader();

		Class[] interfaces = new Class[] { roleInterface };
		
		InvocationHandler ih = new MyInvocationHandler(objectToAssignRoleTo);

		if(isSystemPropertyForCheckSet()){
			doCheck(roleInterface, objectToAssignRoleTo, null);
		}
		
		return (U) Proxy.newProxyInstance(classLoader, interfaces, ih);
	}

	private static class MyInvocationHandler<S>
			extends AbstractDCIInvocationHandler<S> {

		public MyInvocationHandler(S domainObject) {
			super(domainObject);
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {

			Method m = getDomainObjectMethod(method);
			try{
				if(m == null){
					//god only knows...
					m = method; //so we can log it in a second...
					throw new Exception("Cannot find method on domain object");
				}
	
				//no need for special case (check isEqualsMethod()) here - because we ALWAYS perform the 
				//method on the domain object
				
				return m.invoke(domainObject, args);
			}catch(Exception e){
				String s = "Failed to call method " + m + " on object of type " + 
								domainObject.getClass() + " - is that method implemented there?";
				throw new InvocationTargetException(e, s);
			}
		}

	}
}
