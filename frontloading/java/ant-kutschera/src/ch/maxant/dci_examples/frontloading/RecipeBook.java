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

import ch.maxant.dci_examples.frontloading.data.Recipe;
import ch.maxant.dci_examples.frontloading.data.RecipeStep;

public final class RecipeBook {

	private RecipeBook() {}

	public static Recipe getBreakfast() {
		RecipeStep a = new RecipeStep("Boil water", 5);
		RecipeStep b = new RecipeStep("Turn hob on and wait for it to heat up", 3);
		RecipeStep c = new RecipeStep("Eat", 12);
		RecipeStep d = new RecipeStep("Make coffee", 1);
		RecipeStep e = new RecipeStep("Cook bacon & eggs", 7);
		RecipeStep f = new RecipeStep("Make toast", 3);

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
		
		Recipe project = new Recipe("Breakfast");
		project.getSteps().add(a);
		project.getSteps().add(b);
		project.getSteps().add(c);
		project.getSteps().add(d);
		project.getSteps().add(e);
		project.getSteps().add(f);
		
		return project;
	}
	
	public static String outputRecipe(Recipe recipe) {
		RecipeStep firstTask = recipe.getFirstStep();
		long duration = recipe.getLastStep().getEnd().getTime() - firstTask.getStart().getTime();
		duration /= RecipeStep.MS_IN_ONE_MINUTE;
		StringBuilder sb = new StringBuilder();
		sb.append("Project ").append(recipe.getName()).append(".  Duration: " + duration + " mins.\r\n");
		sb.append("\t----------------> time axis\r\n");
		for(int i = 0; i < duration + 2; i++){
			sb.append(" |");
		}
		sb.append("\r\n");
		for(RecipeStep t : recipe.getSteps()){
			long minsFromStart = t.getStart().getTime() - firstTask.getStart().getTime();
			minsFromStart /= RecipeStep.MS_IN_ONE_MINUTE;
			for(int i = 0; i < minsFromStart; i++){
				sb.append("  ");
			}
			for(int i = 0; i < t.getEstimatedMinutes(); i++){
				sb.append("XX");
			}
			sb.append("\tTask ").append(t.getId()).append(": ").append(t.getEstimatedMinutes());
			long startDay = t.getStart().getTime() - firstTask.getStart().getTime();
			startDay /= RecipeStep.MS_IN_ONE_MINUTE;
			sb.append(" mins").append(", starts after ").append(startDay).append(" mins\r\n");
		}
		return sb.toString();
	}

	/*
	 * A user command is |frontloadFrom(projectStartWeek)| A _FrontloadContext
	 * _has four Roles: |_CurrentContext_|, _|Frontloader|_. _|Activity|_,
	 * _|Predecessors|_.
	 * 
	 * An _|Activity |_role method looks something like this | *frontloadFrom
	 * (projectStartWeek)* ... // set my earlyStart to the maximum of
	 * projectStartWeek and the earlyFinish of all predecessors
	 * 
	 * |The _|Frontloader|_| |role method is the main loop: |*fronloadFrom
	 * (projectStartWeek)* {_CurrentContext_.reselectObjectsForRoles. _Activity
	 * _is not nil } whileTrue {_Activity_.frontloadFrom (projectStartWeek) }
	 * 
	 * |This very simple loop runs until no |_Activity_ can be |selected for
	 * frontloading.
	 * 
	 * The interesting part is the |reselectObjectsForRoles |method in the
	 * |Context|: |_CurrentContext_ := self. _Frontloader_ := self //or any
	 * other object, its role method is stateless _Activity_ := ... // select an
	 * _Activity_ that has not been planned // AND all its predecessors have
	 * been planned _Predecessors_ := _Activity_.predecessors // depends on the
	 * Data implementation. // (Note that the object for this Role is a
	 * collection) The result of the _Activity_ selection will change as new
	 * Activties are being planned. |
	 */
	
}
