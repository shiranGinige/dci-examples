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
package ch.maxant.dci_examples.frontloading.data;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
	private List<RecipeStep> steps = new ArrayList<RecipeStep>();
	private List<Ingredient> ingredients = new ArrayList<Ingredient>();
	private String name;
	public Recipe(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public List<RecipeStep> getSteps(){
		return steps;
	}
	public List<Ingredient> getIngredients() {
		return ingredients;
	}
	/** somewhat complex, but its ok to keep this in the data object - its local */
	public RecipeStep getLastStep(){
		RecipeStep last = null;
		for(RecipeStep s : steps){
			if(last == null){
				last = s;
			}else{
				if(s.getEnd().after(last.getEnd())){
					last = s;
				}
			}
		}
		return last;
	}

	/** somewhat complex, but its ok to keep this in the data object - its local */
	public RecipeStep getFirstStep(){
		RecipeStep first = null;
		for(RecipeStep s : steps){
			if(first == null){
				first = s;
			}else{
				if(s.getStart().before(first.getStart())){
					first = s;
				}
			}
		}
		return first;
	}
	
}

