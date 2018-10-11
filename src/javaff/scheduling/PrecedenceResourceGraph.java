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

package javaff.scheduling;

import javaff.data.Action;
import javaff.data.strips.InstantAction;
import javaff.data.metric.BinaryComparator;
import javaff.data.metric.ResourceOperator;
import javaff.data.metric.MetricSymbolStore;
import javaff.data.temporal.DurationFunction;
import javaff.data.temporal.DurativeAction;

import java.util.Map;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.math.BigDecimal;

// OK for new precedence relations (i.e. meetCosntraints) should move consumers
// to AFTER the >= etc..) (actually maybe no)
// AND for the new bounds should do incremental sweeps as in precedence
// relations

public class PrecedenceResourceGraph
{
	public Map operators = new Hashtable();
	public Map conditions = new Hashtable();
	public Map states = new Hashtable(); // Maps (Operators || Conditions =>
											// States)

	public MatrixSTN stn;

	public PrecedenceResourceGraph(MatrixSTN s)
	{
		stn = s;
	}

	public void addCondition(BinaryComparator bc, Action a)
	{
		conditions.put(bc, a);
	}

	public void addOperator(ResourceOperator ro, Action a)
	{
		operators.put(ro, a);
	}

	public boolean meetConditions()
	{
		boolean changed = false;
		Iterator bcit = conditions.keySet().iterator();
		while (bcit.hasNext())
		{
			BinaryComparator bc = (BinaryComparator) bcit.next();
			BigDecimal comp = bc.second.getValue(null);
			Action a = (Action) conditions.get(bc);

			if (bc.type == MetricSymbolStore.LESS_THAN
					|| bc.type == MetricSymbolStore.LESS_THAN_EQUAL)
			{
				BigDecimal value = findBeforeMin(a);

				if (value.compareTo(comp) >= 0)
				{
					// move an unordered consumer back
					Set u = getUnorderedConsumers(a);
					Action a2 = stn.getEarliest(u);
					stn.addConstraint(TemporalConstraint.getConstraint(
							(InstantAction) a2, (InstantAction) a));
					changed = true;
				}
			} else if (bc.type == MetricSymbolStore.GREATER_THAN
					|| bc.type == MetricSymbolStore.GREATER_THAN_EQUAL)
			{
				BigDecimal value = findBeforeMax(a);
				if (value.compareTo(comp) <= 0)
				{
					// move an unordered producer back
					Set u = getUnorderedProducers(a);
					Action a2 = stn.getEarliest(u);
					stn.addConstraint(TemporalConstraint.getConstraint(
							(InstantAction) a2, (InstantAction) a));
					changed = true;
				}
			}
		}
		return changed;
	}

	private BigDecimal findBeforeMax(Action a)
	{
		BigDecimal value = new BigDecimal(0);
		Iterator opit = operators.keySet().iterator();
		while (opit.hasNext())
		{
			ResourceOperator ro = (ResourceOperator) opit.next();
			Action a2 = (Action) operators.get(ro);
			if (ro.type == MetricSymbolStore.INCREASE
					|| ro.type == MetricSymbolStore.SCALE_UP)
			{
				if (stn.B(a2, a) || stn.BS(a2, a))
					value = ro.applyMax(value, stn); // WARNING This is not
														// taking into the
														// account the order of
														// the actions

			} else if (ro.type == MetricSymbolStore.DECREASE
					|| ro.type == MetricSymbolStore.SCALE_DOWN)
			{
				if (stn.B(a2, a))
					value = ro.applyMin(value, stn);
			}
		}
		return value;
	}

	private BigDecimal findBeforeMin(Action a)
	{
		BigDecimal value = new BigDecimal(0);
		Iterator opit = operators.keySet().iterator();
		while (opit.hasNext())
		{
			ResourceOperator ro = (ResourceOperator) opit.next();
			Action a2 = (Action) operators.get(ro);
			if (ro.type == MetricSymbolStore.INCREASE
					|| ro.type == MetricSymbolStore.SCALE_UP)
			{
				if (stn.B(a2, a))
					value = ro.applyMin(value, stn); // WARNING This is not
														// taking into the
														// account the order of
														// the actions
			} else if (ro.type == MetricSymbolStore.DECREASE
					|| ro.type == MetricSymbolStore.SCALE_DOWN)
			{
				if (stn.B(a2, a) || stn.BS(a2, a))
					value = ro.applyMax(value, stn);
			}
		}
		return value;
	}

	private Set getUnorderedProducers(Action a)
	{
		Set rSet = new HashSet();
		Iterator opit = operators.keySet().iterator();
		while (opit.hasNext())
		{
			ResourceOperator ro = (ResourceOperator) opit.next();
			Action a2 = (Action) operators.get(ro);
			if (ro.type == MetricSymbolStore.INCREASE
					|| ro.type == MetricSymbolStore.SCALE_UP)
			{
				if (stn.U(a2, a))
					rSet.add(a2);
			}
		}
		return rSet;
	}

