package javaff.scheduling;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.GroundProblem;
import javaff.data.Literal;
import javaff.data.MutexSpace;
import javaff.data.Plan;
import javaff.data.TimeStampedAction;
import javaff.data.TimeStampedPlan;
import javaff.data.TotalOrderPlan;
import javaff.data.strips.And;
import javaff.data.strips.Not;
import javaff.data.strips.Proposition;
import javaff.data.strips.STRIPSFact;
import javaff.data.strips.STRIPSInstantAction;
import javaff.data.strips.SingleLiteral;
import javaff.data.strips.TrueCondition;
import javaff.planning.STRIPSState;

/**
 * A basic scheduler for STRIPS only plans. Actions are assigned timestamps based upon their earliest possible application in the plan.
 * Mutexes are fully constructed on every call to {@link #schedule(TotalOrderPlan)}, via {@link #detectMutexes(List)}.
 * @author David Pattison
 *
 */
public class STRIPSScheduler implements Scheduler
{
	protected STRIPSState initial;
	protected MutexSpace factMutexes;
	protected Map<Action, Set<Action>> actionMutexes;
	//FIXME storing action mutexes is a very brute force approach
	
	/**
	 * Need to store static facts as opposed to setting a Fact's static flag to True. This
	 * is because if the detectMutexes() method (which detects statics) is called, it will set
	 * the flag based upon only those actions which have been passed in (i.e. the plan). But
	 * if the scheduler has not been called at the end of execution, then the flag is being set
	 * in the relevant facts, which are then used in their original context by the planner/whatever.
	 * Lets just say this leads to bugs, especially when replanning or doign plan recognition.
	 */
	protected Set<Fact> staticFacts;
	
	private String epsilonAccuracy;
	
	private boolean partiallyObservableAllowed;
	
	/**
	 * Construct a scheduler from the specified ground problem.
	 * @param problem
	 */
	public STRIPSScheduler(STRIPSState initialState)
	{
		this.initial = initialState;
		this.factMutexes = new MutexSpace();
		this.actionMutexes = new HashMap<Action, Set<Action>>();
		
		this.epsilonAccuracy = this.getEpsilonString(3);
		this.partiallyObservableAllowed = false;
		this.staticFacts = new HashSet<Fact>();
	}	
	
	/**
	 * Constructs a String representation of the epsilon value appended to scheduled actions.
	 * @param leadingZeros
	 * @return
	 */
	protected String getEpsilonString(int leadingZeros)
	{
		StringBuffer buf = new StringBuffer("0.");
		for (int i = 0; i < leadingZeros; i++)
		{
			buf.append('0');
		}
		buf.append('1');
		
		return buf.toString();
	}
	
	/**
	 * Detects mutexes between actions and then schedules each action in the linear plan to allow
	 * for maximum concurrency with the earliest start times.
	 * 
	 * @param top The plan to schedule.
	 * @see #schedule(TotalOrderPlan)
	 */
	public TimeStampedPlan schedule(TotalOrderPlan top) throws SchedulingException
	{
		return this.schedule(top, new HashMap<Fact, TimeStampedAction>());
	}
	
