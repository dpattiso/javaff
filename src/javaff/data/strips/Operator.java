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

package javaff.data.strips;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.Parameter;
import javaff.data.UngroundProblem;
import java.util.Arrays;
import java.util.Map;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

public abstract class Operator implements javaff.data.PDDLPrintable
{
	public OperatorName name;
	public List<Parameter> params = new ArrayList<Parameter>(); // list of Variables

	public String toString()
	{
		String stringrep = name.toString();
		Iterator<Parameter> i = params.iterator();
		while (i.hasNext())
		{
			//Variable v = (Variable) i.next();
			stringrep += " " + i.next().toString();
		}
		return stringrep;
	}
	
	public abstract Object clone();

	public String toStringTyped()
	{
		String stringrep = name.toString();
		Iterator<Parameter> i = params.iterator();
		while (i.hasNext())
		{
			Variable v = (Variable) i.next();
			stringrep += " " + v.toStringTyped();
		}
		return stringrep;
	}

	public abstract boolean effects(PredicateSymbol ps);

	protected abstract Action ground(Map<Variable, PDDLObject> varMap);

	public abstract Set<Fact> getStaticConditionPredicates();

	public Action ground(List<PDDLObject> values)
	{
		Map<Variable, PDDLObject> varMap = new Hashtable<Variable, PDDLObject>();
		Iterator<PDDLObject> vit = values.iterator();
		Iterator<Parameter> pit = params.iterator();
		while (pit.hasNext())
		{
			Variable v = (Variable) pit.next();
			PDDLObject o = (PDDLObject) vit.next();
			varMap.put(v, o);
		}
		
		Action a = this.ground(varMap);
		return a;

	}

	public Set<Action> ground(UngroundProblem up)
	{
		Set<ArrayList<PDDLObject>> s = this.getParameterCombinations(up);
		Set<Action> rSet = new HashSet<Action>();
		out : for (ArrayList<PDDLObject> l : s)
		{
			//This is a hack to stop actions which have no associated parameters being created -- as this causes 
			//a NullPointerException to be thrown during grounding. The alternative to this is to construct a pfile
			//which has one of every object type.
			//TODO Do this in getParameterCombinations()
			for (PDDLObject p : l)
			{
				//if p is null then the pfile has no parameter with which to ground out an action, so ignore it. This can massively reduce the action set for planning!
				if (p == null)
					continue out;
			}
			
			
			
			Action groundedAction = ground(l); //ground an individual action using the specified grounded parameters
		
			//23/8/11 -- Eliminate any grounded actions whose preconditions contain
			//			 a static fact which is not true in the initial state, rendering it impossible
			//18/5/14 -- Also remove any simple preconditions which always evaluate to True. This has been 
			//			 caused by the realisation that all Equals which do not have the same set of parameters
			//			 will always evaluate to false, making the whole action unusable, so it can be pruned.
			//			//TODO move this code to the getParameterCombinations() method
			for (Fact pc : groundedAction.getPreconditions())
			{
				//It is possible that an unground fact may return a TrueCondition as its grounded form
				//(see ForAlls), so just ignore these. They are still valid, but the IF statement below
				//this will say that the action is invalid, as the probability of a TrueCondition appearing
				//in the initial state is low.
//				if (pc instanceof TrueCondition)
				if (TrueCondition.isSimpleTrue(pc) == 1)
				{
					continue;
				}
				if (this.isSimpleFalse(pc) == -1)
					continue out;

				if (pc.isStatic() && up.initial.contains(pc) == false)
					continue out;
			}
			
			rSet.add(groundedAction); 
		}
		return rSet;
	}

	/**
	 * 
	 * @param f
	 * @return -1 for always false, +1 for never false, and 0 for normal fact type
	 */
	protected int isSimpleFalse(Fact f)
	{
		if (f instanceof TrueCondition)
			return 1;
		
		if (f instanceof Not)
		{
			return -1 * isSimpleFalse(((Not)f).getLiteral());
		}
		else
			return 0;
	}
	
	
	

