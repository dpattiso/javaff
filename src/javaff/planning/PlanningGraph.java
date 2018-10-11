/************************************************************************
 * Strathclyde Planning Group,
 * Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK
 * http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey
 * Copyright 2008, Andrew Coles and Amanda Smith
 * Copyright 2011, David Pattison
 *
 * (Questions/bug reports now to be sent to Andrew Coles)
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
import javaff.data.GroundProblem;
import javaff.data.GroundFact;
import javaff.data.Plan;
import javaff.data.TotalOrderPlan;
import javaff.data.adl.Imply;
import javaff.data.metric.Function;
import javaff.data.metric.NamedFunction;
import javaff.data.strips.And;
import javaff.data.strips.InstantAction;
import javaff.data.strips.Not;
import javaff.data.strips.Proposition;
import javaff.data.strips.RelaxedFFPlan;
import javaff.data.strips.STRIPSInstantAction;
import javaff.data.strips.SingleLiteral;
import javaff.data.strips.TrueCondition;
import javaff.data.temporal.DurativeAction;
import javaff.graph.ActionMutex;
import javaff.graph.FactMutex;
import javaff.planning.PlanningGraph.MutexPair;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Hashtable;

public class PlanningGraph
{
	// ******************************************************
	// Data Structures
	// ******************************************************
	protected Map<Fact, PGFact> propositionMap = new Hashtable<Fact, PGFact>(); // (Fact
																				// =>
																				// PGProposition)
	protected Map<Action, PGAction> actionMap = new Hashtable<Action, PGAction>(); // (Action
																				// =>
																				// PGAction)

	protected Set propositions = new HashSet();// cant use Fact generics because,
											// insanely, it is used to store
											// both Propositions and
											// PGPropositions
	protected Set<PGAction> actions = new HashSet<PGAction>();
	protected Set<PGAction> negativePCActions = new HashSet<PGAction>(); //quick lookup for actions which only have negative PCs -- better than looking through all actions at every layer

	protected Set<PGFact> initial, goal;
	protected Set<MutexPair> propMutexes;
	protected Set<MutexPair> actionMutexes;
	public List<Set<PGFact>> memoised; 

	protected List<Set<Fact>> factLayers = new ArrayList<Set<Fact>>();
	

	protected Set<PGAction> readyActions = null; // PGActions that have all
							
	protected boolean level_off = false;
	protected static int NUMERIC_LIMIT = 4;
	protected int numeric_level_off = 0;
	protected int num_layers;

	// ******************************************************
	// Main methods
	// ******************************************************
	protected PlanningGraph()
	{
		this.actionMap = new HashMap<Action, PlanningGraph.PGAction>();
		this.actionMutexes = new HashSet<MutexPair>();
		this.actions = new HashSet<PGAction>();
		this.factLayers = new ArrayList<Set<Fact>>();
		this.goal = new HashSet<PlanningGraph.PGFact>();
		this.initial = new HashSet<PlanningGraph.PGFact>();
		this.level_off = false;
		this.num_layers = 0;
		this.numeric_level_off = 4;
		this.propMutexes = new HashSet();
		this.propositionMap = new HashMap<Fact, PlanningGraph.PGFact>();
		this.propositions = new HashSet();
		this.readyActions = new HashSet<PlanningGraph.PGAction>();
		this.memoised = new ArrayList<Set<PGFact>>();
	}
	
//	/**
//	 * Initialise this planning graph based upon another. Note that this does not actually construct
//	 * the PG, see clone() for that functionality. Instead, it is intended for PGs which have the same
//	 * resources (actions, proposition etc), so that the internals of the PG do not need to be set
//	 * up every time. 
//	 * 
//	 * @param existingGraph
//	 */
//	public PlanningGraph(PlanningGraph existingGraph)
//	{
//		this();
//		
//		this.actionMap = existingGraph.actionMap;
//		this.actionMutexes = existingGraph.actionMutexes;
//		this.actions = existingGraph.actions;
////		this.factLayers = existingGraph.factLayers;
//		this.goal = existingGraph.goal;
//		this.initial = existingGraph.initial;
////		this.level_off = existingGraph.level_off;
////		this.num_layers = existingGraph.num_layers;
////		this.numeric_level_off = existingGraph.numeric_level_off;
//		this.propMutexes = existingGraph.propMutexes;
//		this.propositionMap = existingGraph.propositionMap;
//		this.propositions = existingGraph.propositions;
////		this.readyActions = existingGraph.readyActions;
////		this.state = existingGraph.state;
//	}

	public PlanningGraph(GroundProblem gp)
	{
		this(gp.getActions(), gp.getGoal());
//		this.setInitial(gp.state);
	}

	public PlanningGraph(Set groundActions, GroundFact goal)
	{
		this();

		setActionMap(groundActions);
		setLinks();
		createNoOps();
		setGoal(goal);
	}
	

	/**
	 * Populate a new plan graph from an existing plan graph. Note that the data within the existing graph
	 * is only references, not cloned/ 
	 * @param existingPG
	 */
	protected PlanningGraph(PlanningGraph existingPG)
	{
		this.actionMap = existingPG.actionMap;

		this.actionMutexes = existingPG.actionMutexes;
		this.actions = existingPG.actions;
		this.factLayers = existingPG.factLayers;
		this.goal = existingPG.goal;


		this.initial = existingPG.initial;
		this.level_off = existingPG.level_off;
		this.num_layers = existingPG.num_layers;
		this.numeric_level_off = existingPG.numeric_level_off;
		this.propMutexes = existingPG.propMutexes;
		this.propositionMap = existingPG.propositionMap;
		this.propositions = existingPG.propositions;
		this.readyActions = existingPG.readyActions;
		this.negativePCActions = existingPG.negativePCActions;
		this.memoised = existingPG.memoised;
	}

	public Object clone()
	{
		PlanningGraph clone = new PlanningGraph();
		clone.actionMap = new HashMap<Action, PlanningGraph.PGAction>(
				this.actionMap);

		clone.actionMutexes = new HashSet<MutexPair>(this.actionMutexes);
		clone.actions = new HashSet<PGAction>(this.actions);
		clone.factLayers = new ArrayList<Set<Fact>>(this.factLayers);
		clone.goal = new HashSet<PGFact>(this.goal);


		clone.initial = new HashSet<PGFact>(this.initial);
		clone.level_off = this.level_off;
		clone.num_layers = this.num_layers;
		clone.numeric_level_off = this.numeric_level_off;
		clone.propMutexes = new HashSet<MutexPair>(this.propMutexes);
		clone.propositionMap = new HashMap<Fact, PlanningGraph.PGFact>(
				this.propositionMap);
		clone.propositions = new HashSet(this.propositions);
		clone.readyActions = new HashSet<PlanningGraph.PGAction>(
				this.readyActions);
		clone.negativePCActions = new HashSet<PGAction>(this.negativePCActions);
		clone.memoised = new ArrayList<Set<PGFact>>(this.memoised);

		return clone;
	}
	
	
	/**
	 * The creation of a Planning Graph is an extremely costly process in both time and resources. This is
	 * primarily due to the need to recreate information used during the construction process, but for most problems
	 * this information will never change between instances of PG. Therefore, this method creates a new PG while
	 * retaining all this information which would otherwise be destroyed and recreated (such as mutex relationships
	 * etc).
	 * @return An empty PlanningGraph, which has already had its preprocessing information computed/copied from 
	 * this PG.
	 */
	public PlanningGraph branch()
	{
		PlanningGraph branch = new PlanningGraph();
	
		branch.actionMap = this.actionMap;
		branch.actionMutexes = this.actionMutexes;
		branch.actions = this.actions;
//		branch.factLayers = this.factLayers;
//		branch.goal = this.goal;
//		branch.initial = this.initial;
//		branch.level_off = this.level_off;
//		branch.num_layers = this.num_layers;
//		branch.numeric_level_off = this.numeric_level_off;
		branch.propMutexes = this.propMutexes;
		branch.propositionMap = this.propositionMap;
		branch.propositions = this.propositions;
		branch.negativePCActions = this.negativePCActions;
//		branch.readyActions = this.readyActions;
//		branch.state = this.state;
		
		return branch;
	}
	
	/**
	 * Returns the number of fact layers in the graph, including the initial state.
	 * @return
	 */
	public int size()
	{
		return this.num_layers;
	}

	
	public Plan getPlan(State s)
	{
		setInitial(s);
		resetAll(s);
		// AND oldGoal = new AND(this.goal);
		setGoal(s.goal);

		// set up the initial set of facts
		List<PGFact> scheduledFacts = new ArrayList<PGFact>(initial);
		scheduledFacts = this.filterFactLayer(scheduledFacts);
		List<PGAction> scheduledActs = null;

		scheduledActs = this.createFactLayer(scheduledFacts, 0);
		List plan = null;

		//can't remember why I did this... so I'm just leaving it. Probably something to do with not destroying object references.
		HashSet<Fact> realInitial = new HashSet<Fact>();
		for (PGFact i : this.initial)
		{
			realInitial.add((Fact) i.getFact());
		}

		this.factLayers.add(realInitial); // add current layer

		// create the graph==========================================
		while (true)
		{
			scheduledFacts = this.createActionLayer(scheduledActs, num_layers);
			scheduledFacts = this.filterFactLayer(scheduledFacts);
//			if (scheduledFacts != null && scheduledFacts.isEmpty())
//			{
//				this.level_off = true;
//				break;
//			}
			
			++num_layers;
			scheduledActs = this.createFactLayer(scheduledFacts, num_layers);

			if (scheduledFacts != null)
			{
				HashSet<Fact> factList = new HashSet<Fact>();
				// plan = extractPlan();
				for (Object pgp : scheduledFacts)
					factList.add(((PGFact) pgp).getFact());

				boolean res = factList.addAll(this.factLayers.get(num_layers - 1));
//				if (res == false)
//				{
//					--this.num_layers;
//					this.level_off = true;
//					break; //no new facts added
//				}

				this.factLayers.add(factList); // add current layer

			}
			
//			currentState = this.applyActions(currentState, scheduledActs);

			if (this.goalMet() && !this.goalMutex())
			{
				plan = this.extractPlan();
			}
			if (plan != null)
				break;
			if (!level_off)
				numeric_level_off = 0;
			if (level_off || numeric_level_off >= NUMERIC_LIMIT)
			{
				// printGraph();
				break;
			}
		}
//		this.printGraph();

		Plan top = this.constructPlan(plan, s.goal);
		
		//assert(top.getPlanLength() == num_layers);
//		if (top.getPlanLength() != (num_layers-1))
//			System.out.println("WRONG");

		return top;

	}
	
	/**
	 * Construct a {@link TotalOrderPlan} for the Planning graph. This method is called when a Plan must be returned, and acts as a hotspot for overriding what a plan
	 * graph classes as a "plan".
	 * @param plan A collection of collections. In this class (@{link {@link PlanningGraph}), the first collection is a list, and the second collection is a series
	 *  of individual collections containing a single action in each. This is really a side-effect of having to use the RPG elsewhere in JavaFF, and the introduction of
	 *  {@link RelaxedFFPlan} as a subtype of {@link Plan}.
	 * @param goal
	 * @return
	 */
	protected Plan constructPlan(Collection<Collection<PGAction>> plan, Fact goal)
	{

		TotalOrderPlan p = null;
		if (plan != null)
		{
			p = new TotalOrderPlan(goal);
			Iterator pit = plan.iterator();
			while (pit.hasNext())
			{
				PGAction a = (PGAction) pit.next();
				if (!(a instanceof PGNoOp))
					p.addAction(a.getAction());
			}
			// p.print(javaff.JavaFF.infoOutput);
			return p;
		}
		// this.setGoal(oldGoal);
		
		return p;
	}

	// GET LAYER CONTAINING needs changed to reflect single literals,
	// conjunctions, ors, etc
	public Plan getPlanFromExistingGraph(Fact g)
	{
		readyActions = new HashSet();

		this.setGoal(g);

		List plan = null;
		if (this.goalMet() && !this.goalMutex())
		{
			plan = this.extractPlan();
		}

		Plan p = this.constructPlan(plan, g);

		return p;
	}

	protected List<PGFact> filterFactLayer(List<PGFact> facts)
	{
		//FIXME in the same way that action which have only negative preconditions are cached, so should all which have at least one negative PC
		//because this loop is extremely inefficient
		
		ArrayList<PGFact> set = new ArrayList<PlanningGraph.PGFact>(facts);
		
		//this for loop is necessary for negative preconditions. Essentially, it checks if an 
		//action which is otherwise scheduled to be checked at this layer (caused by it having
		//at-least one positive precondition) has a negative precondition. If it does, and the 
		//previous layer does not explicitly have this negative fact, we increment its counter
		//by 1, which is us effectively saying "this negative precondition is implicitly true". 
		//Without this increment, the requirement of having N actions achieve the N preconditions
		//will never be met in getAvailableActions(), so the action may never be applicable or 
		//only applicable in layers later than it should be.
		for (PGFact f : facts)
		{
			for (PGAction a : f.enables)
			{
				for (PGFact pc : a.conditions)
				{
					if (pc.getFact() instanceof Not)
					{
						//if a negative precondition is not explicit, assume it is true by its absence
						set.add(pc);
//						if (set.contains(pc) == false)
//						{
//							a.setCounter(a.getCounter() + 1);
//							a.setDifficulty(a.getDifficulty() + num_layers);
//						}
					}
				}
			}
		}
		
		return set;
	}
	
	/**
	 * Build the PG until it is fully stable with the specified state (including
	 * goal), but do not construct any kind of plan.
	 * 
	 * @param s
	 * @return
	 */
	//FIXME merge this code into getPlan()/remove duplication
	public void constructStableGraph(State init)
	{
		resetAll(init);
		setInitial(init);
		setGoal(init.goal);

		// set up the initial set of facts
		List<PGFact> scheduledFacts = new ArrayList<PGFact>(this.initial);
		scheduledFacts = this.filterFactLayer(scheduledFacts);
		List<PGAction> scheduledActs = null;
		
		scheduledActs = createFactLayer(scheduledFacts, 0);
		
		List plan = null;

		//
		HashSet<Fact> realInitial = new HashSet<Fact>();
		for (PGFact i : this.initial)
		{
			realInitial.add((Fact) i.getFact());
		}

		this.factLayers.add(realInitial); // add current layer
		// this.pgFactLayers.add(scheduledFacts); //add current layer

		// create the graph==========================================
		while (true)
		{

			
			scheduledFacts = createActionLayer(scheduledActs, num_layers);
			scheduledFacts = this.filterFactLayer(scheduledFacts);

			++num_layers;
			scheduledActs = createFactLayer(scheduledFacts, num_layers);

			if (scheduledFacts != null)
			{
				HashSet factList = new HashSet();
				// plan = extractPlan();
				for (Object pgp : scheduledFacts)
					factList.add(((PGFact) pgp).getFact());

				boolean res = factList.addAll(this.factLayers.get(num_layers-1));
//				if (res == false)
//				{
//					--this.num_layers;
//					this.level_off = true;
//					break; //no new facts added
//				}
				
				this.factLayers.add(factList); // add current layer

			}

			if (!level_off)
				numeric_level_off = 0;
			if (level_off || numeric_level_off >= NUMERIC_LIMIT)
			{
//				 printGraph();
				break;
			}
		}
	}

	public Set<Fact> getFactsAtLayer(int i)
	{
		return this.factLayers.get(i);
	}
	

	public Set<Action> getActionsAtLayer(int l)
	{
		HashSet<Action> app = new HashSet<Action>();
		if (l < 0)
			return app;
		
		for (Entry<Action, PGAction> a : this.actionMap.entrySet())
		{
			if (a.getValue().getLayer() == l)
//			if (a.getValue().layer <= l && a.getValue().layer >= 0)
				app.add(a.getKey());
		
		}
		
		return app;
	}
	

