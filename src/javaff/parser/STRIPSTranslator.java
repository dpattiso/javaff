package javaff.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.GroundProblem;
import javaff.data.Parameter;
import javaff.data.UngroundProblem;
import javaff.data.strips.And;
import javaff.data.strips.PDDLObject;
import javaff.data.strips.PredicateSymbol;
import javaff.data.strips.Proposition;
import javaff.data.strips.UngroundInstantAction;
import javaff.data.strips.Variable;

public abstract class STRIPSTranslator
{
	public static File translateToSTRIPSProblemFile(GroundProblem gproblem, String name)
	{
		return STRIPSTranslator.translateToSTRIPSProblemFile(gproblem, new File(name));
	}
	
	/**
	 * Translate the specified {@link GroundProblem} instance into a PDDL STRIPS problem file, which will be
	 * written to a File.
	 * @param gproblem The problem to encode.
	 * @param stripsPFile The output file.
	 * @return The output file
	 */
	public static File translateToSTRIPSProblemFile(GroundProblem gproblem, File stripsPFile)
	{
		FileWriter domainFWriter, pfileFWriter;
		BufferedWriter domainBufWriter, pfileBufWriter;
		
		try
		{
			pfileFWriter = new FileWriter(stripsPFile);
			
			
			pfileBufWriter = new BufferedWriter(pfileFWriter);
			//parse out STRIPS pfile here
			stripsPFile.createNewFile();
			//write out requirements
			pfileBufWriter.write("(define (problem "+stripsPFile.getName().replace(".", "_")+") (:domain "+gproblem.getName()+")\n");
			//write out types
			pfileBufWriter.write("(:objects \n");
			
			HashSet<Parameter> params = new HashSet<Parameter>();
			for (Object o : gproblem.getActions())
			{
					params.addAll(((Action)o).getParameters());
			}
			
			for (Object to : params)
			{
				pfileBufWriter.write("\t"+((PDDLObject)to).toStringTyped().toLowerCase()+"\n");
			}
			pfileBufWriter.write(")\n");
			//write out predicates
			pfileBufWriter.write("(:init\n");			
			for (Object io: gproblem.getInitial())
			{
				pfileBufWriter.write("\t("+io.toString().toLowerCase()+")\n");	
			}
			pfileBufWriter.write(")\n");
			//write out actions
			pfileBufWriter.write("(:goal (and\n");
			for (Object go : gproblem.getGoal().getFacts())
			{
				pfileBufWriter.write("\t("+go.toString().toLowerCase()+")\n");
			}
			pfileBufWriter.write(")))\n");
			
			pfileBufWriter.close();
			pfileFWriter.close();
			
			System.out.println();
		}
		catch (IOException ioe)
		{
			System.err.println("An error was encountered while converting the domain or problem file: "+ioe.getMessage());
		}
		
		
		return stripsPFile;	
	}
	
//	/**
//	 * Converts a Complex ground problem into a STRIPS problem.
//	 * @param complexGP
//	 * @return
//	 */
//	public static GroundProblem translateToSTRIPSGroundProblem(GroundProblem complexGP)
//	{
//		Set stripsActions = new HashSet(complexGP.actions);
//		Set stripsInitial = new HashSet();
//		And stripsGoal = new And();
//		Map stripsFunctions = new Hashtable();
//		
//		for (Proposition io : complexGP.initial)
//		{
//			stripsInitial.add(io);
//		}
//		
//		for (Fact go : complexGP.goal.getFacts())
//		{
//			if (go instanceof GroundFact)
//				stripsGoal.add(go);
//		}
//		
//		GroundProblem stripsProblem = new GroundProblem(stripsActions, stripsInitial, stripsGoal, stripsFunctions, complexGP.metric);
//		
//		return stripsProblem;
//	}
	
//	public static GroundProblem translateToSTRIPSGroundProblem(File domain, File pfile)
//	{
//		UngroundProblem up = PDDL21parser.parseFiles(domain, pfile);
//		GroundProblem complexGP = up.ground();
//		
//		Set stripsActions = new HashSet(complexGP.actions);
//		Set stripsInitial = new HashSet();
//		And stripsGoal = new And();
//		Map stripsFunctions = new Hashtable();
//		
//		for (Proposition io : complexGP.initial)
//		{
//			stripsInitial.add(io);
//		}
//		
//		for (Fact go : complexGP.goal.getFacts())
//		{
//			if (go instanceof GroundFact)
//				stripsGoal.add(go);
//			else
//				throw new IllegalArgumentException("Illegal type found");
//		}
//		
//		GroundProblem stripsProblem = new GroundProblem(stripsActions, stripsInitial, stripsGoal, stripsFunctions, complexGP.metric);
//		
//		return stripsProblem;
//	}
	
