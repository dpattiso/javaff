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

package javaff.data;

import javaff.parser.SolutionParser;
import javaff.planning.STRIPSState;
import javaff.planning.MetricState;
import javaff.planning.State;
import javaff.planning.TemporalMetricState;
import javaff.planning.RelaxedPlanningGraph;
import javaff.planning.RelaxedMetricPlanningGraph;
import javaff.planning.RelaxedTemporalMetricPlanningGraph;
import javaff.search.UnreachableGoalException;
import javaff.data.adl.ADLFact;
import javaff.data.adl.ConditionalEffect;
import javaff.data.adl.Exists;
import javaff.data.adl.ForAll;
import javaff.data.adl.Imply;
import javaff.data.adl.Or;
import javaff.data.adl.Quantifier;
import javaff.data.metric.BinaryComparator;
import javaff.data.metric.Function;
import javaff.data.metric.Metric;
import javaff.data.metric.MetricType;
import javaff.data.metric.NamedFunction;
import javaff.data.metric.ResourceOperator;
import javaff.data.strips.And;
import javaff.data.strips.Equals;
import javaff.data.strips.InstantAction;
import javaff.data.strips.Not;
import javaff.data.strips.NullFact;
import javaff.data.strips.PDDLObject;
import javaff.data.strips.Proposition;
import javaff.data.strips.STRIPSFact;
import javaff.data.strips.SingleLiteral;
import javaff.data.strips.TrueCondition;
import javaff.data.temporal.DurativeAction;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;

public class GroundProblem implements Cloneable
{
	private String name;
	private Set<Action> actions = new HashSet<Action>();
	private Map<NamedFunction, BigDecimal> functionValues = new Hashtable<NamedFunction, BigDecimal>();
	private Metric metric;

	private GroundFact goal;
	private Set<Fact> initial;
	
	/**
	 * A map parameters to all propositions which contain it.
	 */
	private Map<Parameter, Set<Proposition>> objectPropositionMap; 
	
	/**
	 * A set of all grounded propositions which can be in the domain. This will include
	 * all propositions which could exist, but do not appear in the init or goal conditions, and are not preconditions
	 * or effects of any actions.
	 */
	private Set<Proposition> groundedPropositions;
	private Set<Fact> reachableFacts;
	private Set<Fact> staticFacts;

//	private TemporalMetricState tmstate = null;
//	private MetricState mstate = null;
//	private STRIPSState state = null;
	private State state;
	
	private Set<Parameter> objects;
	private DomainRequirements requirements;
	
	protected GroundProblem()
	{
		actions = new HashSet<Action>();
		initial = new HashSet<Fact>();
		goal = new And();
		functionValues = new HashMap<NamedFunction, BigDecimal>();
		metric = new Metric(MetricType.Minimize, null);
		name = "unknown";
		this.reachableFacts = new HashSet<Fact>();
		this.staticFacts = new HashSet<Fact>();
//		this.state = this.getTemporalMetricInitialState();
		this.objects = new HashSet<Parameter>();
		this.objectPropositionMap = new HashMap<Parameter, Set<Proposition>>();
		this.requirements = new DomainRequirements();
		
		this.reachableFacts = new HashSet<Fact>();
	}


	public GroundProblem(Set<Action> a, Set<Fact> i, GroundFact g, Map<NamedFunction, BigDecimal> f, Metric m)
	{
		this();
		
		actions = a;
		initial = i;
		goal = g;
		functionValues = f;
		metric = m;
		name = "UntitledDomain";
		
		extractPddlObjects();
		computeGroundedProps();
		makeAllLowerCase();
		createTypePropositionMap();
		
		this.reachableFacts = new HashSet<Fact>(this.groundedPropositions);
	}
	
