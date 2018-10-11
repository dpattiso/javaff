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
import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.GroundProblem;
import javaff.data.Plan;
import javaff.data.RelaxedPlan;
import javaff.data.TotalOrderPlan;
import javaff.data.strips.And;
import javaff.data.strips.InstantAction;
import javaff.data.strips.Not;
import javaff.data.strips.Proposition;
import javaff.data.strips.RelaxedFFPlan;
import javaff.planning.PlanningGraph.MutexPair;
import javaff.planning.PlanningGraph.PGAction;
import javaff.planning.PlanningGraph.PGNoOp;
import javaff.planning.PlanningGraph.PGFact;
import javaff.planning.RelaxedMetricPlanningGraph.PGBinaryComparator;
import javaff.planning.RelaxedMetricPlanningGraph.PGResourceOperator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Implementation of a Relaxed Planning Graph (RPG), where negative effects of actions are ignored along with
 * all action and fact mutexes. This implementation also holds all helpful actions, as defined by Hoffmann and
 * Nebel, 2001.
 *
 */
public class RelaxedPlanningGraph extends PlanningGraph
{
	private TreeSet<HelpfulAction> helpfulActions;
	
	//needed for clone()
	protected RelaxedPlanningGraph()
	{
		super();
		
		this.helpfulActions = new TreeSet<HelpfulAction>();
	}
	
	protected RelaxedPlanningGraph(RelaxedPlanningGraph existingRpg)
	{
		super(existingRpg);
		
		this.helpfulActions = existingRpg.helpfulActions;
	}
	
	protected RelaxedPlanningGraph(PlanningGraph pg)
	{
		super(pg);
		
		this.helpfulActions = new TreeSet<HelpfulAction>();
	}
	
	
	public RelaxedPlanningGraph(GroundProblem gp)
	{
		super(gp);
		
		this.helpfulActions = new TreeSet<HelpfulAction>();
	}
	
	/**
	 * Construct an RPG with a goal which overrides that contained in the first parameter's GroundProblem.goal
	 * field.
	 * @param gp
	 * @param goal
	 */
	public RelaxedPlanningGraph(GroundProblem gp, GroundFact goal)
	{
		super(gp);
		this.helpfulActions = new TreeSet<HelpfulAction>();
		
		this.setGoal(goal);
	}
	
	public RelaxedPlanningGraph(Set<Action> actions, GroundFact goal)
	{
		super(actions, goal);
		
		this.helpfulActions = new TreeSet<HelpfulAction>();
	}
	
	@Override
	public RelaxedPlan getPlan(State s)
	{
		return (RelaxedPlan) super.getPlan(s);
	}
	
	@Override
	protected RelaxedPlan constructPlan(Collection<Collection<PGAction>> plan, Fact goal)
	{
		RelaxedFFPlan p = null;
		if (plan != null)
		{
			p = new RelaxedFFPlan(goal);
			for (Collection<PGAction> l : plan)
			{
				HashSet<Action> layer = new HashSet<Action>();
				for (PGAction a : l)
				{
					if (!(a instanceof PGNoOp))
						layer.add(a.getAction());
				}
				p.addActions(layer);
			}
			// p.print(javaff.JavaFF.infoOutput);
			return p;
		}
		// this.setGoal(oldGoal);
		
		return p;
	}
	
	
	@Override
	public RelaxedPlan getPlanFromExistingGraph(Fact g) 
	{
		// TODO Auto-generated method stub
		return (RelaxedPlan) super.getPlanFromExistingGraph(g);
	}
	
	@Override
	public Object clone()
	{
		
//		RelaxedPlanningGraph clone = new RelaxedPlanningGraph();
//		clone.actionMap = new HashMap<Action, PlanningGraph.PGAction>(this.actionMap);
//		clone.actionMutexes = new HashSet(this.actionMutexes);
//		clone.actions = new HashSet(this.actions);
//		clone.factLayers = new ArrayList<Set<Fact>>(this.factLayers);
//		clone.goal = new HashSet<PGFact>(this.goal);
//		clone.initial = new HashSet<PGFact>(this.initial);
//		clone.level_off = this.level_off;
//		clone.num_layers = this.num_layers;
//		clone.numeric_level_off = this.numeric_level_off;
//		clone.propMutexes = new HashSet(this.propMutexes);
//		clone.propositionMap = new HashMap<Fact, PlanningGraph.PGFact>(this.propositionMap);
//		clone.propositions = new HashSet(this.propositions);
//		clone.readyActions = new HashSet<PlanningGraph.PGAction>(this.readyActions);
//		clone.helpfulActions = new TreeSet<HelpfulAction>(this.helpfulActions);
//		clone.negativePCActions = new HashSet<PGAction>(this.negativePCActions);
		
		PlanningGraph pg = (PlanningGraph) super.clone();
		RelaxedPlanningGraph clone = new RelaxedPlanningGraph(pg);
//		clone.helpfulActions = new TreeSet<HelpfulAction>(this.helpfulActions.c);
		clone.helpfulActions = new TreeSet<HelpfulAction>();
		for (HelpfulAction ha : this.helpfulActions)
		{
			clone.helpfulActions.add((HelpfulAction) ha.clone());
		}
		
		return clone;
	}
	
