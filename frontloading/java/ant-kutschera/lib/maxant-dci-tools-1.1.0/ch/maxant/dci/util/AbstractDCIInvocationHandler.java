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
import java.util.LinkedList;
import java.util.List;

/**
 * provides helper methods common to invocation handlers.
 */
/*package*/abstract class AbstractDCIInvocationHandler<T> implements InvocationHandler {

	protected T domainObject;

	protected List<Method> domainObjectMethods = new LinkedList<Method>();

	protected AbstractDCIInvocationHandler(T domainObject){
		this.domainObject = domainObject;
		for (Method method : domainObject.getClass().getMethods()) {
			domainObjectMethods.add(method);
		}
	}
	
	/**
	 * @return the method of the domain object, if one is found, otherwise null.
	 */
	protected Method getDomainObjectMethod(Method method)
	throws IllegalAccessException, InvocationTargetException {
		for (Method domainObjectMethod : domainObjectMethods) {
			if (match(method, domainObjectMethod)) {
				return domainObjectMethod;
			}
		}
	
		return null;
	}

	public static boolean match(Method method1, Method method2) {
		return namesAreEqual(method1, method2)
				&& typesAreEqual(method1, method2);
	}

	private static boolean namesAreEqual(Method method1, Method method2) {
		return method2.getName().equals(method1.getName());
	}

	private static boolean typesAreEqual(Method method1, Method method2) {
		Class<?> types1[] = method1.getParameterTypes();
		Class<?> types2[] = method2.getParameterTypes();
		if (types1.length != types2.length) {
			return false;
		}

		for (int i = 0; i < types1.length; ++i) {
			if (!types1[i].equals(types2[i])) {
				return false;
			}
		}
		return true;
	}

}
