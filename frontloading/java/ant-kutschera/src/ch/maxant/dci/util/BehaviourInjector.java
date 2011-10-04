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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Resource;


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
public class BehaviourInjector extends DCIHelper {

	private Stack<ContextData> stack = new Stack<ContextData>();
	
	private SimpleRoleAssigner simpleRoleAssigner = new SimpleRoleAssigner();
	
	/** role interfaces mapped to role impls */
	private Map<Class<? extends Object>, Class<? extends Object>> injectionInfo = new HashMap<Class<? extends Object>, Class<? extends Object>>();

	/** resources, which will be injected into roles as required. */
	private Map<String, Object> resources = new HashMap<String, Object>();

	private Object context;

	/**
	 * @param context The class which starts the interaction.  Required so that during recursion, the
	 * method annotated with Recursable
	 */
	public BehaviourInjector(Object context){
		this.context = context;
		stack.add(new ContextData());
		//setup the following interface-to-impl mappings, so that programmers can use the simpler methods :-)
		setupRoleInterfaceToRoleImplMapping(IIterable_Role.class, Iterable_Role.class);
	}
	
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
	public <T, U> U assignSimpleRole(
			T objectToAssignRoleTo, Class<? extends U> roleInterface) {

		return simpleRoleAssigner.assignRole(objectToAssignRoleTo, roleInterface);
	}
	
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
	 * set the given object into the given role, assuming we have already cast into this role before.
	 * if not, see {@link #assignRole(Object, Class, Class)}.
	 */
	public <S> S assignRole(Object o, Class<S> roleInterfaceClass){
		return assignRole(o, roleInterfaceClass, (String)null);
	}
	
	/** 
	 * set the given object into the given role, assuming we have already cast into this role before.
	 * if not, see {@link #assignRole(Object, Class, Class)}. 
	 * adds it to the role-map with the given name.
	 */
	public <S> S assignRole(Object o, Class<S> roleInterfaceClass, String name){
		//do we know which interface belongs to which impl?
		Class<? extends Object> roleImplClass = injectionInfo.get(roleInterfaceClass);
		if(roleImplClass == null){
			throw new IllegalArgumentException("I have never cast an object in the role " + roleInterfaceClass.getName() + ".  Please tell me explicitly which role implementation to use!");
		}
		return assignRole(o, roleImplClass, roleInterfaceClass, name, true);
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
	public <S, T, U> U assignRole(
			S domainObjectToInjectInto,
			Class<? extends T> roleImplClass,
			Class<? extends U> roleInterface) {
		return assignRole(domainObjectToInjectInto, roleImplClass, roleInterface, (String)null, true);
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
	 * @param name the name to use for this object playing the role, so it is accessible through the context. adds it to the role-map with the given name.
	 * @return a dynamic proxy with the role interface.
	 */
	public <S, T, U> U assignRole(
			S domainObjectToInjectInto,
			Class<? extends T> roleImplClass,
			Class<? extends U> roleInterface,
			String name) {
		return assignRole(domainObjectToInjectInto, roleImplClass, roleInterface, name, true);
	}

	/** 
	 * see public methods.  this one also has the parameter injectContext, telling it if it should 
	 * inject the context or not.  normally, you call this method with true, but it calls itself with false.
	 */
	@SuppressWarnings("unchecked")
	private <S, T, U> U assignRole(
			S domainObjectToInjectInto,
			Class<? extends T> roleImplClass,
			Class<? extends U> roleInterface,
			String name,
			boolean injectContext) {
		//create a new instance of the methodful role class
		Object roleInstance;
		try {
			roleInstance = roleImplClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("unable to instantiate role implementation class " + roleImplClass.getName(), e);
		}
		
		//add any optional resources to the role
		injectResources(roleInstance);

		//now create the proxy for the role, containing the role impl and domain model
		ClassLoader classLoader = getClassLoader(domainObjectToInjectInto);
		Class[] interfaces = new Class[] { roleInterface };
		MyInvocationHandler ih = new MyInvocationHandler(domainObjectToInjectInto, roleInstance);

		U proxy = (U) Proxy.newProxyInstance(classLoader, interfaces, ih);

		//search recursively for any fields with the @Self annotation and inject
		//the proxy into those fields. normally, there would just be one such field.
		injectSelf(roleInstance, proxy);

		//search recursively for any fields with the @DataObject annotation and inject
		//the proxy into those fields. normally, there would just be one such field.
		injectDataObject(roleInstance, domainObjectToInjectInto);

		//create the context in its role, then inject it
		if(injectContext){
			//cast this to the context class, and inject it!
			Context ctx = assignRole(
					this, 
					CurrentContext_Role.class, 
					Context.class,
					name, 
					false/*otherwise we get a stack overflow!*/);
			injectCurrentContext(roleInstance, ctx);
		}

		if(isSystemPropertyForCheckSet()){
			doCheck(roleInterface, domainObjectToInjectInto, roleImplClass);
		}
		
		//everything went well, lets make a note about which classes belong together
		collectInjectionInfo(roleImplClass, roleInterface, proxy, name);
		
		doAfterInjection(roleInstance);
		
		return proxy;
	}

	private ClassLoader getClassLoader(Object o){
		ClassLoader cl = o.getClass().getClassLoader();
		if(cl == null){
			//strange, but happens...
			cl = this.getClass().getClassLoader();
			if(cl == null){
				//even stranger...
				cl = Thread.currentThread().getContextClassLoader();
			}
		}
		return cl;
	}
	
	/**
	 * lets make a note about which classes belong together - might be useful
	 * for when we need to recast roles :-)
	 * @see #setObjectPlayingRole(Object, Class)
	 */
	private void collectInjectionInfo(Class<? extends Object> roleImplClass,
			Class<? extends Object> roleInterfaceClass, Object proxy, String name) {
		setupRoleInterfaceToRoleImplMapping(roleInterfaceClass, roleImplClass);
		stack.peek().getObjectsPlayingRoles().put(name, proxy);
	}
	
	/** 
	 * tells the {@link BehaviourInjector} that if its given the roleInterfaceClass, it needs to use
	 * the roleImplClass as the implementation of the state behaviour.
	 */
	public void setupRoleInterfaceToRoleImplMapping(Class<? extends Object> roleInterfaceClass,
			Class<? extends Object> roleImplClass){
		injectionInfo.put(roleInterfaceClass, roleImplClass);
	}

	/** get hold of the object playing the role.   gets it from the role-map with the given name.*/
	public Object getObjectPlayingRole(String name){
		return stack.peek().getObjectsPlayingRoles().get(name);
	}
	
	/**
	 * @return an iterator of type T.  ie casts each object to type T.
	 */
	public <S,T> IIterable_Role<S> getIterator(Object o, Class<T> roleInterface){
		return getIterator(o, roleInterface, null);
	}
	
	/**
	 * @return an iterator of type T.  ie casts each object to type T. 
	 * adds it to the role-map with the given name.
	 */
	@SuppressWarnings("unchecked")
	public <S,T> IIterable_Role<S> getIterator(Object o, Class<T> roleInterface, String name){
		IIterable_Role ir = assignRole(
				o, 
				IIterable_Role.class);
		ir.setType(roleInterface);
		stack.peek().getObjectsPlayingRoles().put(name, ir);
		return ir;
	}

	/** see {@link Context#prepareForRecurse()} */
	public void prepareForRecurse(){
		stack.add(new ContextData());
	}

	/** see {@link Context#recurse()} */
	public void recurse(){
		for(Method m : context.getClass().getMethods()){
			if(m.getAnnotation(Recursable.class) != null){
				try {
					m.invoke(context, (Object[])null);
				} catch (Exception e) {
					throw new RuntimeException("Failed to call Recursable method in context.", e);
				}
			}
		}
		
		//now get rid of the (now old) context role-map.
		stack.pop();
	}

	/** handles callign any methods marked with {@link AfterInjection}, recursively thru the inheritance tree. */
	private void doAfterInjection(Object roleInstance) {
		doAfterInjection(roleInstance, roleInstance.getClass());
	}

	/** handles callign any methods marked with {@link AfterInjection}, recursively thru the inheritance tree. */
	private void doAfterInjection(Object roleInstance, Class<? extends Object> clazz) {
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.getAnnotation(AfterInjection.class) != null) {
				method.setAccessible(true); //it may be private!
				try {
					method.invoke(roleInstance, (Object[])null);
				} catch (Exception e) {
					throw new RuntimeException("unable to run AfterInjection method " + method + " in class " + roleInstance.getClass().getName(), e);
				}
			}
		}

		//check recursively, because roles can have superclasses, where Self is declared!
		if(clazz.getSuperclass() != null){
			doAfterInjection(roleInstance, clazz.getSuperclass());
		}
	}
	
	/** 
	 * handles injecting this behaviour injector into any fields marked with 
	 * the {@link CurrentContext} annotation.  in doing so, casts this to
	 * role {@link Context}.
	 */
	private void injectCurrentContext(Object roleInstance, Context context) {
		injectCurrentContext(roleInstance, context, roleInstance.getClass());
	}

	private void injectCurrentContext(Object roleInstance, Context context,
			Class<? extends Object> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(CurrentContext.class) != null) {
				field.setAccessible(true); //it may be private!
				try {
					field.set(roleInstance, context);
				} catch (Exception e) {
					throw new RuntimeException("unable to set currentContext in class " + roleInstance.getClass().getName(), e);
				}

				// roles can derive from each other and we only need to do this to the 
				// first instance we find
				return;
			}
		}

