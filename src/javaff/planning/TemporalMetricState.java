/*******************************************************************************
 * Strathclyde Planning Group, Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey Copyright 2008, Andrew Coles and Amanda Smith
 * 
 * (Questions/bug reports now to be sent to Andrew Coles)
 * 
 * This file is part of JavaFF.
 * 
 * JavaFF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * JavaFF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * JavaFF. If not, see <http://www.gnu.org/licenses/>.
 * 
 ******************************************************************************/

// WARNING - This State does deal with Metric Invariants
package javaff.planning;

import javaff.data.GroundFact;
import javaff.data.TotalOrderPlan;
import javaff.data.Action;
import javaff.data.metric.Metric;
import javaff.data.strips.InstantAction;
import javaff.data.temporal.StartInstantAction;
import javaff.data.temporal.SplitInstantAction;
import javaff.scheduling.SchedulabilityChecker;
import javaff.scheduling.VelosoSchedulabilityChecker;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Hashtable;

// import java.math.BigDecimal;

public class TemporalMetricState extends MetricState
{
	public Set openActions; // Set of (DurativeActions)
	public List invariants; // Set of Propositions (Propositions)
	public SchedulabilityChecker checker;

	protected TemporalMetricState()
	{
		super();
		openActions = new HashSet(0);
		invariants = new ArrayList(0);
		checker = null;
	}
	
	protected TemporalMetricState(Set a, Set f, GroundFact g, Map funcs,
			TotalOrderPlan p, Metric m, Set oAc, List i)
	{
		super(a, f, g, funcs, p, m);
		openActions = oAc;
		invariants = i;
	}

	public TemporalMetricState(Set a, Set f, GroundFact g, Map funcs,
			Metric m)
	{
		super(a, f, g, funcs, m);
		openActions = new HashSet();
		invariants = new ArrayList();
		checker = new VelosoSchedulabilityChecker();

	}

	public boolean goalReached()
	{
		return (openActions.isEmpty() && super.goalReached());
	}

	public boolean checkAvailability(Action a)
	{
		List rList = new ArrayList(invariants);
		if (a instanceof SplitInstantAction)
		{
			SplitInstantAction da = (SplitInstantAction) a;
			Iterator iit = da.parent.invariant.getFacts()
					.iterator();
			while (iit.hasNext())
			{
				rList.remove(iit.next());
			}
		}
		rList.retainAll(a.getDeletePropositions());
		if (!rList.isEmpty())
			return false;
		SchedulabilityChecker c = (VelosoSchedulabilityChecker) checker.clone(); // This
																					// should
																					// have
																					// to
																					// be
																					// cloned
																					// here
																					// and
																					// below
		boolean result = c.addAction((InstantAction) a, this);
		if (result && a instanceof StartInstantAction)
		{
			TemporalMetricState dupli = (TemporalMetricState) this.clone();
			dupli.apply(a);
			result = c.addAction(((StartInstantAction) a).getSibling(), dupli);

		}
		return result;
	}

	public Object clone()
	{
		//FIXME needs updated
		Set nf = (Set) ((HashSet) factsTrue).clone();
		TotalOrderPlan p = (TotalOrderPlan) plan.clone();
		Map nfuncs = (Map) ((Hashtable) funcValues).clone();
		Set oA = (Set) ((HashSet) openActions).clone();
		List i = (List) ((ArrayList) invariants).clone();
		Set na = (Set) ((HashSet) actions).clone();
		TemporalMetricState ts = new TemporalMetricState(na, nf, goal, nfuncs,
				p, metric, oA, i);
		ts.setRPG(RPG);
		// ts.setFilter(filter);
		ts.checker = (VelosoSchedulabilityChecker) checker.clone();
		return ts;
	}

	public State apply(Action planScheduleAction) // return a cloned copy
	{
		TemporalMetricState s = (TemporalMetricState) super.apply(planScheduleAction);
		if (planScheduleAction instanceof SplitInstantAction)
		{
			SplitInstantAction sia = (SplitInstantAction) planScheduleAction;
			sia.applySplit(s);
		}
		s.checker.addAction((InstantAction) planScheduleAction, s);
		return s;
	}

	// public BigDecimal getGValue()
	// {
	// return super.getGValue().subtract(new BigDecimal(openActions.size()));
	// }
}