	/**
	 * Checks whether the specified goals are part of the known set of reachable facts in this problem.
	 * @param goals The single literal goals to check for.
	 * @return True if all goals are reachable, false otherwise.
	 * @see #getReachableFacts()
	 */
	public boolean isGoalReachable(Collection<SingleLiteral> goals)
	{
		for (Fact f : goals)
		{
			if (this.reachableFacts.contains(f) == false)
				return false;
			
		}
		
		return true;
	}
	
	
	public Object clone()
	{
		GroundProblem clone = new GroundProblem(new HashSet<Action>(this.getActions()),
												new HashSet<Fact>(this.getInitial()),
												(GroundFact)this.getGoal().clone(),
												new Hashtable<NamedFunction, BigDecimal>(this.getFunctionValues()),
												this.getMetric());
		
		if (this.getState() != null)
			clone.setState((State) this.getState().clone());
//		if (this.state != null)
//			clone.state = (STRIPSState) this.state.clone();
//		if (this.mstate != null)
//			clone.mstate = (MetricState) this.mstate.clone();
//		if (this.tmstate != null)
//			clone.tmstate = (TemporalMetricState) this.tmstate.clone();
		
		clone.setGroundedPropositions(new HashSet<Proposition>(this.getGroundedPropositions()));
		clone.setReachableFacts(new HashSet<Fact>(this.getReachableFacts()));
		clone.setName(new String(this.getName()));
		clone.setObjects(new HashSet<Parameter>(this.getObjects()));
		clone.setObjectPropositionMap(new HashMap<Parameter, Set<Proposition>>(this.getObjectPropositionMap()));
		clone.setRequirements((DomainRequirements) this.getRequirements().clone());
		return clone;
												
	}
	
	protected void createTypePropositionMap()
	{
		this.getObjectPropositionMap().clear();
		for (Proposition p : this.getGroundedPropositions())
		{
			for (Parameter par : p.getParameters())
			{
				if (this.getObjectPropositionMap().containsKey(par) == false)
					this.getObjectPropositionMap().put(par, new HashSet<Proposition>());
				
				this.getObjectPropositionMap().get(par).add(p);
			}
		}
	}

	protected void extractPddlObjects()
	{
		this.getObjects().clear();
		for (Action a : this.getActions())
		{
			this.getObjects().addAll(a.getParameters());
		}
	}

	protected void makeAllLowerCase()
	{
		//	action, goal, initial, groundedprops
		Hashtable<String, Fact> lookup = new Hashtable<String, Fact>();
		for (Fact p : this.getGroundedPropositions())
		{
			lookup.put(p.toString().toLowerCase(), p);
		}
	}
	
	/**
	 * This method extracts all individual facts which can exist in the domain by decompiling out all variants of a Fact (ADL, Equals etc). Anything which 
	 * exists in the domain, such as an Equality fact, is ignored.
	 */
	protected void computeGroundedProps()
	{
		this.setGroundedPropositions(new HashSet<Proposition>());
		for (Fact p : this.getInitial())
		{
			if (p.isStatic())
				this.getStaticFacts().add(p);
			else if (p instanceof Proposition)
				this.getGroundedPropositions().add((Proposition) p);
		}
		
		for (Action a : this.getActions())
		{
			for (Fact pc : a.getPreconditions())
			{
				Collection<Fact> c = GroundProblem.decompileFact(pc, this.staticFacts);
				for (Fact f : c)
				{
					if (f instanceof Not && ((Not)f).getLiteral() instanceof Proposition)
						this.getGroundedPropositions().add((Proposition) ((Not) f).getLiteral());
					else if (f instanceof Proposition)
						this.getGroundedPropositions().add((Proposition) f);
					
					if (f instanceof Proposition && f.isStatic())
						this.getStaticFacts().add((Proposition) f);
				}
			}
			
			for (Fact add : a.getAddPropositions())
			{
				Collection<Fact> c = this.decompileFact(add, this.staticFacts);
				for (Fact f : c)
				{
					if (f instanceof Not && ((Not)f).getLiteral() instanceof Proposition)
						this.getGroundedPropositions().add((Proposition) ((Not) f).getLiteral());
					else if (f instanceof Proposition)
						this.getGroundedPropositions().add((Proposition) f);

					if (f instanceof Proposition && f.isStatic())
						this.getStaticFacts().add((Proposition) f);
				}
			}
			
			for (Not del : a.getDeletePropositions())
			{
				Collection<Fact> c = this.decompileFact(del, this.staticFacts);
				for (Fact f : c)
				{
					if (f instanceof Not && ((Not)f).getLiteral() instanceof Proposition)
						this.getGroundedPropositions().add((Proposition) ((Not) f).getLiteral());
					else if (f instanceof Proposition)
						this.getGroundedPropositions().add((Proposition) f);

					if (f instanceof Proposition && f.isStatic())
						this.getStaticFacts().add((Proposition) f);
				}
			}
		}
	}
	
