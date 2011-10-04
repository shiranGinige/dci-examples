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
package ch.maxant.dci.util;

import java.util.Iterator;

/** pretty much an iterator, for iterating */
/* package */class Iterable_Role<T> implements Iterator<T> {

	@CurrentContext
	private Context currentContext;

	@DataObject
	private Iterator<T> dataObject;

	private Class<? extends Object> type;

	public Iterator<T> iterator() {
		return this;
	}

	@SuppressWarnings("unchecked")
	public T next() {
		T t = dataObject.next();

		//need to cast, because it doesnt know that type is of type T :-)
		T nextActivity = (T) currentContext.assignRole(t, type); 

		return nextActivity;
	}

	@Override
	public boolean hasNext() {
		return dataObject.hasNext();
	}

	@Override
	public void remove() {
		dataObject.remove();
	}

	public void setType(Class<? extends Object> type) {
		this.type = type;
	}

}
