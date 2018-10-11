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

import javaff.data.Fact;
import javaff.data.GroundProblem;
import javaff.data.GroundFact;
import javaff.data.metric.MetricSymbolStore;
import javaff.data.metric.NamedFunction;
import javaff.data.metric.BinaryComparator;
import javaff.data.metric.ResourceOperator;
import javaff.data.metric.Function;
import javaff.data.metric.BinaryFunction;
import javaff.data.metric.NumberFunction;
import javaff.data.strips.Not;
import javaff.planning.PlanningGraph.PGAction;
import javaff.planning.PlanningGraph.PGNoOp;

import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

public class RelaxedMetricPlanningGraph extends RelaxedPlanningGraph
{
	protected Set metricGoal = new HashSet();

	protected List minResources = null; // List of Maps [(PGFunction =>
										// BigDecimal)]
	protected List maxResources = null;

	protected Map PGFuncMap = new Hashtable(); // (NamedFunction =>
												// PGNamedFunction)
	protected Map ActionComparators = new Hashtable(); // (PGAction =>
														// (PGBinaryComparator))
	protected Map ActionOperators = new Hashtable(); // (PGAction =>
														// (PGFunctionOperator))

	// Used during graph construction
	private Set resOps = null; // PGResourceOperators that are in the graph

	private boolean functionsInitialised = false;

	public RelaxedMetricPlanningGraph(GroundProblem gp)
	{
		super(gp);
		setupPGFuncMap(gp.getFunctionValues().keySet());
		setGoal(gp.getGoal());
		makeComparators(actions);
		makeOperators(actions);
		resOps = new HashSet();
	}
	
	protected RelaxedMetricPlanningGraph(RelaxedMetricPlanningGraph mrpg)
	{
		super(mrpg);
		
		this.metricGoal = mrpg.metricGoal;
		this.minResources = mrpg.minResources;
		this.maxResources = mrpg.maxResources;
		this.PGFuncMap = mrpg.PGFuncMap;
		this.ActionComparators = mrpg.ActionComparators;
		this.ActionOperators = mrpg.ActionOperators;
		this.resOps = mrpg.resOps;
		this.functionsInitialised = mrpg.functionsInitialised;
	}
	
//	public RelaxedMetricPlanningGraph(RelaxedMetricPlanningGraph mrpg)
//	{
//		super(mrpg);
//		
//		this.metricGoal = mrpg.metricGoal;
//		this.minResources = mrpg.minResources;
//		this.maxResources = mrpg.maxResources;
//		this.PGFuncMap = mrpg.PGFuncMap;
//		this.ActionComparators = mrpg.ActionComparators;
//		this.ActionOperators = mrpg.ActionOperators;
//		this.resOps = mrpg.resOps;
//		//ignore functionsInitialised.
//	}
	

	// *************************************
	// Initial Setup
	// *************************************

	private void setupPGFuncMap(Set funcs)
	{
		Iterator fit = funcs.iterator();
		while (fit.hasNext())
		{
			NamedFunction nf = (NamedFunction) fit.next();
			PGFuncMap.put(nf, new PGNamedFunction(nf));
		}
		
		this.functionsInitialised = true;
	}

	private void makeComparators(Set actions)
	{
		Iterator ait = actions.iterator();
		while (ait.hasNext())
		{
			PGAction pga = (PGAction) ait.next();
			Set ss = new HashSet();
			ActionComparators.put(pga, ss);
			Set cs = pga.getComparators();
			Iterator cit = cs.iterator();
			while (cit.hasNext())
			{
				BinaryComparator bc = (BinaryComparator) cit.next();
				ss.add(makeComparator(bc));
			}
		}
	}

	private PGBinaryComparator makeComparator(BinaryComparator bc)
	{
		PGFunction f = makeFunction(bc.first);
		PGFunction s = makeFunction(bc.second);
		return new PGBinaryComparator(f, s, bc.type);
	}