	/**
	 * This helper method deconstructs any Fact into individual literals for use
	 * in the planning graph. If a new type is introduced into the hierarchy, this will
	 * probably need modified.
	 * 
	 * @param f
	 * @return The various facts which the parameter decompiles into.
	 */
	protected static Collection<Fact> decompileFact(Fact f, final Set<Fact> staticFacts)
	{
		HashSet<Fact> decompiled = new HashSet<Fact>(); 
		
		if (f instanceof NullFact || f instanceof Equals || TrueCondition.isSimpleTrue(f) == 1)
		{
			//don't want to decompile these fact types
			decompiled.add(f);
		}
		else if (f instanceof ADLFact)
		{
			Collection<? extends STRIPSFact> strips = ((ADLFact) f).toSTRIPS(staticFacts);
			for (STRIPSFact stripsFact : strips)
			{
				decompiled.addAll(GroundProblem.decompileFact(stripsFact, staticFacts));
			}
		}
		else if (f instanceof And || f instanceof Or)
		{
//			decompiled.add(f);
			for (Fact subFact : f.getFacts())
			{
				decompiled.addAll(GroundProblem.decompileFact(subFact, staticFacts));
			}
		}
		else if (f instanceof SingleLiteral || f instanceof Function || f instanceof BinaryComparator || f instanceof ResourceOperator)
		{
			decompiled.add(f);
		}
		else if (f instanceof Not)
		{
			//Nots are a special case, and a very annoying one at that. The facts which are held inside the original Not, must themselves
			//be decompiled, but then re-wrapped once the method returns.
			Collection<Fact> needsNotted = GroundProblem.decompileFact(((Not)f).getLiteral(), staticFacts);
			for (Fact nn : needsNotted)
			{
				Not wrapped = new Not(nn);
				decompiled.add(wrapped);
			}
		}
		else
			throw new IllegalArgumentException("Cannot decompile fact "+f+" - unknown type: "+f.getClass());
		
		return decompiled;
	}

	public STRIPSState getSTRIPSInitialState()
	{
		if (this.getState() == null)
		{
			STRIPSState s = new STRIPSState(getActions(), getInitial(), getGoal());
			this.setState(s);
			s.setRPG(new RelaxedPlanningGraph(this));
		}
		return (STRIPSState) getState();
	}

	public MetricState getMetricInitialState()
	{
		if (this.getState() == null)
		{
			MetricState ms = new MetricState(getActions(), getInitial(), getGoal(),
					getFunctionValues(), getMetric());
			this.setState(ms);
			ms.setRPG(new RelaxedMetricPlanningGraph(this));
		}
		return (MetricState) getState();
	}
	
	public STRIPSState recomputeSTRIPSInitialState()
	{
		STRIPSState s = new STRIPSState(getActions(), getInitial(), getGoal());
		s.setRPG(new RelaxedPlanningGraph(this));
		this.setState(s);
		
		return (STRIPSState) this.getState();
	}

	public MetricState recomputeMetricInitialState()
	{
		MetricState ms = new MetricState(getActions(), getInitial(), getGoal(),
				getFunctionValues(), getMetric());
		ms.setRPG(new RelaxedMetricPlanningGraph(this));
		this.setState(ms);
		
		return (MetricState) getState();
	}

