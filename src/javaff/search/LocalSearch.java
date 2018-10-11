//
//  LocalSearch.java
//  JavaFF
//
//  Created by Andrew Coles on Thu Jan 31 2008.
//

package javaff.search;

import javaff.planning.State;
import javaff.planning.Filter;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Comparator;
import java.math.BigDecimal;

import java.util.Hashtable;
import java.util.Iterator;

public class LocalSearch extends Search
{
	protected BigDecimal bestHValue;

	protected Hashtable closed;
	protected LinkedList open;
	protected SuccessorSelector selector = null;

	protected int depthBound = 10000;
	protected int restartBound = 10000;

	public LocalSearch(State s)
	{
		this(s, new HValueComparator());
	}

	public LocalSearch(State s, Comparator c)
	{
		super(s);
		setComparator(c);

		closed = new Hashtable();
		open = new LinkedList();
	}

	public void setFilter(Filter f)
	{
		filter = f;
	}

	public void setSuccessorSelector(SuccessorSelector s)
	{
		selector = s;
	}

	public void setDepthBound(int i)
	{
		depthBound = i;
	}

	public void setRestartBound(int r)
	{
		restartBound = r;
	}

	public State removeNext()
	{

		return (State) (open).removeFirst();
	}

	public boolean needToVisit(State s)
	{
		Integer Shash = new Integer(s.hashCode());
		State D = (State) closed.get(Shash);

		if (closed.containsKey(Shash) && D.equals(s))
			return false;

		closed.put(Shash, s);
		return true;
	}

	public State search()
	{

		if (start.goalReached())
		{ // wishful thinking
			return start;
		}

		needToVisit(start); // dummy call (adds start to the list of 'closed'
							// states so we don't visit it again

		open.add(start); // add it to the open list

		int currentDepth = 0;
		int currentRestarts = 0;

		State bestState = start;
		BigDecimal bestHValue = start.getHValue();
		Hashtable bestClosed = (Hashtable) closed.clone();

		while (!open.isEmpty()) // whilst still states to consider
		{
			State s = removeNext(); // get the next one

			Set successors = s.getNextStates(filter.getActions(s)); // and find
																	// its
																	// neighbourhood

			Set toChooseFrom = new HashSet();

			Iterator succItr = successors.iterator();

			while (succItr.hasNext())
			{
				State succ = (State) succItr.next();

				if (needToVisit(succ))
				{
					if (succ.goalReached())
					{ // if we've found a goal tmstate - return it as the solution
						return succ;
					}
					else
					{
						toChooseFrom.add(succ); // otherwise, add to the set of
												// successors to choose from
					}
				}
			}

			if (!toChooseFrom.isEmpty())
			{ // if the tmstate actually has any successors
				State chosenSuccessor = selector.choose(toChooseFrom); // choose
																		// one

				if (chosenSuccessor.getHValue().compareTo(bestHValue) < 0)
				{ // if this is a new 'best tmstate'
					currentDepth = 0; // reset the depth bound
					open.add(chosenSuccessor); // add this to the open list
					bestState = chosenSuccessor; // and note it's the best
					bestHValue = chosenSuccessor.getHValue();
					bestClosed = (Hashtable) closed.clone();
				}
				else
				{
					++currentDepth;
					if (currentDepth < depthBound)
					{
						open.add(chosenSuccessor);
					}
					else
					{
						++currentRestarts;
						if (currentRestarts == restartBound)
						{
							return null;
						}
						currentDepth = 0;
						closed = (Hashtable) bestClosed.clone();
						open.add(bestState);
					}
				}
			}

		}
		return null;
	}
}
