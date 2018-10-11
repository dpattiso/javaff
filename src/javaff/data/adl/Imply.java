package javaff.data.adl;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javaff.data.CompoundLiteral;
import javaff.data.Fact;
import javaff.data.Fact;
import javaff.data.Fact;
import javaff.data.GroundFact;

import javaff.data.Literal;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundFact;

import javaff.data.metric.NamedFunction;
import javaff.data.strips.And;
import javaff.data.strips.Not;
import javaff.data.strips.PDDLObject;
import javaff.data.strips.PredicateSymbol;
import javaff.data.strips.Proposition;
import javaff.data.strips.STRIPSFact;
import javaff.data.strips.SingleLiteral;
import javaff.data.strips.TrueCondition;
import javaff.data.strips.Variable;
import javaff.planning.State;

public class Imply extends Literal implements GroundFact, UngroundFact, ADLFact
{
	private Fact a, b;
	private int hash;
	
	public Imply(Fact a, Fact b)
	{
		this.a = a;
		this.b = b;
		
		this.updateHash();
	}
	
	public Object clone()
	{
		return new Imply((Fact)this.a.clone(), (Fact)this.b.clone());
	}
	
	
	@Override
	public int hashCode()
	{
		return this.hash;
	}
	
	@Override
	protected int updateHash()
	{
		//do not call super.updateHash() -- it will contain Nulls
		this.hash = 31;
		if (this.getFactA() != null)
			this.hash ^= this.a.hashCode();
		if (this.getFactB() != null)
			this.hash ^= this.b.hashCode();
		
		return this.hash;
	}
	
	@Override
	public Set<Fact> getFacts()
	{
		Set<Fact> s = new HashSet<Fact>(1);
		s.add(a);
		s.add(b);
		return s;
	}
	
	public Fact getFactA()
	{
		return a;
	}


	public void setFactA(Fact a)
	{
		this.a = a;
		
		this.updateHash();
	}


	public Fact getFactB()
	{
		return b;
	}


	public void setFactB(Fact b)
	{
		this.b = b;
		
		this.updateHash();
	}


	@Override
	public Set<NamedFunction> getComparators()
	{
		HashSet<NamedFunction> s = new HashSet<NamedFunction>();
		s.addAll(((GroundFact)this.a).getComparators());
		s.addAll(((GroundFact)this.b).getComparators());
		
		return s;
	}


//	/**
//	 * Gets the conditional propositions of the Imply condition, i.e. condition A (Note, B is not included!)
//	 */
//	@Override
//	public Set<Fact> getConditionalPropositions()
//	{
//		Set<Fact> s = new HashSet<Fact>();
//		s.add(this);
//		return s;
//		//return ((GroundCondition)this.a).getConditionalPropositions();
//	}

	/**
	 * Returns true iff 
	 * 
	 * | A | B | A -> B |
	 * | 0 | 0 | 1 |
	 * | 0 | 1 | 1 |
	 * | 1 | 0 | 0 |
	 * | 1 | 1 | 1 |
	 */
	@Override
	public boolean isTrue(State s)
	{
		boolean aTrue = ((GroundFact)this.a).isTrue(s);
		boolean bTrue = ((GroundFact)this.b).isTrue(s);
		
		//a -> b
		if (aTrue == false) //(~a -> b) || (~a -> ~b)
			return true;
		else if (bTrue) //a -> b
			return true;
		else 
			return false; //a -> ~b
	}


	@Override
	public GroundFact staticify()
	{
		return this;
		
//		
//		GroundFact newA = ((GroundFact)this.a).staticify();
//		GroundFact newB = ((GroundFact)this.b).staticify();
//		
//		if (newB instanceof TrueCondition)
//			return TrueCondition.getInstance();
//		
//		return new Imply(newA, newB);
	}


	/**
	 * Returns false.
	 */
	@Override
	public boolean isStatic()
	{
//		return ((GroundCondition)this.a).isStatic();
//		return false;
		return this.b.isStatic();
	}


	@Override
	public void PDDLPrint(PrintStream p, int indent)
	{
		p.print("(imply (");
		PDDLPrinter.printToString(this.a, p, false, true, indent);
		p.print(") (");
		PDDLPrinter.printToString(this.b, p, false, true, indent);
		p.print(")");
	}