	private Set getUnorderedConsumers(Action a)
	{
		Set rSet = new HashSet();
		Iterator opit = operators.keySet().iterator();
		while (opit.hasNext())
		{
			ResourceOperator ro = (ResourceOperator) opit.next();
			Action a2 = (Action) operators.get(ro);
			if (ro.type == MetricSymbolStore.DECREASE
					|| ro.type == MetricSymbolStore.SCALE_DOWN)
			{
				if (stn.U(a2, a))
					rSet.add(a2);
			}
		}
		return rSet;
	}

	private Set getBeforeOperators(Action a)
	{
		Set rSet = new HashSet();
		Iterator opit = operators.keySet().iterator();
		while (opit.hasNext())
		{
			ResourceOperator ro = (ResourceOperator) opit.next();
			Action a2 = (Action) operators.get(ro);
			rSet.add(ro);
		}
		return rSet;
	}

	public boolean limitBounds()
	{
		boolean change = false;
		Iterator bcit = conditions.keySet().iterator();
		while (bcit.hasNext())
		{
			BinaryComparator bc = (BinaryComparator) bcit.next();

			BigDecimal comp = bc.second.getValue(null);
			Action a = (Action) conditions.get(bc);

			if (bc.type == MetricSymbolStore.LESS_THAN
					|| bc.type == MetricSymbolStore.LESS_THAN_EQUAL)
			{
				BigDecimal value = findBeforeMax(a);
				if (value.compareTo(comp) > 0)
				{
					BigDecimal diff = value.subtract(comp);
					// change an before producers back
					Set u = getBeforeOperators(a);
					Iterator uit = u.iterator();
					while (uit.hasNext())
					{
						ResourceOperator ro = (ResourceOperator) uit.next();
						if (ro.change instanceof DurationFunction)
						{
							DurationFunction df = (DurationFunction) ro.change;
							DurativeAction da = df.durativeAction;
							if (ro.type == MetricSymbolStore.INCREASE
									|| ro.type == MetricSymbolStore.SCALE_UP)
								stn.decreaseMax(da, diff);
							else if (ro.type == MetricSymbolStore.DECREASE
									|| ro.type == MetricSymbolStore.SCALE_DOWN)
								stn.increaseMin(da, diff);
							change = true;
							break;
						}
					}
				}
			}

			else if (bc.type == MetricSymbolStore.GREATER_THAN
					|| bc.type == MetricSymbolStore.GREATER_THAN_EQUAL)
			{
				BigDecimal value = findBeforeMin(a);
				if (value.compareTo(comp) < 0)
				{
					BigDecimal diff = comp.subtract(value);
					// change an before producers back
					Set u = getBeforeOperators(a);
					Iterator uit = u.iterator();
					while (uit.hasNext())
					{
						ResourceOperator ro = (ResourceOperator) uit.next();
						if (ro.change instanceof DurationFunction)
						{
							DurationFunction df = (DurationFunction) ro.change;
							DurativeAction da = df.durativeAction;
							if (ro.type == MetricSymbolStore.INCREASE
									|| ro.type == MetricSymbolStore.SCALE_UP)
								stn.increaseMin(da, diff);
							else if (ro.type == MetricSymbolStore.DECREASE
									|| ro.type == MetricSymbolStore.SCALE_DOWN)
								stn.decreaseMax(da, diff);
							change = true;
							break;
						}
					}
				}
			}
		}
		return change;
	}

	public void minimize()
	{
		Iterator roit = operators.keySet().iterator();
		while (roit.hasNext())
		{
			ResourceOperator ro = (ResourceOperator) roit.next();
			if (ro.change instanceof DurationFunction)
			{
				DurativeAction da = ((DurationFunction) ro.change).durativeAction;
				if (ro.type == MetricSymbolStore.INCREASE
						|| ro.type == MetricSymbolStore.SCALE_UP)
				{
					stn.minimize(da);
				} else if (ro.type == MetricSymbolStore.DECREASE
						|| ro.type == MetricSymbolStore.SCALE_DOWN)
				{
					stn.maximize(da);
				}
			}
		}
	}

	public void maximize()
	{
		Iterator roit = operators.keySet().iterator();
		while (roit.hasNext())
		{
			ResourceOperator ro = (ResourceOperator) roit.next();
			if (ro.change instanceof DurationFunction)
			{
				DurativeAction da = ((DurationFunction) ro.change).durativeAction;
				if (ro.type == MetricSymbolStore.INCREASE
						|| ro.type == MetricSymbolStore.SCALE_UP)
				{
					stn.maximize(da);
				} else if (ro.type == MetricSymbolStore.DECREASE
						|| ro.type == MetricSymbolStore.SCALE_DOWN)
				{
					stn.minimize(da);
				}
			}
		}
	}

}
