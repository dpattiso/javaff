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

import javaff.data.temporal.DurativeAction;
import javaff.data.Fact;
import javaff.data.GroundProblem;
import javaff.data.Action;
import javaff.data.Plan;
import javaff.data.RelaxedPlan;
import javaff.data.TotalOrderPlan;
import javaff.data.strips.Proposition;
import javaff.data.temporal.StartInstantAction;
import javaff.data.temporal.DurationFunction;
import javaff.data.metric.Function;
import javaff.planning.PlanningGraph.PGAction;
import javaff.planning.PlanningGraph.PGNoOp;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.math.BigDecimal;

public class RelaxedTemporalMetricPlanningGraph extends
		RelaxedMetricPlanningGraph
{
	public RelaxedTemporalMetricPlanningGraph(GroundProblem gp)
	{
		super(gp);
	}
	
	protected RelaxedTemporalMetricPlanningGraph(RelaxedTemporalMetricPlanningGraph tmrpg)
	{
		super(tmrpg);
	}
	
	//FIXME add clone() and default constructor

	protected void resetAll(State S)
	{
		super.resetAll(S);
		super.setGoal(S.goal);
		addEndActionGoals((TemporalMetricState) S);
	}

	protected void addEndActionGoals(TemporalMetricState S)
	{
		Iterator dait = S.openActions.iterator();
		while (dait.hasNext())
		{
			DurativeAction da = (DurativeAction) dait.next();
			Proposition p = da.dummyGoal;
			goal.add(getPGFact(p));
		}
	}

	public RelaxedPlan getPlan(State s)// if on the start action is in also add the end
								// action as a matter of course
	{
		RelaxedPlan p = super.getPlan(s);
		if (p != null)
		{
			Set acts = new HashSet(p.getActions());
			Iterator lit = acts.iterator();
			while (lit.hasNext())
			{
				Action a = (Action) lit.next();
				if (a instanceof StartInstantAction)
				{
					StartInstantAction sa = (StartInstantAction) a;
					if (!acts.contains(sa.getSibling()))
						p.addAction(sa.getSibling());
				}
			}
		}

		return p;
	}

	protected PGFunction makeFunction(Function f)
	{
		PGFunction pgf = super.makeFunction(f);
		if (pgf == null && f instanceof DurationFunction)
			pgf = new PGDurationFunction(((DurationFunction) f).durativeAction);
		return pgf;
	}

	protected class PGDurationFunction extends PGNamedFunction
	{
		public DurativeAction durAct;

		public PGDurationFunction(DurativeAction da)
		{
			durAct = da;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins)
		{
			return durAct.durationConstraint.getMaxDuration(null);
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins)
		{
			return durAct.durationConstraint.getMinDuration(null);
		}

		public int hashcode()
		{
			return durAct.hashCode();
		}

		public boolean effectedBy(PGResourceOperator ro)
		{
			return false;
		}

		public boolean increase(PGResourceOperator ro)
		{
			return false;
		}

		public boolean decrease(PGResourceOperator ro)
		{
			return false;
		}
	}
}