	private void makeOperators(Set actions)
	{
		Iterator ait = actions.iterator();
		while (ait.hasNext())
		{
			PGAction pga = (PGAction) ait.next();
			Set s = new HashSet();
			ActionOperators.put(pga, s);
			Set os = pga.getOperators();
			Iterator oit = os.iterator();
			while (oit.hasNext())
			{
				ResourceOperator ro = (ResourceOperator) oit.next();
				s.add(makeOperator(ro));
			}
		}
	}

	private PGResourceOperator makeOperator(ResourceOperator ro)
	{
		PGNamedFunction r = (PGNamedFunction) PGFuncMap.get(ro.resource);
		PGFunction c = makeFunction(ro.change);
		return new PGResourceOperator(r, c, ro.type);
	}

	protected PGFunction makeFunction(Function f)
	{
		if (f instanceof NamedFunction)
			return (PGNamedFunction) PGFuncMap.get(f);
		else if (f instanceof NumberFunction)
			return new PGNumberFunction(f.getValue(null));
		else if (f instanceof BinaryFunction)
		{
			BinaryFunction bf = (BinaryFunction) f;
			PGFunction f1 = makeFunction(bf.first);
			PGFunction s2 = makeFunction(bf.second);
			return new PGBinaryFunction(f1, s2, bf.type);
		} else
			return null;
	}

	public void setGoal(GroundFact g)
	{
		//hack to get around fact this constructor calls up to PlaningGraph, which calls this overridden
		//method, which in turn will require that PGFuncMap has been populated- something which does 
		//not happen until AFTER the superclass constructors have been called
		if (this.functionsInitialised)
		{
			super.setGoal(g);
			Iterator cit = g.getComparators().iterator();
			while (cit.hasNext())
			{
				BinaryComparator c = (BinaryComparator) cit.next();
				metricGoal.add(makeComparator(c));
			}
		}
	}

	// *************************************
	// Graph Setup
	// *************************************
	protected void resetAll(State s)
	{
		super.resetAll(s);
		maxResources = new ArrayList();
		minResources = new ArrayList();
		setInitialValues((MetricState) s);
	}

	protected void setInitialValues(MetricState ms)
	{
		Iterator fit = ms.funcValues.keySet().iterator();
		Map max = new Hashtable();
		Map min = new Hashtable();
		while (fit.hasNext())
		{
			NamedFunction nf = (NamedFunction) fit.next();
			
//			/* Hack- */
//			PGFunction pgf = null;
//			if (PGFuncMap.containsKey(nf) == false)
//			{
////				System.out.println("Cannot find "+nf);
//				for (Object o : PGFuncMap.entrySet())
//				{
//					Entry e = (Entry) o;
//					if (e.getKey().toString().equals(nf.toString()))
//					{
//						pgf = (PGNamedFunction) e.getValue();
////						System.out.println("But found it's string...");
//						break;
//					}
//				}
//			}
//			else
//			{
//				pgf = (PGNamedFunction) PGFuncMap.get(nf);
//			}
			
			PGFunction pgf = (PGNamedFunction) PGFuncMap.get(nf);
			BigDecimal bd = (BigDecimal) ms.funcValues.get(nf);
			max.put(pgf, bd);
			min.put(pgf, bd);
		}
		maxResources.add(max);
		minResources.add(min);
	}

	// *************************************
	// Graph Construction Methods
	// *************************************

	protected ArrayList createFactLayer(List scheduledFacts, int layer)
	{
		ArrayList rActionList = super.createFactLayer(scheduledFacts, layer);
		updateResourceValues(layer);
		return rActionList;
	}

