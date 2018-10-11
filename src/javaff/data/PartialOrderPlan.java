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

import javaff.data.strips.Proposition;
import javaff.data.strips.InstantAction;
import javaff.data.temporal.SplitInstantAction;
import javaff.scheduling.TemporalConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;

public class PartialOrderPlan implements Plan
{
	public Map strictOrderings = new Hashtable();
	public Map equalOrderings = new Hashtable();
	public Set actions = new HashSet();
	
	private Fact goal;

	public PartialOrderPlan(Fact goal)
	{
		this.setGoal(goal);
	}
	
	@Override
	public Plan clone()
	{
		PartialOrderPlan clone = new PartialOrderPlan((Fact) this.goal.clone());
		clone.strictOrderings = new Hashtable(this.strictOrderings);
		clone.equalOrderings = new Hashtable(this.equalOrderings);
		clone.actions = new HashSet(this.actions);
		
		return clone;
	}
	
	
	/**
	 * Returns the sum of all action-costs in this plan.
	 */
	@Override
	public BigDecimal getCost()
	{
		BigDecimal cost = new BigDecimal(0);
		for (Action a : this)
		{
			cost = cost.add(a.getCost());
		}
		
		return cost;
	}
	
	@Override
	public Iterator<Action> iterator()
	{
		return this.getActions().iterator();
	}
	
	@Override
	public Fact getGoal()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setGoal(Fact g)
	{
		// TODO Auto-generated method stub
		
	}
	
	public int getActionCount()
	{
		return this.actions.size();
	}
	
	/**
	 * @see #getActionCount()
	 */
	@Override
	public int getPlanLength()
	{
		return this.getActionCount();
	}

	public void addStrictOrdering(Action first, Action second)
	{
		Set ord = null;
		Object o = strictOrderings.get(first);
		if (o == null)
		{
			ord = new HashSet();
			strictOrderings.put(first, ord);
		} else
			ord = (HashSet) o;
		ord.add(second);
		actions.add(first);
		actions.add(second);
	}

	public void addEqualOrdering(Action first, Action second)
	{
		Set ord = null;
		Object o = equalOrderings.get(first);
		if (o == null)
		{
			ord = new HashSet();
			equalOrderings.put(first, ord);
		} else
			ord = (HashSet) o;
		ord.add(second);
		actions.add(first);
		actions.add(second);
	}

	public void addOrder(Action first, Action second, Proposition p)
	{
		if (first instanceof SplitInstantAction)
		{
			SplitInstantAction sa = (SplitInstantAction) first;
			if (!sa.exclusivelyInvariant(p))
			{
				addEqualOrdering(first, second);
				return;
			}

		}

		if (second instanceof SplitInstantAction)
		{
			SplitInstantAction sa = (SplitInstantAction) second;
			if (!sa.exclusivelyInvariant(p))
			{
				addEqualOrdering(first, second);
				return;
			}
		}

		addStrictOrdering(first, second);

	}


	public boolean addAction(Action a)
	{
		boolean res = actions.add(a);
		strictOrderings.put(a, new HashSet());
		equalOrderings.put(a, new HashSet());
		return res;
	}

	public void addActions(Set s)
	{
		Iterator sit = s.iterator();
		while (sit.hasNext())
			addAction((Action) sit.next());
	}

	public List<Action> getActions()
	{
		return new ArrayList<Action>(actions);
	}

	public Set getTemporalConstraints()
	{
		Set rSet = new HashSet();
		Iterator ait = actions.iterator();
		while (ait.hasNext())
		{
			Action a = (Action) ait.next();

			Set ss = (HashSet) strictOrderings.get(a);
			Iterator sit = ss.iterator();
			while (sit.hasNext())
			{
				Action b = (Action) sit.next();
				rSet.add(TemporalConstraint.getConstraint((InstantAction) a,
						(InstantAction) b));
			}

			Set es = (HashSet) equalOrderings.get(a);
			Iterator eit = es.iterator();
			while (eit.hasNext())
			{
				Action b = (Action) eit.next();
				rSet.add(TemporalConstraint.getConstraintEqual(
						(InstantAction) a, (InstantAction) b));
			}
		}
		return rSet;

	}

	public void print(PrintStream p)
	{
		Iterator sit = actions.iterator();
		while (sit.hasNext())
		{
			Action a = (Action) sit.next();
			p.println(a);
			p.println("\tStrict Orderings: " + strictOrderings.get(a));
			p.println("\tLess than or equal Orderings: "
					+ equalOrderings.get(a));
		}
	}

	public void print(PrintWriter p)
	{
		Iterator sit = actions.iterator();
		while (sit.hasNext())
		{
			Action a = (Action) sit.next();
			p.println(a);
			p.println("\tStrict Orderings: " + strictOrderings.get(a));
			p.println("\tLess than or equal Orderings: "
					+ equalOrderings.get(a));
		}
	}
}
