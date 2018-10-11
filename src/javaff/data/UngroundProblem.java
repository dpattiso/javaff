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

import javaff.data.strips.And;
import javaff.data.strips.Constant;
import javaff.data.strips.Not;
import javaff.data.strips.Predicate;
import javaff.data.strips.STRIPSFact;
import javaff.data.strips.STRIPSInstantAction;
import javaff.data.strips.SimpleType;
import javaff.data.strips.PredicateSymbol;
import javaff.data.strips.PDDLObject;
import javaff.data.strips.Proposition;
import javaff.data.strips.Operator;
import javaff.data.strips.SingleLiteral;
import javaff.data.strips.UngroundInstantAction;
import javaff.data.temporal.TimedInitialLiteral;
import javaff.data.adl.ForAll;
import javaff.data.adl.Imply;
import javaff.data.adl.Quantifier;
import javaff.data.metric.BinaryComparator;
import javaff.data.metric.Function;
import javaff.data.metric.FunctionSymbol;
import javaff.data.metric.Metric;
import javaff.data.metric.NamedFunction;
import javaff.data.metric.NumberFunction;
import javaff.parser.ParseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;

public class UngroundProblem
{
	//TODO setting this to true this breaks ADL decompilation for Implys as it is important that 
	//facts which are NOT true are included in the decompilation
	/**
	 * If set to True, grounding will remove out all static facts generated during grounding from
	 * all actions and the initial state. These would otherwise
	 * only slow down search. Defaults to false. Must be set prior to calling ground().
	 */
	public static boolean RemoveStaticFacts = true;
	
	public String DomainName = ""; // Name of Domain
	public String ProblemName = ""; // Name of Problem
	public String ProblemDomainName = ""; // Name of Domain as specified by the
										// Problem


	public Set<Type> types = new HashSet<Type>(); // For simple object types in this
										// domain (SimpleTypes)
	public Map<String, Type> typeMap = new Hashtable<String, Type>(); // Set for mapping String -> types
											// (String => Type)
	public Map<Type, Set<PDDLObject>> typeSets = new Hashtable<Type, Set<PDDLObject>>(); // Maps a type on to a set of
											// PDDLObjects (Type => Set
											// (PDDLObjects))

	public Set<PredicateSymbol> predSymbols = new HashSet<PredicateSymbol>(); // Set of all (ungrounded) predicate
											// (PredicateSymbol)
	public Map<String, PredicateSymbol> predSymbolMap = new Hashtable<String, PredicateSymbol>(); // Maps Strings of the symbol to
												// the Symbols (String =>
												// PredicateSymbol)

	public Set<Constant> constants = new HashSet<Constant>(); // Set of all constant (PDDLObjects)
	public Map<String, Constant> constantMap = new Hashtable<String, Constant>(); // Maps Strings of the constant
												// to the PDDLObject

	public Set<FunctionSymbol> funcSymbols = new HashSet<FunctionSymbol>(); // Set of all function symbols
											// (FunctionSymbol)
	public Map<String, FunctionSymbol> funcSymbolMap = new Hashtable<String, FunctionSymbol>(); // Maps Strings onto the Symbols
												// (String => FunctionSymbol)

	public Set<Operator> actions = new HashSet<Operator>(); // List of all (ungrounded) actions
										// (Operators)

	public Set<PDDLObject> objects = new HashSet<PDDLObject>(); // Objects in the problem (PDDLObject)
	public Map<String, PDDLObject> objectMap = new Hashtable<String, PDDLObject>(); // Maps Strings onto PDDLObjects
											// (String => PDDLObject)

	public Set<Fact> initial = new HashSet<Fact>(); // Set of initial facts (Proposition)
	public Map<NamedFunction, BigDecimal> funcValues = new Hashtable<NamedFunction, BigDecimal>(); // Maps functions onto numbers
												// (NamedFunction => BigDecimal)
	public GroundFact goal = new And();

	public Metric metric;
	
	public DomainRequirements requirements = new DomainRequirements();

	public Map<PredicateSymbol, Set<Proposition>> staticPropositionMap = new Hashtable<PredicateSymbol, Set<Proposition>>(); // (PredicateName => Set
														// (Proposition))

