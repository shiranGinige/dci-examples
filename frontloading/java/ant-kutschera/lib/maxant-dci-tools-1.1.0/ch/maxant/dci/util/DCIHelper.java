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

import java.lang.reflect.Method;

/**
 * provides helpful stuff to {@link BehaviourInjector} and {@link SimpleRoleAssigner}.
 */
abstract class DCIHelper {

	/** 
	 * the system property which can be set =true in order that the {@link BehaviourInjector} and
	 * {@link SimpleRoleAssigner} classes ensure that all methods of the interface can be found.
	 * allows the programmer to program less tests, but still have a guarantee that all methods do
	 * exist in the given objects.
	 */
	public static final String SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST = "ch.maxant.dci.util.checkAllRoleMethodsExist";
	
	/** 
	 * checks if the system property is set =true.
	 */
	protected boolean isSystemPropertyForCheckSet(){
		String s = System.getProperty(SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST);
		if(s != null){
			if(s.equals("true")){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param interfaceToCheck which interface should be checked to see if its methods are implemented elsewhere?
	 * @param object the object which will be cast to this interface
	 * @param implementation optional (can be null), a class containing extra methods not defined in the object.
	 */
	@SuppressWarnings("unchecked")
	protected void doCheck(Class interfaceToCheck, Object object, Class implementation){
		Method[] interfaceMethods = interfaceToCheck.getMethods();
		Method[] objectMethods = object.getClass().getMethods();
		Method[] implementationMethods = new Method[0];
		if(implementation != null){
			implementationMethods = implementation.getMethods();
		}

		for (Method interfaceMethod : interfaceMethods) {
			//can we find it in the implementation, if its been specified?
			boolean foundInObject = false;
			for(Method objectMethod : objectMethods){
				if(AbstractDCIInvocationHandler.match(interfaceMethod, objectMethod)){
					foundInObject = true;
					break;
				}
			}
			if(!foundInObject){
				//search impl, if available
				boolean foundInImplementation = false;
				for(Method implementationMethod : implementationMethods){
					if(AbstractDCIInvocationHandler.match(interfaceMethod, implementationMethod)){
						foundInImplementation = true;
						break;
					}
				}
				if(!foundInImplementation){
					String s = "Unable to find method " + interfaceMethod + " in class " + object.getClass().getName();
					if(implementation != null){
						s += ", or in class " + implementation.getName();
					}
					throw new UnsupportedOperationException(s);
				}
			}
		}

	}
}