//	public Set<Action> getActionsUpToLayer(int l)
//	{
//		HashSet<Action> app = new HashSet<Action>();
//		for (Entry<Action, PGAction> a : this.actionMap.entrySet())
//		{
//			if (a.getValue().layer <= l)
//				app.add(a.getKey());
//		
//		}
//		
//		return app;
//	}

	/**
	 * Returns the distance/layer which contains the first instance of the
	 * specified proposition.
	 * 
	 * @param p
	 * @return The distance to the proposition, or -1 if it is not found in any
	 *         layer.
	 */
	public int getLayerContaining(Fact p)
	{
		for (int i = 0; i < this.factLayers.size(); i++)
		{
			if (this.factLayers.get(i).contains(p))
				return i;
		}

		return -1;
	}

	public int getFactLayerSize()
	{
		return this.factLayers.size();
	}

	// ******************************************************
	// Setting it all up
	// ******************************************************
	protected void setActionMap(Set<Action> gactions)
	{
		Queue<Action> queue = new LinkedList<Action>(gactions);
		while (queue.isEmpty() == false)
		{
			Action a = queue.remove();

			PGAction pga = new PGAction(a);
			actionMap.put(a, pga);
			actions.add(pga);
		}
	}

	protected PGFact getPGFact(Fact p)
	{

		Object o = propositionMap.get(p);
		PGFact pgp;
		if (o == null)
		{
			pgp = new PGFact(p);
			propositionMap.put(p, pgp);
			propositions.add(pgp);
		}
		else
			pgp = (PGFact) o;
		return pgp;
	}

	protected void setLinks()
	{
		for (PGAction pga : this.actions)
		{
//			//special case for actions which have no preconditions, or whose preconditions
//			//are static and maybe removed by optimisation processes.
//			if (pga.conditions.isEmpty())
//			{
//				this.emptyPreconditions.add(pga);
//			}
			

			boolean onlyNegativePcs = true;
			Set<Fact> pcs = pga.getAction().getPreconditions();
			for (Fact p : pcs)
			{				
				PGFact pgp = this.getPGFact(p);
				pga.getConditions().add(pgp);
				pgp.getEnables().add(pga);
				
				//if there is at least one precondition which is positive
				//then it is not worth recording it, as the PG construction
				//will work as normal. Actions which only have negative PCs
				//need special consideration in createFactLayer().
				if (p instanceof Not == false)
				{
					onlyNegativePcs = false;
				}
			}
			if (onlyNegativePcs)
			{
				this.negativePCActions.add(pga);
			}

			Set<Fact> adds = pga.getAction().getAddPropositions();
			for (Fact p : adds)
			{
				PGFact pgp = this.getPGFact(p);
				pga.getAchieves().add(pgp);
				pgp.getAchievedBy().add(pga);
			}

			Set<Not> dels = pga.getAction().getDeletePropositions();
			for (Not p : dels)
			{
				//now that negative preconditions are allowed, we have to say that deleting facts
				//actually "achieves" them in order for PG and RPG to work
				PGFact pgNot = this.getPGFact(p);
				PGFact pgFact = this.getPGFact(p.getLiteral());
				
				//say that the actual deleted fact is deleted by this action, and that this action
				//deletes the (positive) fact
				pga.getDeletes().add(pgFact);
				pgFact.getDeletedBy().add(pga);

				//then say that this action also "achieves" the negated literal, and that this
				//negated literal is achieves by the action
				pga.getAchieves().add(pgNot);
				pgNot.getAchievedBy().add(pga);
			}
		}
	}

	protected void resetAll(State s)
	{
		factLayers = new ArrayList<Set<Fact>>();

		//TODO should these resets be ignored? If we use another PG to create this one then we're just destroying
		//all the hard work of the previous PG
		propMutexes = new HashSet();
		actionMutexes = new HashSet();

		memoised.clear();

		readyActions = new HashSet();

		num_layers = 0;

		Iterator ait = actions.iterator();
		while (ait.hasNext())
		{
			PGAction a = (PGAction) ait.next();
			a.reset();
		}

		Iterator pit = propositions.iterator();
		while (pit.hasNext())
		{
			PGFact p = (PGFact) pit.next();
			p.reset();
		}
	}

	public void setGoal(Fact g)
	{
		goal = new HashSet();
		for (Fact p : g.getFacts())
		{
			PGFact pgp = getPGFact(p);
			goal.add(pgp);
		}
	}

	public void setInitial(State S)
	{
		this.initial = new HashSet<PGFact>();
		
		//always add a TrueCondition to allow empty-precondition actions to execute
		PGFact truePGFact = this.getPGFact(TrueCondition.getInstance());
		initial.add(truePGFact);
		
		for (Fact p : ((STRIPSState) S).getFacts())
		{
			PGFact pgp = this.getPGFact(p);
			this.initial.add(pgp);
		}
	}

	protected void createNoOps()
	{
		for (Object po : this.propositions)
		{
			PGFact p = (PGFact) po;
			PGNoOp n = new PGNoOp(p);
			n.getConditions().add(p);
			n.getAchieves().add(p);
			p.getEnables().add(n);
			p.getAchievedBy().add(n);
			actions.add(n);
		}
	}

	// ******************************************************
	// Graph Construction
	// ******************************************************

	protected ArrayList<PGAction> createFactLayer(List<PGFact> trueFacts, int pLayer)
	{
		memoised.add(new HashSet<PGFact>());
		ArrayList<PGAction> scheduledActs = new ArrayList<PGAction>();
		HashSet<MutexPair> newMutexes = new HashSet<MutexPair>();
		
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

				// calculate mutexes
				if (pLayer != 0)
				{
					Iterator pit = propositions.iterator();
					while (pit.hasNext())
					{
						PGFact p = (PGFact) pit.next();
						if (p.getLayer() >= 0 && this.checkPropMutex(f, p, pLayer))
						{
							this.makeMutex(f, p, pLayer, newMutexes);
						}
					}
				}

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

							// calculate mutexes
							if (pLayer != 0)
							{
								Iterator pit = propositions.iterator();
								while (pit.hasNext())
								{
									PGFact p = (PGFact) pit.next();
									if (p.getLayer() >= 0 && this.checkPropMutex(f, p, pLayer))
									{
										this.makeMutex(f, p, pLayer, newMutexes);
									}
								}
							}

						}
					}
				}
			}
			
		}

		// check old mutexes
		Iterator pmit = propMutexes.iterator();
		while (pmit.hasNext())
		{
			MutexPair m = (MutexPair) pmit.next();
			if (checkPropMutex(m, pLayer))
			{
				this.makeMutex(m.getNode1(), m.getNode2(), pLayer, newMutexes);
			}
			else
			{
				level_off = false;
			}
		}

		// add new mutexes to old mutexes and remove those which have
		// disappeared
		propMutexes = newMutexes;

		return scheduledActs;
	}

	public boolean checkPropMutex(MutexPair m, int l)
	{
		return checkPropMutex((PGFact) m.getNode1(), (PGFact) m.getNode2(), l);
	}

	public boolean checkPropMutex(Fact p1, Fact p2, int l)
	{
		if (p1 == p2)
			return false;

		PGFact pgp1 = this.getPGFact(p1);
		PGFact pgp2 = this.getPGFact(p2);

		if (pgp1 == null || pgp2 == null)
			return false;

		return this.checkPropMutex(pgp1, pgp2, l);
	}

	protected boolean checkPropMutex(PGFact p1, PGFact p2, int l)
	{
		if (p1 == p2)
			return false;

		// Componsate for statics
		if (p1.getAchievedBy().isEmpty() || p2.getAchievedBy().isEmpty())
			return false;

		Iterator a1it = p1.getAchievedBy().iterator();
		while (a1it.hasNext())
		{
			PGAction a1 = (PGAction) a1it.next();
			if (a1.getLayer() >= 0)
			{
				Iterator a2it = p2.getAchievedBy().iterator();
				while (a2it.hasNext())
				{
					PGAction a2 = (PGAction) a2it.next();
					if (a2.getLayer() >= 0 && !a1.mutexWith(a2, l - 1))
						return false;
				}
			}

		}
		return true;
	}

	protected void makeMutex(Node n1, Node n2, int l, Set<MutexPair> mutexPairs)
	{
		n1.setMutex(n2, l);
		n2.setMutex(n1, l);
		mutexPairs.add(new MutexPair(n1, n2));
	}

	protected ArrayList<PGFact> createActionLayer(List<PGAction> pActions,
			int pLayer)
	{
		level_off = true;
		HashSet<PGAction> actionSet = this
				.getAvailableActions(pActions, pLayer);
		actionSet.addAll(readyActions);
		readyActions = new HashSet<PGAction>();
		HashSet<PGAction> filteredSet = this.filterSet(actionSet, pLayer);
		ArrayList<PGFact> scheduledFacts = this.calculateActionMutexesAndProps(
				filteredSet, pLayer);
		return scheduledFacts;
	}

	/*
	 * 24/5/2011 - David Pattison From what I can tell, this method returns the
	 * set of actions whose preconditions have been satisfied. But this is done
	 * by comparing the number of PCs against the number of actions in the List
	 * provided which satisfy at least one of the PCs. I guess the idea is that
	 * as each PC (PGFact) is mapped to X actions which require it, when the set
	 * of actions which is passed in is constructed, if the layer at p-1
	 * contains N PCs, there will be at least N actions in the list (hence why
	 * it is a list- allows duplicates). If |A| < N, then the actions cannot be
	 * applicable. This is a both a genius and terrible way to do this.
	 */
	protected HashSet<PGAction> getAvailableActions(List<PGAction> pActions,
			int pLayer)
	{
		HashSet<PGAction> actionSet = new HashSet<PGAction>();
		for (PGAction a : pActions)
		{
			
			if (a.getLayer() < 0)
			{
				a.setCounter(a.getCounter() + 1);
				a.setDifficulty(a.getDifficulty() + pLayer);
				if (a.counter >= a.conditions.size())
				{
					actionSet.add(a);
					level_off = false;
				}
			}
		}
		return actionSet;
	}
	
	//this is how PG actions were being chosen for quite some time (non vanilla JavaFF) -- it is wrong. 
	//The bug is in checking to see if an actionis applicable in the current state. the problem is this current
	//state is formed in an RPG style (probalby a cut-paste job at some point), where all facts are true in a
	//single state, which makes more actions applicable than should be. Current version of 
	//getAvailableActions() just uses the vanilla JavaFF/Graphplan approach