	public List<TimedInitialLiteral> tils = new ArrayList<TimedInitialLiteral>();
	
	public TypeGraph typeGraph = new TypeGraph();
	
	public UngroundProblem()
	{
		typeMap.put(SimpleType.rootType.toString(), SimpleType.rootType);
	}
	
	/**
	 * Quantifier facts require that the set of relevant domain objects be added AFTER domain and pfile parsing has completed.
	 */
	public void setupQuantifiers()
	{
		for (Operator o : this.actions)
		{
			if (o instanceof UngroundInstantAction)
			{
				UngroundInstantAction a = (UngroundInstantAction) o;
				for (Fact pc : a.condition.getFacts())
				{
					this.setupQuantifier(pc);
				}
				for (Fact eff : a.effect.getFacts())
				{
					this.setupQuantifier(eff);
				}
			}
		}
	}
	
	protected void setupQuantifier(Fact f)
	{
		if (f instanceof Quantifier)
		{
			Set<PDDLObject> objects = this.typeSets.get(((Quantifier)f).getVariable().getType());
			
			((Quantifier) f).setQuantifiedObjects(objects);
		}
		//TODO this is a hack to get negated Quantifiers working. Really, it should consider all possible types in the
		//hierarchy
		else if (f instanceof Not) 
		{
			this.setupQuantifier(((Not)f).getLiteral());
		}
	}

//	public GroundProblem groundEverything()
//	{
////		long startTime = System.nanoTime();
//		calculateStatics();
//		makeStaticPropositionMap();
////		long afterStatics = System.nanoTime();
//		buildTypeSets();
////		long afterTypeSets = System.nanoTime();
//		Set<Action> groundActions = new HashSet<Action>();
//		Iterator<Operator> ait = actions.iterator();
//		while (ait.hasNext())
//		{
//			Operator o = (Operator) ait.next();
//			Set<Action> s = o.ground(this);
//			groundActions.addAll(s);
//		}
////		long afterActions = System.nanoTime();
//		
//		for (Object ao : groundActions)
//		{
//			Action a = (Action) ao;
//			for (Object co : a.getComparators())
//			{
////				System.out.println("found comparator "+co+" in "+a);
//				BinaryComparator bc = (BinaryComparator) co;
//				NamedFunction namef = (NamedFunction) bc.first;
//				NumberFunction numf = (NumberFunction) bc.second;
//				
//				if (this.funcValues.containsKey(namef) == false)
//					this.funcValues.put(namef, new BigDecimal(0));
//			}
//		}
////		long afterFunc = System.nanoTime();
//
//		// static-ify the functions
////		Iterator<Action> gait = groundActions.iterator();
////		while (gait.hasNext())
////		{
////			Action a = (Action) gait.next();
////			a.staticify(funcValues);
//////			System.out.println("Statified "+a);
////		}
////		long afterStaticify = System.nanoTime();
//
//		// remove static functions from the intial state
//		//removeStaticsFromInitialState();
//
//		// -could put in code here to
//		// a) get rid of static functions in initial state - DONE
//		// b) get rid of static predicates in initial state - DONE
//		// c) get rid of static propositions in the actions (this may have
//		// already been done)
//		// d) get rid of no use actions (i.e. whose preconditions can't be
//		// achieved) - DONE - David Pattison, 23/5/2011
//		
//		
//		GroundProblem rGP = new GroundProblem(groundActions, initial, goal,
//				funcValues, metric);
////		long afterCreate = System.nanoTime();
//		rGP.name = this.DomainName;//+"_-_"+this.ProblemName;
//		
//		
////		System.out.println("Statics: "+(afterStatics - startTime)/1000000000f);
////		System.out.println("Type sets: "+(afterTypeSets - afterStatics)/1000000000f);
////		System.out.println("Actions: "+(afterActions - afterTypeSets)/1000000000f);
////		System.out.println("Functions: "+(afterFunc - afterActions)/1000000000f);
////		System.out.println("Staticify: "+(afterStaticify - afterFunc)/1000000000f);
////		System.out.println("Creation: "+(afterCreate - afterStaticify)/1000000000f);
//		
//		return rGP;
//	}

