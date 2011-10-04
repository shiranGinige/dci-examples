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

import ch.maxant.dci_examples.frontloading.FrontLoaderExample8.IFrontLoaderTO;
import ch.maxant.dci_examples.frontloading.FrontLoaderExample8.IActivityTO;
import ch.maxant.dci_examples.frontloading.data.Recipe;
import ch.maxant.dci_examples.frontloading.data.RecipeStep;

/** what happens in here, is for the moment also a secret */
public class Mapper {

	public FrontLoaderTO mapFrom(Recipe recipe) {

		//map.
		//it is IMPERATIVE, that the roles know nothing about the data objects.  data objects
		//are in a different world than these service objects.
		
		//for recursive problems, mappings are damn complicated, because the TOs need to reflect the 
		//graph of the object model :-(
		
		Map<String, RecipeStep> recipeStepIndex = new HashMap<String, RecipeStep>();
		Map<String, IActivityTO> activityIndex = new HashMap<String, IActivityTO>();
		FrontLoaderTO fl = new FrontLoaderTO();
		for(RecipeStep rs : recipe.getSteps()){
			IActivityTO a = new ActivityTO(rs.getId(), rs.getEstimatedMinutes());
			fl.getActivities().add(a);

			activityIndex.put(rs.getId(), a);
			recipeStepIndex.put(rs.getId(), rs);
		}
		
		//now map dependencies
		for(IActivityTO a : fl.getActivities()){
			RecipeStep rs = recipeStepIndex.get(((ActivityTO)a).getId());
			for(RecipeStep dependency : rs.getDependencies()){
				a.getPredecessors().add(activityIndex.get(dependency.getId()));
			}
		}
		
		return fl;
	}

	public void mapBack(Recipe recipe, IFrontLoaderTO frontLoader) {
		for(IActivityTO activity : frontLoader.getActivities()){
			ActivityTO ato = (ActivityTO)activity;
			for(RecipeStep step : recipe.getSteps()){
				if(ato.getId().equals(step.getId())){
					step.setStart(ato.getStart());
					break;
				}
			}
		}
	}

	private static class FrontLoaderTO implements IFrontLoaderTO {
		private List<IActivityTO> activities = new ArrayList<IActivityTO>();

//		@Override
		public List<IActivityTO> getActivities() {
			return activities;
		}
	}
	
	private static class ActivityTO implements IActivityTO {
		
		private static final long MS_IN_ONE_MINUTE = 60000L;
		private Date from;
		private int duration;
		private String id;
		private List<IActivityTO> predecessors = new ArrayList<IActivityTO>();
		
		public ActivityTO(String id, int duration) {
			this.id = id;
			this.duration = duration;
		}

		public Date getStart() {
			return from;
		}

//		@Override
		public Date getUntil() {
			return new Date(from.getTime() + (MS_IN_ONE_MINUTE * duration));
		}
		
//		@Override
		public List<IActivityTO> getPredecessors() {
			return predecessors;
		}
		
//		@Override
		public boolean hasBeenPlanned() {
			//planned is true, if a start date has been set.
			return from != null;
		}
		
//		@Override
		public void setFrom(Date from) {
			this.from = from;
		}
		
		public String getId(){
			return id;
		}
	}
}
