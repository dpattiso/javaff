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

package javaff.data;

import javaff.data.strips.SimpleType;
import java.io.PrintStream;

public abstract class Parameter implements PDDLPrintable
{
	protected String name;
	protected Type type;

	public Parameter(String n)
	{
		setName(n);
		setRootType();
	}
	
	public abstract Object clone();

	public Parameter(String n, Type t)
	{
		setName(n);
		setType(t);
	}

	public void setName(String n)
	{
		name = n;
	}

	public void setType(Type t)
	{
		type = t;
	}

	public void setRootType()
	{
		type = SimpleType.rootType;
	}

	public void setDetails(String n, Type t)
	{
		setName(n);
		setType(t);
	}

	public String getName()
	{
		return name;
	}

	public Type getType()
	{
		return type;
	}

	public boolean isOfType(Type t) // is this of Type t
	{
		return type.isOfType(t);
	}

	public String toString()
	{
		return name;
	}

	public String toStringTyped()
	{
		return name + " - " + type.toString();
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof Parameter)
		{
			Parameter po = (Parameter) obj;
			return (name.equals(po.name) && type.equals(po.type) && this
					.getClass() == po.getClass());
		} else
			return false;
	}

	public void PDDLPrint(PrintStream p, int indent)
	{
		PDDLPrinter.printToString(this, p, false, false, indent);
	}
}