	//1/11/2012 -- Most of this code now appears in GroundProblem. this is just kept around for nostalgia.
//	/**
//	 * This helper method deconstructs any Fact into individual literals for use
//	 * in the planning graph. If a new type is introduced into the hierarchy, this will
//	 * probably need modified.
//	 * 
//	 * @param f
//	 * @return
//	 */
//	protected Collection<Fact> decompileFact(Fact f)
//	{
//		HashSet<Fact> lits = new HashSet<Fact>();
//		this.decompileFact(f, lits);
//		return lits;
//	}
//	
//	
//	/**
//	 * This helper method deconstructs any Fact into individual literals for use
//	 * in the planning graph. If a new type is introduced into the hierarchy, this will
//	 * probably need modified.
//	 * 
//	 * @param f
//	 * @return
//	 */
//	protected void decompileFact(Fact f, Collection<Fact> existing)
//	{
//		if (f instanceof Imply)
//		{
//			Collection<? extends STRIPSFact> ands = ((Imply)f).toSTRIPS();
//			
//			for (STRIPSFact dec : ands)
//			{
//				this.decompileFact(dec, existing);
//			}
////			existing.add(f);
//		
////			PGFact apgp = this.getProposition(((Imply)f.fact).getA());
////			PGFact bpgp = this.getProposition(((Imply)f.fact).getB());
////			
////			this.decompileFact(apgp, existing);
////			this.decompileFact(bpgp, existing);
//		}
//		else if (f instanceof CompoundLiteral)
//		{
//			for (Fact cf : ((CompoundLiteral) f).getCompoundFacts())
//			{
//				this.decompileFact(cf, existing);
//			}
//		}
//		else if (f instanceof SingleLiteral)
//		{
//			existing.add(f);
//		}
//		else if (f instanceof Not)
//		{
//			existing.add(f);
//		}
//		else if (f instanceof Function)
//		{
//			existing.add(f);
//		}
//		else
//			throw new IllegalArgumentException("Cannot decompile fact "+f+" - unknown type");
//	}
//
//	//note that the order of these is important
//	private boolean isReachable(Fact pc, Collection<Fact> reachableAdds, Collection<Fact> reachableDels)
//	{
//		//IMPLY has a strange condition, see Imply.isTrue()
//		if (pc instanceof Imply)
//		{
//			boolean aReachable = this.isReachable(((Imply)pc).getA(), reachableAdds, reachableDels);
//			boolean bReachable = this.isReachable(((Imply)pc).getB(), reachableAdds, reachableDels);
//			
//			
//			return aReachable && bReachable;
////			if (!aReachable)
////				return true;
////			else if (aReachable && !bReachable)
////				return true;
////			else 
////				return false;
//		}
//		else if (pc instanceof Not)
//		{
//			Collection<Fact> dec = this.decompileFact(pc);
//			if (dec.size() == 1)
//				return reachableDels.contains(((Not)pc).literal);
//			else
//			{
//				throw new IllegalArgumentException("Negated conjunctions not supported!");
////				for (Fact f : dec)
////				{
////					return this.isReachable(f, reachableAdds, reachableDels);
////				}
//			}
//		}
//		else if (pc instanceof Proposition)
//		{
//			return reachableAdds.contains(pc);
//		}
//		else if (pc instanceof CompoundLiteral)
//		{
//			for (Fact f : pc.getFacts())
//				if (this.isReachable(f, reachableAdds, reachableDels) == false)
//					return false;
//			
//			return true;
//		}
//		else
//			throw new IllegalArgumentException("Unknown precondition type");
//	}
	
