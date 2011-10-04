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

import ch.maxant.dci.util.BehaviourInjector;
import ch.maxant.dci.util.Context;
import ch.maxant.dci.util.CurrentContext;
import ch.maxant.dci.util.Self;
import ch.maxant.dci_examples.frontloading.data.Project_Data;
import ch.maxant.dci_examples.frontloading.data.Task_Data;

/**
 * This example of the front loader uses iteration, and creates a new context each time
 * that a task needs to act as an activity.  As such, there is no need for the {@link CurrentContext}
 * in the frontloading role played by the project.
 * 
 * @see http
 *      ://groups.google.com/group/object-composition/browse_thread/thread/9e2
 *      b3e28803fca44/aa8a2d7aa2f4a7cb
 */
public class FrontLoaderExample2 {

	public static class FrontLoad_Context {

		private Project_Data project;
		private BehaviourInjector bi = new BehaviourInjector(this);

		public FrontLoad_Context(Project_Data project){
			this.project = project;
		}
		
		public void frontLoadFrom(Date projectStart){

			//casts the project into a front loader role, so it can plan itself.
			//to do so, it depends on activities.  the casting from task->activity is
			//done inside the role method, using a new context.
			IFrontLoader_Role fl = bi.assignRole(
					project, 
					FrontLoader_Role.class, 
					IFrontLoader_Role.class);
			
			fl.frontloadFrom(projectStart);
		}
		
	}

	/** role impl for front loader role, taken on by the project */
	public static class FrontLoader_Role {

		@Self
		private IFrontLoader_Role self;
		
		public void frontloadFrom(Date projectStart) {

			//select an activity which has not been planned
			while(true){
				boolean plannedAll = true;
				for(Task_Data task : self.getTasks()){
					if(!task.hasBeenPlanned()){
						if(!hasUnplannedPredecessors(task)){
							
							//plan the task using a sub-context!
							new Activity_Context(task).plan(projectStart);

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

		private boolean hasUnplannedPredecessors(Task_Data task) {
			//does this task have any predecessors who are unplanned? if so, it cannot be planned
			boolean hasUnplannedPredecessor = false;
			for(Task_Data p : task.getDependencies()){
				if(!p.hasBeenPlanned()){
					hasUnplannedPredecessor = true;
					break;
				}
			}
			return hasUnplannedPredecessor;
		}
	}
	
	/** methodless role (interface) for front loader */
	public static interface IFrontLoader_Role {

		void frontloadFrom(Date projectStart);

		List<Task_Data> getTasks();
		
	}

	/** this is the context in charge of making tasks act as activities to do something. */
	public static class Activity_Context {

		private Task_Data task;
		private BehaviourInjector bi = new BehaviourInjector(this);

		public Activity_Context(Task_Data task){
			this.task = task;
		}
		
		public void plan(Date projectStart){

			//casts the task into the activity role so it can plan itself
			IActivity_Role activity = bi.assignRole(
					task, 
					Activity_Role.class, 
					IActivity_Role.class);
			
			activity.frontloadFrom(projectStart);
		}
		
	}

	/** role impl for activity role, taken on by the tasks */
	public static class Activity_Role {

		@Self
		private IActivity_Role self;

		@CurrentContext
		private Context currentContext;
		
		public void frontloadFrom(Date projectStart) {
			// set my earlyStart to the maximum of projectStart and
			// the earlyFinish of all predecessors
			Date earliest = projectStart;
			for(IActivity_Role predecessor : getPredecessorsList()){
				if(predecessor.getEnd().after(earliest)){
					earliest = predecessor.getEnd();
				}
			}
			self.setStart(earliest);
		}
		
		public List<IActivity_Role> getPredecessorsList(){
			//not so nice: activity role cant get the list of predecessors in the right role, 
			//it needs to cast each object into the role!  it does that, with the help of the context
			//i find this wierd, because casting object into roles is the contexts job, not the roles.
			//but hey, i guess i am using the context to do it, so its not so bad...
			List<IActivity_Role> predecessors = new ArrayList<IActivity_Role>();
			
			for(Task_Data t : self.getDependencies()){
				predecessors.add(currentContext.assignRole(t, IActivity_Role.class));
			}
			return predecessors;
		}
	}

	/** methodless role (interface) for activity */
	public static interface IActivity_Role {
		void frontloadFrom(Date projectStart);
		List<Task_Data> getDependencies();
		Date getEnd();
		Date getStart();
		void setStart(Date start);
	}

	/** tester method */
	public static void main(String[] args) {
		
		Task_Data a = new Task_Data("Boil water", 5);
		Task_Data b = new Task_Data("Turn hob on and wait for it to heat up", 3);
		Task_Data c = new Task_Data("Eat", 12);
		Task_Data d = new Task_Data("Make coffee", 1);
		Task_Data e = new Task_Data("Cook bacon & eggs", 7);
		Task_Data f = new Task_Data("Make toast", 3);

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
		
		Project_Data project = new Project_Data("Cook breakfast");
		project.getTasks().add(a);
		project.getTasks().add(b);
		project.getTasks().add(c);
		project.getTasks().add(d);
		project.getTasks().add(e);
		project.getTasks().add(f);
		
		FrontLoad_Context ctx = new FrontLoad_Context(project);
		ctx.frontLoadFrom(new Date());
		System.out.println("Done: " + outputPlannedProject(project));
	}

	private static String outputPlannedProject(Project_Data project) {
		Task_Data firstTask = project.getFirstTask();
		long duration = project.getLastTask().getEnd().getTime() - firstTask.getStart().getTime();
		duration /= Task_Data.MS_IN_ONE_MINUTE;
		StringBuilder sb = new StringBuilder();
		sb.append("Project ").append(project.getName()).append(".  Duration: " + duration + " mins.\r\n");
		sb.append("\t----------------> time axis\r\n");
		for(int i = 0; i < duration + 2; i++){
			sb.append(" |");
		}
		sb.append("\r\n");
		for(Task_Data t : project.getTasks()){
			long minsFromStart = t.getStart().getTime() - firstTask.getStart().getTime();
			minsFromStart /= Task_Data.MS_IN_ONE_MINUTE;
			for(int i = 0; i < minsFromStart; i++){
				sb.append("  ");
			}
			for(int i = 0; i < t.getEstimatedMinutes(); i++){
				sb.append("XX");
			}
			sb.append("\tTask ").append(t.getId()).append(": ").append(t.getEstimatedMinutes());
			long startDay = t.getStart().getTime() - firstTask.getStart().getTime();
			startDay /= Task_Data.MS_IN_ONE_MINUTE;
			sb.append(" mins").append(", starts after ").append(startDay).append(" mins\r\n");
		}
		return sb.toString();
	}

}