	@Override
	public String toStringTyped()
	{
		return "imply ("+this.a.toStringTyped()+") ("+this.b.toStringTyped()+")";
	}
	
	@Override
	public String toString()
	{
		return "imply ("+this.a.toString()+") ("+this.b.toString()+")";
	}


	@Override
	public void apply(State s)
	{
		if (this.isTrue(s))
		{
			((GroundFact)this.b).apply(s);
		}
	}


	@Override
	public void applyAdds(State s)
	{
		this.apply(s);
	}


	@Override
	public void applyDels(State s)
	{
		this.apply(s);
	}

	@Override
	public Set getOperators()
	{
		HashSet s = new HashSet();
		s.addAll(((GroundFact)this.a).getOperators());
		s.addAll(((GroundFact)this.b).getOperators());
		
		return s;
	}


	@Override
	public Set getStaticPredicates()
	{
		return new HashSet();
		//return ((GroundFact)this.b).get
	}


	@Override
	public GroundFact ground(Map<Variable, PDDLObject> varMap)
	{
		GroundFact ga = ((UngroundFact)this.a).ground(varMap);
		GroundFact gb = ((UngroundFact)this.b).ground(varMap);
		
		//A -> ~B = 1 and ~A -> ~B = 1, so static B means grounding is always true
		if (gb.isStatic())
			return TrueCondition.getInstance();
		
		
		return new Imply(ga, gb);
	}


	@Override
	public UngroundFact minus(UngroundFact effect)
	{
		//probably wrong!
		return this;  //((UngroundFact)this.b).minus(effect);
	}


	@Override
	public boolean effects(PredicateSymbol ps)
	{
		return ((UngroundFact)this.a).effects(ps) || ((UngroundFact)this.b).effects(ps);
	}


	@Override
	public UngroundFact effectsAdd(UngroundFact cond)
	{
		return ((UngroundFact)this.b).effectsAdd(cond);
	}
	
//	/**
//	 * Returns a set of ANDs which correspond to the three states of A and B in which this Implys would be true,
//	 * e.g. (and (not(a)) (not(b))); (and (not(a)) (b)); (and (a) (b)). However, less than 3 conjunctions
//	 * may be returned if either A or B are static. This is because a negated static fact will always be
//	 * true, but illegal in terms of applicability in any state.
//	 * @return
//	 */
//	public Collection<? extends STRIPSFact> toSTRIPS()
//	{
//		HashSet<STRIPSFact> ands = new HashSet<STRIPSFact>();
//
//		//if A can be 1 or 0
//		if (this.a.isStatic() == false)
//		{
//			//if A can be 1 or 0, and B can be 1 or 0
//			if (this.b.isStatic() == false)
//			{
//				//give all TRUE imply types, 0 -> 0, 0 -> 1, 1 -> 1
//				And one = new And();
//				one.add(new Not((Fact) this.a.clone()));
//				one.add(new Not((Fact) this.b.clone()));
//				ands.add(one);
//			
//				And two = new And();
//				two.add(new Not((Fact) this.a.clone()));
//				two.add((Fact) this.b.clone());
//				ands.add(two);
//				
//				And three = new And();
//				three.add((Fact) this.a.clone());
//				three.add((Fact) this.b.clone());
//				ands.add(three);
//			}
//			//if A can be 1 or 0, but B is always 1
//			else
//			{
//				And one = new And();
//				one.add(new Not((Fact) this.a.clone()));
//				one.add(new Not((Fact) this.b.clone()));
//				ands.add(one);
//				
//				//no need for A == 0 and B == 1 
//				//or A == 1 and B == 1, as these are
//				//both always true if B is static
//			}
//		}
//		//else, if A is always 1
//		else
//		{
//			//if A is always 1 and B is always 1
//			if (this.b.isStatic() == true)
//			{
//				//return nothing
//				TrueCondition c = TrueCondition.getInstance();
//				ands.add(c);
//			}
//			//if A is always 1 but B can be 0 or 1
//			else
//			{
//				//only return A == 1, B == 1, 
//				//as A == 1 and B == 0 is FALSE
//				And three = new And();
//				three.add((Fact) this.a.clone());
//				three.add((Fact) this.b.clone());
//				ands.add(three);
//			}
//		}
//		
//		return ands;
//	}
	
