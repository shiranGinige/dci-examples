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
package ch.maxant.dci_examples.frontloading;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.maxant.dci_examples.frontloading.FrontLoaderExample6Recursion.IFrontLoader_Role;
import ch.maxant.dci_examples.frontloading.FrontLoaderExample6Recursion.IActivity_Role;
import ch.maxant.dci_examples.frontloading.data.Project_Data;
import ch.maxant.dci_examples.frontloading.data.Task_Data;

/** what happens in here, is for the moment also a secret */
public class BehaviourInjector_ForExample6Recursion {

	public IFrontLoader_Role assignRoles(Project_Data project) {

		//map.
		//it is IMPERATIVE, that the roles know nothing about the data objects.  data objects
		//are in a different world than these service objects.
		Map<String, Task_Data> task_index = new HashMap<String, Task_Data>();
		Map<String, IActivity_Role> activity_index = new HashMap<String, IActivity_Role>();
		FrontLoaderRole fl = new FrontLoaderRole();
		for(Task_Data t : project.getTasks()){
			IActivity_Role a = new ActivityRole(t.getId(), t.getEstimatedMinutes());
			fl.getActivities().add(a);

			activity_index.put(t.getId(), a);
			task_index.put(t.getId(), t);
		}
		
		//now map dependencies
		for(IActivity_Role a : fl.getActivities()){
			Task_Data t = task_index.get(((ActivityRole)a).getId());
			for(Task_Data t2 : t.getDependencies()){
				a.getPredecessors().add(activity_index.get(t2.getId()));
			}
		}
		
		return fl;
	}

	public void unassignRoles(Project_Data project, IFrontLoader_Role frontLoader) {
		for(IActivity_Role activity : frontLoader.getActivities()){
			ActivityRole ar = (ActivityRole)activity;
			for(Task_Data task : project.getTasks()){
				if(ar.getId().equals(task.getId())){
					task.setStart(ar.getStart());
					break;
				}
			}
		}
	}

	private static class FrontLoaderRole implements IFrontLoader_Role {
		private List<IActivity_Role> activities = new ArrayList<IActivity_Role>();

		@Override
		public List<IActivity_Role> getActivities() {
			return activities;
		}
	}
	
	private static class ActivityRole implements IActivity_Role {
		
		private static final long MS_IN_ONE_MINUTE = 60000L;
		private Date start;
		private int duration;
		private String id;
		private List<IActivity_Role> predecessors = new ArrayList<IActivity_Role>();
		
		public ActivityRole(String id, int duration) {
			this.id = id;
			this.duration = duration;
		}

		public Date getStart() {
			return start;
		}

		@Override
		public Date getEnd() {
			return new Date(start.getTime() + (MS_IN_ONE_MINUTE * duration));
		}
		
		@Override
		public List<IActivity_Role> getPredecessors() {
			return predecessors;
		}
		
		@Override
		public boolean hasBeenPlanned() {
			//planned is true, if a start date has been set.
			return start != null;
		}
		
		@Override
		public void setStart(Date start) {
			this.start = start;
		}
		
		public String getId(){
			return id;
		}
	}
}
