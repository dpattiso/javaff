package javaff.landmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.GroundProblem;
import javaff.data.strips.Proposition;
import javaff.data.strips.SingleLiteral;
import javaff.graph.ActionEdge;
import javaff.graph.ActionEdge.ActionEdgeType;
import javaff.planning.STRIPSState;

/**
 * A regression-based landmark generator.
 * 
 * @author David Pattison
 *
 */
public class RegressionLandmarkGenerator implements ILandmarkGenerator
{
    protected LandmarkGraph graph;
//    private ArrayList<Proposition> landmarks;
    protected ArrayList<Action> groundedActions;
    protected STRIPSState init;
    protected Collection<SingleLiteral> goals;
//    private boolean allowForcedLandmarks;
    protected GroundProblem gproblem;
    private boolean allowDisjunctive;

    private HashMap<Proposition, ArrayList<Proposition>> naturalOrderings, 
        necessaryOrderings, 
        greedyNecessaryOrderings,
        impossibleOrderings;


//    public LmGenerator(State init, ArrayList<State> goals, ArrayList<Action> actions)
//    {
//        this.init = init;
//        this.goals = goals;
//        this.actions = actions;
//
//        this.landmarks = new ArrayList<Proposition>();
//        this.graph = new DefaultDirectedWeightedGraph<STRIPSState, DefaultEdge>(DefaultWeightedEdge.class);
//
//        this.naturalOrderings = new HashMap<Proposition, ArrayList<Proposition>>();
//        this.necessaryOrderings = new HashMap<Proposition, ArrayList<Proposition>>();
//        this.greedyNecessaryOrderings = new HashMap<Proposition, ArrayList<Proposition>>();
//        this.impossibleOrderings = new HashMap<Proposition, ArrayList<Proposition>>();
//    }
//    public LandmarkGenerator(GroundProblem gp)
//    {
//    	this(gp.getSTRIPSInitialState(), gp.goal, gp.actions);
//    	this.gproblem = gp;
//    }
    
//    public LandmarkGenerator(STRIPSState init, GroundCondition gc, Set actions)
    public RegressionLandmarkGenerator(GroundProblem gp, boolean allowDisjunctive)
    {
    	this.gproblem = gp;
    	this.allowDisjunctive = allowDisjunctive;
//    	this.allowForcedLandmarks = allowForcedLandmarks;
        this.init = gp.getSTRIPSInitialState();
        this.goals = new ArrayList<SingleLiteral>();
        for (Object o : this.gproblem.getGoal().getFacts())
        {
//            System.out.println("goal condition is instance of : "+o.getClass().getCanonicalName());
        	if (o instanceof Proposition)
        		this.goals.add((Proposition)o);
        	else
        		throw new IllegalArgumentException("Invalid proposition in goal tmstate.");
        }
        
        this.groundedActions = new ArrayList<Action>();
        for (Object o : this.gproblem.getActions())
        {
//            System.out.println("action is instance of : "+o.getClass().getCanonicalName());
        	if (o instanceof Action)
        	{
        		this.groundedActions.add((Action)o);
                //System.out.println("Grounded action is "+(Action)o);
        	}
        	else
        		throw new IllegalArgumentException("Invalid action type.");
        }
        
//        this.landmarks = new ArrayList<Proposition>();
        //this.graph = new Graph()

        graph = new LandmarkGraph();


        this.naturalOrderings = new HashMap<Proposition, ArrayList<Proposition>>();
        this.necessaryOrderings = new HashMap<Proposition, ArrayList<Proposition>>();
        this.greedyNecessaryOrderings = new HashMap<Proposition, ArrayList<Proposition>>();
        this.impossibleOrderings = new HashMap<Proposition, ArrayList<Proposition>>();
    }
    
    
    /**
     * 1- Get actions which can be applied to open landmarks
     * 2- get common preconditions of this action set and make these new landmarks
     * 3- filter out preconditions which appear in initial tmstate
     * 4- loop
     */
    public void generateLandmarks()
    {
        this.clearLandmarks();
    	this.generateLandmarks(this.goals);
    }
    