	public TemporalMetricState getTemporalMetricInitialState()
	{
		if (this.getState() == null)
		{
			Set na = new HashSet();
			Set ni = new HashSet();
			Iterator ait = getActions().iterator();
			while (ait.hasNext())
			{
				Action act = (Action) ait.next();
				if (act instanceof InstantAction)
				{
					na.add(act);
					ni.add(act);
				} else if (act instanceof DurativeAction)
				{
					DurativeAction dact = (DurativeAction) act;
					na.add(dact.startAction);
					na.add(dact.endAction);
					ni.add(dact.startAction);
				}
			}
			TemporalMetricState ts = new TemporalMetricState(ni, getInitial(), getGoal(),
					getFunctionValues(), getMetric());
			GroundProblem gp = new GroundProblem(na, getInitial(), getGoal(),
					getFunctionValues(), getMetric());
			gp.setName(this.getName());
			gp.setReachableFacts(new HashSet<Fact>(this.getReachableFacts()));
			ts.setRPG(new RelaxedTemporalMetricPlanningGraph(gp));
			this.setState(ts);
		}
		return (TemporalMetricState) this.getState();
	}
	

	public TemporalMetricState recomputeTemporalMetricInitialState()
	{
		Set na = new HashSet();
		Set ni = new HashSet();
		Iterator ait = getActions().iterator();
		while (ait.hasNext())
		{
			Action act = (Action) ait.next();
			if (act instanceof InstantAction)
			{
				na.add(act);
				ni.add(act);
			} else if (act instanceof DurativeAction)
			{
				DurativeAction dact = (DurativeAction) act;
				na.add(dact.startAction);
				na.add(dact.endAction);
				ni.add(dact.startAction);
			}
		}
		TemporalMetricState ts = new TemporalMetricState(ni, getInitial(), getGoal(),
				getFunctionValues(), getMetric());
		GroundProblem gp = new GroundProblem(na, getInitial(), getGoal(),
				getFunctionValues(), getMetric());
		ts.setRPG(new RelaxedTemporalMetricPlanningGraph(gp));
		this.setState(ts);	
		
		return (TemporalMetricState) this.getState();
	}

	@Override
	public String toString() 
	{
		return "GroundProblem: "+this.getName();
	}
	
	/**
	 * Performs reachability analysis on this groun problem by constructing a stable RPG. Any facts 
	 * or actions which have not been achieved by the final layer of the RPG are not reachable and
	 * will be removed from this problem's actions and reachable facts. Calling this will construct a STRIPSState
	 * via {@link #getSTRIPSInitialState()}.
	 * @throws UnreachableGoalException Thrown if a subset of the goal is not present in the final RPG layer.
	 * @see #getActions()
	 * @see #getReachableFacts()
	 */
	public void filterReachableFacts() throws UnreachableGoalException
	{
		this.filterReachableFacts(false);
	}
	
	/**
	 * This overloaded method allows the developer to decide whether unreachable goals should cause
	 * an exception to be thrown. This exists purely because it is of relevance to work in goal recognition.
	 * @param ignoreUnreachableGoal Whether unreachable goals should be ignored.
	 * @throws UnreachableGoalException Thrown only if parameter flag is false
	 * @see #getGoal()
	 */
	public void filterReachableFacts(boolean ignoreUnreachableGoal) throws UnreachableGoalException
	{
		STRIPSState init = this.getSTRIPSInitialState();

		init.getRPG().constructStableGraph(init);

		Set<Fact> finalFacts = init.getRPG().getFactsAtLayer(init.getRPG().size());

		HashSet<Fact> unmetGoals = new HashSet<Fact>(this.getGoal().getFacts());
		unmetGoals.removeAll(finalFacts);
		
		if (ignoreUnreachableGoal == false)
		{
			if (unmetGoals.isEmpty() == false)
				throw new UnreachableGoalException(unmetGoals, "Goal is not reachable through RPG reachability analysis");
		}
		
		int oldFactCount = this.getReachableFacts().size();
		this.getReachableFacts().retainAll(finalFacts);
		
		
		HashSet<Action> actionsUsed = new HashSet<Action>();
		for (int i = 0; i < init.getRPG().size()-1; i++) //TODO is -1 correct? I don't think it should make a difference -- layer N will be empty
		{
			Set<Action> a = init.getRPG().getActionsAtLayer(i);
			
			actionsUsed.addAll(a);
		}
		
		int oldActionCount = this.getActions().size();
		this.getActions().retainAll(actionsUsed);

		System.out.println("Found " + this.getReachableFacts().size()
				+ " reachable facts from " + oldFactCount + " original facts.");
		System.out.println("Found " + this.getActions().size()
				+ " applicable actions from " + oldActionCount
				+ " original actions");
		
	}