	/**
	 * To be called in the event that ground() will not be called at some point. Detects static facts
	 * and constructs type sets. If not explicitly called here, this will never happen.
	 * 
	 * Calling this on a Temporal problem will cause a crash. 
	 * @deprecated Not in sync with ADL code updates
	 */
	public void postProcess()
	{
		this.calculateStatics();
		this.makeStaticPropositionMap();
		this.buildTypeSets();
		
		//now run through all actions and modify whether their PCs are static- because
		//they have a different object reference
		for (Operator ao : this.actions)
		{
			UngroundInstantAction a = (UngroundInstantAction) ao;
			if (a.condition instanceof And)
			{
				And and = (And) a.condition;
				for (Object lo : and.getFacts())
				{
					Literal l = (Literal) lo;
					l.getPredicateSymbol().setStatic(this.predSymbolMap.get(l.name.toString()).isStatic());
				}
			}
			else if (a.condition instanceof Predicate)
			{
				((Predicate)a.condition).getPredicateSymbol().setStatic(this.predSymbolMap.get(((Predicate)a.condition).name.toString()).isStatic());
			}
		}
		
	}

	public GroundProblem ground()
	{
		this.calculateStatics();
		this.makeStaticPropositionMap();
		this.buildTypeSets();
		Set<Action> groundActions = new HashSet<Action>();
		for (Operator o : this.actions)
		{
			Set<Action> s = o.ground(this);
			groundActions.addAll(s);
		}
		
		//FIXME this will need fixed for JavaFF2.2/numerics
		//TODO move this code to a better location?
		//This loop goes around all actions looking for functions
		//
		for (Action a : groundActions)
		{
			for (NamedFunction co : a.getComparators())
			{
				System.out.println("found comparator "+co+" in "+a);
//				BinaryComparator bc = (BinaryComparator) co;
//				NamedFunction namef = (NamedFunction) bc.first;
//				NumberFunction numf = (NumberFunction) bc.second;
//				
//				if (this.funcValues.containsKey(namef) == false)
//					this.funcValues.put(namef, new BigDecimal(0));
			}
		}
	
		if (UngroundProblem.RemoveStaticFacts == true)
		{
			this.staticify(groundActions);
		}
		
		//create the final ground problem
		GroundProblem rGP = new GroundProblem(groundActions, initial, goal,
				funcValues, metric);
		rGP.setName(this.DomainName);//+"_-_"+this.ProblemName;
		rGP.setRequirements(this.requirements);
		
		HashSet<Fact> allStatics = new HashSet<Fact>();
		for (Set<Proposition> predicateStatics : this.staticPropositionMap.values())
		{
			allStatics.addAll(predicateStatics);
		}
		rGP.setStaticFacts(allStatics);
		return rGP;
	}

	/**
	 * Removes static facts from actions' preconditions and the initial state.
	 * @param groundActions
	 */
	protected void staticify(Collection<Action> groundActions)
	{
		// static-ify the functions
		for (Action a : groundActions)
		{
			a.staticify(funcValues);
		}

		// remove static functions from the initial state
		this.removeStaticsFromInitialState();
	}

	public void buildTypeSets() // builds typeSets for easy access of all
									// the objects of a particular type
	{
		
		for (Type t : this.types)
		{
//			SimpleType st = (SimpleType) t;
			Set<PDDLObject> typeObjects = new HashSet<PDDLObject>();
			typeSets.put(t, typeObjects);

			for (PDDLObject o : this.objects)
			{
				if (o.isOfType(t))
					typeObjects.add(o);
			}

			for (Constant c : this.constants)
			{
				if (c.isOfType(t))
					typeObjects.add(c);
			}
			
			this.typeSets.get(t).addAll(typeObjects);
		}

		Set<PDDLObject> s = new HashSet<PDDLObject>(objects);
		s.addAll(constants);
		typeSets.put(SimpleType.rootType, s);
	}

