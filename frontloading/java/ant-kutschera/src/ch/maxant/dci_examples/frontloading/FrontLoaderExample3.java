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

import java.util.Date;
import java.util.List;

import ch.maxant.dci.util.BehaviourInjector;
import ch.maxant.dci.util.Context;
import ch.maxant.dci.util.CurrentContext;
import ch.maxant.dci.util.IIterable_Role;
import ch.maxant.dci.util.Self;
import ch.maxant.dci_examples.frontloading.data.Project_Data;
import ch.maxant.dci_examples.frontloading.data.Task_Data;

/**
 * This example of the frontloader uses the {@link CurrentContext} to create an object playing the
 * role of an iterator capable of iteratiting over objects of the required type, in this 
 * case {@link Activity_Role}.  This example, like {@link FrontLoaderExample1}, uses iteration only.
 * 
 * @see http
 *      ://groups.google.com/group/object-composition/browse_thread/thread/9e2
 *      b3e28803fca44/aa8a2d7aa2f4a7cb
 */
public class FrontLoaderExample3 {

	public static class FrontLoad_Context {

		private Project_Data project;
		private BehaviourInjector bi = new BehaviourInjector(this);

		public FrontLoad_Context(Project_Data project){
			this.project = project;
		}
		
		public void frontLoadFrom(Date projectStart){

			//casts the project into a front loader role, so it can plan itself.
			//to do so, it depends on activities.  the casting from task->activity is
			//done inside the role method, using the current context.
			IFrontLoader_Role fl = bi.assignRole(
					project, 
					FrontLoader_Role.class, 
					IFrontLoader_Role.class);

			//additional info that will be useful to the context
			bi.setupRoleInterfaceToRoleImplMapping(IActivity_Role.class, Activity_Role.class);
			
			fl.frontloadFrom(projectStart);
		}
		
	}

	/** role impl for front loader, applicable to the project */
	public static class FrontLoader_Role {

		@Self
		private IFrontLoader_Role self;
		
		@CurrentContext
		private Context currentContext;
		
		public void frontloadFrom(Date projectStart) {

			//select an activity which has not been planned
			while(true){
				boolean plannedAll = true;
				IIterable_Role<IActivity_Role> activities = getActivities();
				for(IActivity_Role activity : activities){
					if(!activity.hasBeenPlanned()){
						if(!hasUnplannedPredecessors(activity)){
							activity.frontloadFrom(projectStart);
						}else{
							//gotta wait until predecessors are planned :-(
							plannedAll = false;
						}
					}
				}
				if(plannedAll){
					break;
				}
			}
		}

		/** casts the list self.tasks into a list of activities */
		private IIterable_Role<IActivity_Role> getActivities() {
			IIterable_Role<IActivity_Role> iter = 
				currentContext.getIterator(
						self.getTasks().iterator(), 
						IActivity_Role.class);
			return iter;
		}

		private boolean hasUnplannedPredecessors(IActivity_Role activity) {
			//does this task have any predecessors who are unplanned? if so, it cannot be planned
			boolean hasUnplannedPredecessor = false;
			for(IActivity_Role a : activity.getPredecessorsIterator()){
				if(!a.hasBeenPlanned()){
					hasUnplannedPredecessor = true;
					break;
				}
			}
			return hasUnplannedPredecessor;
		}
	}
	
	/** methodless role (interface) for front loader, applicable to project */
	public static interface IFrontLoader_Role {

		void frontloadFrom(Date projectStart);

		List<Task_Data> getTasks();
		
	}

	/** role impl for activity, applicable to tasks */
	public static class Activity_Role {

		@Self
		private IActivity_Role self;

		@CurrentContext
		private Context currentContext;
		
		public void frontloadFrom(Date projectStart) {
			// set my earlyStart to the maximum of projectStart and
			// the earlyFinish of all predecessors
			Date earliest = projectStart;
			IIterable_Role<IActivity_Role> predecessors = getPredecessorsIterator();
			while(predecessors.hasNext()){
				IActivity_Role predecessor = predecessors.next();
				if(predecessor.getEnd().after(earliest)){
					earliest = predecessor.getEnd();
				}
			}
			self.setStart(earliest);
		}

		/** casts the list self.dependencies from task_data into a list of activities */
		public IIterable_Role<IActivity_Role> getPredecessorsIterator(){
			IIterable_Role<IActivity_Role> iter = currentContext.getIterator(
					self.getDependencies().iterator(), IActivity_Role.class);
			return iter;
		}
	}

	/** methodless role (interface) for activity, applicable to task */
	public static interface IActivity_Role {
		void frontloadFrom(Date projectStart); //role method
		
		/** a list of activities, which must come before self */
		IIterable_Role<IActivity_Role> getPredecessorsIterator(); //role method
		
		/** tasks which this activity depends on */
		List<Task_Data> getDependencies(); //from data object

		public boolean hasBeenPlanned(); //from data object
		Date getEnd(); //from data object
		Date getStart(); //from data object
		void setStart(Date start); //from data object
	}

	/** tester method */
	public static void main(String[] args) {
		
		Project_Data project = ProjectHelper.setupProject();
		
		FrontLoad_Context ctx = new FrontLoad_Context(project);
		ctx.frontLoadFrom(new Date());
		System.out.println("Done: " + ProjectHelper.outputPlannedProject(project));
	}

}