	private void updateResourceValues(int layer)
	{
		if (layer == 0)
			return;
		// duplicate the previous layers
		Hashtable maxminus = (Hashtable) maxResources.get(layer - 1);
		Hashtable minminus = (Hashtable) minResources.get(layer - 1);
		Map newmax = (Hashtable) maxminus.clone();
		Map newmin = (Hashtable) minminus.clone();
		maxResources.add(layer, newmax);
		minResources.add(layer, newmin);

		// loop throught the current resource operators and if they
		// increase/decrease update the values on the new layer
		Iterator roit = resOps.iterator();
		while (roit.hasNext())
		{
			PGResourceOperator ro = (PGResourceOperator) roit.next();
			BigDecimal max = ro.resource.getMaxValue(layer - 1, maxResources,
					minResources);
			BigDecimal min = ro.resource.getMinValue(layer - 1, maxResources,
					minResources);
			BigDecimal nmax = ro
					.maximise(layer - 1, maxResources, minResources);
			BigDecimal nmin = ro
					.minimise(layer - 1, maxResources, minResources);
			if (nmax.compareTo(max) > 0)
			{
				newmax.put(ro.resource, nmax);
				numeric_level_off++;
			}
			if (nmin.compareTo(min) < 0)
			{
				newmin.put(ro.resource, nmin);
				numeric_level_off++;
			}
		}
	}

	protected HashSet filterSet(Set pActions, int layer)
	{
		Set fActions = super.filterSet(pActions, layer);
		HashSet rSet = new HashSet();
		Iterator ait = fActions.iterator();
		while (ait.hasNext())
		{
			PGAction a = (PGAction) ait.next();
			if (actionReady(a, layer))
				rSet.add(a);
			else
				readyActions.add(a);
		}
		readyActions.removeAll(rSet);
		return rSet;
	}

	private boolean actionReady(PGAction a, int layer)
	{
		Set cs = (HashSet) ActionComparators.get(a);
		Iterator csit = cs.iterator();
		boolean allmet = true;
		while (csit.hasNext() && allmet)
		{
			PGBinaryComparator c = (PGBinaryComparator) csit.next();
			allmet = c.met(layer, maxResources, minResources);
		}
		return allmet;
	}

	public ArrayList<PGFact> calculateActionMutexesAndProps(Set<PGAction> filteredSet, int pLayer)
	{
		ArrayList<PGFact> rSet = super.calculateActionMutexesAndProps(filteredSet, pLayer);
		Iterator ait = filteredSet.iterator();
		while (ait.hasNext())
		{
			PGAction a = (PGAction) ait.next();
			Set ops = (Set) ActionOperators.get(a);
			resOps.addAll(ops);
		}
		return rSet;
	}

	// *************************************
	// Graph Extraction
	// *************************************

	protected boolean goalMet()
	{
		boolean met = super.goalMet();
		Iterator mgit = metricGoal.iterator();
		while (met && mgit.hasNext())
		{
			PGBinaryComparator c = (PGBinaryComparator) mgit.next();
			met = c.met(num_layers, maxResources, minResources);
		}
		return met;
	}

	public List extractPlan() // Should be done better but its not
	{
		return searchRelaxedPlan(this.goal, metricGoal, num_layers);
	}

