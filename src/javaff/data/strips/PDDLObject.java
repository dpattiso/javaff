/************************************************************************
 * Strathclyde Planning Group,
 * Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK
 * http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey
 * Copyright 2008, Andrew Coles and Amanda Smith
 * Copyright 2015, David Pattison
 *
 * This file is part of JavaFF.
 * 
 * JavaFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JavaFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JavaFF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ************************************************************************/

package javaff.data.strips;

import javaff.data.Type;

public class PDDLObject extends javaff.data.Parameter
{
	public PDDLObject(String n)
	{
		super(n);
	}

	public PDDLObject(String n, Type t)
	{
		super(n, t);
	}

	public int hashCode()
	{
		int hash = 10;
		hash = 31 * hash ^ name.hashCode();
		hash = 31 * hash ^ type.hashCode();
		return hash;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof PDDLObject && super.equals(obj);
	}
	
	@Override
	public Object clone()
	{
		PDDLObject cloned = new PDDLObject(this.name, this.type);
		
		return cloned;
	}
}
