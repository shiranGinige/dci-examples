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
import ch.maxant.dci.util.Recursable;
import ch.maxant.dci.util.Self;
import ch.maxant.dci_examples.frontloading.data.Project_Data;
import ch.maxant.dci_examples.frontloading.data.Task_Data;

/**
 * This example of the front loader uses recursion.  To recurse, the {@link CurrentContext}
 * is used, in order to 1) prepare for recursion, 2) setup recursion, and finally 3) do the recursion.
 * 
 * @see http
 *      ://groups.google.com/group/object-composition/browse_thread/thread/9e2
 *      b3e28803fca44/aa8a2d7aa2f4a7cb
 */
public class FrontLoaderExample4 {

	public static class FrontLoad_Context {

		private static final String ACTIVITIES = "activities";
		private Project_Data project;
		private BehaviourInjector bi = new BehaviourInjector(this);
		private Date projectStart;
		private IFrontLoader_Role frontLoader;

		public FrontLoad_Context(Project_Data project){
			this.project = project;
		}
		
		public void frontLoadFrom(Date projectStart){
			
			this.projectStart = projectStart;

			//casts the project into a front loader role, so it can plan itself.
			frontLoader = bi.assignRole(
					project, 
					FrontLoader_Role.class, 
					IFrontLoader_Role.class);
			
			//add the iterator to the context with the name ACTIVITIES
			bi.getIterator(
						project.getTasks().iterator(), 
						IActivity_Role.class,
						ACTIVITIES);

			//help the injector know which implementation to choose (saves code later on)
			bi.setupRoleInterfaceToRoleImplMapping(IActivity_Role.class, Activity_Role.class);
			
			recurse();
		}
		
		@Recursable
		public void recurse(){
			@SuppressWarnings("unchecked")
			IIterable_Role<IActivity_Role> currentActivities = 
						(IIterable_Role<IActivity_Role>) bi.getObjectPlayingRole(ACTIVITIES);
			frontLoader.frontloadFrom(projectStart, currentActivities); //start the interaction
		}
		
	}

	/** role impl for front loader, applicable to the project */
	public static class FrontLoader_Role {

		@CurrentContext
		private Context currentContext;
		
		public void doFrontloadingFrom(Date projectStart, IIterable_Role<IActivity_Role> currentActivities) {

			for(IActivity_Role activity : currentActivities){
				
				if(activity.hasBeenPlanned()) continue;
				
				//plan the predecessors first! do it recursively by 
				//using the context to re-cast
				currentContext.prepareForRecurse();
				
				//tell the context that the kids are now playing the role of the iterator
				//ie recast the role in the role-map
				currentContext.getIterator(
						activity.getDependencies().iterator(), 
						IActivity_Role.class, 
						FrontLoad_Context.ACTIVITIES);
				
				//step into context with new role-map
				currentContext.recurse();

				//finally we can plan this task, because all predecessors are now planned
				activity.frontloadFrom(projectStart);
			}
		}
	}
	
	/** methodless role (interface) for front loader, applicable to project */
	public static interface IFrontLoader_Role {

		void frontloadFrom(Date projectStart, IIterable_Role<IActivity_Role> currentActivities);
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