	/**
	 * Detects mutexes between actions and then schedules each action in the linear plan to allow
	 * for maximum concurrency with the earliest start times.
	 * 
	 * @param top The plan to schedule.
	 * @param achieverMap An empty map which upon returning will contain a mapping of facts to those actions in the 
	 * scheduled plan which last achieved them.
	 * @see #detectMutexes(List)
	 */
	public TimeStampedPlan schedule(TotalOrderPlan top, Map<Fact, TimeStampedAction> achieverMap) throws SchedulingException
	{		
		this.detectMutexes(top.getActions());
//		System.out.println("Finished mutex detection");
		
//		System.out.println("Plan to schedule is ");
//		top.print(System.out);
		
		//this map contains the last achiever of a fact, which will potentially be a PC to another
		//HashMap<Fact, TimeStampedAction> achieverMap = new HashMap<Fact, TimeStampedAction>(); //map of the facts to the last action which achieved/deleted them
		HashMap<Fact, TreeSet<TimeStampedAction>> requiredBy = new HashMap<Fact, TreeSet<TimeStampedAction>>();
		
		TreeMap<BigDecimal, TreeSet<TimeStampedAction>> actionTimes = new TreeMap<BigDecimal, TreeSet<TimeStampedAction>>();
		
		//setup initial action, which adds initial state
		STRIPSInstantAction initialAction = new STRIPSInstantAction("initialise_action");
		And effect = new And(new HashSet<Fact>(initial.getFacts()));
		initialAction.setEffect(effect);
		
		TimeStampedAction initialActionScheduled = new TimeStampedAction(initialAction, new BigDecimal(-1), BigDecimal.ONE);
		for (Fact f : initialActionScheduled.getAction().getAddPropositions())
		{
			achieverMap.put(f, initialActionScheduled);
		}
		for (Fact pc : initialActionScheduled.getAction().getPreconditions())
		{
			requiredBy.put(pc, new TreeSet<TimeStampedAction>());
			requiredBy.get(pc).add(initialActionScheduled);
		}

//		BigDecimal epsilonOffset = new BigDecimal(0);
		BigDecimal epsilonOffset = new BigDecimal(this.epsilonAccuracy); 
		epsilonOffset.setScale((this.epsilonAccuracy+"").length());
		TimeStampedPlan tsp = new TimeStampedPlan(top.getGoal());
		
		//keep a map of the number of actions assigned to each timestamp
		HashMap<Integer, Integer> timestampCounter = new HashMap<Integer, Integer>();
		
		//now actually schedule the plan
		for (Action a : top.getActions())
		{
			//first, find out when this action becomes applicable. this will be equal to the time at which
			//its precondition with the largest value for T becomes true, which is equal to the time
			//that the precondition becomes true, plus 1 (as this is STRIPS only)
			BigDecimal minPcTime = new BigDecimal(0);
			
			//track the ancestors of this action
//			TreeSet<TimeStampedAction> ancestors = new TreeSet<TimeStampedAction>();
			
			//create an unobserved action here so that 
			UnobservedInstantAction unobservedAction = new UnobservedInstantAction();
			boolean hasUnseenPrecondition = false;
			TimeStampedAction tsUnobservedAction = new TimeStampedAction(unobservedAction, minPcTime, BigDecimal.ONE);
			for (Fact pc : a.getPreconditions())
			{
//				if (pc.isStatic() || (pc instanceof Proposition == false && pc instanceof Not == false))
				if (this.staticFacts.contains(pc) || (pc instanceof Proposition == false && pc instanceof Not == false))
					continue;
				
				if (pc instanceof Not && (((Not)pc).getLiteral() instanceof Proposition == false))
					continue;
				
				//this action is required to appear before the current action
				TimeStampedAction ancestor = null;
				//check for STRIPS PCs
				if (pc instanceof Not)
				{
					Not npc = (Not)pc;
					//if the negated literal is not present, we are happy
					if (achieverMap.containsKey(npc.getLiteral()) == false)
					{
						continue;
					}
				}
//				else
//					continue; //skip Equals tests and other non-STRIPS conditions
				
				//if the precondition is both added and deleted by the action, then we say that it is a pause mutex.
				//these actions exist only to hold a resource in a STRIPS environment. Therefore, we say that the
				//action has not been achieved by this action, but rather remains with the previous achieved action 
				//(which is probably the initial state -- see channel-open predicate in Rovers) by virtue of the 
				//fact it has to be true in order for this action even to execute. //TODO this probably breaks partial observability code
				boolean isPauseMutex = a.adds(pc) && a.deletes(pc);
				if (isPauseMutex)
					continue;
				
				ancestor = achieverMap.get(pc);
				if (ancestor == null)
				{
					//this is where we would throw an exception if the fact had never been achieved in a fully
					//observable context. But as this is partially observable we just assume that
					//the fact was achieved by an unseen action, which needs to be added to the set of
					//achievers, but not the actual scheduled plan.
					if (this.isPartiallyObservableAllowed())
					{
						hasUnseenPrecondition = true;
						
						unobservedAction.addEffect((GroundFact) pc);
						
						ancestor = tsUnobservedAction;
						achieverMap.put(pc, tsUnobservedAction);
						
						//want the unobserved action to have occurred at the same time that the last
						//known-to-be-achieved fact occurred.
						//So if the precondition is 
						if (ancestor.getMajorTime().compareTo(minPcTime) > 0)
						{
							ancestor.setTime(minPcTime);
						}
					}
					else
					{
						throw new SchedulingException("No way to achieve precondition "+pc+" in action "+a);
					}
				}
				else
				{
					if (ancestor.getMajorTime().add(BigDecimal.ONE).compareTo(minPcTime) > 0)
					{
						minPcTime = ancestor.getMajorTime().add(BigDecimal.ONE);
					}
				}
			}
			
			//now have max time. Action now needs to be checked to see if any of it's effects are mutex with another action
			//which is scheduled for the same time.
			//make sure to floor() the associated timestamp to get the key, and in turn the correct
			//integer start time
			BigDecimal currentIntegerTime = minPcTime.setScale(0, RoundingMode.FLOOR);//.add(BigDecimal.ONE);
			
			TimeStampedAction tsa = new TimeStampedAction(a, currentIntegerTime, BigDecimal.ONE);
			
			//check each action at time t, until there are no mutexes
			boolean nonMutex = false;
			out: while (nonMutex == false)
			{
				nonMutex = false;
				
				TreeSet<TimeStampedAction> existingTSA = actionTimes.get(currentIntegerTime);
				if (existingTSA == null)
					break out;
				
				HashSet<Action> existingAtT = new HashSet<Action>(existingTSA);
				if (this.areActionsMutex(tsa, existingAtT) != MutexType.None)
				{
					nonMutex = false;
					currentIntegerTime = currentIntegerTime.add(BigDecimal.ONE);
					continue out;
				}
				
				//the above code may decide that an action is applicable at time T, but ignore the fact there may
				//already be an identical action at this time. That is, the plan has the same action more than once. If action A 
				//has been scheduled for time T, then action B will also fit these requirements. This has to be detected, or
				//the action which is meant to appear later in the plan will not be correctly scheduled and the plan becomes invalid
				for (Action ex : existingAtT)
				{
					if (tsa.getAction().equals(((TimeStampedAction)ex).getAction()))
					{
						BigDecimal latestRequirement = currentIntegerTime.abs();
						for (Fact pc : ex.getPreconditions())
						{
							//if we have found a scheduled action which is identical to the unscheduled action, then find at what time the 
							//new/existing action's preconditions are last added. This is the best time to start looking for a new schedule time for the 
							//new action. 
							TimeStampedAction deletes = achieverMap.get((pc));
							if (deletes == null)
								continue;
							
							if (deletes.getMajorTime().compareTo(latestRequirement) > 0)
							{
								latestRequirement = deletes.getMajorTime().abs();
							}
						}
						//now have latest time the action is required, so start at this + 1
						nonMutex = false;
						currentIntegerTime = latestRequirement.setScale(0, RoundingMode.FLOOR).add(BigDecimal.ONE);
						continue out;
					}
				}
				
				
				//if made it this far, then actions are not mutex in a lifted context.
				//But, this action's effects may threaten already-scheduled actions at future times
				for (Not del : tsa.getDeletePropositions())
				{
					Fact f = del.getLiteral();
					Set<TimeStampedAction> requires = requiredBy.get(f);

					//if the action is deleted then re-added at the same time
					if (requires == null || (tsa.deletes(f) && tsa.adds(f)))
						continue;
					
					for (TimeStampedAction req : requires)
					{
						BigDecimal requiredByTime = req.getMajorTime();
						if (requiredByTime.compareTo(currentIntegerTime) >= 0)
						{
							//illegal -- this action will block the achievement of a later action
							nonMutex = false;
							currentIntegerTime = currentIntegerTime.add(BigDecimal.ONE);
							continue out;
						}
					}
				}
			
				nonMutex = true;
			}

			//THIS IS A MUST! without it there is the risk that two identical actions can be scheduled at
			//the same timestep. This would mean the later action being ignored or overwritten. That is,
			//the action would vanish, resulting in an invalid plan
			BigDecimal epsilonTime = currentIntegerTime;
			
			Integer countAtT = timestampCounter.get(currentIntegerTime.intValue());
			if (countAtT != null)
			{
				epsilonTime = epsilonTime.add(epsilonOffset.multiply(new BigDecimal(countAtT)));
			}
			else
			{
				epsilonTime = epsilonTime.add(epsilonOffset);
				timestampCounter.put(currentIntegerTime.intValue(), 1);
			}
			int newCount = timestampCounter.get(currentIntegerTime.intValue()) + 1;
			timestampCounter.put(currentIntegerTime.intValue(), newCount);
			
			//set final time
			tsa.setTime(epsilonTime);
			
			//if the scheduling of this action had an unmet precondition, this
			//flag will be true. If so, we need to add the hidden action to the plan
			//which achieves the precondition.
			if (hasUnseenPrecondition)
			{
				BigDecimal unseenActionTime = tsUnobservedAction.getMajorTime();
				countAtT = timestampCounter.get(unseenActionTime.intValue());
				if (countAtT != null)
				{
					unseenActionTime = unseenActionTime.add(epsilonOffset.multiply(new BigDecimal(countAtT)));
				}
				else
				{
					unseenActionTime = unseenActionTime.add(epsilonOffset);
					timestampCounter.put(tsUnobservedAction.getMajorTime().intValue(), 1);
				}
				newCount = timestampCounter.get(tsUnobservedAction.getMajorTime().intValue()) + 1;
				timestampCounter.put(tsUnobservedAction.getMajorTime().intValue(), newCount);
				
				tsUnobservedAction.setTime(unseenActionTime);
				tsp.addAction(tsUnobservedAction);
			}
			tsp.addAction(tsa); //add the scheduled action to the plan
			
			if (actionTimes.containsKey(currentIntegerTime) == false)
			{
				actionTimes.put(currentIntegerTime, new TreeSet<TimeStampedAction>());
			}
					
			//if we;ve gotten to this point, the action is schedulable at the computed time
//			System.out.println("Scheduled "+tsa);
			actionTimes.get(currentIntegerTime).add(tsa); //add new schedule time
			
			//record that this action requires its preconditions be true by time T
			for (Fact f : tsa.getPreconditions())
			{
//				if (f.isStatic())
				if (this.staticFacts.contains(f))
					continue;
				
				if (requiredBy.containsKey(f) == false)
				{
					requiredBy.put(f, new TreeSet<TimeStampedAction>());
				}
				
				boolean needsCleared = false;
				for (TimeStampedAction req : requiredBy.get(f))
				{
					if (req.getMajorTime().compareTo(tsa.getMajorTime()) < 0)
					{
						needsCleared = true;
					}
				}
				if (needsCleared)
					requiredBy.get(f).clear();
				
				requiredBy.get(f).add(tsa);
			}
					
			//now need to setup what the scheduled fact achieved or deleted
			for (Not del : tsa.getDeletePropositions())
			{
				//ignore actions which add and delete the same literal -- they are not the last achiever
				if (tsa.adds(del.getLiteral()))
					continue;
				
				achieverMap.remove(del.getLiteral());
				//only add the negation achiever if the delete effect is STRIPS syntax
				if (del.getLiteral() instanceof Proposition == true)
					achieverMap.put(del, tsa);
			}
			
			for (Fact add : tsa.getAddPropositions())
			{
				//ignore actions which add and delete the same literal -- they are not the last achiever
				if (tsa.deletes(add))
					continue;
				
				achieverMap.put(add, tsa);
			}
			
			
			
//			//update the epsilonAccuracy offset before considering the next action
//			epsilonOffset = epsilonOffset.add(bigEpsilon);
		}
		
		return tsp;
	}
	
