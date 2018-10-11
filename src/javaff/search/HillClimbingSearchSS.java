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

package javaff.search;

import javaff.planning.State;
import javaff.planning.Filter;

import java.util.Set;
import java.util.LinkedList;
import java.util.Comparator;
import java.math.BigDecimal;

import java.util.Hashtable;

public class HillClimbingSearchSS extends Search
{
	//protected BigDecimal bestHValue;
	protected SuccessorSelector successorSelector;

	protected Hashtable closed;
	protected LinkedList open;
	
	private int maxDepth;

	public HillClimbingSearchSS(State s, int maxDepth, SuccessorSelector ss)
	{
		this(s, new HValueComparator(), maxDepth, ss);
	}

	public HillClimbingSearchSS(State s, Comparator c, int maxDepth, SuccessorSelector ss)
	{
		super(s);
		setComparator(c);

		closed = new Hashtable();
		open = new LinkedList();
		
		this.maxDepth = maxDepth;
		this.successorSelector = ss;
	}
	
	public int getMaxDepth()
	{
		return maxDepth;
	}
	
	public void setMaxDepth(int d)
	{
		this.maxDepth = d;
	}

	public void setFilter(Filter f)
	{
		filter = f;
	}

	public State removeNext()
	{

		return (State) (open).removeFirst();
	}

	public boolean needToVisit(State s)
	{
		Integer Shash = new Integer(s.hashCode()); // compute hash for tmstate
		State D = (State) closed.get(Shash); // see if its on the closed list

		if (closed.containsKey(Shash) && D.equals(s))
			return false; // if it is return false

		closed.put(Shash, s); // otherwise put it on
		return true; // and return true
	}

	public State search()
	{

		if (start.goalReached())
		{ // wishful thinking
			return start;
		}
		

		int depth = 0;
		needToVisit(start); // dummy call (adds start to the list of 'closed'
							// states so we don't visit it again

		open.add(start); // add it to the open list


		while (!open.isEmpty()) // whilst still states to consider
		{		
			if (depth >= this.maxDepth)
				return null;
			
			BigDecimal bestHValue = new BigDecimal(Integer.MAX_VALUE); // and take its heuristic value as the
															// best so far

			//javaff.JavaFF.infoOutput.println(bestHValue);
		
			State s = removeNext(); // get the next one
			if (s.goalReached())
				return s;
//System.out.println("s is "+s);
			Set successors = s.getNextStates(filter.getActions(s));
			open.add(this.successorSelector.choose(successors));
			
			depth++;
		}
		return null;
	}

	public SuccessorSelector getSuccessorSelector()
	{
		return successorSelector;
	}

	public void setSuccessorSelector(SuccessorSelector successorSelector)
	{
		this.successorSelector = successorSelector;
	}
}
