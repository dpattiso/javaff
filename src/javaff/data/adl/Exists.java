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
import javaff.data.strips.Variable;
import javaff.planning.State;

/**
 * Represents ADL exists predicate.
 * @author pattison
 *
 */
public class Exists implements GroundFact, UngroundFact, Quantifier, ADLFact
{
	private Variable variable;
	private Fact condition;
//	private Set<Fact> grounded;
	private boolean grounded;
	private Set<PDDLObject> objects;
	
	protected Exists(Variable v, Fact c, boolean grounded)
	{
		this.variable = v;
		this.condition = c;
//		this.grounded = new HashSet<Fact>();
		this.grounded = grounded;
		this.objects = new HashSet<PDDLObject>();
	}
	
	@Override
	public int compareTo(Fact o)
	{
		return this.toString().compareTo(o.toString());
	}
	
	protected Exists(Variable v, Fact c, Set<PDDLObject> objects, boolean grounded)
	{
		this.variable = v;
		this.condition = c;
//		this.grounded = new HashSet<Fact>();
		this.grounded = grounded;
		this.objects = objects;
	}
	
	public Exists(Variable v, Fact c)
	{
		this(v, c, false);
	}
	
	public Exists(Variable v, Fact c, Set<PDDLObject> objects)
	{
		this(v, c, objects, false);
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
		Exists forall = new Exists((Variable)this.variable.clone(), (Fact) ((Literal) this.condition).clone());
		return forall;
	
	}
	
	@Override
	public void PDDLPrint(PrintStream p, int indent)
	{
		p.print("(exists ");
		PDDLPrinter.printToString(this.condition, p, false, true, indent);
		p.print(")");
	}
	
	public int hashCode()
	{
		return this.condition.hashCode() ^ this.variable.hashCode() * 4411;
	}

	public String toString()
	{
		return "exists (" + this.variable.toString() + ") (" + condition.toString() + ")";
	}

	public String toStringTyped()
	{
		return "exists (" + this.variable.toStringTyped() + ") (" + condition.toStringTyped() + ")";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Exists)
		{
			Exists n = (Exists) obj;
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
		return ((GroundFact)this.condition).isTrue(s);
	}

	/**
	 * Staticifying an Exists is a real pain. As a grounded Exists, is really just an Or statement, if any of the literals inside the Or are 
	 * modified by the statification, that whole clause becomes redundant.
	 */
	@Override
	public GroundFact staticify()
	{
//		if (this.grounded == true)
//		{
//			HashSet<Fact> stillValid = new HashSet<Fact>();
//			
//			Or condition = (Or) this.condition;
//			for (Fact f : condition.getFacts())
//			{
//				GroundFact gf = (GroundFact) f.clone();
//				
//				GroundFact sgf = ((GroundFact) f).staticify(fValues);
//				
//				if (sgf.equals(gf) == true)
//				{
//					stillValid.add(gf);
//					
//				}
//				
//			}
//			
//			Or newCondition = new Or(stillValid);
//			return new Exists(this.variable, newCondition, true);
//		}
//		else
			return new Exists(this.variable, ((GroundFact)this.condition).staticify(), true); //TODO check this works...
//			return this;
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
	
//	public GroundFact groundEffect(Map<Variable, PDDLObject> varMap)
//	{
//		return (GroundFact) this.ground(varMap);
//	}
	
	/**
	 * This method compiles out all Exists quantified
	 * literals into individual ones. Individual literals/conjunctions are wrapped in an
 	 * single OR which is returned by getFacts().
	 */
	@Override
	public GroundFact ground(Map<Variable, PDDLObject> varMap)
	{
		HashSet<Fact> groundedFacts = new HashSet<Fact>();

		Or compiledOut = new Or();
		for (PDDLObject v : this.objects)
		{
			Map<Variable, PDDLObject> newMap = new HashMap<Variable, PDDLObject>(varMap);
			newMap.put(this.variable, v);
			
			Fact g = ((UngroundFact)this.condition).ground(newMap);
			groundedFacts.add(g);
			
			compiledOut.add(g);
		}
		
		Exists gfa = new Exists(this.variable, compiledOut, true);
		return gfa;
	}
	
	/**
	 * Constructs N And facts which represent the original form of this Exists. For example, if this Exists encapsulated
	 * 2 facts (A || B), the return set would contain A and B as separate facts. All individual facts returned in the collection
	 * are wrapped in an And predicate.
	 */
	@Override
	public Collection<? extends STRIPSFact> toSTRIPS(Set<Fact> staticFacts)
	{
		HashSet<STRIPSFact> groundedFacts = new HashSet<STRIPSFact>();

		if (this.grounded == true)
		{
			//if grounded, then the condition should be an OR
			for (Fact f : this.condition.getFacts())
			{
				groundedFacts.add(new And(f));					
			}		
			
			return groundedFacts;
		}
		else
			throw new NullPointerException("Exists predicates must be grounded prior to STRIPS conversion.");
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
	 * Returns the grounded set of literals which this Exists clause compiles out to. Note if ground()
	 * has not yet been called this will return null.
	 */
	@Override
	public Set<Fact> getFacts()
	{
		HashSet<Fact> s = new HashSet<Fact>(1);
		s.add(this.condition);
		return s;
	}
}
