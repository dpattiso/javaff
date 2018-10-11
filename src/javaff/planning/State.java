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

package javaff.planning;

import javaff.data.Action;
import javaff.data.GroundFact;
import javaff.data.Plan;
import javaff.data.TotalOrderPlan;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public abstract class State implements Cloneable
{
	public GroundFact goal;
	public TotalOrderPlan plan;
	
	public State()
	{
		plan = new TotalOrderPlan(this.goal);
	}
	
	/**
	 * Returns the cost of the plan which achieved this state, or -1 if there is no plan associated 
	 * with this state.
	 * @return
	 */
	public BigDecimal getCost()
	{
		if (this.plan == null)
			return new BigDecimal(-1);
					
		return this.plan.getCost();
	}
	
	public Plan getPlan()
	{
		return plan;
	}
	

	public State getNextState(Action action) 
	{
		return this.apply(action);
	}


	public Set<State> getNextStates(Collection<Action> actions) // get all the states after applying
											// this set of actions
	{
		Set<State> rSet = new LinkedHashSet<State>(); //retain ordering -- helpful actions may be first
		for (Action a : actions)
		{
			rSet.add(this.getNextState(a));
		}
		return rSet;
	}

	public abstract State apply(Action a);

	public abstract BigDecimal getHValue();

	public abstract BigDecimal getGValue();

	public boolean goalReached()
	{
		return goal.isTrue(this);
	}

	public abstract Plan getSolution();

	public abstract Set<Action> getActions();

	public boolean checkAvailability(Action a) // put in for invariant checking
	{
		return true;
	}
	
	public abstract Object clone();
}