		//check recursively, becaues roles can have superclasses, where Self is declared!
		if(clazz.getSuperclass() != null){
			injectCurrentContext(roleInstance, context, clazz.getSuperclass());
		}
	}

	/**
	 * recursively search for {@link Self} annotations and inject such fields with the 
	 * role object.
	 */
	private void injectSelf(Object roleInstance, Object roleProxy){
		injectSelf(roleInstance, roleProxy, roleInstance.getClass());
	}

	/**
	 * @see #injectSelf(Object, Object)
	 */
	private void injectSelf(Object roleInstance, Object roleProxy, Class<? extends Object> clazz){
		
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(Self.class) != null) {
				field.setAccessible(true); //it may be private!
				try {
					field.set(roleInstance, roleProxy);
				} catch (Exception e) {
					throw new RuntimeException("unable to set self in class " + roleInstance.getClass().getName(), e);
				}

				// roles can derive from each other and we only need to do this to the 
				// first instance we find
				return;
			}
		}

		//check recursively, becaues roles can have superclasses, where Self is declared!
		if(clazz.getSuperclass() != null){
			injectSelf(roleInstance, roleProxy, clazz.getSuperclass());
		}
	}

	/**
	 * recursively search for {@link DataObject} annotations and inject such fields with the 
	 * data object.
	 */
	private void injectDataObject(Object roleInstance, Object dataObject){
		injectDataObject(roleInstance, dataObject, roleInstance.getClass());
	}
	
	/**
	 * @see #injectDataObject(Object, Object)
	 */
	private void injectDataObject(Object roleInstance, Object dataObject, Class<? extends Object> clazz){
		
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(DataObject.class) != null) {
				field.setAccessible(true); //it may be private!
				try {
					field.set(roleInstance, dataObject);
				} catch (Exception e) {
					throw new RuntimeException("unable to set data object in class " + roleInstance.getClass().getName(), e);
				}
				
				// roles can derive from each other and we only need to do this to the 
				// first instance we find
				return;
			}
		}
		
		//check recursively, becaues roles can have superclasses, where Self is declared!
		if(clazz.getSuperclass() != null){
			injectSelf(roleInstance, dataObject, clazz.getSuperclass());
		}
	}
	
	/**
	 * recursively inject fields marked with {@link Resource}.
	 */
	private void injectResources(Object roleInstance){
		injectResources(roleInstance, roleInstance.getClass());
	}

	/**
	 * @see #injectResources(Object)
	 */
	@SuppressWarnings("unchecked")
	private void injectResources(Object roleInstance, Class clazz) {
		Field[] declaredFields = clazz.getDeclaredFields();
		Field[] otherFields = clazz.getFields();
		Field[] allFields = new Field[declaredFields.length + otherFields.length];
		System.arraycopy(declaredFields, 0, allFields, 0, declaredFields.length);
		System.arraycopy(otherFields, 0, allFields, declaredFields.length, otherFields.length);
		
		for (Field field : allFields) {
			Annotation a = field.getAnnotation(Resource.class);
			if (a != null) {
				String name = field.getName();
				Object resource = resources.get(name);
				if(resource == null){
					//check using the name defined in the annotation
					String aName = ((Resource)a).name();
					if(aName != null){
						name = aName;
						resource = resources.get(name);
					}
				}
				if(resource != null){
					field.setAccessible(true); //it may be private!
					try {
						field.set(roleInstance, resource);
					} catch (Exception e) {
						throw new RuntimeException("unable to set resource " + name + " in class " + roleInstance.getClass().getName(), e);
					}
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
			extends AbstractDCIInvocationHandler<S> {

		private List<Method> roleMethods = new LinkedList<Method>();
		private T roleInstance;

		/**
		 * @param domainObject the domain object
		 * @param roleInstance the instance of the role methods.
		 */
		public MyInvocationHandler(S domainObject, T roleInstance) {
			super(domainObject);
			this.roleInstance = roleInstance;

			//TODO is it correct to take them from the domain object, or should we really be taking them from the role?
			for (Method method : roleInstance.getClass().getMethods()) {
				roleMethods.add(method);
			}
		}

		/** {@inheritDoc} */
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			
			//TODO add ability to have the method on both objects, and using an annotation
			// we can run it on a specific object.  

			//prefer role methods over objects.  can cause stack overflows, but the
			//DataObject annotation is there to help. altho its currently internal...
			Object o = null;
			Method m = getRoleMethod(method);
			try{
				if (m != null) {
					//we found this method in the role impl, so execute it there
					o = roleInstance;
				}else{
					//it must be a method in the data object
					m = getDomainObjectMethod(method);
					if(m == null){
						//god only knows...
						m = method; // so we can log it in a sec
						throw new Exception("Cannot find method on role impl instance, or on the domain object");
					}else{
						o = domainObject;
					}
				}

				//special case - if its the equals method, we do a direct comparison to the domain object
				//in order to help solve the object schizophrenia problem
				if(isEqualsMethod(m, args)){
					return domainObject.equals(args[0]);
				}
				
				return m.invoke(o, args);
			}catch(Exception e){
				String s = "Failed to call method " + m + " on object of type " + (o==null?o:o.getClass()) + " - is that method implemented there?";
				throw new InvocationTargetException(e, s);
			}
		}

		/**
		 * @return the method from the domain object, with the same name and parameters as the given method,
		 * or null, if it doesnt exist
		 */
		private Method getRoleMethod(Method method)
				throws IllegalAccessException, InvocationTargetException {
			for (Method roleMethod : roleMethods) {
				if (match(method, roleMethod)) {
					return roleMethod;
				}
			}
			
			return null;
		}

		/** @return true if the given method is the equals method */
		@SuppressWarnings("unchecked")
		protected boolean isEqualsMethod(Method m, Object[] args){
			//special case - if its the equals method, we do a direct comparison to the domain object
			//in order to help solve the object schizophrenia problem
			if(m.getName().equals("equals") && m.getReturnType() != null && m.getReturnType().getName().equals("boolean")){
				Class[] paramTypes = m.getParameterTypes();
				if(paramTypes.length == 1 && args.length == 1){
					if(paramTypes[0].getName().equals("java.lang.Object")){
						return true;
					}
				}
			}
			return false;
		}
	}
}