    /**
     * Generates a set of landmarks from the given goal conjunction.
     */
    public void generateLandmarks(Fact goal)
    {
    	this.generateLandmarks(goal);
    }
    
    
    private LandmarkOrdering getEmptyLMO(Fact goal)
    {
		int applicableCount = 0;
		LandmarkOrdering lmo = new LandmarkOrdering(goal);
		for (Action a : this.groundedActions) //loop over each grounded action in the tmstate space
		{
			boolean isApplicable = false;
			for (Fact addProp : a.getAddPropositions())
			{
//				Singl addProp = (Proposition) addObj;
				if (goal.equals(addProp))
				{
					isApplicable = true; //if effects of action can be applied to Open Landmark
					applicableCount++;
				}
			}
//			for (Object delObj : a.getDeletePropositions())
//			{
//				Proposition delProp = (Proposition)delObj;
//				if (openL.equals(delProp))
//				{
//					isApplicable = true; //if effects of action can be applied to Open Landmark
//					applicableCount++;
//				}
//			}
			
//			System.out.println("openl is "+openLandmarks.size());
			if (isApplicable)
			{
				lmo.addAction(a);
				//add to possible landmark list
//				System.out.println("Found possible landmark");
			}
		}
		
		return lmo;
    }
    
    /**
     * Generates a set of landmarks from the given set of goal propositions.
     */
    public void generateLandmarks(Collection<SingleLiteral> goalProps)
    {
        //first construct first landmarks as goals.
        Queue<Fact> openLandmarks = new LinkedList<Fact>();
        openLandmarks.addAll(goalProps);
//        for (GroundCondition p : goalProps)
//        {
//            openLandmarks.add(p);
////            System.out.println("Goal: "+p.toString());
        
//        }
        
//        System.out.println(this.groundedActions.size()+" grounded actions available to regression landmarker");
        ArrayList<LandmarkOrdering> mappings = new ArrayList<LandmarkOrdering>();
        while (openLandmarks.isEmpty() == false) //loop while there are still landmarks to regress from
        {
            mappings = new ArrayList<LandmarkOrdering>();	//array for possible predecessor landmarks
            
        	//loop over each open landmark- initialised to the goal set
        	for (Fact openL : openLandmarks)
        	{
        		LandmarkOrdering lmo = this.getEmptyLMO(openL);
        		mappings.add(lmo);
//    			System.out.println("Applicable count for "+openL+" is "+applicableCount);
        	}
    		
        	//loop over each action open mapping to find if any share common preconditions
        	ArrayList<Proposition> newLandmarks = new ArrayList<Proposition>();
        	for (LandmarkOrdering lmoo : mappings)
        	{
//        		System.out.println(lmoo);
        		List<Action> actions = lmoo.getActions();
        		ArrayList<Proposition> alreadySeen = new ArrayList<Proposition>();
            	int[] alreadySeenCount = new int[1000000];
            	int count = 0;
        		for (Action a : actions) //all action which achieve lmoo.successor
        		{
        			for (Object pco : a.getPreconditions())
        			{
        				Proposition pc = (Proposition)pco;
        				if (alreadySeen.contains(pc) == false)
        				{
        					alreadySeen.add(pc);
        					alreadySeenCount[count++] = 1;
        				}
        				else
        				{
        					int index = alreadySeen.indexOf(pc);
        					alreadySeenCount[index] += 1;
        				}
        			}
        		}
        		
        		boolean foundOne = false; // found at least one necessary landmark
        		for (int i = 0; i < count; i++)
        		{
        			if (alreadySeenCount[i] == actions.size()) //if all actions which achieve goal have the same PC then PC is a landmark 
        			{
        				newLandmarks.add(alreadySeen.get(i));
        				lmoo.setPrevious(alreadySeen.get(i));
        				lmoo.setOrdering(ActionEdgeType.Necessary);
        				//System.out.println("XXXXXXXXXXX found necessary ordering");
        				foundOne = true;
        			}
        		}
        		
        		//if no necessary landmarks were found for this proposition, do one-step-lookahead
//        		if (foundOne == false) 
//        		{
//        			HashSet<Action> allApplicable = new HashSet<Action>();
//        			for (Proposition asp : alreadySeen)
//        			{
//        				for (Object ao : this.gproblem.actions)
//        				{
//        					Action a = (Action)ao;
//        					if (a.getAddPropositions().contains(asp))
//        						allApplicable.add(a);
//        				}        					
//        			}
//        			alreadySeen.clear();
//                	alreadySeenCount = new int[1000000]; 
//                	count = 0;
//                	
//        			for (Action a : allApplicable)
//        			{
//        				for (Object pco : a.getConditionalPropositions())
//            			{
//            				Proposition pc = (Proposition)pco;
//            				if (alreadySeen.contains(pc) == false)
//            				{
//            					alreadySeen.add(pc);
//            					alreadySeenCount[count++] = 1;
//            				}
//            				else
//            				{
//            					int index = alreadySeen.indexOf(pc);
//            					alreadySeenCount[index] += 1;
//            				}
//            			}
//            		}
//        			
//            		for (int i = 0; i < count; i++)
//            		{
//            			if (alreadySeenCount[i] == actions.size()) //if all actions which achieve goal have the same PC then PC is a landmark 
//            			{
//            				if (alreadySeen.get(i).equals(lmoo.getSuccessor()) ||
//            					newLandmarks.contains(alreadySeen.get(i)) ||
//            					openLandmarks.contains(alreadySeen.get(i))
//            					)
//            				{
//            					continue;
//            				}
//            					
//            				newLandmarks.add(alreadySeen.get(i));
//            				lmoo.setPrevious(alreadySeen.get(i));
//            				lmoo.setOrdering(ActionEdgeType.Natural);
//            				System.out.println("ZZZZZZZZZZZ found natural ordering: "+lmoo);
//            			}
//            		}
//        		}
        	}
        	
        	//TODO if clear isnt done, will more than simple natural-orderings be produced?
        	openLandmarks.clear(); //wipe previous open landmarks now that we have checked all of them
        	
			//check new landmarks are not in initial tmstate
    		for (Proposition lm : newLandmarks)
    		{
    			boolean found = false;
	        	for (Object initPropObj : this.init.getTrueFacts())
	        	{
	    			Proposition initProp = (Proposition)initPropObj;
	        		if (initProp.toString().equals(lm.toString())) //if true, landmark is in initial tmstate, and is illegal
	        		{
//		        		System.out.println("Found init fact: "+initProp);
		        		found = true;
		        		break;
	        		}
	        	}
	        	
        		if (!found && openLandmarks.contains(lm) == false)
        		{
        			openLandmarks.add(lm);
//	        		System.out.println("Added landmark: "+lm);
        		}
    		}

			//actually construct graph
	        for (LandmarkOrdering lmo : mappings)
	        {
	        	//only add edges for mappings which have a start and end proposition
	        	if (lmo.getPrevious() != null && lmo.getSuccessor() != null)
	        	{
	        		for (Action a :lmo.getActions())
	        		{
		        		this.graph.addEdge(lmo.getPrevious(), 
		        						   lmo.getSuccessor(),
		        						   new ActionEdge(a, lmo.getOrdering()));
	        		}
	        		
	        	}
	        }
//			System.out.println("openlandmarks has "+openLandmarks.size());
        }
    }	
    
    public List<Fact> getLandmarks()
    {
    	List<Fact> lms = new ArrayList<Fact>();
    	Collection<Fact> verts = this.graph.vertexSet();
    	for (Fact v : verts)
    		lms.add(v);
    	
    	return lms;
    }
    
    public LandmarkGraph getLandmarkGraph()
    {
    	return this.graph;
    }
    
    public void clearLandmarks()
    {
        this.graph.clear();
        this.naturalOrderings.clear();
        this.necessaryOrderings.clear();
        this.greedyNecessaryOrderings.clear();
    }
}