	/**
	 * By default, GroundProblems can contain ADL actions and predicates. Calling this will
	 * convert the problem into a STRIPS-only form, by removing ADL from actions and replacing them with
	 * STRIPS-equivalent actions. 
	 * @param ground
	 * @return The number of STRIPS actions which would have been generated, including unreachable actions
	 *  which are automatically removed.
	 */
	public int decompileADL()
	{
		Set<Action> refinedActions = new HashSet<Action>();
		int adlActionCount = 0;
		
		//FIXME this is pretty badly written, and incomplete. Should be recursive -- too many assumptions on format of data
		//keep a queue of potentially-ADL actions. Add partially compiled out actions to it. When
		//no ADL constructs exist in the PCs (actions unsupported for now), it can be added to the set of
		//legal actions.
		Queue<Action> queue = new LinkedList<Action>(this.getActions());
		out: while (queue.isEmpty() == false)
		{
			Action a = queue.remove();
			
			if (a instanceof InstantAction)
			{
				for (Fact pc : a.getPreconditions())
				{
					if (this.decompileInstantAction(pc, (InstantAction) a, this.staticFacts, queue) == true) //if the action needed changed and requires to be reparsed
					{
						continue out;
					}
				}
				
				//FIXME ADL in effects cannot be used unless decompileInstanceAction is modified
				//with a parameter flag to indicate whether the Fact passed in is a PC or Effect
//				for (Fact add : a.getAddPropositions())
//				{
//					if (this.decompileInstantAction(add, (InstantAction) a, queue) == true) //if the action needed changed and requires to be reparsed
//					{
//						continue out;
//					}
//				}
//				
//				for (Not del : a.getDeletePropositions())
//				{
//					if (this.decompileInstantAction(del, (InstantAction) a, queue) == true) //if the action needed changed and requires to be reparsed
//					{
//						continue out;
//					}
//				}
			}
			
			++adlActionCount;
			
			//finally, filter out any unreachable actions which have a static precondition that is not in the initial state.
			//TODO could do more filtering here.
//			for (Fact pc : a.getPreconditions())
//			{
//				if (pc.isStatic() && this.getSTRIPSInitialState().isTrue(pc) == false)
//					continue out;
//			}
				
			refinedActions.add(a);
			
		}
		
		
		Collection<Fact> newGoals = GroundProblem.decompileFact(this.getGoal(), this.staticFacts);
		this.setGoal(new And(newGoals));
		
		this.getActions().clear();
		this.setActions(refinedActions);
		
		return adlActionCount;
	}
	
	
	/**
	 * Removes all ADL code from the specified action. This is a recursive process, with any modified actions being added
	 * to the queue. Once an action is completely ADL-free, the method returns false, indicating that the action is available
	 * for use in a STRIPS-only context (like a planning graph).
	 * @param pc
	 * @param a
	 * @param queue
	 * @return
	 */
	//TODO this only works with preconditions?
	//HERE BE DRAGONS! I'm convinced this code is riddled with bugs
	protected boolean decompileInstantAction(Fact pc, InstantAction a, Set<Fact> staticFacts, Queue<Action> queue)
	{
		if (pc instanceof SingleLiteral) //if a simple proposition, then just return
		{
			return false;
		}
		else if (pc instanceof CompoundLiteral)
		{
			boolean changed = false;
			for (Fact f : ((CompoundLiteral)pc).getCompoundFacts())
			{
				boolean res = this.decompileInstantAction(f, a, staticFacts, queue);
				if (res == true)
				{
					changed = true;
				}
				//if false, then no modification to the action was required, so continue on as normal
			}
			
			return changed;
		}
		else if (pc instanceof ADLFact)
		{
			Collection<? extends STRIPSFact> compiledOut = ((ADLFact)pc).toSTRIPS(staticFacts);
			if (compiledOut.isEmpty())
			{
				Set<Fact> modifiedPCs = new HashSet<Fact>(a.getPreconditions());
				modifiedPCs.remove(pc);
//				a.setCondition(new And(modifiedPCs));
				
				InstantAction actionClone = (InstantAction) a.clone();
		
				actionClone.setCondition(new And(modifiedPCs));
				queue.add(actionClone);
			}
			else
			{
				for (STRIPSFact strips : compiledOut)
				{
					Set<Fact> modifiedPCs = new HashSet<Fact>(a.getPreconditions());
					modifiedPCs.remove(pc);
					modifiedPCs.add(strips);
	//				a.setCondition(new And(modifiedPCs));
					
					InstantAction actionClone = (InstantAction) a.clone();
			
					actionClone.setCondition(new And(modifiedPCs));
					
	//				Set<Fact> modifiedEffects = new HashSet<Fact>(a.getAddPropositions());
	//				modifiedEffects.addAll(a.getDeletePropositions());
	//				
	//				actionClone.setEffect(new And(modifiedEffects));
					
					
					queue.add(actionClone);
				}
			}

			return true;
		}
		else if (pc instanceof Not)
		{
			boolean res = this.decompileInstantAction(((Not) pc).getLiteral(), a, staticFacts, queue);
			if (res == true)
				return true;
		}
		else if (pc instanceof ConditionalEffect)
		{
			throw new NullPointerException("Decompiling conditional effects is not yet supported");
		}
		
		return false; //no changes required, action is ADL-free
	}