//	protected HashSet<PGAction> getAvailableActions(List<PGAction> pActions,
//			int pLayer)
//	{
//		STRIPSState hackState = new STRIPSState();
//		hackState.addFacts(factLayers.get(pLayer));
//
//		HashSet<PGAction> actionSet = new HashSet<PGAction>();
//		for (PGAction a : pActions)
//		{
//			if (a.getLayer() < 0)
//			{
//				a.setCounter(a.getCounter() + 1);
//				a.setDifficulty(a.getDifficulty() + pLayer);
//				if (a instanceof PGNoOp || a.getAction().isApplicable(hackState)) //FIXME this breaks non-STRIPS planning
//				{
//					a.setLayer(pLayer);
//					actionSet.add(a);
//					level_off = false;
//				}
//			}
//		}
//		return actionSet;
//	}

	protected HashSet<PGAction> filterSet(Set<PGAction> pActions, int pLayer)
	{
		HashSet<PGAction> filteredSet = new HashSet<PGAction>();
		for (PGAction a : pActions)
		{
			if (this.noMutexes(a.getConditions(), pLayer))
				filteredSet.add(a);
			else
				readyActions.add(a);
		}
		return filteredSet;
	}

	public ArrayList<PGFact> calculateActionMutexesAndProps(
			Set<PGAction> filteredSet, int pLayer)
	{
		HashSet<MutexPair> newMutexes = new HashSet<MutexPair>();

		ArrayList<PGFact> scheduledFacts = new ArrayList<PGFact>();

		for (PGAction a : filteredSet)
		{
			scheduledFacts.addAll(a.getAchieves());
			a.setLayer(pLayer);
			level_off = false;

			// caculate new mutexes
			Iterator a2it = actions.iterator();
			while (a2it.hasNext())
			{
				PGAction a2 = (PGAction) a2it.next();
				if (a2.getLayer() >= 0 && checkActionMutex(a, a2, pLayer))
				{
//					System.out.println("adding action mutex at layer " + pLayer
//							+ "- " + a2 + " <-> " + a);
					this.makeMutex(a, a2, pLayer, newMutexes);
				}
			}
		}
		// check old mutexes
		Iterator amit = actionMutexes.iterator();
		while (amit.hasNext())
		{
			MutexPair m = (MutexPair) amit.next();
			if (checkActionMutex(m, pLayer))
			{
				this.makeMutex(m.getNode1(), m.getNode2(), pLayer, newMutexes);
			}
			else
			{
				level_off = false;
			}
		}

		// add new mutexes to old mutexes and remove those which have
		// disappeared
		actionMutexes = newMutexes;
		return scheduledFacts;
	}

	public boolean checkActionMutex(MutexPair m, int l)
	{
		return checkActionMutex((PGAction) m.getNode1(), (PGAction) m.getNode2(), l);
	}
	
	public boolean checkActionMutex(PGAction a1, PGAction a2, int l)
	{
		if (a1 == a2)
			return false;

		//check for delete mutex (A1 delete PC or Add of A2)
		Iterator p1it = a1.getDeletes().iterator();
		while (p1it.hasNext())
		{
			PGFact p1 = (PGFact) p1it.next();
			
			if (a2.getAchieves().contains(p1))
				return true;
			if (a2.getConditions().contains(p1))
				return true;
		}
		
		//check for delete mutex (A2 delete PC or Add of A1)
		Iterator p2it = a2.getDeletes().iterator();
		while (p2it.hasNext())
		{
			PGFact p2 = (PGFact) p2it.next();
			if (a1.getAchieves().contains(p2))
				return true;
			if (a1.getConditions().contains(p2))
				return true;
		}

		//check to see if any PCs of A1 and A2 are mutex
		Iterator pc1it = a1.getConditions().iterator();
		while (pc1it.hasNext())
		{
			PGFact p1 = (PGFact) pc1it.next();
			Iterator pc2it = a2.getConditions().iterator();
			while (pc2it.hasNext())
			{
				PGFact p2 = (PGFact) pc2it.next();
				if (p1.mutexWith(p2, l))
					return true;
			}
		}

		return false;
	}