	/**
	 * Convert a complex domain and problem into a STRIPS PDDL representation. This essentially removes any non-STRIPS definitions.
	 * @param domain
	 * @param pfile
	 * @return An array of length 2, which contains the domain and problem files as STRIPS-only representations.
	 */
	public static File[] translateToSTRIPSFiles(File domain, File pfile)
	{
		File stripsDomainFile = new File(domain.getAbsolutePath()+".strips");
		File stripsPFile = new File(pfile.getAbsolutePath()+".strips");
		
		FileReader domainFReader, pfileFReader;
		FileWriter domainFWriter, pfileFWriter;
		BufferedReader domainBufReader, pfileBufReader;
		BufferedWriter domainBufWriter, pfileBufWriter;
		
		try
		{
			domainFReader = new FileReader(domain);
			pfileFReader = new FileReader(pfile);
			
			domainFWriter = new FileWriter(stripsDomainFile); 
			pfileFWriter = new FileWriter(stripsPFile);

			domainBufReader = new BufferedReader(domainFReader);
			domainBufWriter = new BufferedWriter(domainFWriter);
			
			while (domainBufReader.ready())
			{
				String line = domainBufReader.readLine();
				if (isLegal(line, domainBufReader))
					domainFWriter.write(line);
			}
			domainFReader.close();
			domainFWriter.close();
			domainBufReader.close();
			domainBufWriter.close();
			

			pfileBufReader = new BufferedReader(pfileFReader);
			pfileBufWriter = new BufferedWriter(pfileFWriter);
			
			while (pfileBufReader.ready())
			{
				String line = pfileBufReader.readLine();
				if (isLegal(line, pfileBufReader))
					pfileFWriter.write(line);
			}
			pfileFReader.close();
			pfileFWriter.close();
			pfileBufReader.close();
			pfileBufWriter.close();
			
			
		}
		catch (IOException ioe)
		{
			System.err.println("An error was encountered while converting the domain or problem file: "+ioe.getMessage());
		}
		
		File[] arr = new File[2];
		arr[0] = stripsDomainFile;
		arr[1] = stripsPFile;
		
		return arr;		
	}
	

