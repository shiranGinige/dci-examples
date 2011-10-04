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
package ch.maxant.oopsoadci_dci.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * using a dynamic proxy, calls the methodless role method on the domain object.
 * 
 * built, thanks to Sebastian Kübeck!
 */
/*package*/ class RoleAssigner {
	
	@SuppressWarnings("unchecked")
	<T, U> U assignRole(
			T objectToAssignRoleTo, Class<? extends U> methodlessRoleInterface) {

		try {
			ClassLoader classLoader = objectToAssignRoleTo.getClass().getClassLoader();

			Class[] interfaces = new Class[] { methodlessRoleInterface };
			
			InvocationHandler ih = new MyInvocationHandler(objectToAssignRoleTo);

			return (U) Proxy.newProxyInstance(classLoader, interfaces, ih);

		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static class MyInvocationHandler<T>
			extends AbstractDCIInvocationHandler<T> {

		public MyInvocationHandler(T roleInstance) {
			super(roleInstance);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Method m = getRoleMethod(method);
			if(m == null){
				m = method;
			}
			return m.invoke(roleInstance, args);
		}

	}
}