	@Override
	public RelaxedPlanningGraph branch()
	{
		RelaxedPlanningGraph branch = new RelaxedPlanningGraph(super.branch());
		
//		branch.actionMap = this.actionMap;
//		branch.actionMutexes = this.actionMutexes;
//		branch.actions = this.actions;
////		branch.factLayers = this.factLayers;
////		branch.goal = this.goal;
////		branch.initial = this.initial;
////		branch.level_off = this.level_off;
////		branch.num_layers = this.num_layers;
////		branch.numeric_level_off = this.numeric_level_off;
//		branch.propMutexes = this.propMutexes;
//		branch.propositionMap = this.propositionMap;
//		branch.propositions = this.propositions;
//		branch.negativePCActions = this.negativePCActions;
////		branch.readyActions = this.readyActions;
		
		
		return branch; 
	}
	
	@Override
	public List extractPlan()
	{
		return this.searchRelaxedPlan(this.goal, super.num_layers);
	}
	
	
	
	/**
	 * No mutexes allowed in RPG, so just returns false.
	 */
	@Override
	public boolean checkPropMutex(Fact p1, Fact p2, int l)
	{
		return false;
	}
		
	/**
	 * No mutexes allowed in RPG construction, so just returns immediately.
	 */
	@Override
	protected void makeMutex(Node n1, Node n2, int l, Set<MutexPair> mutexPairs)
	{
		return;
	}
	

	// ******************************************************
	// Graph Construction
	// ******************************************************

	
	//NOTE!!!!
	//this is a direct copy of PlanningGraph's createFactLayer() method, but with the mutex checks
	//removed. Profiling showed that 30% (!) of all planning time was just calling checkPropMutex()
	//in this class, which itself just returns false. Thats quite a lot of calls to get 30% CPU time.
	//Naturally, any changes to the superclass' code will need to be reflected here.
	protected ArrayList<PGAction> createFactLayer(List<PGFact> trueFacts, int pLayer)
	{

		memoised.add(new HashSet<PGFact>());
		ArrayList<PGAction> scheduledActs = new ArrayList<PGAction>();
		
//		//first, we add in all actions which have no preconditions and therefore are always applicable.
//		//this has to be done separately because the loop below assumes that there will actually be at-least
//		//one fact true in the current relaxed state. If there are none, and the first action in every plan has
//		//no preconditions, then the RPG will never be constructed.
//		for (Action always : this.emptyPreconditions)
//		{
//			if (pLayer != 0)
//			{
//				Iterator pit = propositions.iterator();
//				while (pit.hasNext())
//				{
//					PGFact p = (PGFact) pit.next();
//					if (p.getLayer() >= 0 && this.checkPropMutex(f, p, pLayer))
//					{
//						this.makeMutex(f, p, pLayer, newMutexes);
//					}
//				}
//			}
//		}
		
		
		//check positive facts
		for (PGFact f : trueFacts)
		{
			if (f.getLayer() < 0)
			{
				//if this fact has never been seen in the PG so far (its layer is < 0), say that it appears at this layer -- this will determine its "difficulty"
				f.setLayer(pLayer);

				//add all actions which this fact enables
				scheduledActs.addAll(f.getEnables());
				 
				level_off = false;


			}
		}
		/* 26/10/2012 -- David Pattison
		 * Yet another hack to enable negative preconditions in a PG. Having checked for actions which are
		 * activated using the positive literals in the current state, we need to check for actions which are
		 * activated using *negative* preconditions. This is a subtle bug, as actions which contain at-least 
		 * one positive precondition mask the problem at hand. However, if only negative preconditions are
		 * present in the action spec, then the action can only be activated by the addition, and subsequent
		 * deletion of the required Nots -- when in reality they should be activatable right from the initial state
		 * (assuming none of the Nots are true in this state -- the normal code would work if Nots were present in the
		 * initial state, but this is unlikely due to the sheer number of possibilities).
		 * I freely admit this is not a good way to do this, but the existing infrastructure of JavaFF is not geared towards
		 * non-STRIPS usage, so any solution is better than no solution (pun explicitly and unashamedly intended).
		 */
		if (this.negativePCActions.isEmpty())
			return scheduledActs;
		
		STRIPSState hackState = new STRIPSState(); //easier to create a state than duplicate the Not code check for isTrue()
		for (PGFact f : trueFacts)
		{
			hackState.addFact(f.getFact());
		}
		

		//loop through only the actions which we know have ONLY negative preconditions -- no point in checking the others as actions with positive
		//preconditions will always be picked up by the above code.
		for (PGAction a : this.negativePCActions)
		{
			for (PGFact f : a.getConditions())
			{
				if (f.getFact() instanceof Not)
				{
					if (((Not)f.getFact()).isTrue(hackState))
					{
						if (f.getLayer() < 0)
						{
							//if this fact has never been seen in the PG so far (its layer is < 0), say that it appears at this layer -- this will determine its "difficulty"
							f.setLayer(pLayer);

							//add all actions which this fact enables
							scheduledActs.addAll(f.getEnables());
							 
							level_off = false;


						}
					}
				}
			}
			
		}


		return scheduledActs;
	}
	