	public boolean isMetric()
	{
		return this.getRequirements().isMetric();
	}
	
	public boolean isTemporal()
	{
		return this.getRequirements().isTemporal();		
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Set<Action> getActions()
	{
		return actions;
	}

	public void setActions(Set<Action> actions)
	{
		this.actions = actions;
	}

	public Map<NamedFunction, BigDecimal> getFunctionValues()
	{
		return functionValues;
	}

	public void setFunctionValues(Map<NamedFunction, BigDecimal> functionValues)
	{
		this.functionValues = functionValues;
	}

	public Metric getMetric()
	{
		return metric;
	}

	public void setMetric(Metric metric)
	{
		this.metric = metric;
	}

	public GroundFact getGoal()
	{
		return goal;
	}

	public void setGoal(GroundFact goal)
	{
		this.goal = goal;
	}

	public Set<Fact> getInitial()
	{
		return initial;
	}

	public void setInitial(Set<Fact> initial)
	{
		this.initial = initial;
	}

	public Map<Parameter, Set<Proposition>> getObjectPropositionMap()
	{
		return objectPropositionMap;
	}

	public void setObjectPropositionMap(Map<Parameter, Set<Proposition>> objectPropositionMap)
	{
		this.objectPropositionMap = objectPropositionMap;
	}

	public Set<Proposition> getGroundedPropositions()
	{
		return groundedPropositions;
	}

	public void setGroundedPropositions(Set<Proposition> groundedPropositions)
	{
		this.groundedPropositions = groundedPropositions;
	}

	public Set<Fact> getReachableFacts()
	{
		return reachableFacts;
	}

	public void setReachableFacts(Set<Fact> reachableFacts)
	{
		this.reachableFacts = reachableFacts;
	}

	public Set<Fact> getStaticFacts()
	{
		return staticFacts;
	}

	public void setStaticFacts(Set<Fact> staticFacts)
	{
		this.staticFacts = staticFacts;
	}

	public Set<Parameter> getObjects()
	{
		return objects;
	}

	public void setObjects(Set<Parameter> objects)
	{
		this.objects = objects;
	}

	public DomainRequirements getRequirements()
	{
		return requirements;
	}

	public void setRequirements(DomainRequirements requirements)
	{
		this.requirements = requirements;
	}

	public State getState()
	{
		return state;
	}

	public void setState(State state)
	{
		this.state = state;
	}

//	protected boolean isFactSatisfiable(Fact fact)
//	{
//		for (Fact f : fact.getFacts())
//		{
//			if (f.isStatic() && this.staticFacts.contains(f) == false)
//				return false;
//		}
//		
//		return true;
//	}
}