	public Set<ArrayList<PDDLObject>> getParameterCombinations(UngroundProblem up)
	{
		int arraysize = params.size();

		Set<Fact> staticConditions = getStaticConditionPredicates();

		boolean[] set = new boolean[arraysize]; // which of the parameters has
												// been fully set
		Arrays.fill(set, false);

		ArrayList<PDDLObject> combination = new ArrayList<PDDLObject>(arraysize);
		for (int i = 0; i < arraysize; ++i)
		{
			combination.add(null);
		}

		// Set for holding the combinations
		Set<ArrayList<PDDLObject>> combinations = new HashSet<ArrayList<PDDLObject>>();
		combinations.add(combination);

		// Loop through any preconditions which are known to be static. We will use
		// the ground parameters for these preconditions to partially ground out this
		// action before other parameter combinations are considered.
		for (Fact fp : staticConditions)
		{
			Predicate p = (Predicate) fp;
			Set<ArrayList<PDDLObject>> newcombs = new HashSet<ArrayList<PDDLObject>>();

			Set<Proposition> sp = up.staticPropositionMap.get(p
					.getPredicateSymbol());

			// Loop through those in the initial tmstate
			for (Proposition prop : sp)
			{
				for (ArrayList<PDDLObject> c : combinations)
				{
					// check its ok to put in
					boolean ok = true;
					Iterator<Parameter> propargit = prop.getParameters().iterator();
					int counter = 0;
					while (propargit.hasNext() && ok)
					{
						PDDLObject arg = (PDDLObject) propargit.next();
						Parameter k = (Parameter) p.getParameters().get(counter);
						int i = params.indexOf(k);
						if (i >= 0 && set[i])
						{
							if (!c.get(i).equals(arg))
							{
								ok = false;
								break;
							}
						}
						//this picks up a subtle bug whereby the static fact contains parameters
						//which are valid in the context of the precondition in this action, but
						//that their actual type is too abstract to be applied to the other
						//predicates in the action. Disable this elseif and run IPC5 storage
						//problem 2 to see how this affects the resulting plan (VAL says it is
						//invalid due to the ground parameter being of an invalid type).
						else if (i > 0 && arg.isOfType(k.getType()) == false)
						{
							ok = false;
							break;
						}
						
						counter++;
					}
					// if so, duplicate it and put it in and put it in newcombs
					if (ok)
					{
						ArrayList<PDDLObject> sdup = (ArrayList<PDDLObject>) c.clone();
						counter = 0;
						propargit = prop.getParameters().iterator();
						while (propargit.hasNext())
						{
							PDDLObject arg = (PDDLObject) propargit.next();
							Parameter k = (Parameter) p.getParameters().get(
									counter);
							int i = params.indexOf(k);
							if (i >= 0)
							{
								sdup.set(i, arg);
								counter++;
							}
						}
						newcombs.add(sdup);
					}
				}
			}

			combinations = newcombs;

			for (Parameter s : p.getParameters())
			{
				int i = params.indexOf(s);

				if (i >= 0)
					set[i] = true;
			}
		}

		int counter = 0;
		//foreach parameter
		for (Parameter p : params)
		{
			//if unset so far (not static?)
			if (!set[counter])
			{
				Set<ArrayList<PDDLObject>> newcombs = new HashSet<ArrayList<PDDLObject>>();
				for (ArrayList<PDDLObject> s : combinations)
				{
					Set<PDDLObject> objs = (HashSet<PDDLObject>) up.typeSets.get(p.getType());
					for (PDDLObject ob : objs)
					{
						ArrayList<PDDLObject> sdup = (ArrayList<PDDLObject>) s.clone();
						sdup.set(counter, ob);
						newcombs.add(sdup);
					}
				}
				combinations = newcombs;

			}
			++counter;
		}
		return combinations;

	}
}
