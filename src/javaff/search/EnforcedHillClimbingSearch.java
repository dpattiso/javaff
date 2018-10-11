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

import javaff.JavaFF;
import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.Plan;
import javaff.data.TotalOrderPlan;
import javaff.data.strips.NullFact;
import javaff.data.strips.RelaxedFFPlan;
import javaff.planning.HelpfulFilter;
import javaff.planning.NullFilter;
import javaff.planning.STRIPSState;
import javaff.planning.State;
import javaff.planning.Filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.LinkedList;
import java.util.Comparator;
import java.math.BigDecimal;

import java.util.Hashtable;
import java.util.Iterator;

public class EnforcedHillClimbingSearch extends Search
{
	protected BigDecimal bestHValue;

	//cache the hash codes rather than the states for memory efficiency
	protected HashSet<Integer> closed;
	protected LinkedList<State> open;

	public EnforcedHillClimbingSearch(State s)
	{
		this(s, new HValueComparator());
	}

	public EnforcedHillClimbingSearch(State s, Comparator c)
	{
		super(s);
		setComparator(c);

		closed = new HashSet<Integer>();
		open = new LinkedList<State>();
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
		if (closed.contains(s.hashCode()))
			return false;

		closed.add(s.hashCode()); // otherwise put it on
		return true; // and return true
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
		bestHValue = start.getHValue(); // and take its heuristic value as the
										// best so far

		javaff.JavaFF.infoOutput.print(bestHValue+" into depth ");
		int statesEvaluated = 1;
		int maxDepth = 1;
		HashMap<State, Integer> successorLayers = new HashMap<State, Integer>();
		successorLayers.put(start, 1);
		
		int currentDepth = 1, prevDepth = 0;
		out: while (!open.isEmpty()) // whilst still states to consider
		{
			State curr = open.pop();
			closed.add(curr.hashCode());
			currentDepth = successorLayers.get(curr);
			if (currentDepth > maxDepth)
				maxDepth = currentDepth;
			
			if (currentDepth > prevDepth)
			{
				JavaFF.infoOutput.print("["+(currentDepth)+"]");
				prevDepth = currentDepth;
			}
			
			List<Action> applicable = filter.getActions(curr);
			in: for (Action a : applicable)
			{
				State succ = curr.getNextState(a);
				
				if (this.closed.contains(succ.hashCode()) == true)
				{
					continue in;
				}
				++statesEvaluated;
				
				BigDecimal succH = succ.getHValue();
				//check we have no entered a dead-end
				if (((STRIPSState) succ).getRelaxedPlan() == null)
					continue;
				
				//now do online goal-ordering check -- this is used by FF to prevent deleting goals early in the 
				//relaxed plan, which would then need negated again later on
				boolean threatensGoal = this.isGoalThreatened(((STRIPSState) succ).getRelaxedPlan(), succ.goal);
				if (threatensGoal)
				{
//					closed.add(succ); //The real FF says that this state should be "removed" from the state-space -- we just skip it
					continue; //skip successor state
				}
				
				if (succ.goalReached())
				{ // if we've found a goal state -
					// return it as the
					// solution
					JavaFF.infoOutput.println("\nEvaluated "+statesEvaluated+" states to a max depth of "+maxDepth);
					
					return succ;
				}
				else if (succH.compareTo(bestHValue) < 0)
				{
					// if we've found a state with a better heuristic
					// value
					// than the best seen so far

					bestHValue = succH; // note the new best value
					open.clear();
					
					open.add(succ); // add it to the open list
					successorLayers.clear();
					successorLayers.put(succ, 1);
					prevDepth = 0;
					currentDepth = 1;
					
					JavaFF.infoOutput.print("\n"+bestHValue+" into depth ");
					
					continue out; // and skip looking at the other successors
				}
				else
				{
					open.add(succ); // add it to the open list
					successorLayers.put(succ, currentDepth + 1);
//					prevDepth = currentDepth;
				}
			}
			
		}
		JavaFF.infoOutput.println();
		
		return null;
	}

	/**
	 * Tests whether any of the actions in the RELAXED plan associated with this state
	 * delete a goal.
	 * @param relaxedPlan The relaxed plan which will be used to detect goal orderings
	 * @param goal The goal to check
	 * @return
	 */
	private boolean isGoalThreatened(Plan relaxedPlan, Fact goal)
	{
		//maintain a list of achieved goals as we go through the RP in-order. These will
		//be checked at each timestep to see if any already achieved goals are deleted.
		HashSet<Fact> achieved = new HashSet<Fact>();
		List<Action> actions = relaxedPlan.getActions();
		
		
		for (Action a : actions)
		{
			for (Fact g : goal.getFacts())
			{
				//if this action deletes a goal and that goal has already been achieved by 
				//a previous action in the RP, immediately return
				if (a.deletes(g) && achieved.contains(g))
					return true;
			}
			
			achieved.addAll(a.getAddPropositions());
			achieved.addAll(a.getDeletePropositions()); //needed for ADL goals
		}
		
		return false;
	}
}