	/**
	 * Validate that the plan specified has a totally ordered set of actions which 
	 * form a causal-link that achieves the goal.
	 * @param plan The plan to validate.
	 * @param initialState The state which execution starts in.
	 * @return A flag indicating the success of the validation process.
	 * @see #validate(Plan, STRIPSState, GroundFact)
	 */
	public PlanValidationResult validate(TimeStampedPlan plan, STRIPSState initialState)
	{
		return this.validate(plan, initialState, TrueCondition.getInstance());
	}
	
	/**
	 * Validate that the plan specified has a totally ordered set of actions which 
	 * form a causal-link that achieves the goal.
	 * @param plan The plan to validate.
	 * @param initialState The state which execution starts in.
	 * @param goal The goal of the plan.
	 * @return A flag indicating the success of the validation process.
	 */
	public PlanValidationResult validate(Plan plan, STRIPSState initialState, GroundFact goal)
	{
		STRIPSState currentState = initialState;
		
		List<Action> ordered = plan.getActions();
		for (Action a : ordered)
		{
			if (a.isApplicable(currentState) == false)
				return PlanValidationResult.UnsatisfiedPrecondition;
			
			currentState = (STRIPSState) currentState.apply(a);
		}
		
		if (currentState.isTrue(goal) == false)
			return PlanValidationResult.UnsatisfiedGoal;
		
		return PlanValidationResult.Valid;
	}
	
