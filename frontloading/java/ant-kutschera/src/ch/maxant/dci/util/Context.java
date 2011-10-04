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



/**
 * this is the methodless role of a default context.
 */
public interface Context {

	/** @see BehaviourInjector#assignSimpleRole(Object, Class) */
	<T, U> U assignSimpleRole(
			T objectToAssignRoleTo, Class<? extends U> roleInterface);

	/** @see BehaviourInjector#assignRole(Object, Class) */
	<S> S assignRole(Object o, Class<S> roleInterfaceClass);

	/** @see BehaviourInjector#assignRole(Object, Class, String) */
	<S> S assignRole(Object o, Class<S> roleInterfaceClass, String name);
	
	/** @see BehaviourInjector#assignRole(Object, Class, Class) */
	<S, T, U> U assignRole(
			S domainObjectToInjectInto,
			Class<? extends T> roleImplClass,
			Class<? extends U> roleInterface);
	
	/** @see BehaviourInjector#assignRole(Object, Class, Class, String) */
	<S, T, U> U assignRole(
			S domainObjectToInjectInto,
			Class<? extends T> roleImplClass,
			Class<? extends U> roleInterface,
			String name);

	/** @see BehaviourInjector#getObjectPlayingRole(String) */
	Object getObjectPlayingRole(String name);

	/** @see BehaviourInjector#getIterator(Object, Class) */
	<T> IIterable_Role<T> getIterator(Object o, Class<T> roleInterface);
	
	/** @see BehaviourInjector#getIterator(Object, Class, String) */
	<T> IIterable_Role<T> getIterator(Object o, Class<T> roleInterface, String name);
	
	/** 
	 * pauses the current context and puts a new context role-map onto the 
	 * 
	 * the context.  before calling this, recast any required objects into the right roles.
	 * this method will also cause a new context to be pushed to the stack and the current one to 
	 * be suspended until it is complete after which it is popped and the initial context is continued.
	 */
	void prepareForRecurse();

	/** 
	 * starts the context role-map that was pushed onto the stack with {@link #prepareForRecurse()}.  
	 * before calling this, recast any required objects into the right roles by calling any of the 
	 * assignRole methods.  This method then calls the method on the context annotated with {@link Recursable}.
	 * once that method returns, the context role-map is popped and the initial context role-map is 
	 * used again and this method returns to its caller. fails fast and hard if the method annotated
	 * with {@link Recursable} fails or is not found.  if multiple methods have the annotation, they are all called.
	 */
	void recurse();
}
