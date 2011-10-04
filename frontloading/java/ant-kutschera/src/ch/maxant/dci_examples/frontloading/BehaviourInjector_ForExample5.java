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
import java.util.List;

import ch.maxant.dci_examples.frontloading.FrontLoaderExample5.IActivity_Role;
import ch.maxant.dci_examples.frontloading.FrontLoaderExample5.IFrontLoader_Role;
import ch.maxant.dci_examples.frontloading.data.Project_Data;
import ch.maxant.dci_examples.frontloading.data.Task_Data;

/** what happens in here, is for the moment secret */
public class BehaviourInjector_ForExample5 {

	public IFrontLoader_Role assignRolesMagically(Project_Data project) {

		//map using wrappers.  we could map without wrappers, but then we would have to map back afterwards.
		//the problem with wrapping, is that it makes the context dependent upon the role.  in SOA, thats
		//not allowed, because the context is the caller, and is unknown to the service.
		//for demonstration purposes, this is however ok.
		FrontLoaderRole fl = new FrontLoaderRole();
		for(Task_Data t : project.getTasks()){
			IActivity_Role a = new ActivityRole(t);
			fl.getActivities().add(a);
		}
		return fl;
	}

	private static class FrontLoaderRole implements IFrontLoader_Role {
		private List<IActivity_Role> activities = new ArrayList<IActivity_Role>();

		@Override
		public List<IActivity_Role> getActivities() {
			return activities;
		}
	}
	
	private static class ActivityRole implements IActivity_Role {
		
		private Task_Data task;
		
		public ActivityRole(Task_Data task){
			this.task = task;
		}
		
		@Override
		public Date getEnd() {
			return task.getEnd();
		}
		
		@Override
		public List<IActivity_Role> getPredecessors() {
			List<IActivity_Role> ps = new ArrayList<IActivity_Role>();
			for(Task_Data t : task.getDependencies()){
				ps.add(new ActivityRole(t));
			}
			return ps;
		}
		
		@Override
		public boolean hasBeenPlanned() {
			return task.hasBeenPlanned();
		}

		@Override
		public String getId() {
			return task.getId();
		}
		
		@Override
		public void setStart(Date start) {
			task.setStart(start);
		}
		
		@Override
		public Date getStart() {
			return task.getStart();
		}
	}
}
