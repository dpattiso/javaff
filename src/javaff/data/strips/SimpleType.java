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

public class SimpleType extends Type
{
	protected String name;
	protected Type superType;

	protected SimpleType()
	{
	}

	public SimpleType(String pName)
	{
		name = pName;
		superType = rootType;
	}

	public SimpleType(String pName, Type pSuperType)
	{
		name = pName;
		superType = pSuperType;
	}

	public void setSuperType(Type pSuperType)
	{
		superType = pSuperType;
	}

	public String toString()
	{
		return name;
	}

	public String toStringTyped()
	{
		return name + " - " + superType;
	}
	
	public Object clone()
	{
		String name = this.name;
		Type superTypeClone = Type.rootType;
		if (this.superType != null)
			superTypeClone = (Type) this.superType.clone();
		
		return new SimpleType(name, superTypeClone);
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof SimpleType)
		{
			SimpleType ty = (SimpleType) obj;
			return (name.equals(ty.name));
		} else
			return false;
	}

	public int hashCode()
	{
		return 31 * 8 + name.hashCode();
	}

	public boolean isOfType(Type t) // is this of type t (i.e. is type further
									// up the hierarchy)
	{
		if (this.equals(t))
			return true;
		else
			return superType.isOfType(t);
	}
}
