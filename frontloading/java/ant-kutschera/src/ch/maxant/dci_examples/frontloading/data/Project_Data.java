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

/** a data class.  projects contain tasks, which need planning. */
public class Project_Data {
	private List<Task_Data> tasks = new ArrayList<Task_Data>();
	private String name;
	public Project_Data(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public List<Task_Data> getTasks(){
		return tasks;
	}

	/** somewhat complex, but its ok to keep this in the data object - its local */
	public Task_Data getLastTask(){
		Task_Data last = null;
		for(Task_Data t : tasks){
			if(last == null){
				last = t;
			}else{
				if(t.getEnd().after(last.getEnd())){
					last = t;
				}
			}
		}
		return last;
	}

	/** somewhat complex, but its ok to keep this in the data object - its local */
	public Task_Data getFirstTask(){
		Task_Data first = null;
		for(Task_Data t : tasks){
			if(first == null){
				first = t;
			}else{
				if(t.getStart().before(first.getStart())){
					first = t;
				}
			}
		}
		return first;
	}
}

