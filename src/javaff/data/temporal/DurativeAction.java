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

package javaff.data.temporal;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.GroundFact;

import javaff.data.strips.Not;
import javaff.data.strips.Proposition;
import javaff.planning.State;
import javaff.planning.MetricState;

import java.util.Set;
import java.util.Map;
import java.math.BigDecimal;

public class DurativeAction extends Action
{
	public DurationFunction duration;

	public DurationConstraint durationConstraint;

	public GroundFact startCondition;
	public GroundFact endCondition;
	public GroundFact invariant;

	public GroundFact startEffect;
	public GroundFact endEffect;

	public SplitInstantAction startAction;
	public SplitInstantAction endAction;

	public Proposition dummyJoin;
	public Proposition dummyGoal;

	public DurativeAction()
	{
		duration = new DurationFunction(this);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode(); //TODO compute a real hash code for this
	}
	
	@Override
	public Object clone()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean staticDuration()
	{
		return durationConstraint.staticDuration();
	}

	public BigDecimal getDuration(MetricState ms)
	{
		return durationConstraint.getDuration(ms);
	}

	public BigDecimal getMaxDuration(MetricState ms)
	{
		return durationConstraint.getMaxDuration(ms);
	}

	public BigDecimal getMinDuration(MetricState ms)
	{
		return durationConstraint.getMinDuration(ms);
	}

	// WARNING these methods may not work correctly. Only the instant actions
	// should be worked with
	public boolean isApplicable(State s)
	{
		return startAction.isApplicable(s);
	}

	public void apply(State s)
	{
		startAction.apply(s);
		endAction.apply(s);
	}

	public Set getPreconditions()
	{
		Set rSet = startAction.getPreconditions();
		rSet.addAll(endAction.getPreconditions());
		return rSet;
	}

	public Set<Fact> getAddPropositions()
	{
		Set rSet = startAction.getAddPropositions();
		rSet.addAll(endAction.getAddPropositions());
		return rSet;
	}

	public Set<Not> getDeletePropositions()
	{
		Set rSet = startAction.getDeletePropositions();
		rSet.addAll(endAction.getDeletePropositions());
		return rSet;
	}

	public Set getComparators()
	{
		Set rSet = startAction.getComparators();
		rSet.addAll(endAction.getComparators());
		return rSet;
	}

	public Set getOperators()
	{
		Set rSet = startAction.getOperators();
		rSet.addAll(endAction.getOperators());
		return rSet;
	}

	public void staticify(Map fValues)
	{
		startCondition = startCondition.staticify();
		startEffect = startEffect.staticify();
		invariant = invariant.staticify();
		endCondition = endCondition.staticify();
		endEffect = endEffect.staticify();

		startAction.staticify(fValues);
		endAction.staticify(fValues);
	}

}