	/**
	 * Results of plan validation.
	 * @author David Pattison
	 *
	 */
	public enum PlanValidationResult
	{
		/**
		 * The plan is valid.
		 */
		Valid,
		/**
		 * The plan has an unsatsfied precondition.
		 */
		UnsatisfiedPrecondition,
		/**
		 * The plan executes correctly, but the goal is not met.
		 */
		UnsatisfiedGoal;
	}

	/**
	 * Checks whether a mutex relation exists between the specified action and set of other actions. Assumes that
	 * {@link #detectMutexes(List)} has been called prior to this.
	 * @param a The action to check against the set.
	 * @param b A set of actions to check for mutex relations against.
	 * @return The first type of mutex relation which is encountered that is not {@link MutexType#None}. 
	 * @see #detectMutexes(List)
	 */
	public MutexType areActionsMutex(Action a, Set<Action> others)
	{
		for (Action b : others)
		{
			MutexType mut = this.areActionsMutex(a, b);
			if (mut != MutexType.None)
				return mut;
		}
		
		return MutexType.None;
	}
	
	/**
	 * Checks whether a mutex relation exists between the 2 specified actions. Assumes that
	 * {@link #detectMutexes(List)} has been called prior to this.
	 * @param a The first action.
	 * @param b The second action.
	 * @return The type of mutex relation.
	 * @see #detectMutexes(List)
	 */
	public MutexType areActionsMutex(Action a, Action b)
	{
		MutexType mutex = MutexType.None;
		
		//first check that parameters dont contain conflicting causal graph leads
		if (a == b)
			return MutexType.None;
				
		mutex = this.checkPauseMutex(a, b);
		if (mutex != MutexType.None)
			return mutex;
			
		mutex = this.checkDeleteMutex(a, b);
		if (mutex != MutexType.None)
			return mutex;
		
		mutex = this.checkPreconditionMutex(a, b);
		if (mutex != MutexType.None)
			return mutex;
		
		
//		for (Proposition p : a.getAddPropositions())
//		{
//			for (Proposition q : b.getAddPropositions())
//			{
//				mutex = this.checkCompetingEffectMutex(p, q);
//				if (mutex != MutexType.None)
//				{
//					return mutex; 
//				}
//			}
//		}
		
		return MutexType.None;
	}