	private void calculateStatics() // Determines whether the predicateSymbols
									// and funcSymbols are static or not
	{
		for (PredicateSymbol ps : this.predSymbols)
		{			
			//TODO this is a very brute force way of detecting static facts, and ignores more subtle static
			//interactions involving the ground Type of a parameter. For example, this method says that all
			//"at" facts in the Depots domain are not static, as there is an action which deletes 
			//predicates of this type. However, in practice any "at" fact relating to pallet locations is
			//static, while those relating to crate locations are not.
			boolean isStatic = true;
			Iterator<Operator> oit = actions.iterator();
			while (oit.hasNext() && isStatic)
			{
				Operator o = (Operator) oit.next();
				isStatic = !o.effects(ps); //if the operator effects the fact, then it is not static
			}
			ps.setStatic(isStatic);
		}

		for (FunctionSymbol fs : this.funcSymbols)
		{
			boolean isStatic = true;
			Iterator<Operator> oit = actions.iterator();
			while (oit.hasNext() && isStatic)
			{
				Operator o = (Operator) oit.next();
				isStatic = !o.effects(fs); //if the operator effects the function, then it is not static
			}
			fs.setStatic(isStatic);
		}
	}

	private void makeStaticPropositionMap()
	{
		Iterator<PredicateSymbol> pit = predSymbols.iterator();
		while (pit.hasNext())
		{
			PredicateSymbol ps = (PredicateSymbol) pit.next();
			if (ps.isStatic())
			{
				staticPropositionMap.put(ps, new HashSet<Proposition>());
			}
		}

		Iterator<Fact> iit = initial.iterator();
		while (iit.hasNext())
		{
			Fact p = (Fact) iit.next();
			if (p.isStatic() && this.initial.contains(p) && p instanceof Proposition) //second condition eliminate any illegal static facts, ie unachievable
			{
				(staticPropositionMap.get(((Proposition)p).name)).add((Proposition) p);
			}
		}
	}

	private void removeStaticsFromInitialState()
	{
		// remove static functions
		
		  Iterator fit = funcValues.keySet().iterator(); Set staticFuncs = new
		  HashSet(); while (fit.hasNext()) { NamedFunction nf = (NamedFunction)
		  fit.next(); if (nf.isStatic()) staticFuncs.add(nf); } fit =
		  staticFuncs.iterator(); while (fit.hasNext()) { Object o =
		  fit.next(); funcValues.remove(o); }
		 

		// remove static Propositions
		Iterator<Fact> init = initial.iterator();
		Set<Fact> staticProps = new HashSet<Fact>();
		while (init.hasNext())
		{
			Fact p = (Fact) init.next();
			if (p.isStatic())
				staticProps.add(p);
		}
		initial.removeAll(staticProps);
	}

	public Object clone()
	{
		UngroundProblem clone = new UngroundProblem();
		
		clone.actions = new HashSet<Operator>(this.actions);
		clone.constantMap = new Hashtable<String, Constant>(this.constantMap);
		clone.constants = new HashSet<Constant>(this.constants);
		clone.DomainName = this.DomainName;
		clone.funcSymbolMap = new Hashtable<String, FunctionSymbol>(this.funcSymbolMap);
		clone.funcSymbols = new HashSet<FunctionSymbol>(this.funcSymbols);
		clone.funcValues = new Hashtable<NamedFunction, BigDecimal>(this.funcValues);
		clone.goal = (GroundFact) this.goal.clone();
		clone.initial = new HashSet<Fact>(this.initial);
		clone.metric = this.metric; //FIXME shallow clone
		clone.objectMap = new Hashtable<String, PDDLObject>(this.objectMap);
		clone.objects = new HashSet<PDDLObject>(this.objects);
		clone.predSymbolMap = new Hashtable<String, PredicateSymbol>(this.predSymbolMap);
		clone.predSymbols = new HashSet<PredicateSymbol>(this.predSymbols);
		clone.ProblemDomainName = this.ProblemDomainName;
		clone.ProblemName = this.ProblemName;
		clone.requirements = (DomainRequirements) this.requirements.clone();
		clone.staticPropositionMap = new Hashtable<PredicateSymbol, Set<Proposition>>(this.staticPropositionMap);
		clone.typeMap = new Hashtable<String, Type>(this.typeMap);
		clone.types = new HashSet<Type>(this.types);
		clone.typeSets = new Hashtable<Type, Set<PDDLObject>>(this.typeSets);
		clone.typeGraph = (TypeGraph) typeGraph.clone();
		
		return clone;
	}
	
	@Override
	public String toString() 
	{
		return "UngroundProblem: "+this.DomainName+"_-_"+this.ProblemName;
	}
}