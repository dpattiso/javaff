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
import javaff.data.CompoundLiteral;
import javaff.data.Fact;
import javaff.data.Parameter;
import javaff.data.TotalOrderPlan;
import javaff.data.GroundFact;
import javaff.data.metric.NamedFunction;
import javaff.data.strips.InstantAction;
import javaff.data.strips.Not;
import javaff.data.strips.NullFact;
import javaff.data.strips.OperatorName;
import javaff.data.strips.Proposition;
import javaff.data.strips.STRIPSInstantAction;
import javaff.data.strips.TrueCondition;
import javaff.data.Plan;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class STRIPSState extends State implements Cloneable
{
	protected Set<Fact> factsTrue;
	protected Set<Not> factsNegated;
	protected Set<Action> actions;
	
	protected Plan RelaxedPlan;

	protected RelaxedPlanningGraph RPG;
	protected boolean RPCalculated;
	protected BigDecimal HValue;

	protected STRIPSState()
	{
		super();
		
		this.factsNegated = new HashSet<Not>();
		this.factsTrue = new HashSet<Fact>();
		this.actions = new HashSet<Action>();
		
		this.RelaxedPlan = null;
		
		this.HValue = new BigDecimal(-1);
		
		this.RPG = null;
		this.RPCalculated = false;
		this.HValue = null;
				
	}

	public STRIPSState(Set<Action> a, Set<Fact> f, GroundFact g)
	{
		this();
		
		factsTrue = f;
		goal = g;
		actions = a;
	}
	
	public STRIPSState(Set<Action> actions, Set<Fact> trueFacts, Set<Not> negatedFacts, GroundFact goal)
	{
		this(actions, trueFacts, goal);
		
		factsNegated = negatedFacts;
	}

	protected STRIPSState(Set a, Set f, GroundFact g, TotalOrderPlan p)
	{
		this(a, f, g);
		plan = p;
	}

	
	public Set<Fact> getTrueFacts()
	{
		return this.factsTrue;
	}
	
	public Set<Not> getFalseFacts()
	{
		return this.factsNegated;
	}
	
	public void addFacts(Collection<Fact> facts)
	{
		for (Fact f : facts)
			this.addFact(f);
	}
	
	public Plan getRelaxedPlan()
	{
		return this.RelaxedPlan;
	}
	
	public void addFact(Fact f)
	{
		if (f instanceof Proposition || TrueCondition.isSimpleTrue(f) == 1)
		{
			this.factsTrue.add(f);
		}
		else if (f instanceof Not)
		{
			if (((Not)f).getLiteral() instanceof Not == true)
			{
				Not notf = (Not)f;
				Not internalLiteral = (Not)notf.getLiteral();
				this.addFact(internalLiteral.getLiteral()); //recurse
			}
			else if (((Not)f).getLiteral() instanceof Proposition == false)
				throw new IllegalArgumentException("STRIPSState only supports Propositions and Nots whose literal is also a Proposition");
			else
				this.factsNegated.add((Not)f);
		}
		else
			throw new IllegalArgumentException("Invalid fact type. Must be of type Proposition or Not.");
	}
	
	public boolean removeFact(Fact f)
	{
		if (f instanceof Proposition)
		{
			return this.factsTrue.remove((Proposition) f);
		}
		else if (f instanceof Not)
		{
			if (((Not)f).getLiteral() instanceof Not == true)
			{
				Not notf = (Not)f;
				Not internalLiteral = (Not)notf.getLiteral();
				this.removeFact(internalLiteral.getLiteral()); //recurse
			}
			if (((Not)f).getLiteral() instanceof Proposition == false)
				throw new IllegalArgumentException("STRIPSState only supports Propositions and Nots whose literal is also a Proposition");
			else
//				return this.factsNegated.remove((Not)f);
				return this.factsNegated.remove(((Not)f).getLiteral());
		}
		else	
			throw new IllegalArgumentException("STRIPSState only supports Propositions and Nots");
		
	}

	public TotalOrderPlan getPlan()
	{
		return (TotalOrderPlan) plan;
	}
	
//	public State cloneShallow()
//	{
//		
//	}
	
	public Object clone()
	{
		Set<Proposition> trueFacts = (Set<Proposition>) ((HashSet) factsTrue).clone();
		Set<Not> falseFacts = (Set<Not>) ((HashSet) factsNegated).clone();
		TotalOrderPlan p = (TotalOrderPlan) plan.clone();
		STRIPSState SS = new STRIPSState(new HashSet<Action>(actions), trueFacts, (GroundFact) goal.clone(), p);
		SS.factsNegated = falseFacts;
		
		if (this.RPG != null)
		{
			RelaxedPlanningGraph rpg = (RelaxedPlanningGraph) this.RPG.clone();
			SS.RPG = rpg;
			
			SS.RPCalculated = this.RPCalculated;
		}
		
		if (this.HValue != null)
		{
			SS.HValue = new BigDecimal(this.HValue.toString());
		}
		
		if (SS.plan != null)
		{
			SS.plan = (TotalOrderPlan) this.plan.clone();
		}
		
		// SS.setFilter(filter);
		return SS;
	}

	public void setRPG(RelaxedPlanningGraph rpg)
	{
		RPG = rpg;
	}

	public RelaxedPlanningGraph getRPG()
	{
		return RPG;
	}

	public State apply(Action a)
	{
		//old code -- works but is inefficient as RPg is cloned then immediately destroyed and recomputed by successor state
//		STRIPSState succ = (STRIPSState) this.clone(); //VERY slow because RPG is cloned too

		//create a successor state, which is the same as this one, but has no RPG. The positive and negative facts must be 
		//copied to new Sets because application of actions will add/remove facts from these. If a new set is not constructed
		//for each, then plans will be invalid.
		STRIPSState succ = new STRIPSState(this.actions, new HashSet<Fact>(this.factsTrue), new HashSet<Not>(this.factsNegated), this.goal);
//		succ.RPG = new RelaxedPlanningGraph(this.actions, this.goal); //MASSIVE memory consumption -- cannot explain why
		succ.RPG = this.RPG.branch(); //branch the RPG instead of cloning the old one -- retains the mutex info etc
		
		//TODO add in initial state to RPG from RPG constructor
		succ.RPCalculated = false; //should be false by default, but put it in anyway. Forces the EHC heuristic to construct the RPG -- the
								   //above line only sets up the required parameters.
		succ.getRPG().setInitial(succ);
		succ.getRPG().setGoal(this.goal);
		succ.plan = (TotalOrderPlan) this.plan.clone(); //clone because we may have multiple lead states
		
//		System.out.print("Applying in state "+succ.hashCode());
//		if (a.isApplicable(this) == false)
//			throw new NullPointerException("Action not applicable in state");
			
		a.apply(succ);
		
		succ.plan.addAction(a);
		return succ;
	}

//	public void removeProposition(Proposition p)
//	{
//		factsTrue.remove(p);
//	}

	public boolean isTrue(Fact p)
	{
		//23/8/11 - all illegal statics should have been removed by now, so if a fact has somehow
		//			made it to here (probably embedded in a Quantified literal), then it must be legal and
		//			therefore true.
		
		if (p instanceof CompoundLiteral)
		{
			for (Fact c : ((CompoundLiteral) p).getCompoundFacts())
			{
				boolean res = this.isTrue(c);
				if (res == false)
					return false;
			}
					
			return true;
		}
		
		//20/5/14 -- commented out static check as ADL decompilation is causing static facts which are
		//not true in the initial state to be generated, which leads to untrue facts being classed as true 
		//in every state.
		if (p.isStatic())
			return true;  
		
		if (p instanceof Proposition)
			return this.factsTrue.contains(p);
		else if (p instanceof Not)
		{
			if (((Not)p).getLiteral() instanceof Not)
				return this.isTrue(((Not)((Not)p).getLiteral()).getLiteral());
			else
				return this.factsNegated.contains(p);// ^ this.factsTrue.contains(((Not)p).literal) == false;
		}
		else
			throw new IllegalArgumentException("STRIPSStates can only check whether Propositions or Nots are true");
	}

	public Set<Action> getActions()
	{
		return actions;
	}

	public void calculateRP()
	{
		if (!RPCalculated)
		{
			this.RelaxedPlan = RPG.getPlan(this);
			if (this.RelaxedPlan != null)
			{
				this.HValue = new BigDecimal(this.RelaxedPlan.getPlanLength());


			} 
			else
				this.HValue = javaff.JavaFF.MAX_DURATION;
			
			this.RPCalculated = true;
		}
	}
	

	public BigDecimal getHValue()
	{
		calculateRP();
		return HValue;
	}

	public BigDecimal getGValue()
	{
		return new BigDecimal(plan.getPlanLength());
	}

	public Plan getSolution()
	{
		return plan;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof STRIPSState)
		{
			STRIPSState s = (STRIPSState) obj;
			return s.factsTrue.equals(factsTrue);
		} else
			return false;
	}

	public int hashCode()
	{
		int hash = 7;
		hash = 31 * hash ^ factsTrue.hashCode();
		return hash;
	}
	
	@Override
	public String toString()
	{
		StringBuffer strBuf = new StringBuffer();
		for (Object o : this.factsTrue)
		{
			strBuf.append(o+", ");
		}
		
		return strBuf.toString();
	}

	/**
	 * Returns all true and false facts in the domain in a single set.
	 * @return
	 */
	public Set<Fact> getFacts()
	{
		HashSet<Fact> pos = new HashSet<Fact>(this.getTrueFacts());
		pos.addAll(this.getFalseFacts());
		return pos;
	}

}