	public List<Collection<PGAction>> searchRelaxedPlan(Set<PGFact> goalSet, int l)
	{
		if (l == 0)
		{
			//if l is 0 then there are we are in the current state, and there is no need to find the
			//action which achieves the goals true in this state.
			return new ArrayList();
		}
		
		
		Set<PGAction> chosenActions = new HashSet<PGAction>();
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
						break; //always choose NO-Ops if they exist
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
						else if (a.getDifficulty() > na.getDifficulty()) //this is the "min" in h_add
							a = na;
					}
				}
			}

			if (a != null)
			{
				chosenActions.add(a);
			}
		}


		Set<PGFact> newGoalSet = new HashSet<PGFact>();
		// loop through chosen actions adding in propositions and comparators
		Iterator<PGAction> cait = chosenActions.iterator();
		while (cait.hasNext())
		{
			PGAction ca = (PGAction) cait.next();
			newGoalSet.addAll(ca.getConditions());
		}

		//if l == 1, then we want to find helpful actions. These are the actions which are applicable at layer l-1,
		//(the current state), which achieve at-least one of the goals required at layer l. This is NOT the same as
		//actions which achieve one of the Final goals. 
		//
		if (l == 1)
		{
			STRIPSState initialState = new STRIPSState();
			for (PGFact f : this.initial)
			{
				initialState.addFact(f.getFact());
			}
						
			for (PGAction a : chosenActions)
			{
				if (a instanceof PGNoOp)
					continue;
				
				int helpfulness = this.isHelpful(a, goalSet, initialState);
				if (helpfulness >= 0)
				{
					this.helpfulActions.add(new HelpfulAction((InstantAction) a.getAction(), helpfulness));
				}
			}
		}
		
		List<Collection<PGAction>> rplan = this.searchRelaxedPlan(newGoalSet, l - 1);
		rplan.add(chosenActions);
		
		return rplan;
	}
	
	@Override
	protected void resetAll(State s)
	{
		super.resetAll(s);
		
		this.helpfulActions.clear();
	}

	/**
	 * For a STRIPSState, an action is helpful if it adds a goal literal from layer 1 during 
	 * extraction of the relaxed plan, and is applicable in the specified state (i.e. the initial state).
	 *  
	 * @param a The action to check
	 * @param goals The goals present at layer 1 of the RPG, taken from the relaxed-plan extraction process.
	 * @param state The initial/current state of the problem.
	 * @return The number of goal literals achieved provided that the action is applicable, or -1 if the action
	 * is not applicable.
	 */
	protected int isHelpful(PGAction a, Collection<PGFact> goals, State state)
	{
		int achieved = 0;
		
		if (a.getAction().isApplicable(state) == false)
			return -1;
		
		
		for (PGFact g : goals)
		{
//			if (a.achieves.contains(g))
			if (a.getAction().adds(g.getFact()))
			{
//				return 1;
				++achieved;
			}
		}
		
		return achieved;
	}

	public SortedSet<HelpfulAction> getHelpfulActions()
	{
		return helpfulActions;
	}

	public boolean checkPropMutex(MutexPair m, int l)
	{
		return false;
	}

	public boolean checkPropMutex(PGFact p1, PGFact p2, int l)
	{
		return false;
	}

	public boolean checkActionMutex(MutexPair m, int l)
	{
		return false;
	}
	
	//24/11/12 -- David Pattison
	//Without this method being overridden, mutexes are still created by PlanningGraph, even though they will
	//neve be used. This used to be the single biggest bottleneck in JavaFF, as in, 100x speedup.
	@Override
	public ArrayList<PGFact> calculateActionMutexesAndProps(
			Set<PGAction> filteredSet, int pLayer)
	{
		ArrayList<PGFact> scheduledFacts = new ArrayList<PGFact>();

		for (PGAction a : filteredSet)
		{
			scheduledFacts.addAll(a.getAchieves());
			a.setLayer(pLayer);
			level_off = false; //if there is at least one applicable, filtered action, then we haven't levelled out
		}
		
		return scheduledFacts;
	}
	
	@Override
	protected boolean goalMutex()
	{
		return false;
	}
	
	public boolean checkActionMutex(PGAction a1, PGAction a2, int l)
	{
		return false;
	}

	protected boolean noMutexes(Set s, int l)
	{
		return true;
	}

	protected boolean noMutexesTest(Node n, Set s, int l) 
	{
		return true;
	}

}