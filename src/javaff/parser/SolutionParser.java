package javaff.parser;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.GroundProblem;
import javaff.data.Parameter;
import javaff.data.TotalOrderPlan;
import javaff.data.UngroundProblem;
import javaff.data.adl.Exists;
import javaff.data.adl.ForAll;
import javaff.data.adl.Imply;
import javaff.data.adl.Or;
import javaff.data.strips.And;
import javaff.data.strips.InstantAction;
import javaff.data.strips.Not;
import javaff.data.strips.Operator;
import javaff.data.strips.OperatorName;
import javaff.data.strips.PDDLObject;
import javaff.data.strips.PredicateSymbol;
import javaff.data.strips.Proposition;
import javaff.data.strips.STRIPSFact;
import javaff.data.strips.STRIPSInstantAction;
import javaff.data.strips.UngroundInstantAction;
import javaff.data.strips.Variable;
import javaff.data.temporal.DurativeAction;
import javaff.planning.STRIPSState;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Parses a .SOL file and produces the plan which created it.
 * @author David Pattison
 *
 */
public class SolutionParser
{

	public static TotalOrderPlan parseSTRIPSPlan(UngroundProblem up, String solutionPath) throws IOException, ParseException
	{
		return SolutionParser.parse(up, new File(solutionPath));
	}

	public static TotalOrderPlan parse(UngroundProblem up, File solutionFile) throws IOException, ParseException
	{		
		if (solutionFile.exists() == false)
			throw new FileNotFoundException("Cannot find solution file \""+solutionFile.getAbsolutePath()+"\"");
		//first, construct a map of the grounded parameters names to their objects
		UngroundProblem ungroundProblem = up;//(UngroundProblem)gp.clone();
//		Map<String, Parameter> map = new HashMap<String, Parameter>();
//		for (Proposition p : groundProblem.groundedPropositions)
//		{
//			for (Object paro : p.getParameters())
//			{
//				Parameter param = (Parameter)paro;
////				System.out.println("Adding "+param.getName().toLowerCase());
//				map.put(param.getName().toLowerCase(), param);
//			}
//		}
		
		FileReader fReader = new FileReader(solutionFile);
		
		BufferedReader bufReader = new BufferedReader(fReader);
		String line;
		
		TotalOrderPlan top = new TotalOrderPlan(ungroundProblem.goal);
		Scanner scan;
		StringTokenizer strTok;
		
		try 
		{
			out : while (bufReader.ready())
			{
				line = bufReader.readLine();
				//need to trim off any comments at the end of the line
				StringBuffer filteredLine = new StringBuffer();
				for (int i = 0; i < line.length(); i++)
				{
					if (line.charAt(i) == ';')
						break;
					
					filteredLine.append(line.charAt(i));
				}
				
				
				strTok = new StringTokenizer(filteredLine.toString(), ":() \t\n");
//				System.out.println("line is "+line);
				//check to see if this line is a comment
				if (strTok.hasMoreTokens() == false || line.charAt(0) == ';' )
					continue;
//				scan = new Scanner(line);
//				scan.useDelimiter(":(.*)");
//				scan.skip(Pattern.compile(".+:("));
				
//				System.out.println("Next is "+scan.next());
//				int stepNumber = scan.nextInt();
//				int stepNumber = Integer.parseInt(strTok.nextToken());
//				STRIPSInstantAction action = new STRIPSInstantAction();
				if (line.startsWith("(") == false)
						strTok.nextToken(); //skip action number
				
				OperatorName actionName = new OperatorName(strTok.nextToken());
				ArrayList<PDDLObject> vars = new ArrayList<PDDLObject>();
				while (strTok.hasMoreTokens())
				{
					String tok = strTok.nextToken().toLowerCase();
					if (tok.startsWith("["))
						continue;
					
					Parameter var = ungroundProblem.objectMap.get(tok);
					if (var == null)
					{
						var = ungroundProblem.constantMap.get(tok);
					}
					
					if (var == null)
						throw new NullPointerException("Cannot find parameter mapping: "+tok);
					
					vars.add((PDDLObject)var);
				}
				
				UngroundInstantAction ungroundAction = null;
				for (Operator a : ungroundProblem.actions)
				{
					if (a.name.toString().equalsIgnoreCase(actionName.toString()))
						ungroundAction = (UngroundInstantAction) a;
				}
				if (ungroundAction == null)
					throw new NullPointerException("Cannot find action with name \""+actionName+"\" in domain file");
				
				//ground action
				Action groundAction = ungroundAction.ground(vars);
				
				//action may be ADL, so convert to STRIPS only representation
				groundAction = SolutionParser.decompileADL(groundAction, up);
				
				top.addAction(groundAction);
			}
		} 
		catch (IOException e) 
		{
			throw new IOException("Incorrectly formatted solution file");
		}
		finally
		{
			bufReader.close();
			fReader.close();
		}
		
		return top;
	}
	