	/**
	 * Checks whether a mutex relation exists between the 2 specified states. Assumes that
	 * {@link #detectMutexes(List)} has been called prior to this.
	 * @param a The first state.
	 * @param b The second state.
	 * @return The type of mutex relation.
	 * @see #detectMutexes(List)
	 */
	public boolean areStatesMutex(STRIPSState a, STRIPSState b)
	{
		for (Object afo : a.getTrueFacts())
		{
			Proposition af = (Proposition) afo;
			for (Object bfo : b.getTrueFacts())
			{
				Proposition bf = (Proposition) bfo;
				
				if (this.factMutexes.isMutex(af, bf))
					return true;
			}
		}
			
		return false;
	}

	/**
	 * Checks if 2 facts can ever exist at the same time, given that the only known actions are
	 * those specified. This will only ever be true if all actions which require these facts
	 * are themselves mutex.
	 * @param p The first fact.
	 * @param q The second fact.
	 * @param actionSet
	 * @return Returns {@link MutexType#CompetingPCs} if the 2 specified facts can never exist at the same time, 
	 * {@link MutexType#None} otherwise.
	 */
	protected MutexType checkCompetingPreconditionMutex(Fact p, Fact q, Collection<Action> actionSet)
	{
		//find all achievers of fact p
		HashSet<Action> achieversA = new HashSet<Action>();
		for (Action a : actionSet)
		{
			//ignore pause mutexes
			if (a.adds(p) && a.deletes(p) == false)
				achieversA.add(a);
		}
		
		//find all achievers of fact q
		HashSet<Action> achieversB = new HashSet<Action>();
		for (Action b : actionSet)
		{
			if (b.adds(q) && b.deletes(q) == false)
				achieversB.add(b);
		}
		
		//determine if ALL achievers of p and q are mutex with one-another
		if (achieversA.isEmpty() || achieversB.isEmpty())
			return MutexType.None;
		
		for (Action a : achieversA)
		{
			for (Action b : achieversB)
			{
				if (a == b)
					continue;
				
				Set<Action> aMut = this.actionMutexes.get(a);
				Set<Action> bMut = this.actionMutexes.get(b);
				if (aMut.contains(b) == false && bMut.contains(a) == false)
					return MutexType.None;
			}
		}
		
		return MutexType.CompetingPCs;
	}

