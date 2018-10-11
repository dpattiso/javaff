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

import javaff.data.GroundFact;
import javaff.data.TotalOrderPlan;
import javaff.data.metric.Metric;
import javaff.data.metric.MetricType;
import javaff.data.metric.NamedFunction;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Hashtable;
import java.math.BigDecimal;

public class MetricState extends STRIPSState
{
	public Map funcValues; // maps Named Functions onto BigDecimals
	public Metric metric;

	protected MetricState()
	{
		super();
		this.funcValues = new HashMap(1);
		this.metric = null;
	}

	public MetricState(Set a, Set f, GroundFact g, Map funcs, Metric m)
	{
		super(a, f, g);
		funcValues = funcs;
		metric = m;
	}

	protected MetricState(Set a, Set f, GroundFact g, Map funcs,
			TotalOrderPlan p, Metric m)
	{
		super(a, f, g, p);
		funcValues = funcs;
		metric = m;
	}

	public Object clone()
	{
		
		//FIXME this needs to clone the supertype
		Set nf = (Set) ((HashSet) factsTrue).clone();
		TotalOrderPlan p = (TotalOrderPlan) plan.clone();
		Map nfuncs = (Map) ((Hashtable) funcValues).clone();
		MetricState ms = new MetricState(actions, nf, goal, nfuncs, p, metric);
		ms.setRPG(RPG);
		// ms.setFilter(filter);
		return ms;
	}

	public BigDecimal getValue(NamedFunction nf)
	{
		return (BigDecimal) funcValues.get(nf);
	}

	public void setValue(NamedFunction nf, BigDecimal d)
	{
		funcValues.put(nf, d);
	}

	// WARNING - not yet implemented - must be overridden and take account of
	// the metric
	//10/11/12 - dpattiso - Did I write this comment? Seems to make sense. No FF-style bounds checking in place?
	//not that this is really the place to even be getting H values...
	public BigDecimal getHValue()
	{
		return super.getHValue();
	}

	public BigDecimal getGValue()
	{
		return super.getGValue();
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof MetricState)
		{
			MetricState s = (MetricState) obj;
			return (s.factsTrue.equals(factsTrue) && s.funcValues.equals(funcValues));
		} else
			return false;
	}

	public int hashCode()
	{
		int hash = 8;
		hash = 31 * hash ^ factsTrue.hashCode();
		hash = 31 * hash ^ funcValues.hashCode();
		return hash;
	}

}
