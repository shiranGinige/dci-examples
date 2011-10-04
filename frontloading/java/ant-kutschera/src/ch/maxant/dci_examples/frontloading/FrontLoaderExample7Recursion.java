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

import ch.maxant.dci_examples.frontloading.data.Project_Data;

/**
 * This example of the front loader uses a service based execution model...
 * 
 * @see http
 *      ://groups.google.com/group/object-composition/browse_thread/thread/9e2
 *      b3e28803fca44/aa8a2d7aa2f4a7cb
 */
public class FrontLoaderExample7Recursion {

	public static class FrontLoad_Context {

		private Project_Data project;
		private Mapper_ForExample7Recursion mapper = new Mapper_ForExample7Recursion();

		public FrontLoad_Context(Project_Data project){
			this.project = project;
		}
		
		public void frontLoadFrom(Date projectStart){
			
			//casts the project into a front loader TO.
			//at the same time, it casts all tasks into activity TOs.
			//that is ok, because in the context of frontloading, we know that 
			//a project consists of activities!
			IFrontLoaderTO frontLoaderTO = mapper.mapFromProject(project);
			
			FrontLoaderService.frontloadFrom(projectStart, frontLoaderTO.getActivities()); 
			
			//in SOA we also unmap / merge
			mapper.mergeResults(project, frontLoaderTO);
		}
		
	}

	/** role impl for front loader, applicable to the project */
	public static class FrontLoaderService {

		public static void frontloadFrom(Date projectStart, List<IActivityTO> currentActivities) {

			for(IActivityTO activity : currentActivities){
				
				if(activity.hasBeenPlanned()) continue;

				frontloadFrom(projectStart, activity.getPredecessors());

				//finally we can plan this task, because all predecessors are now planned
				ActivityService.frontloadFrom(projectStart, activity);
			}
		}

	}
	
	/** methodless role (interface) for front loader, applicable to project */
	public static interface IFrontLoaderTO {
		List<IActivityTO> getActivities();
	}

	/** role impl for activity, applicable to tasks */
	public static class ActivityService {

		public static void frontloadFrom(Date projectStart, IActivityTO activity) {
			// set my earlyStart to the maximum of projectStart and
			// the earlyFinish of all predecessors
			Date earliest = projectStart;
			for(IActivityTO predecessor : activity.getPredecessors()){
				if(predecessor.getEnd().after(earliest)){
					earliest = predecessor.getEnd();
				}
			}
			activity.setStart(earliest);
		}
	}

	/** methodless role (interface) for activity, applicable to task */
	public static interface IActivityTO {

		/** a list of activities, which must come before this one */
		List<IActivityTO> getPredecessors();
		
		public boolean hasBeenPlanned();
		Date getEnd();
		void setStart(Date start);
	}

	/** tester method */
	public static void main(String[] args) {

		Project_Data project = ProjectHelper.setupProject();
		
		FrontLoad_Context ctx = new FrontLoad_Context(project);
		ctx.frontLoadFrom(new Date());
		System.out.println("Done: " + ProjectHelper.outputPlannedProject(project));
	}
}
