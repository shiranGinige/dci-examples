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


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BehaviourInjectorTest2 {

	private Project_Data project;
	
	@Before
	public void setup(){
		Task_Data a = new Task_Data("A");
		Task_Data b = new Task_Data("B");
		Task_Data c = new Task_Data("C");
		Task_Data d = new Task_Data("D");
		Task_Data e = new Task_Data("E");
		Task_Data f = new Task_Data("F");

		//eating depends on everything
		c.addDependency(a);
		c.addDependency(b);
		c.addDependency(d);
		c.addDependency(e);
		c.addDependency(f);

		//making coffee depends on boiling water
		d.addDependency(a); 

		//cooking depends on a hot hob
		e.addDependency(b);

		//lets make toast a little later, so its not too cold when we eat
		f.addDependency(d);
		
		project = new Project_Data();
		project.getTasks().add(a);
		project.getTasks().add(b);
		project.getTasks().add(c);
		project.getTasks().add(d);
		project.getTasks().add(e);
		project.getTasks().add(f);
	}
	
	@Test
	public void testIterable() {
		
		//force checks to ensure all interface methods exist somewhere
		System.setProperty(DCIHelper.SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST, "true");

		BehaviourInjector bi = new BehaviourInjector(null); //can pass null, as no recursion is done here

		bi.setupRoleInterfaceToRoleImplMapping(IActivity_Role.class, Activity_Role.class);
		
		IIterable_Role<IActivity_Role> iter = 
			bi.getIterator(
					project.getTasks().iterator(), 
					IActivity_Role.class);

		StringBuilder sb = new StringBuilder();
		for(IActivity_Role a : iter){
			sb.append(a.getId());
		}
		
		assertEquals("ABCDEF", sb.toString());
	}
	
	@Test
	public void testNaming() {
		
		//force checks to ensure all interface methods exist somewhere
		System.setProperty(DCIHelper.SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST, "true");
		
		BehaviourInjector bi = new BehaviourInjector(null); //can pass null, as no recursion is done here
		
		bi.setupRoleInterfaceToRoleImplMapping(IActivity_Role.class, Activity_Role.class);
		
		IIterable_Role<IActivity_Role> iter = 
			bi.getIterator(
					project.getTasks().iterator(), 
					IActivity_Role.class, "ITERATOR");
		
		IActivity_Role taskG = bi.assignRole(new Task_Data("G"), IActivity_Role.class, "TASK_G");
		
		assertTrue(iter == bi.getObjectPlayingRole("ITERATOR"));
		assertTrue(taskG == bi.getObjectPlayingRole("TASK_G"));
		
		StringBuilder sb = new StringBuilder();
		for(IActivity_Role a : iter){
			sb.append(a.getId());
		}
		
		assertEquals("ABCDEF", sb.toString());
	}
	
	static class Project_Data {
		private List<Task_Data> tasks = new ArrayList<Task_Data>();
		public List<Task_Data> getTasks(){
			return tasks;
		}
	}

	static class Task_Data {

		public static final long MS_IN_ONE_MINUTE = 60000L;

		private String id;

		public Task_Data(String id) {
			this.id = id;
		}

		/** a list of tasks which this task depend upon */
		private List<Task_Data> dependencies = new ArrayList<Task_Data>();

		public String getId() {
			return id;
		}
		public List<Task_Data> getDependencies() {
			return dependencies;
		}
		public void addDependency(Task_Data t){
			dependencies.add(t);
		}
	}
	
	static class Activity_Role {

		public void frontloadFrom(Date projectStart){
			//doesnt need to do anything in this test!
		}

	}

	static interface IActivity_Role {
		void frontloadFrom(Date projectStart); //role method
		
		public String getId(); //from data object
	}
	
}