	/**
	 * Checks whether 2 actions cannot exist at the same timestep by testing whether
	 * one deletes the precondition of the other.
	 * @param a The first action.
	 * @param b The second action.
	 * @return {@link MutexType#AdeleteBpc} if A deletes a precondition of B, or {@link MutexType#BdeleteApc}
	 * if vice versa. If neither holds, {@link MutexType#None} is returned.
	 */
	protected MutexType checkPreconditionMutex(Action a, Action b)
	{
		//check to see if A deletes any of Bs preconditions and vice versa
		//ignore anything which deletes and adds in the same action effect, because it is 
		//not really blocking the other action
		for (Not apc : a.getDeletePropositions())
		{
			if (a.adds(apc.getLiteral()))
				continue; //ignore if deleted and added in same action
			
			if (b.requires(apc.getLiteral()) == true)
			{
				return MutexType.AdeleteBpc;
			}
		}
		//TODO be smarter about which should get delayed first- it should be the one which doesnt delete a fact first
		for (Not bpc : b.getDeletePropositions())
		{
			if (b.adds(bpc.getLiteral()))
				continue; //ignore if deleted and added in same action
			
			if (a.getPreconditions().contains(bpc.getLiteral()))
			{
				return MutexType.BdeleteApc;
			}
		}
		
		return MutexType.None;
	}

	/**
	 * Checks whether the effects of 2 actions have a destructive interaction. That is, does one
	 * action delete the effect of another.
	 * @param a The first action.
	 * @param b The second action.
	 * @return {@link MutexType#AdeleteBadd} if A deletes the same add effect of B, or {@link MutexType#BdeleteAadd}
	 * if vice versa. If neither holds, {@link MutexType#None} is returned.
	 */
	protected MutexType checkDeleteMutex(Action a, Action b)
	{
		//check if A's add effects deletes any of B's add effects and vice versa
		//ignore anything which deletes and adds in the same action effect, because it is 
		//not really blocking the other action
		for (Not apc : a.getDeletePropositions())
		{
			if (b.adds(apc.getLiteral()) == true &&
				b.deletes(apc.getLiteral()) == false)
			{
				return MutexType.AdeleteBadd;
			}
		}

		for (Not bpc : b.getDeletePropositions())
		{
			if (a.adds(bpc.getLiteral()) == true &&
				a.deletes(bpc.getLiteral()) == false)
			{
				return MutexType.BdeleteAadd;
			}
		}
		
		return MutexType.None;
	}

