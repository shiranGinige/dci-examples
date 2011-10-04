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
import java.util.Date;
import java.util.List;

public class RecipeStep {

	public static final long MS_IN_ONE_MINUTE = 60000L;

	private Date start;
	private String id;
	private int estimatedMinutes;

	public RecipeStep(String id, int estimatedMinutes) {
		this.id = id;
		this.estimatedMinutes = estimatedMinutes;
	}

	/** a list of tasks which this task depend upon */
	private List<RecipeStep> dependencies = new ArrayList<RecipeStep>();

	public String getId() {
		return id;
	}
	public List<RecipeStep> getDependencies() {
		return dependencies;
	}
	public void addDependency(RecipeStep t){
		dependencies.add(t);
	}
	public Date getStart(){
		return start;
	}
	public void setStart(Date start){
		this.start = start;
	}
	public Date getEnd(){
		return new Date(start.getTime() + (MS_IN_ONE_MINUTE * estimatedMinutes));
	}
	public int getEstimatedMinutes() {
		return estimatedMinutes;
	}
}

