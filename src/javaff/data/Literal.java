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

import javaff.data.strips.PredicateSymbol;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.PrintStream;

public abstract class Literal implements Fact
{
	protected static final HashSet EmptySet = new HashSet();
	
	protected PredicateSymbol name;
	protected List<Parameter> parameters; // list of Parameters

	private int hash;
	
	
	/**
	 * Empty constructor -- initialises name to Null.
	 */
	protected Literal()
	{
		this.name = null;
		this.parameters = new ArrayList<Parameter>();
		
		this.updateHash();
	}
	
	/**
	 * Basic comparator return value is determined by the natural alphabetical ordering of 
	 * this fact's signature.
	 * @return -1 if this fact is alphabetically before the other, 0 if they are the same, and +1 if it is after the other.
	 * @see #toString()
	 */
	@Override
	public int compareTo(Fact o)
	{
		return this.toString().compareTo(o.toString());
	}
	

	/**
	 * Hash lookups in the original JavaFF framework are terrifyingly slow, and make up 
	 * over 50% of processing time during a typical planning session. This is despite
	 * the fact that once a literal is created, it rarely changes its hash value.
	 * This method is intended for computing the hash when appropriate, such that
	 * a variable can be stored and returned when needed, speeding up execution. 
	 * @return
	 */
	protected int updateHash()
	{
		this.hash = 31 ^ this.parameters.hashCode();
		if (this.name != null)
			this.hash += this.name.hashCode();
		

//		if (this.name != null)
//			System.out.println("Updated Literal hash of \""+this.toString()+"\" to be "+hash);
//		else
//			System.out.println("Updated Literal hash to be "+hash);
		
		return this.hash;
	}
	
	@Override
	public int hashCode()
	{
		return this.hash;
	}
	
	
	public void setPredicateSymbol(PredicateSymbol n)
	{
		name = n;
		
		this.updateHash();
	}

	public PredicateSymbol getPredicateSymbol()
	{
		return name;
	}

	/**
	 * Returns an unmodifiable list of {@link Parameter}s.
	 * @return
	 */
	public List<Parameter> getParameters()
	{
		return Collections.unmodifiableList(parameters);
	}
	
	public void setParameters(List<Parameter> params)
	{
		this.parameters = params;
		
		this.updateHash();
	}

	public void addParameter(Parameter p)
	{
		parameters.add(p);
		
		this.updateHash();
	}

	public void addParameters(List<Parameter> l)
	{
		parameters.addAll(l);
		
		this.updateHash();
	}

	public String toString()
	{
		String stringrep = name.toString();
		Iterator i = parameters.iterator();
		while (i.hasNext())
		{
			stringrep = stringrep + " " + i.next();
		}
		return stringrep;
	}

	public String toStringTyped()
	{
		String stringrep = name.toString();
		Iterator i = parameters.iterator();
		while (i.hasNext())
		{
			Parameter o = (Parameter) i.next();
			stringrep += " " + o + " - " + o.type.toString();
		}
		return stringrep;
	}

	@Override
	public boolean isStatic()
	{
		return name.isStatic();
	}

	@Override
	public void setStatic(boolean value)
	{
		this.name.setStatic(value);
		
		this.updateHash();
	}
	
	public void PDDLPrint(PrintStream p, int indent)
	{
		PDDLPrinter.printToString(this, p, false, true, indent);
	}
	
	@Override
	public abstract Object clone();
}