	/**
	 * Checks whether the effects of 2 actions cannot exist at the same time, as one action is blocking (pausing)
	 * access to the literal. This occurs when the literal is both deleted and added at the same time.
	 * @param a The first action.
	 * @param b The second action.
	 * @return {@link MutexType#ApauseB} if A blocks an effect of B, or {@link MutexType#BpauseA}
	 * if vice versa. If neither holds, {@link MutexType#None} is returned.
	 */
	protected MutexType checkPauseMutex(Action a, Action b)
	{
		//check to see if A both adds and deletes the same fact in it's effects. If this fact is 
		//in B's preconditions or effects, A must block B
		HashSet<SingleLiteral> aPauseSet = new HashSet<SingleLiteral>();
		for (Not d : a.getDeletePropositions())
		{
			aPauseSet.add((SingleLiteral) d.getLiteral());
		}
		
		aPauseSet.retainAll(a.getAddPropositions());
		if (aPauseSet.size() > 0) //if anything exists after a logical AND operation
		{
			for (SingleLiteral blockFact : aPauseSet)
			{
				if (b.getAddPropositions().contains(blockFact))
				{ 
					for (Not n : b.getDeletePropositions())
					{
						if (n.getLiteral().equals(blockFact))
							return MutexType.ApauseB;
					}
				}
			}
		}
		
		HashSet<SingleLiteral> bPauseSet = new HashSet<SingleLiteral>();
		for (Not d : a.getDeletePropositions())
			bPauseSet.add((SingleLiteral) d.getLiteral());
		
		bPauseSet.retainAll(b.getAddPropositions());
		if (bPauseSet.size() > 0) //if anything exists after a logical AND operation
		{
			for (SingleLiteral blockFact : bPauseSet)
			{
				if (a.getAddPropositions().contains(blockFact))
				{
					for (Not n : a.getDeletePropositions())
					{
						if (n.getLiteral().equals(blockFact))
							return MutexType.BpauseA;
					}
				}
			}
		}
		
		return MutexType.None;
	}

	/**
	 * Performs both action and fact mutex detection on all provided actions, which is probably a
	 * totally ordered plan. The results of this process (action and fact mutexes) are stored internally to this
	 * object for faster lookup during scheduling. 
	 * @param actions
	 */
	protected void detectMutexes(List<Action> actions)
	{
		this.actionMutexes.clear();
		this.factMutexes.clear();
		if (actions.isEmpty())
			return;
		
//		System.out.println("Detecting mutexes amongst "+actions.size()+" actions");
		
		this.detectStaticFacts(actions);
		
		//detect all effect and PC delete mutexes
		for (Action a : actions)
		{
			HashSet<Action> mutex = new HashSet<Action>();
			
			//TODO optimise- only check tail of list
			for (Action b : actions)
			{			
				if (a == b)
					continue;
				
				if (this.areActionsMutex(a, b) != MutexType.None)
					mutex.add(b);
			}
			this.actionMutexes.put(a, mutex);
		}
		
		//now use the above mutexes to detect any PC fact mutexes
		HashSet<Fact> allPCs = new HashSet<Fact>();
		for (Action a : actions)
		{
			for (Fact pc : a.getPreconditions())
				allPCs.add(pc);
		}
		
		for (Fact p : allPCs)
		{
//			if (p.isStatic())
			if (this.staticFacts.contains(p))
			{
//				System.out.println(p+" is static");
				continue;
			}
			
			HashSet<Fact> mutexGroup = new HashSet<Fact>();
			for (Fact q : allPCs)
			{
//				if (q.isStatic() || p == q)
				if (this.staticFacts.contains(q) || p == q)
					continue;
				
				MutexType mutex = this.checkCompetingPreconditionMutex(p, q, actions);
				if (mutex != MutexType.None)
				{
					mutexGroup.add(q);
				}
			}
			this.factMutexes.addMutex(p, mutexGroup); //TODO optimise for q, p
		}
		
		//finally, add the fact mutex info into the action mutexes
		for (Action a : actions)
		{
			//TODO optimise- only check tail of list
			for (Action b : actions)
			{		
				if (a == b || (this.actionMutexes.containsKey(a) && this.actionMutexes.get(a).contains(b)))
					continue;
				
				for (Fact p : a.getPreconditions())
				{
//					if (p.isStatic())
					if (this.staticFacts.contains(p))
						continue;
						
					for (Fact q : b.getPreconditions())
					{
//						if (p == q || q.isStatic())
						if (p == q || this.staticFacts.contains(q))
							continue;
						
						if (this.factMutexes.isMutex((GroundFact)p, (GroundFact)q))
						{
							if (this.actionMutexes.containsKey(a))
							{
								this.actionMutexes.get(a).add(b);
							}
							else
							{
								Set<Action> mut = new HashSet<Action>();
								mut.add(b);
								this.actionMutexes.put(a, mut);
							}
						}	
					}
				}
			}
		}
	}

