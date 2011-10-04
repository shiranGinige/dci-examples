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


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class BehaviourInjectorTest3 {

	private Project_Data project;
	private StringBuffer sb;
	private BehaviourInjector bi;
	
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
		
		sb = new StringBuffer();
	}
	
	@Test
	public void testRecursion() {
		
		//force checks to ensure all interface methods exist somewhere
		System.setProperty(DCIHelper.SYSTEM_PROPERTY_CHECK_METHODS_DURING_CAST, "true");

		bi = new BehaviourInjector(this);

		bi.setupRoleInterfaceToRoleImplMapping(IActivity_Role.class, Activity_Role.class);
		
		bi.getIterator(
					project.getTasks().iterator(), 
					IActivity_Role.class,
					"ACTIVITIES");

		recurse();
	
		assertEquals("ABDEFC", sb.toString());
	}

	@SuppressWarnings("unchecked")
	@Recursable
	public void recurse(){
		
		IIterable_Role<IActivity_Role> activities = (IIterable_Role<IActivity_Role>) bi.getObjectPlayingRole("ACTIVITIES");
		for(IActivity_Role activity : activities){
			
			if(activity.isPlanned()) continue;
			
			
			bi.prepareForRecurse();
			bi.getIterator(
					activity.getDependencies().iterator(), 
					IActivity_Role.class, 
					"ACTIVITIES");
			bi.recurse();
			
			sb.append(activity.getId());

			activity.setPlanned();
		}
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
		private boolean isPlanned;

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
		public boolean isPlanned() {
			return isPlanned;
		}
		public void setPlanned() {
			this.isPlanned = true;
		}
	}
	
	static class Activity_Role {

		public void frontloadFrom(Date projectStart){
			//doesnt need to do anything in this test!
		}

	}

	static interface IActivity_Role {
		void frontloadFrom(Date projectStart); //role method
		
		List<Task_Data> getDependencies(); //from data object
		String getId(); //from data object
		boolean isPlanned(); //from data object
		void setPlanned(); //from data object
	}
	
}