	/**
	 * Returns a set of ANDs which correspond to the three states of A and B in which this Implys would be true,
	 * e.g. (and (not(a)) (not(b))); (and (not(a)) (b)); (and (a) (b)). However, less than 3 conjunctions
	 * may be returned if either A or B are static. This is because a negated static fact will always be
	 * true, but illegal in terms of applicability in any state.
	 * @return
	 */
	public Collection<? extends STRIPSFact> toSTRIPS(Set<Fact> staticFacts)
	{
//		HashSet<STRIPSFact> ands = new HashSet<STRIPSFact>();
//		if (this.a.isStatic() == false)
//		{
//			if (this.b.isStatic() == false)
//			{
//
//				//~A -> ~B = 1
//				And one = new And();
//				one.add(new Not((Fact) this.a.clone()));
//				one.add(new Not((Fact) this.b.clone()));
//				ands.add(one);
//
//			}
//
//			//~A -> B = 1
//			And two = new And();
//			two.add(new Not((Fact) this.a.clone()));
//			two.add((Fact) this.b.clone());
//			ands.add(two);
//
//		}
//
//		//A -> B = 1
//		And three = new And();
//		three.add((Fact) this.a.clone());
//		
//		three.add((Fact) this.b.clone());
//		ands.add(three);
//		
//		return ands;
		
		if (this.a.isStatic() == false)
		{
			if (this.b.isStatic() == false)
			{
				HashSet<STRIPSFact> ands = new HashSet<STRIPSFact>();

				//~A -> ~B = 1
				And one = new And();
				one.add(new Not((Fact) this.a.clone()));
				one.add(new Not((Fact) this.b.clone()));
				ands.add(one);

				//~A -> B = 1
				And two = new And();
				two.add(new Not((Fact) this.a.clone()));
				two.add((Fact) this.b.clone());
				ands.add(two);

				//A -> B = 1
				And three = new And();
				three.add((Fact) this.a.clone());
				three.add((Fact) this.b.clone());
				ands.add(three);
				
				return ands;
			}
			else
			{
				HashSet<STRIPSFact> ands = new HashSet<STRIPSFact>();

				//note this is an unusual place to end up in the code and is really just here
				//for completeness.
				
				//else B is static. If B is static and true in the initial state, then the
				//result is always True as (A | ~A) -> B = 1
				if (staticFacts.contains(this.b))
					return ands;
				
				//If we are here then A is not static and B is static but not true in the initial state. So 
				//the conditions possible are A -> ~B and ~A -> ~B. As A -> ~B = 0, we can ignore it
				//as a condition, and instead just return A
				And one = new And();
				one.add(new Not((Fact) this.a.clone()));
				ands.add(one);
				
				return ands;
			}
		}
		else
		{
			if (staticFacts.contains(this.a) == false)
			{
				//if in here then A is static but not true in the initial state, so it is unsatisfiable (~A)				
				HashSet<STRIPSFact> ands = new HashSet<STRIPSFact>();

				//if, is static, but not true in the initial state -- so it is unreachable -- which
				//means it is always False. ~A -> (B | ~B) = 1, so return True (or rather don't generate
				//any conditions from this Imply)
//				return ands;
				
//				//~A -> B = 1
//				And two = new And();
////				two.add(new Not((Fact) this.a.clone()));
//				two.add((Fact) this.b.clone());
//				ands.add(two);
//
//				//~A -> ~B = 1
//				And three = new And();
////				three.add(new Not((Fact) this.a.clone()));
//				three.add(new Not((Fact) this.b.clone()));
//				ands.add(three);
//				
				return ands;
			}	
			else
			{
				HashSet<STRIPSFact> ands = new HashSet<STRIPSFact>();

				//else if here A is static and true in the initial state.
				//If B is also static, we can just return True again, as A -> B = 1
				if (this.b.isStatic())
				{ 
					if (staticFacts.contains(this.b))
						return ands; //always true, so return nothing
					else
					{
						//else it is unachievable. A -> ~B = 0, so again return nothing
						return ands;
					}
				}
				else
				{
					
					//else B is just a normal fact, and as A is known to be static and always true, the only
					//condition to return is B
		
					And three = new And();
					three.add((Fact) this.b.clone());
					ands.add(three);
	
					return ands;
				}
			}
		}
		
	}
}