	/**
	 * Splits an ungrounded problem into a PDDL representation.
	 * @param domain
	 * @param pfile
	 * @return 2 Files, the domain and problem
	 * @throws PDDLParseException 
	 */
	public static File[] translateToSTRIPSFiles(UngroundProblem uproblem, String stripsDomainName, 
			String stripsProblemName, boolean overwrite) throws ParseException
	{
		System.out.println("Translating unified unground domain into domain and problem");
		File stripsDomainFile = new File(stripsDomainName);
		File stripsPFile = new File(stripsProblemName);
		
		if (stripsDomainFile.exists() && overwrite)
			stripsDomainFile.delete();
		if (stripsPFile.exists() && overwrite)
			stripsPFile.delete();
		
		FileWriter domainFWriter, pfileFWriter;
		BufferedWriter domainBufWriter, pfileBufWriter;
		
		try
		{
			
			domainFWriter = new FileWriter(stripsDomainFile); 
			pfileFWriter = new FileWriter(stripsPFile);

			domainBufWriter = new BufferedWriter(domainFWriter);
			
//			//durative actions require a stub invariant to be applied after the start action and as a condition of the end action
//			boolean containsDurative = false;
//			for (Object ao : uproblem.actions)
//			{
//				if (ao instanceof UngroundDurativeAction)
//				{
//					containsDurative = true;
//					break;
//				}
//			}
			System.out.println("Creating domain file");
			//parse out STRIPS domain here
			stripsDomainFile.createNewFile();
			//write out requirements
			domainBufWriter.write("(define (domain "+uproblem.DomainName+")\n");
			domainBufWriter.write("(:requirements :typing)\n");
			//write out types
			domainBufWriter.write("(:types \n");
			for (Object to : uproblem.types)
			{
				domainBufWriter.write("\t"+to.toString()+"\n");
			}
//			if (containsDurative)
//				domainBufWriter.write("\tdummy_invariant");
			
			domainBufWriter.write(")\n\n");
			//write out predicates
			domainBufWriter.write("(:predicates \n");
			for (Object po: uproblem.predSymbols)
			{
				domainBufWriter.write("("+((PredicateSymbol)po).toStringTyped().toLowerCase()+")\n");	
			}
//			for (Object ao : uproblem.actions)
//			{
//				if (ao instanceof UngroundDurativeAction)
//				{
//					UngroundDurativeAction a = (UngroundDurativeAction) ao;
//					domainBufWriter.write("("+a.name+"_invariant ?i - dummy_invariant)\n");	
//				}
//			}
			
			
			domainBufWriter.write(")\n"); //close predicates
			//write out actions
			for (Object ao : uproblem.actions)
			{
				if (ao instanceof UngroundInstantAction)
				{
					UngroundInstantAction a = (UngroundInstantAction) ao;
					domainBufWriter.write("(:action "+a.name.toString().toLowerCase()+"\n");
					domainBufWriter.write(":parameters (");
					for (Object po : a.params)
					{
						domainBufWriter.write(""+((Variable)po).toStringTyped().toLowerCase()+" ");
					}
					domainBufWriter.write(")\n");

					if (a.condition instanceof And)
						domainBufWriter.write(":precondition "+a.condition.toString().toLowerCase()+"\n");
					else
						domainBufWriter.write(":precondition ("+a.condition.toString().toLowerCase()+")\n");
					
					if (a.effect instanceof And)
						domainBufWriter.write(":effect "+a.effect.toString().toLowerCase()+"\n");
					else
						domainBufWriter.write(":effect ("+a.effect.toString().toLowerCase()+")\n");					
					domainBufWriter.write(")\n");
				}
//				else if (ao instanceof UngroundDurativeAction)
//				{
//					//System.err.println("Durative actions not currently supported during STRIPS translation");
//					
//					//split into 2 actions, SAPA style
//					UngroundDurativeAction da = (UngroundDurativeAction) ao;
//					UngroundInstantAction sa = da.startAction;
//					UngroundInstantAction ea = da.endAction;
//					
//					//write start action
//					domainBufWriter.write("(:action "+sa.name.toString().toLowerCase()+"\n");
//					domainBufWriter.write(":parameters (");
//					for (Object po : sa.params)
//					{
//						domainBufWriter.write(""+((Variable)po).toStringTyped().toLowerCase()+" ");
//					}
//					domainBufWriter.write(")\n");
//
//					sa = removeDurationPredicates(sa);
//					
//					if (sa.condition instanceof AND)
//					{
//						domainBufWriter.write(":precondition "+sa.condition.toString().toLowerCase()+"\n");
//					}
//					else
//					{
//						domainBufWriter.write(":precondition ("+sa.condition.toString().toLowerCase()+")\n");
//					}
//
//					if (sa.effect instanceof AND)
//					{
//						domainBufWriter.write(":effect "+sa.effect.toString().toLowerCase()+"\n");
//					}
//					else
//					{
//						domainBufWriter.write(":effect ("+sa.effect.toString().toLowerCase()+")\n");
//					}
//					domainBufWriter.write(")\n");
//									
//					//write end action
//					domainBufWriter.write("(:action "+ea.name.toString().toLowerCase()+"\n");
//					domainBufWriter.write(":parameters (");
//					for (Object po : ea.params)
//					{
//						domainBufWriter.write(""+((Variable)po).toStringTyped().toLowerCase()+" ");
//					}
//					domainBufWriter.write(")\n");
//
//					ea = removeDurationPredicates(sa);
//					
//					if (ea.condition instanceof AND)
//						domainBufWriter.write(":precondition "+ea.condition.toString().toLowerCase()+"\n");
//					else
//						domainBufWriter.write(":precondition ("+ea.condition.toString().toLowerCase()+")\n");
//					
//					if (ea.effect instanceof AND)
//						domainBufWriter.write(":effect "+ea.effect.toString().toLowerCase()+"\n");
//					else
//						domainBufWriter.write(":effect ("+ea.effect.toString().toLowerCase()+")\n");					
//					domainBufWriter.write(")\n");
//				}
				else
					throw new ParseException("Illegal Action type discovered during STRIPS translation");
			}
			domainBufWriter.write(")");
			
			domainBufWriter.close();
			domainFWriter.close();
			

			pfileBufWriter = new BufferedWriter(pfileFWriter);
			
			System.out.println("Creating pfile");
			//parse out STRIPS pfile here
			stripsPFile.createNewFile();
			//write out requirements
			pfileBufWriter.write("(define (problem "+stripsPFile.getName().replace(".", "_")+") (:domain "+uproblem.DomainName+")\n");
			//write out types
			pfileBufWriter.write("(:objects \n");
			for (Object to : uproblem.objects)
			{
				pfileBufWriter.write("\t"+((PDDLObject)to).toStringTyped().toLowerCase()+"\n");
			}
			pfileBufWriter.write(")\n");
			//write out predicates
			pfileBufWriter.write("(:init\n");			
			for (Object io: uproblem.initial)
			{
				pfileBufWriter.write("\t("+io.toString().toLowerCase()+")\n");	
			}
			pfileBufWriter.write(")\n");
			//write out actions
			pfileBufWriter.write("(:goal (and\n");
			for (Object go : uproblem.goal.getFacts())
			{
				pfileBufWriter.write("\t("+go.toString().toLowerCase()+")\n");
			}
			pfileBufWriter.write(")))\n");
			
			pfileBufWriter.close();
			pfileFWriter.close();
			
			//TODO Remove this- it only exists for testing purposes
//			UngroundProblem stripsUP = PDDL21parser.parseFiles(stripsDomainFile, stripsPFile);
//			GroundProblem stripsGP = stripsUP.ground();
			
			System.out.println();
		}
		catch (IOException ioe)
		{
			System.err.println("An error was encountered while converting the domain or problem file: "+ioe.getMessage());
		}
		
		File[] arr = new File[2];
		arr[0] = stripsDomainFile;
		arr[1] = stripsPFile;
		
		return arr;		
	}
	
//	private static UngroundInstantAction removeDurationPredicates(UngroundInstantAction a)
//	{		
//		UngroundInstantAction clone = a;
//
//		AND newCondition = new AND();
//		for (Fact o : a.condition.getStaticPredicates())
//		{
//			if (o instanceof BinaryComparator == true && o instanceof TrueCondition == true)
//				continue;
//			
//			newCondition.add(o);
//		}
//		
//		AND newEffect = new AND();
//		if (a.effect instanceof ResourceOperator == true || a.effect instanceof NullEffect == true)
//			;
//		else if (a.effect instanceof Predicate || a.effect instanceof NOT)
//		{
//			newEffect.add(a.effect);
//		}
//		else if (a.effect instanceof AND)
//		{
//			AND and = (AND)a.effect;
//			Set<GroundFact> stat = and.literals;
//			for (GroundFact eff : stat)
//			{
//				if (eff instanceof ResourceOperator == true && eff instanceof NullEffect == true)
//					continue;
//				else if (eff instanceof Predicate || eff instanceof NOT)
//				{
//					newEffect.add(eff);
//				}
//			}
//		}
//		
//		clone.condition = newCondition;
//		clone.effect = newEffect;
//		
//		return clone;
//		
//	}
	
	/**
	 * Tests whether a line is legal STRIPS syntax (very basic).
	 * @param line
	 * @param bufReader
	 * @return
	 */
	protected static boolean isLegal(String line, BufferedReader bufReader)
	{
		if (line.contains("="))
			return false;
		else if (line.contains(">"))
			return false;
		else if (line.contains("<"))
			return false;
		else if (line.contains("increase"))
			return false;
		else if (line.contains("decrease"))
			return false;
		else if (line.contains("(:functions"))
		{
			int paramStack = 0;
			while (true)
			{
				for (int i = 0; i < line.length(); i++)
				{
					if (line.charAt(i) == '(')
						paramStack++;
					else if (line.charAt(i) == ')')
						paramStack--;
				}
				if (paramStack == 0)
					break;
				else
				{
					try
					{
						line = bufReader.readLine();
					}
					catch (IOException e)
					{
						System.err.println("An error occurred while the domain was being translated into a STRIPS version");
						e.printStackTrace();
					}
				}
			}
			
			return false;
		}
		
		return true;
	}
}
