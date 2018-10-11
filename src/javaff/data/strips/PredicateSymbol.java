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

import javaff.data.PDDLPrinter;
import javaff.data.PDDLPrintable;
import javaff.data.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.io.PrintStream;

public class PredicateSymbol implements PDDLPrintable
{
	protected String name;
	protected boolean staticValue;

	protected List<Parameter> params;
	
	private int hash;

	protected PredicateSymbol()
	{
		this.name = "";
		this.staticValue = false;
		this.params = new ArrayList<Parameter>();
		
		this.hash = this.updateHash();
	}

	public PredicateSymbol(String pName)
	{
		this.name = pName;
		this.staticValue = false;
		this.params = new ArrayList<Parameter>();
		
		this.hash = this.updateHash();
	}
	
	protected int updateHash()
	{
		int hash = 31 ^ this.getName().hashCode() ^ this.getParameters().hashCode();
//		if (this.isStatic()) //breaks things!
//			hash ^= 37;
		
		this.hash = hash;
		return hash;
	}
	
	public String getName()
	{
		return name;
	}

	public List<Parameter> getParameters()
	{
		return Collections.unmodifiableList(this.params);
	}
	
	public Object clone()
	{
		PredicateSymbol clone = new PredicateSymbol();
		clone.name = this.name;
		clone.params = new ArrayList<Parameter>(this.params);
		clone.staticValue = this.staticValue;
		
		return clone;
	}

	public String toString()
	{
		return name;
	}

	public String toStringTyped()
	{
		String str = name;
		Iterator<Parameter> it = params.iterator();
		while (it.hasNext())
		{
			Variable v = (Variable) it.next();
			str += " " + v.toStringTyped();
		}
		return str;
	}

	public boolean isStatic()
	{
		return staticValue;
	}

	public void setStatic(boolean stat)
	{
		staticValue = stat;
		this.updateHash();
	}
	
	public void setName(String name)
	{
		this.name = name;
		this.updateHash();
	}

	public void addVar(Variable v)
	{
		params.add(v);
		this.updateHash();
	}

	public boolean removeVar(Variable v)
	{
		boolean res = params.remove(v);
		this.updateHash();
		return res;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof PredicateSymbol)
		{
//			PredicateSymbol ps = (PredicateSymbol) obj;
//			boolean eq = (name.equalsIgnoreCase(ps.name) && params.equals(ps.params));
			
			boolean h = this.hashCode() == obj.hashCode();
//			assert(h == eq);
			
			return h;
		} else
			return false;
	}

	public int hashCode()
	{
		return this.hash;
	}

	public void PDDLPrint(PrintStream p, int indent)
	{
		PDDLPrinter.printToString(this, p, true, true, indent);
	}
}