	public List searchRelaxedPlan(Set<PGFact> goalSet, Set mgoalSet, int l)
	{
		if (l == 0)
			return new ArrayList();
		Set chosenActions = new HashSet();
		// loop through actions to achieve the goal set
		for (PGFact g : goalSet)
		{
			PGAction a = null;
			for (PGAction na : g.getAchievedBy())
			{
				if (na.getLayer() < l && na.getLayer() >= 0)
				{
					if (na instanceof PGNoOp)
					{
						a = na;
						break;
					} 
					else if (chosenActions.contains(na))
					{
						a = na;
						break;
					} 
					else
					{
						if (a == null)
							a = na;
						else if (a.getDifficulty() > na.getDifficulty())
							a = na;
					}
				}
			}

			if (a != null)
			{
				chosenActions.add(a);
			}
		}

		Set newMGoalSet = new HashSet();
		// loop through the metric goals and see if they are satisfied, if not
		// find some actions
		for (Object mit : mgoalSet)
		{
			PGBinaryComparator c = (PGBinaryComparator) mit;
			Iterator ait = actions.iterator();
			while (!c.met(l - 1, maxResources, minResources) && ait.hasNext())
			{
				PGAction a = null;
				while (ait.hasNext())
				{
					a = (PGAction) ait.next();
					if (a.getLayer() < l && a.getLayer() >= 0 && c.makeBetter(a))
						break;
				}

				Iterator roit = ((Set) ActionOperators.get(a)).iterator();
				while (roit.hasNext())
				{
					PGResourceOperator o = (PGResourceOperator) roit.next();
					if (c.makeBetter(o))
					{
						c = c.affect(o);
					}
				}
				chosenActions.add(a);
			}
			newMGoalSet.add(c);
		}

		Set newGoalSet = new HashSet();
		// loop through chosen actions adding in propositions and comparators
		Iterator cait = chosenActions.iterator();
		while (cait.hasNext())
		{
			PGAction ca = (PGAction) cait.next();
			newGoalSet.addAll(ca.getConditions());
			newMGoalSet.addAll((Set) ActionComparators.get(ca));
		}

		List rplan = searchRelaxedPlan(newGoalSet, newMGoalSet, l - 1);
		rplan.addAll(chosenActions);
		return rplan;
	}

	// *************************************
	// Internal Metric Classes
	// *************************************

	// Note these do not deal with max and mins where there a -ve changes with
	// multiplication and division. For example if x*y could be max if max*max
	// or min*min if both mins were negative

	protected interface PGFunction
	{
		public BigDecimal getMaxValue(int layer, List maxes, List mins);

		public BigDecimal getMinValue(int layer, List maxes, List mins);

		public boolean effectedBy(PGResourceOperator ro);

		public boolean increase(PGResourceOperator ro);

		public boolean decrease(PGResourceOperator ro);
	}

	protected class PGNamedFunction implements PGFunction
	{
		public NamedFunction namedFunction;

		protected PGNamedFunction()
		{

		}

		public PGNamedFunction(NamedFunction nf)
		{
			namedFunction = nf;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins)
		{
			Map max = (Hashtable) maxes.get(layer);
			return (BigDecimal) max.get(this);
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins)
		{
			Map min = (Hashtable) mins.get(layer);
			return (BigDecimal) min.get(this);
		}

		public int hashcode()
		{
			return namedFunction.hashCode();
		}

		public boolean effectedBy(PGResourceOperator ro)
		{
			return (this == ro.resource);
		}

		public boolean increase(PGResourceOperator ro)
		{
			return ro.increase(this);
		}

		public boolean decrease(PGResourceOperator ro)
		{
			return ro.decrease(this);
		}
	}

	protected class PGNumberFunction implements PGFunction
	{
		public BigDecimal value;

