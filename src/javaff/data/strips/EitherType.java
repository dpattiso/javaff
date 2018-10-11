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
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class EitherType extends Type
{
	protected Set<SimpleType> types = new HashSet<SimpleType>();

	public void addType(SimpleType t)
	{
		types.add(t);
	}
	
	public Object clone()
	{
		EitherType clone = new EitherType();
		
		for (SimpleType t : this.types)
		{
			clone.addType((SimpleType) t.clone());
		}
		
		
		return clone;
	}

	public String toString()
	{
		String str = "(either";
		Iterator tit = types.iterator();
		while (tit.hasNext())
		{
			str += " " + tit.next();
		}
		str += ")";
		return str;
	}

	public String toStringTyped()
	{
		return toString();
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof EitherType)
		{
			EitherType et = (EitherType) obj;
			return (types.equals(et.types));
		} else
			return false;
	}

	public boolean isOfType(Type t) // is this of type t (i.e. is type further
									// up the hierarchy)
	{
		Iterator tit = types.iterator();
		while (tit.hasNext())
		{
			SimpleType st = (SimpleType) tit.next();
			if (st.isOfType(t))
				return true;
		}
		return false;
	}

}
