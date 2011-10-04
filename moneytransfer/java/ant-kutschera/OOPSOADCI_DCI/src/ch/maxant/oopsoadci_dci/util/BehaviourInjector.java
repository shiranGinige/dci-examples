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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

/**
 * using a dynamic proxy, makes the role interfaces come alive!
 * <br><br>
 * any time a role method is called, the dynamic proxy check if it can call the method on the role
 * implementation object. if not, it calls it on the domain object.
 * <br><br>
 * using this injector, means the caller only knows about the domain object in terms of its 
 * role.  its implementation is split across the domain object and the role impl.
 * <br><br>
 * also injects any field marked with {@link Self} with the domain object - but importantly, wrapped in a proxy,
 * so that the object only has its role interface!
 * <br><br>
 * also injects any field annotated with {@link Resource} if the resource has been added to this injector.
 * <br><br>
 * note, this implementation is rather dynamic - and requires less use of generics than some might prefer. the
 * result is that if you call a role method, with a slightly different signature than exists on either the 
 * role impl class, or domain object, you get a nasty runtime exception!  check {@link System#err} because
 * in such cases, where the method impl isn't found, there is a nice message there saying what the problem is.
 * <br><br>
 * built, thanks to Sebastian Kübeck!
 */
public class BehaviourInjector {

	/** resources, which will be injected into roles as required. */
	private Map<String, Object> resources = new HashMap<String, Object>();

	/** 
	 * add a resource which can be injected into the role.
	 * <br><br>
	 * in the role, there may be a requirement to use say an {@link EntityManager}
	 * in order to persist a new part of the domain model.  the entity manager could theoretically
	 * be passed to the role after its contruction, but the entity manager has nothing to do 
	 * with the users mental model - its a technical thing.  so simply let it be injected, and available,
	 * should it be required.  
	 * <br><br>
	 * to use this, the current implementation looks for fields with the "name" you pass.  any such 
	 * fields in either the role class, or any of its super classes, which are marked with {@link Resource}
	 * get injected.
	 */
	public void addResource(String name, Object o){
		resources.put(name, o);
	}

	/**
	 * this method takes the domain object and the role class.  it instantiates an instance of 
	 * the role class, and hides both the domain object and the role instance behind the 
	 * role interface.  thereafter, the client code can use the role object and the actual implementation 
	 * is hidden behing the role interface and implemented in either the domain object, or role class.
	 * <br><br>
	 * @param <S> the type of the domain object
	 * @param <T> the type of the role impl class
	 * @param domainObjectToInjectInto the domain object into which to inject the role impl.
	 * @param roleImplClass the role impl class to instantiate in order to use the role implementation.
	 * @return a dynamic proxy with the role interface.
	 */
	@SuppressWarnings("unchecked")
	public <S, T, U> U inject(
			S domainObjectToInjectInto,
			Class<? extends T> roleImplClass,
			Class<? extends U> roleInterface) {
		
		try {
			//create a new instance of the methodful role class
			Object roleInstance = roleImplClass.newInstance();
			
			//add any optional resources to the role
			injectResources(roleInstance);

			//because we are working with roles, and not domain object, and SELF has type of role, rather
			//than type of domain object, we need to "cast" the given domain object into the role.
			//we do this by creating a dynamic proxy which has the role interface, and wraps the domain object.
			U domainObjectToInjectIntoAsRole = new RoleAssigner().assignRole(domainObjectToInjectInto, roleInterface);

			//search recursively for any fields with the @Self annotation and inject
			//the proxy of the domain object into those fields. normally, there would just be one such field.
			setSelf(roleInstance, domainObjectToInjectIntoAsRole);

			//now create the proxy for the role, containing the role impl and domain model
			ClassLoader classLoader = domainObjectToInjectInto.getClass().getClassLoader();
			Class[] interfaces = new Class[] { roleInterface };
			MyInvocationHandler ih = new MyInvocationHandler(domainObjectToInjectInto, roleInstance);
			
			return (U) Proxy.newProxyInstance(classLoader, interfaces, ih);
			
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * recursively search for {@link Self} annotations and inject such fields with the 
	 * role object.
	 */
	private void setSelf(Object roleInstance, Object domainObjectAsRole)
			throws IllegalAccessException {
		setSelf(roleInstance, domainObjectAsRole, roleInstance.getClass());
	}

	/**
	 * @see #setSelf(Object, Object)
	 */
	@SuppressWarnings("unchecked")
	private void setSelf(Object roleInstance, Object domainObjectAsRole, Class clazz)
		throws IllegalAccessException {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(Self.class) != null) {
				field.setAccessible(true); //it may be private!
				field.set(roleInstance, domainObjectAsRole);
			}
		}

		//check recursively, becaues roles can have superclasses, where Self is declared!
		if(clazz.getSuperclass() != null){
			setSelf(roleInstance, domainObjectAsRole, clazz.getSuperclass());
		}
	}

