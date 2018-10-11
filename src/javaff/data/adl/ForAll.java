package javaff.data.adl;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javaff.data.CompoundLiteral;
import javaff.data.Fact;
import javaff.data.Fact;
import javaff.data.Fact;
import javaff.data.GroundFact;

import javaff.data.Literal;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundFact;

import javaff.data.UngroundProblem;
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

/**
 * Represents ADL forall predicate.
 * @author pattison
 *
 */
public class ForAll implements GroundFact, UngroundFact, Quantifier, ADLFact
{
	private Variable variable;
	private Fact condition;
	private boolean grounded;
	private Set<PDDLObject> objects;
	
	protected ForAll(Variable v, Fact c, boolean grounded)
	{
		this.variable = v;
		this.condition = c;
		this.grounded = grounded;
		this.objects = new HashSet<PDDLObject>();
	}
	
	protected ForAll(Variable v, Fact c, Set<PDDLObject> objects, boolean grounded)
	{
		this.variable = v;
		this.condition = c;
		this.grounded = grounded;
		this.objects = objects;
	}
	
	public ForAll(Variable v, Fact c)
	{
		this(v, c, false);
	}
	
	public ForAll(Variable v, Fact c, Set<PDDLObject> objects)
	{
		this(v, c, objects, false);
	}
	
	
	@Override
	public int compareTo(Fact o)
	{
		return this.toString().compareTo(o.toString());
	}
	
	public Set<PDDLObject> getQuantifiedObjects()
	{
		return objects;
	}
	
	public void setQuantifiedObjects(Set<PDDLObject> obj)
	{
		this.objects = obj;
	}
	
	public void setVariable(Variable variable)
	{
		this.variable = variable;
	}
	
	public Variable getVariable()
	{
		return variable;
	}
	
	public Object clone()
	{
		ForAll forall = new ForAll((Variable)this.variable.clone(), (Fact) ((Literal) this.condition).clone());
		return forall;
	
	}
	
	@Override
	public void PDDLPrint(PrintStream p, int indent)
	{
		p.print("(forall ");
		PDDLPrinter.printToString(this.condition, p, false, true, indent);
		p.print(")");
	}
	
	public int hashCode()
	{
		return this.condition.hashCode() ^ this.variable.hashCode() * 4422;
	}

	public String toString()
	{
		return "forall (" + this.variable.toString() + ") (" + condition.toString() + ")";
	}

	public String toStringTyped()
	{
		return "forall (" + this.variable.toStringTyped() + ") (" + condition.toStringTyped() + ")";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ForAll)
		{
			ForAll n = (ForAll) obj;
			if (variable.equals(n.variable) == false)
				return false;
			
			return this.condition.equals(n.condition);
		} else
			return false;
	}

	@Override
	public Set<NamedFunction> getComparators()
	{
		return ((GroundFact)this.condition).getComparators();
	}

	@Override
	public boolean isTrue(State s)
	{
//		if (this.grounded)
//		{
//			for (Fact f : this.conditions)
//			{
//				if (f.
////			return ((STRIPSState)s).facts.containsAll(this.conditions.getFacts());
//		}
//		else
//		for (Fact f : this.condition)
//		{
//			if (((GroundFact)f).isTrue(s) == false)
//				return false;
//		}
//		
//		return true;
		
		return ((GroundFact)this.condition).isTrue(s);
	}

	@Override
	public GroundFact staticify()
	{
//		return this;
		return new ForAll(this.variable, ((GroundFact)this.condition).staticify(), true);
		//return ((GroundCondition)this.condition).staticifyCondition(fValues);
	}

	@Override
	public boolean isStatic()
	{
		return ((GroundFact)this.condition).isStatic();
	}
	
	public void setStatic(boolean value)
	{
		this.condition.setStatic(value);
	}


	@Override
	public void apply(State s)
	{
		((GroundFact)this.condition).apply(s);
	}

	@Override
	public void applyAdds(State s)
	{
		((GroundFact)this.condition).applyAdds(s);
	}

	@Override
	public void applyDels(State s)
	{
		((GroundFact)this.condition).applyDels(s);
	}

	@Override
	public Set getOperators()
	{
		return ((GroundFact)this.condition).getOperators();
	}

	@Override
	public Set getStaticPredicates()
	{
		return ((UngroundFact)this.condition).getStaticPredicates();
	}

	@Override
	public UngroundFact minus(UngroundFact effect)
	{
		return ((UngroundFact)this.condition).minus(effect);
	}

	@Override
	public boolean effects(PredicateSymbol ps)
	{
		return ((UngroundFact)this.condition).effects(ps);
	}

	@Override
	public UngroundFact effectsAdd(UngroundFact cond)
	{
		return ((UngroundFact)this.condition).effectsAdd(cond);
	}
	
	/**
	 * This method compiles out all ForAll quantified
	 * literals into individual ones. Individual literals/conjunctions are returned wrapped in an
 	 * AND which is returned by getFacts().
	 */
	@Override
	public GroundFact ground(Map<Variable, PDDLObject> varMap)
	{
		HashSet<Fact> groundedFacts = new HashSet<Fact>();
		And compiledOut = new And();
		for (PDDLObject v : this.objects)
		{
			if (varMap.containsValue(v))
			{
				//Oh hello! I see you want to work with the same object twice in a single predicate. 
				//well, no can do. The existing infrastructure in JavaFF makes it virtually impossible
				//or rather, such a headache that I have spent a week trying to get it to work by
				//reparsing actions which have unreachable preconditions because of this. In short,
				//ForAlls plus Implys are evil. Nils had it right with STRIPS. ADL can burn.
				continue;
			}
			
			Map<Variable, PDDLObject> newMap = new HashMap<Variable, PDDLObject>(varMap);
			newMap.put(this.variable, v);
			
			GroundFact g = ((UngroundFact)this.condition).ground(newMap);
			groundedFacts.add(g);
			
			compiledOut.add(g);
		}
		
		//if this forall compiled out to nothing, then just return a TrueCondition
		if (compiledOut.getFacts().isEmpty())
			return TrueCondition.getInstance();
		
		ForAll gfa = new ForAll(this.variable, compiledOut, true);
		return gfa;
	}
	
	@Override
	public Collection<? extends STRIPSFact> toSTRIPS(Set<Fact> staticFacts)
	{
		if (this.grounded == false)
			throw new NullPointerException("ForAll must be grounded prior to converting to STRIPS");
		else
		{
			Collection<STRIPSFact> set = new HashSet<STRIPSFact>();
			set.add((And)this.condition);
			return set;
		}
	}

	public Fact getCondition()
	{
		return condition;
	}

	public void setCondition(Fact condition)
	{
		this.condition = condition;
	}

	/**
	 * Returns the grounded set of literals which this ForAll clause compiles out to. Note if ground()
	 * has not yet been called this will return null.
	 */
	@Override
	public Set<Fact> getFacts()
	{
//		Set<SingleLiteral> s = new HashSet<SingleLiteral>();
//		for (Literal l : this.grounded)
//		{
//			if (l instanceof SingleLiteral)
//				s.add((SingleLiteral) l);
//			else if (l instanceof CompoundLiteral)
//				s.addAll(((CompoundLiteral) l).getAllLiterals());
//			else
//				throw new IllegalArgumentException("Unknown literal type- must derive from SingleLiteral or CompoundLiteral");
//		}
//		
//		return s;
		
		HashSet<Fact> s = new HashSet<Fact>(1);
		s.add(this.condition);
		return s;
	}
}