	/**
	 * This method detects whether the PCs and effects of an action are static. This may seem like an
	 * unnecessary piece of functionality within this class, but it is not a requirement that
	 * this be set prior to planning, and may not be performed during parsing or grounding. Note that 
	 * static facts are stored in {@link STRIPSScheduler#staticFacts}, not through setting the
	 * relevant fact's static flag. See {@link STRIPSScheduler#staticFacts} for why.
	 * @param act
	 */
	protected void detectStaticFacts(Collection<Action> act)
	{
		this.staticFacts.clear();
		HashSet<Proposition> allPCs = new HashSet<Proposition>();
		for (Action a : act)
		{
			for (Fact pc : a.getPreconditions())
				if (pc instanceof Proposition)
					allPCs.add((Proposition) pc);
		}
		
		for (Proposition p : allPCs)
		{
			boolean found = false;
			for (Action a : act)
			{
				for (Fact add : a.getAddPropositions())
				{
					if (((Literal)add).getPredicateSymbol().equals(p.getPredicateSymbol()))
					{
						found = true;
						break;
					}
				}
			}
			
			if (!found)
//				p.setStatic(true);
				staticFacts.add(p);
//			else
//				p.setStatic(false);
		}
	}

	/**
	 * Returns the accuracy of the epsilon values added to each scheduled action.
	 * @return
	 */
	public String getEpsilon()
	{
		return epsilonAccuracy;
	}

	/**
	 * Set the number of decimal places to which epsilon values are made.
	 * @param epsilon
	 */
	public void setEpsilon(int epsilon)
	{
		this.epsilonAccuracy = this.getEpsilonString(epsilon);
	}

	/**
	 * Get the initial state which is used as a base for scheduling plans.
	 * @return
	 */
	public STRIPSState getInitialState()
	{
		return initial;
	}

	/**
	 * Set the initial state which is used as a base for scheduling plans.
	 * @param initial
	 */
	public void setInitial(STRIPSState initial)
	{
		this.initial = initial;
	}

	/**
	 * If true, this allows actions which do not have their preconditions already met by predecing
	 * actions to be scheduled at an appropriate timestep. For example, if action A has preconditions
	 * x and y, but only x has been achieved so far, it is assumed that y was achieved in the initial
	 * state, so the schedule time becomes time(x). If none of the PCs have been observed,
	 * the schedule time is 0.
	 * @param partiallyObservableAllowed Are unsatisfied preconditions allowed in scheduling
	 */
	public boolean isPartiallyObservableAllowed()
	{
		return partiallyObservableAllowed;
	}

	/**
	 * If true, this allows actions which do not have their preconditions already met by preceding
	 * actions to be scheduled at an appropriate timestep. For example, if action A has preconditions
	 * x and y, but only x has been achieved so far, it is assumed that y was achieved in the initial
	 * state, so the schedule time becomes time(x). If none of the PCs have been observed, then 
	 * the schedule time is 0. Note that any plans which have this flag set to true will naturally
	 * fail validation tests.
	 * @param partiallyObservableAllowed
	 */
	public void setPartiallyObservableAllowed(boolean partiallyObservableAllowed)
	{
		this.partiallyObservableAllowed = partiallyObservableAllowed;
	}
}