	/**
	 * recursively inject fields marked with {@link Resource}.
	 */
	private void injectResources(Object roleInstance)
			throws IllegalAccessException {
		injectResources(roleInstance, roleInstance.getClass());
	}

	/**
	 * @see #injectResources(Object)
	 */
	@SuppressWarnings("unchecked")
	private void injectResources(Object roleInstance, Class clazz)
	throws IllegalAccessException {
		Field[] declaredFields = clazz.getDeclaredFields();
		Field[] otherFields = clazz.getFields();
		Field[] allFields = new Field[declaredFields.length + otherFields.length];
		System.arraycopy(declaredFields, 0, allFields, 0, declaredFields.length);
		System.arraycopy(otherFields, 0, allFields, declaredFields.length, otherFields.length);
		
		for (Field field : allFields) {
			if (field.getAnnotation(Resource.class) != null) {
				field.setAccessible(true); //it may be private!
				Object resource = resources.get(field.getName());
				if(resource != null){
					field.set(roleInstance, resource);
				}
			}
		}
		
		// do it recursively, as roles may have super classes!
		if(clazz.getSuperclass() != null){
			injectResources(roleInstance, clazz.getSuperclass());
		}
		
	}

	/**
	 * This invocation handler searches for the method being called firstly in the domain object
	 * and secondly in the role impl object. the first method found where the name and parameters matches
	 * counts as a hit and is called.
	 * 
	 * @param <S> type of domain object
	 * @param <T> type of class implementing role methods
	 */
	private static class MyInvocationHandler<S, T>
			extends AbstractDCIInvocationHandler<T> {

		private List<Method> domainObjectMethods = new LinkedList<Method>();
		private S domainObject;

		/**
		 * @param domainObject the domain object
		 * @param roleInstance the instance of the role methods.
		 */
		public MyInvocationHandler(S domainObject, T roleInstance) {
			super(roleInstance);
			this.domainObject = domainObject;

			//TODO is it correct to take them from the domain object, or should we really be taking them from the role?
			for (Method method : domainObject.getClass().getMethods()) {
				domainObjectMethods.add(method);
			}
		}

		/** {@inheritDoc} */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			
			//TODO add ability to have the method on both objects, and using an annotation
			// we can run it on a specific object.  
			
			//can i call it on the role impl?
			Object o = null;
			Method m = getRoleMethod(method);
			try{
				if (m != null) {
					//we found this method on the role impl, so execute it there
					o = roleInstance;
				}else{
					//it must be a method on the domain object
					m = getDomainObjectMethod(method);
					if(m == null){
						//god only knows...
						throw new Exception("Cannot find method on role impl instance, or on the domain object");
					}else{
						o = domainObject;
					}
				}
				return m.invoke(o, args);
			}catch(Exception e){
				System.err.println("Failed to call method " + m + " on object of type " + roleInstance.getClass() + " - is that method implemented there?");
				throw e;
			}
		}

		/**
		 * @return the method from the domain object, with the same name and parameters as the given method,
		 * or null, if it doesnt exist
		 */
		private Method getDomainObjectMethod(Method method)
		throws IllegalAccessException, InvocationTargetException {
			for (Method roleMethod : domainObjectMethods) {
				if (match(method, roleMethod)) {
					return roleMethod;
				}
			}
			
			return null;
		}
	}
}