	/**
	 * Converts any ADL into equivalent STRIPS actions. This code has primarily been taken from AUTOGRAPH.
	 * @param ground
	 * @return 
	 */
	private static Set<Action> decompileADL(Collection<Action> actions, UngroundProblem uproblem)
	{
		Set<Action> refinedActions = new HashSet<Action>();
		for (Action a : actions)
		{
			refinedActions.add(SolutionParser.decompileADL(a, uproblem));
		}
		
		return refinedActions;
	}

	private static Action decompileADL(Action action, UngroundProblem uproblem)
	{
		//slow, inefficient, can't be bothered to fix
		HashSet<Fact> statics = new HashSet<Fact>();
		for (Set<Proposition> st : uproblem.staticPropositionMap.values())
		{
			statics.addAll(st);
		}
		
		Set<Action> refinedActions = new HashSet<Action>();
		
		//create stub initial state
		STRIPSState initialState = new STRIPSState(null, uproblem.initial, null);
		
		//keep a queue of potentially-ADL actions. Add partially compiled out actions to it. When
		//no ADL constructs exist in the PCs (actions unsupported for now), it can be added to the set of
		//legal actions.
		Queue<Action> queue = new LinkedList<Action>();
		queue.add(action);
		out: do
		{
			Action a = queue.remove();
			
			for (Fact pc : a.getPreconditions())
			{
				if (pc instanceof Imply)
				{
					Collection<? extends STRIPSFact> strips = ((Imply)pc).toSTRIPS(statics);
					Set<STRIPSFact> satisfiableStrips = new HashSet<STRIPSFact>();
					for (STRIPSFact and : strips)
					{
						if (SolutionParser.isFactSatisfiable(and, statics))
							satisfiableStrips.add(and);
					}
					
					//if the STRIPS version of the Imply cannot be achieved, then remove the Imply
					//conditions from the original action and re-add it to the queue
					if (satisfiableStrips.isEmpty())
					{
						InstantAction actionClone = (InstantAction) a.clone();
						And modifiedPCs = null;
						if (actionClone.getCondition() instanceof And)
						{
							modifiedPCs = (And) actionClone.getCondition().clone();
						}
						else //not an AND, so make it one
						{
							modifiedPCs = new And((Fact)actionClone.getCondition().clone());
						}
						
						modifiedPCs.remove(pc);
						
						actionClone.setCondition(modifiedPCs);
						queue.add(actionClone);
						
						continue out;
					}
					
					for (STRIPSFact stripsPC : satisfiableStrips)
					{
						InstantAction actionClone = (InstantAction) a.clone();
						And modifiedPCs = null;
						if (actionClone.getCondition() instanceof And)
						{
							modifiedPCs = (And) actionClone.getCondition().clone();
						}
						else //not an AND, so make it one
						{
							modifiedPCs = new And((Fact)actionClone.getCondition().clone());
						}
						
						modifiedPCs.remove(pc);

						modifiedPCs.addAll((Collection<Fact>) stripsPC.getFacts());
//							modifiedPCs.add(stripsPC);
						
						actionClone.setCondition(modifiedPCs);
						queue.add(actionClone);
						continue out;
					}
				}
				else if (pc instanceof ForAll)
				{
					//if it is grounded this should be a single And
					And compiledOut = (And) ((ForAll)pc).getFacts().iterator().next();
					
					if (a instanceof InstantAction)
					{
						InstantAction actionClone = (InstantAction) a.clone();
				
						Set<Fact> modifiedPCs = new HashSet<Fact>(a.getPreconditions());
						modifiedPCs.remove(pc);

						modifiedPCs.add(compiledOut);
						
						actionClone.setCondition(new And(modifiedPCs));
						queue.add(actionClone);
					}
					else
					{
//						DurativeAction stripsClone = (DurativeAction) a.clone();
//						
//						stripsClone.startCondition = and;
//						queue.add(stripsClone);
					}
					continue out;
				}
				else if (pc instanceof Exists)
				{
					throw new IllegalArgumentException("Decompiling Exists not yet supported");
				}
				else if (pc instanceof Or)
				{
					throw new IllegalArgumentException("Decompiling Or not yet supported");
				}
				else if (pc instanceof Not)
				{
					//ignore- Nots are legal
				}
			}
			
//			if (refinedActions.contains(a))
//				System.out.println("already here");
			
			
			return a;
		}
		while (queue.isEmpty() == false);
		
		throw new NullPointerException("Cannot convert action from ADL to STRIPS -- (this should be impossible!");
	}
	
	private static boolean isFactSatisfiable(Fact fact, Set<Fact> staticFacts)
	{
		for (Fact f : fact.getFacts())
		{
			if (f.isStatic() && staticFacts.contains(f) == false)
				return false;
		}
		
		return true;
	}
}
