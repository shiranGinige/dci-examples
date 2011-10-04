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

import ch.maxant.dci_examples.frontloading.data.Recipe;

/**
 * This example of the front loader uses the services execution model...
 * 
 * @see http
 *      ://groups.google.com/group/object-composition/browse_thread/thread/9e2
 *      b3e28803fca44/aa8a2d7aa2f4a7cb
 */
public class FrontLoaderExample8 {

	public static class KitchenService {

		public static void assembleMeal(Recipe breakfast, Date projectStart){
			
			// ... check ingredients in stock
			
			//map from kitchen world, into project planning world
			Mapper mapper = new Mapper();
			IFrontLoaderTO frontLoaderTO = mapper.mapFrom(breakfast);

			//do planning
			ProjectPlanningService.frontloadFrom(projectStart, frontLoaderTO.getActivities()); 
			
			//merge results so we can display them
			mapper.mapBack(breakfast, frontLoaderTO);
			
			// ... create order

			// ... restock, etc
		}
		
	}

	public static class ProjectPlanningService {

		public static void frontloadFrom(Date projectStart, List<IActivityTO> currentActivities) {

			for(IActivityTO activity : currentActivities){
				
				if(activity.hasBeenPlanned()) continue;

				frontloadFrom(projectStart, activity.getPredecessors());

				//finally we can plan this task, because all predecessors are now planned
				frontloadFrom(projectStart, activity);
			}
		}

		private static void frontloadFrom(Date projectStart, IActivityTO activity) {
			// set my earlyStart to the maximum of projectStart and
			// the earlyFinish of all predecessors
			Date earliest = projectStart;
			for(IActivityTO predecessor : activity.getPredecessors()){
				if(predecessor.getUntil().after(earliest)){
					earliest = predecessor.getUntil();
				}
			}
			activity.setFrom(earliest);
		}
	}
	
	/** methodless role (interface) for front loader, applicable to project */
	public static interface IFrontLoaderTO {
		List<IActivityTO> getActivities();
	}

	/** methodless role (interface) for activity, applicable to task */
	public static interface IActivityTO {

		/** a list of activities, which must come before this one */
		List<IActivityTO> getPredecessors();
		
		public boolean hasBeenPlanned();
		Date getUntil();
		void setFrom(Date from);
	}

	/** tester method */
	public static void main(String[] args) {

		Recipe breakfast = RecipeBook.getBreakfast();
		
		KitchenService.assembleMeal(breakfast, new Date());
		System.out.println("Done: " + RecipeBook.outputRecipe(breakfast));
	}
}