		public PGNumberFunction(BigDecimal v)
		{
			value = v;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins)
		{
			return value;
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins)
		{
			return value;
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

	protected class PGBinaryFunction implements PGFunction
	{
		public PGFunction first, second;
		public int type;

		public PGBinaryFunction(PGFunction f, PGFunction s, int t)
		{
			first = f;
			second = s;
			type = t;
		}

		public BigDecimal getMaxValue(int layer, List maxes, List mins)
		{
			if (type == MetricSymbolStore.PLUS)
				return first.getMaxValue(layer, maxes, mins).add(
						second.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.MINUS)
				return first.getMaxValue(layer, maxes, mins).subtract(
						second.getMinValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.MULTIPLY)
				return first.getMaxValue(layer, maxes, mins).multiply(
						second.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.DIVIDE)
				return first.getMaxValue(layer, maxes, mins).divide(
						second.getMinValue(layer, maxes, mins),
						MetricSymbolStore.SCALE, MetricSymbolStore.ROUND);
			else
				return null;
		}

		public BigDecimal getMinValue(int layer, List maxes, List mins)
		{
			return getMaxValue(layer, mins, maxes);
		}

		public boolean effectedBy(PGResourceOperator ro)
		{
			return (first.effectedBy(ro) || second.effectedBy(ro));
		}

		public boolean increase(PGResourceOperator ro)
		{
			if (type == MetricSymbolStore.PLUS)
				return (first.increase(ro) || second.increase(ro));
			else if (type == MetricSymbolStore.MINUS)
				return (first.increase(ro) || second.decrease(ro));
			else if (type == MetricSymbolStore.MULTIPLY)
				return (first.increase(ro) || second.increase(ro));
			else if (type == MetricSymbolStore.DIVIDE)
				return (first.increase(ro) || second.decrease(ro));
			else
				return false;
		}

		public boolean decrease(PGResourceOperator ro)
		{
			if (type == MetricSymbolStore.PLUS)
				return (first.decrease(ro) || second.decrease(ro));
			else if (type == MetricSymbolStore.MINUS)
				return (first.decrease(ro) || second.increase(ro));
			else if (type == MetricSymbolStore.MULTIPLY)
				return (first.decrease(ro) || second.decrease(ro));
			else if (type == MetricSymbolStore.DIVIDE)
				return (first.decrease(ro) || second.increase(ro));
			else
				return false;
		}
	}

	protected class PGResourceOperator
	{
		public PGNamedFunction resource;
		public PGFunction change;
		public int type;

		public PGResourceOperator(PGNamedFunction r, PGFunction c, int t)
		{
			resource = r;
			change = c;
			type = t;
		}

		public BigDecimal maximise(int layer, List maxes, List mins)
		{
			if (type == MetricSymbolStore.ASSIGN)
				return change.getMaxValue(layer, maxes, mins);
			else if (type == MetricSymbolStore.INCREASE)
				return resource.getMaxValue(layer, maxes, mins).add(
						change.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.DECREASE)
				return resource.getMaxValue(layer, maxes, mins).subtract(
						change.getMinValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.SCALE_UP)
				return resource.getMaxValue(layer, maxes, mins).multiply(
						change.getMaxValue(layer, maxes, mins));
			else if (type == MetricSymbolStore.SCALE_DOWN)
				return resource.getMaxValue(layer, maxes, mins).divide(
						change.getMinValue(layer, maxes, mins),
						MetricSymbolStore.SCALE, MetricSymbolStore.ROUND);
			else
				return null;
		}

		public BigDecimal minimise(int layer, List maxes, List mins)
		{
			return maximise(layer, mins, maxes);
		}

		public PGFunction invertFunction(PGFunction f)
		{
			if (type == MetricSymbolStore.ASSIGN)
				return change;
			else if (type == MetricSymbolStore.INCREASE)
				return new PGBinaryFunction(f, change, MetricSymbolStore.MINUS);
			else if (type == MetricSymbolStore.DECREASE)
				return new PGBinaryFunction(f, change, MetricSymbolStore.PLUS);
			else if (type == MetricSymbolStore.SCALE_UP)
				return new PGBinaryFunction(f, change, MetricSymbolStore.DIVIDE);
			else if (type == MetricSymbolStore.SCALE_DOWN)
				return new PGBinaryFunction(f, change,
						MetricSymbolStore.MULTIPLY);
			else
				return null;
		}

		public boolean increase(PGFunction f)
		{
			if (resource == f)
			{
				if (type == MetricSymbolStore.ASSIGN)
					return true; // could do something better here by looking
									// at the values
				else if (type == MetricSymbolStore.INCREASE)
					return true;
				else if (type == MetricSymbolStore.DECREASE)
					return false;
				else if (type == MetricSymbolStore.SCALE_UP)
					return true;
				else if (type == MetricSymbolStore.SCALE_DOWN)
					return false;
				else
					return false;
			} else
				return false;
		}

		public boolean decrease(PGFunction f)
		{
			if (resource == f)
			{
				if (type == MetricSymbolStore.ASSIGN)
					return true; // could do something better here by looking
									// at the values
				else if (type == MetricSymbolStore.INCREASE)
					return false;
				else if (type == MetricSymbolStore.DECREASE)
					return true;
				else if (type == MetricSymbolStore.SCALE_UP)
					return false;
				else if (type == MetricSymbolStore.SCALE_DOWN)
					return true;
				else
					return false;
			} else
				return false;
		}
	}

	protected class PGBinaryComparator
	{
		public PGFunction left, right;
		public int type;

		public PGBinaryComparator(PGFunction l, PGFunction r, int t)
		{
			left = l;
			right = r;
			type = t;
		}

		public boolean met(int layer, List maxes, List mins)
		{
			if (type == MetricSymbolStore.GREATER_THAN)
				return left.getMaxValue(layer, maxes, mins).compareTo(
						right.getMinValue(layer, maxes, mins)) > 0;
			else if (type == MetricSymbolStore.GREATER_THAN_EQUAL)
				return left.getMaxValue(layer, maxes, mins).compareTo(
						right.getMinValue(layer, maxes, mins)) >= 0;
			else if (type == MetricSymbolStore.LESS_THAN)
				return left.getMinValue(layer, maxes, mins).compareTo(
						right.getMaxValue(layer, maxes, mins)) < 0;
			else if (type == MetricSymbolStore.LESS_THAN_EQUAL)
				return left.getMinValue(layer, maxes, mins).compareTo(
						right.getMaxValue(layer, maxes, mins)) <= 0;
			else if (type == MetricSymbolStore.EQUAL)
				return (left.getMaxValue(layer, maxes, mins).compareTo(
						right.getMinValue(layer, maxes, mins)) >= 0 && left
						.getMinValue(layer, maxes, mins).compareTo(
								right.getMaxValue(layer, maxes, mins)) <= 0);
			else
				return true;
		}

		public boolean effectedBy(PGResourceOperator ro)
		{
			return (left.effectedBy(ro) || right.effectedBy(ro));
		}

		public PGBinaryComparator affect(PGResourceOperator ro)
		{
			if (effectedBy(ro))
			{
				PGFunction newLeft = left;
				PGFunction newRight = right;
				if (left.effectedBy(ro))
				{
					newLeft = ro.invertFunction(left);
				}
				if (right.effectedBy(ro))
				{
					newRight = ro.invertFunction(right);
				}
				PGBinaryComparator nbc = new PGBinaryComparator(newLeft,
						newRight, type);
				return nbc;
			} else
				return this;
		}

		public boolean makeBetter(PGResourceOperator ro)
		{
			if (type == MetricSymbolStore.GREATER_THAN
					|| type == MetricSymbolStore.GREATER_THAN_EQUAL)
				return left.increase(ro) || right.decrease(ro);
			else if (type == MetricSymbolStore.LESS_THAN
					|| type == MetricSymbolStore.LESS_THAN_EQUAL)
				return left.decrease(ro) || right.increase(ro);
			else if (type == MetricSymbolStore.EQUAL)
				return true;
			else
				return true;
		}

		public boolean makeBetter(PGAction a)
		{
			Iterator oit = ((Set) ActionOperators.get(a)).iterator();
			while (oit.hasNext())
			{
				PGResourceOperator o = (PGResourceOperator) oit.next();
				if (makeBetter(o))
					return true;
			}
			return false;
		}
	}

	// *************************************
	// Debugging Classes
	// *************************************

	public void printLayer(int i)
	{
		super.printLayer(i);
		Map max = (Hashtable) maxResources.get(i);
		Map min = (Hashtable) minResources.get(i);
		System.out.println("Resources:");
		Iterator rit = max.keySet().iterator();
		while (rit.hasNext())
		{
			PGNamedFunction r = (PGNamedFunction) rit.next();
			System.out.print("\t" + r.namedFunction.toString());
			System.out.print(" max:" + max.get(r));
			System.out.println(" min:" + min.get(r));
		}
	}

}