//	public boolean checkActionMutex(PGAction a1, PGAction a2, int l)
//	{
//		if (a1 == a2)
//			return false;
//
//		//check for delete mutex (A1 delete PC or Add of A2)
//		Iterator p1it = a1.getDeletes().iterator();
//		while (p1it.hasNext())
//		{
//			PGFact p1 = (PGFact) p1it.next();
//			
//			if (a2.getAchieves().contains(p1))
//				return true;
//			if (a2.getConditions().contains(p1))
//				return true;
//		}
//		
//		//check for delete mutex (A2 delete PC or Add of A1)
//		Iterator p2it = a2.getDeletes().iterator();
//		while (p2it.hasNext())
//		{
//			PGFact p2 = (PGFact) p2it.next();
//			if (a1.getAchieves().contains(p2))
//				return true;
//			if (a1.getConditions().contains(p2))
//				return true;
//		}
//
//		//check to see if any PCs of A1 and A2 are mutex
//		Iterator pc1it = a1.getConditions().iterator();
//		while (pc1it.hasNext())
//		{
//			PGFact p1 = (PGFact) pc1it.next();
//			Iterator pc2it = a2.getConditions().iterator();
//			while (pc2it.hasNext())
//			{
//				PGFact p2 = (PGFact) pc2it.next();
//				if (p1.mutexWith(p2, l))
//					return true;
//			}
//		}
//
//		return false;
//	}

	protected boolean goalMet()
	{
		for (PGFact p : this.goal)
		{
			if (p.getLayer() < 0)
			{
				return false;
			}
		}
		return true;
	}

	protected boolean goalMutex()
	{
		return !noMutexes(this.goal, num_layers);
	}

	protected boolean noMutexes(Set s, int l)
	{
		Iterator sit = s.iterator();
		if (sit.hasNext())
		{
			Node n = (Node) sit.next();
			HashSet s2 = new HashSet(s);
			s2.remove(n);
			Iterator s2it = s2.iterator();
			while (s2it.hasNext())
			{
				Node n2 = (Node) s2it.next();
				if (n.mutexWith(n2, l))
					return false;
			}
			return noMutexes(s2, l);
		}
		else
			return true;
	}

	protected boolean noMutexesTest(Node n, Set<PGAction> s, int l) // Tests to see if
															// there is a mutex
															// between n and all
															// nodes in s
	{
		Iterator<PGAction> sit = s.iterator();
		while (sit.hasNext())
		{
			Node n2 = (Node) sit.next();
			if (n.mutexWith(n2, l))
				return false;
		}
		return true;
	}

	// ******************************************************
	// Plan Extraction
	// ******************************************************

	/**
	 * Extracts a valid plan from the PG as it currently stands.
	 * @return A list of {@link PGAction}s which achieve this PG's goal
	 * @see #setGoal(Fact)
	 */
	public List<PGAction> extractPlan()
	{
		return searchPlan(this.goal, num_layers);
	}

	/**
	 * Searches for a plan which achieves the specified set of goals,
	 * starting at the specified layer of the PG.
	 * @param goalSet The set of goals to find a plan for
	 * @param l The layer to start the extraction process on
	 * @return A list of {@link PGAction}s which achieve the specified goals
	 */
	public List<PGAction> searchPlan(Set<PGFact> goalSet, int l)
	{

		if (l == 0)
		{
			if (initial.containsAll(goalSet))
				return new ArrayList<PGAction>();
			else
				return null;
		}
		// get the set of goals at this layer which are known not to have a 
		// supporting plan. If any of the goals we want are in this set, return
		// null as there is no possible plan
		Set<PGFact> badGoalSet = memoised.get(l);
		if (badGoalSet.isEmpty() == false)
		{
			if (badGoalSet.contains(goalSet))
				return null;
			
//			for (PGFact g : goalSet)
//			{
//				 if (badGoalSet.contains(g))
//					 return null;
//			}
		}

		List<Set<PGAction>> ass = searchLevel(goalSet, (l - 1)); // returns a set of sets of
													// possible action
													// combinations
		//go round each NON-mutex set
		//if a solution (valid solution to the PCs produced by each non mutex set of parallel actions)
		//is found, exit immediately
		for (Set<PGAction> as : ass)
		{
			Set<PGFact> newgoal = new HashSet<PGFact>();

			for (PGAction a : as)
			{
				// construct a new goal set
				// from the non-mutex action
				// set's effects
				
				 //deprecated, as we now keep static facts in actions -- 
				//vanilla JavaFF removes these, but this will cause invalid plans to be produced
				//unless we explicitly ignore them here
				newgoal.addAll(a.getConditions());
//				for (PGFact f : a.getConditions())
//				{
//					if (!f.getFact().isStatic())
//						newgoal.add(f);
//				}
			}

			List<PGAction> al = searchPlan(newgoal, (l - 1)); // try to find a plan to
													// this new goal
			if (al != null)
			{
				List<PGAction> plan = new ArrayList<PGAction>(al);
				plan.addAll(as);
				return plan; // if a plan was found, return it, else loop to the
								// next set.
			}

		}

		// do more memorisation stuff
		badGoalSet.addAll(goalSet);
		return null;

	}

	/**
	 * Searches a specific level of the PG for a set of goal achievers. 
	 * @param goalSet The set of goals to find achievers for
	 * @param layer
	 * @return A list of non-mutex sets of actions which can be applied at this layer
	 */
	public List<Set<PGAction>> searchLevel(Set<PGFact> goalSet, int layer)
	{
		if (goalSet.isEmpty())
		{
			Set<PGAction> s = new HashSet<PGAction>();
			List<Set<PGAction>> li = new ArrayList<Set<PGAction>>();
			li.add(s);
			return li;
		}

		List<Set<PGAction>> actionSetList = new ArrayList<Set<PGAction>>();
		Set<PGFact> newGoalSet = new HashSet<PGFact>(goalSet);

		Iterator<PGFact> git = goalSet.iterator();
		PGFact g = (PGFact) git.next(); 
		newGoalSet.remove(g); //pop the first thing off the queue and deal with the others in a recursive call?

		// always prefer No-ops
		for (PGAction a : g.getAchievedBy())
		{
			// System.out.println("Checking "+a+" for no op");
			if ((a instanceof PGNoOp) && a.getLayer() <= layer && a.getLayer() >= 0)
			{
				Set<PGFact> newnewGoalSet = new HashSet<PGFact>(newGoalSet);
				newnewGoalSet.removeAll(a.getAchieves());

				//search the same layer for the remaining goals
				List<Set<PGAction>> allActionCombinations = this.searchLevel(newnewGoalSet, layer);

				for (Set<PGAction> actionSet : allActionCombinations)
				{
					if (this.noMutexesTest(a, actionSet, layer))
					{
						actionSet.add(a);
						actionSetList.add(actionSet);
					}
				}
			}
		}
		
		for (PGAction a : g.getAchievedBy())
		{
			// ignore no-ops
			if (!(a instanceof PGNoOp) && a.getLayer() <= layer && a.getLayer() >= 0)
			{
				Set<PGFact> newnewGoalSet = new HashSet<PGFact>(newGoalSet); // copy over
																// current goal
																// set
				newnewGoalSet.removeAll(a.getAchieves()); // remove all facts
														// achieved by A
				//search the same layer for the remaining goals
				List<Set<PGAction>> allActionCombinations = this.searchLevel(newnewGoalSet, layer);
				
				for (Set<PGAction> actionSet : allActionCombinations)
				{
					if (this.noMutexesTest(a, actionSet, layer))
					{
						actionSet.add(a);
						actionSetList.add(actionSet);
					}
				}
			}
		}

		return actionSetList;
	}

	// ******************************************************
	// Useful Methods
	// ******************************************************

	public int getLayer(Action a)
	{
		PGAction pg = (PGAction) actionMap.get(a);
		return pg.getLayer();
	}

	// ******************************************************
	// protected Classes
	// ******************************************************
	public class Node
	{
		private int layer;
		private Set mutexes;

		private Map mutexTable;
		
		//speed up access to hashcodes
		private int hash;

		public Node()
		{
			this.mutexes = new HashSet(1);
			this.mutexTable = new HashMap(1);
			
			this.updateHash();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj == null || obj instanceof Node == false)
				return false;
			
			Node other = (Node) obj;
			
			boolean eq = this.getLayer() == other.getLayer();
			if (!eq)
				return eq;
			
			eq = this.getMutexes().equals(other.getMutexes());
			if (!eq)
				return eq;
			
//			eq = this.getMutexTable().equals(other.getMutexTable());
//			if (!eq)
//				return eq;
			
			return true;
			
		}		
		
		private int updateHash()
		{
			this.hash = this.getLayer() ^ this.getMutexes().hashCode() ^ this.getMutexTable().hashCode() ^ 31;
			
			return this.hash;
		}
		
		@Override
		public int hashCode() 
		{
//			return this.hash;
			
			return this.updateHash(); //in reality, faster to just compute the hash as needed rather than caching it
		}

		public void reset()
		{
			setLayer(-1);
			setMutexes(new HashSet(1));
			setMutexTable(new Hashtable(1));
			
			this.updateHash();
		}

		public void setMutex(Node n, int l)
		{
			n.getMutexTable().put(this, new Integer(l));
			this.getMutexTable().put(n, new Integer(l));
			
//			this.updateHash();
		}

		public boolean mutexWith(Node n, int l)
		{
			/*
			 * if (this == n) return false; Iterator mit = mutexes.iterator();
			 * while (mit.hasNext()) { Mutex m = (Mutex) mit.next(); if
			 * (m.contains(n)) { return m.layer >= l; } } return false;
			 */
			Object o = getMutexTable().get(n);
			if (o == null)
				return false;
			Integer i = (Integer) o;
			return i.intValue() >= l;
		}

		public int getLayer() {
			return layer;
		}

		public void setLayer(int layer) {
			this.layer = layer;
		}

		public Set getMutexes() {
			return mutexes;
		}

		public void setMutexes(Set mutexes) {
			this.mutexes = mutexes;
		}

		public Map getMutexTable() {
			return mutexTable;
		}

		public void setMutexTable(Map mutexTable) {
			this.mutexTable = mutexTable;
		}
	}

	public class PGAction extends Node
	{
		private Action action;
		private int counter;
		private int difficulty;


		private Set<PGFact> conditions;
		private Set<PGFact> achieves;
		private Set<PGFact> deletes;
		
		private int hash;

		public PGAction()
		{
			this.action = null;
			this.counter = -1;
			this.difficulty = -1;
			
			this.setConditions(new HashSet<PlanningGraph.PGFact>());
			this.setAchieves(new HashSet<PlanningGraph.PGFact>());
			this.setDeletes(new HashSet<PlanningGraph.PGFact>());
			
			this.updateHash();
		}

		public PGAction(Action a)
		{
			this();
			
			setAction(a);
			
			this.updateHash();
		}
		
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj == null || obj instanceof PGAction == false)
				return false;
			
			PGAction other = (PGAction) obj;
			
			boolean eq = super.equals(other);
			if (!eq)
				return eq;
			
			eq = (this.getCounter() == other.getCounter()) && (this.getLayer() == other.getLayer());
			if (!eq)
				return eq;
			
			if (this.getAction() == null ^ other.getAction() == null)
				return false;
			else if (this.getAction() != null && other.getAction() != null)
			{
				eq = this.getAction().equals(other.getAction());
				if (!eq)
					return eq;
			}
			
			return true;
		}	
		
		private int updateHash()
		{
			this.hash = super.hashCode();
			if (this.getAction() != null)
			{
				this.hash = this.hashCode() ^ this.getAction().hashCode() 
				^ this.getCounter() ^ this.getDifficulty() ^ 31;
			}
			
			return this.hash;
		}
		
		@Override
		public int hashCode() 
		{
			return this.hash;
		}

		public Set getComparators()
		{
			return getAction().getComparators();
		}

		public Set getOperators()
		{
			return getAction().getOperators();
		}

		public void reset()
		{
			super.reset();
			setCounter(0);
			setDifficulty(0);
			
			this.updateHash();
		}

		public String toString()
		{
			return getAction().toString();
		}

		public Action getAction() {
			return action;
		}

		public void setAction(Action action) {
			this.action = action;
			this.updateHash();
		}

		public int getCounter() {
			return counter;
		}

		public void setCounter(int counter) {
			this.counter = counter;
		}

		public int getDifficulty() {
			return difficulty;
		}

		public void setDifficulty(int difficulty) {
			this.difficulty = difficulty;
		}

		public Set<PGFact> getConditions() {
			return conditions;
		}

		public void setConditions(Set<PGFact> conditions) {
			this.conditions = conditions;
		}

		public Set<PGFact> getAchieves() {
			return achieves;
		}

		public void setAchieves(Set<PGFact> achieves) {
			this.achieves = achieves;
		}

		public Set<PGFact> getDeletes() {
			return deletes;
		}

		public void setDeletes(Set<PGFact> deletes) {
			this.deletes = deletes;
		}
	}

	protected static final HashSet EmptySet = new HashSet(0);
	public class PGNoOp extends PGAction
	{
		private PGFact proposition;
		
		private int hash;

		public PGNoOp(PGFact p)
		{
			setProposition(p);
			
			this.updateHash();
		}
		
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj == null || obj instanceof PGNoOp == false)
				return false;
			
			PGNoOp other = (PGNoOp) obj;
			boolean eq = super.equals(obj);
			if (!eq)
				return eq;
			
			return this.getProposition().equals(other.getProposition());
		}	

		public String toString()
		{
			return ("No-Op " + getProposition());
		}

		public Set getComparators()
		{
			return PlanningGraph.EmptySet;
		}

		public Set getOperators()
		{
			return PlanningGraph.EmptySet;
		}
		
		private int updateHash()
		{
			this.hash = super.hashCode() ^ this.getProposition().hashCode() ^ 31;
			return this.hash;
		}
		
		@Override
		public int hashCode() 
		{
			return this.hash;
		}

		public PGFact getProposition() {
			return proposition;
		}

		public void setProposition(PGFact proposition) {
			this.proposition = proposition;
			this.updateHash();
		}
	}

	public class PGFact extends Node
	{
		private Fact fact;

		private Set<PGAction> enables;
		private Set<PGAction> achievedBy;
		private Set<PGAction> deletedBy;
		
		private int hash;
		
		private PGFact()
		{
			this.setEnables(new HashSet<PGAction>());
			this.setAchievedBy(new HashSet<PGAction>());
			this.setDeletedBy(new HashSet<PGAction>());
		}

		public PGFact(Fact p)
		{
			this();
			
			setFact(p);
			
			this.updateHash();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj == null || obj instanceof PGFact == false)
				return false;
			
			PGFact other = (PGFact) obj;
			boolean eq = super.equals(obj);
			
			if (!eq)
				return eq;
			
			return this.getFact().equals(other.getFact());
		}
		
		private int updateHash()
		{
			this.hash = super.hashCode() ^ this.getFact().hashCode();
			return this.hash;
		}
		
		@Override
		public int hashCode() 
		{
			return this.hash;
		}

		public String toString()
		{
			return getFact().toString();
		}

		public Fact getFact() {
			return fact;
		}

		public void setFact(Fact fact) {
			this.fact = fact;
			this.updateHash();
		}

		public Set<PGAction> getEnables() {
			return enables;
		}

		public void setEnables(Set<PGAction> enables) {
			this.enables = enables;
		}

		public Set<PGAction> getAchievedBy() {
			return achievedBy;
		}

		public void setAchievedBy(Set<PGAction> achievedBy) {
			this.achievedBy = achievedBy;
		}

		public Set<PGAction> getDeletedBy() {
			return deletedBy;
		}

		public void setDeletedBy(Set<PGAction> deletedBy) {
			this.deletedBy = deletedBy;
		}

		// public Object clone()
		// {
		// PGFact clone = new PGFact((Fact) fact.clone());
		// clone.enables = new HashSet<PlanningGraph.PGAction>();
		// clone.
		// }
	}

	protected class MutexPair
	{
		private Node node1;
		private Node node2;
		
		private int hash;

		public MutexPair(Node n1, Node n2)
		{
			setNode1(n1);
			setNode2(n2);
			
			this.updateHash();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return this.getNode1().equals(this.getNode2());
		}
		
		private int updateHash()
		{
			this.hash = 31;
			if (this.getNode1() != null)
				this.hash = this.hash ^ this.getNode1().hashCode();
			if (this.getNode2() != null)
				this.hash = this.hash ^ this.getNode2().hashCode();
			
			return this.hash;
		}
		
		@Override
		public int hashCode() 
		{
			return this.hash;
		}

		public Node getNode1() {
			return node1;
		}

		public void setNode1(Node node1) {
			this.node1 = node1;
			this.updateHash();
		}

		public Node getNode2() {
			return node2;
		}

		public void setNode2(Node node2) {
			this.node2 = node2;
			this.updateHash();
		}
	}

	// ******************************************************
	// Debugging Classes
	// ******************************************************
	public void printGraph()
	{
		for (int i = 0; i <= num_layers; ++i)
		{
			System.out.println("-----Layer " + i
					+ "----------------------------------------");
			printLayer(i);
		}
		System.out
				.println("-----End -----------------------------------------------");
	}

	public void printLayer(int i)
	{
		System.out.println("Facts:");
		Iterator pit = propositions.iterator();
		while (pit.hasNext())
		{
			PGFact p = (PGFact) pit.next();
			if (p.getLayer() <= i && p.getLayer() >= 0)
			{
				System.out.println("\t" + p);
				System.out.println("\t\tmutex with");
				Iterator mit = p.getMutexTable().keySet().iterator();
				while (mit.hasNext())
				{
					PGFact pm = (PGFact) mit.next();
					Integer il = (Integer) p.getMutexTable().get(pm);
					if (il.intValue() >= i)
					{
						System.out.println("\t\t\t" + pm);
					}
				}
			}
		}
		if (i == num_layers)
			return;
		System.out.println("Actions:");
		Iterator ait = actions.iterator();
		while (ait.hasNext())
		{
			PGAction a = (PGAction) ait.next();
			if (a.getLayer() <= i && a.getLayer() >= 0)
			{
				System.out.println("\t" + a);
				System.out.println("\t\tmutex with");
				Iterator mit = a.getMutexTable().keySet().iterator();
				while (mit.hasNext())
				{
					PGAction am = (PGAction) mit.next();
					Integer il = (Integer) a.getMutexTable().get(am);
					if (il.intValue() >= i)
					{
						System.out.println("\t\t\t" + am);
					}
				}
			}
		}
	}

}