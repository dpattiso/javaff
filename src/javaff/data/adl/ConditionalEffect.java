package javaff.data.adl;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javaff.data.CompoundLiteral;
import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundFact;
import javaff.data.metric.NamedFunction;
import javaff.data.strips.PDDLObject;
import javaff.data.strips.PredicateSymbol;
import javaff.data.strips.STRIPSFact;
import javaff.data.strips.TrueCondition;
import javaff.data.strips.Variable;
import javaff.planning.State;

public class ConditionalEffect implements GroundFact, UngroundFact, ADLFact
{
	private Fact condition;
	private Fact effect;

	/**
	 * Initialises this conditional effect by making the condition and effect
	 * both instances of TrueCondition.
	 */
	public ConditionalEffect()
	{
		super();

		this.condition = TrueCondition.getInstance();
		this.effect = TrueCondition.getInstance();
	}

	public ConditionalEffect(Fact condition, Fact effect)
	{
		this();

		this.condition = condition;
		this.effect = effect;
	}
	
	@Override
	public int compareTo(Fact o)
	{
		return this.toString().compareTo(o.toString());
	}

	@Override
	public Object clone()
	{
		ConditionalEffect clone = new ConditionalEffect();
		clone.condition = (Fact) this.condition.clone();
		clone.effect = (Fact) this.effect.clone();

		return clone;
	}

	@Override
	public boolean equals(Object obj)
	{
		ConditionalEffect other = (ConditionalEffect) obj;
		return this.condition.equals(other.condition)
				&& this.effect.equals(other.effect);
	}

	@Override
	public String toString()
	{
		return "when (" + this.condition.toString() + ") ("
				+ this.effect.toString() + ")";
	}

	@Override
	public boolean isStatic()
	{
		return this.condition.isStatic();
	}

	@Override
	public void setStatic(boolean value)
	{
		this.condition.setStatic(value);
	}

	@Override
	public void PDDLPrint(PrintStream p, int indent)
	{
		p.print("(when ");
		PDDLPrinter.printToString(this.condition, p, false, true, indent);
		PDDLPrinter.printToString(this.effect, p, false, true, indent);
		p.print(")");
	}

	@Override
	public String toStringTyped()
	{
		return "when (" + this.condition.toStringTyped() + ") ("
				+ this.effect.toStringTyped() + ")";
	}

	/**
	 * Returns the EFFECT of this object in its STRIPS representation. It is impossible to translate a conditional effect to a STRIPS
	 * representation without knowing the context of the current state/world, so the CONDITION itself cannot be returned.
	 */
	@Override
	public Collection<? extends STRIPSFact> toSTRIPS(Set<Fact> staticFacts)
	{
		if (this.effect instanceof ADLFact)
		{
			Collection<? extends STRIPSFact> set = new HashSet<STRIPSFact>();
			set = ((ADLFact) this.effect).toSTRIPS(staticFacts);
			return set;
		}
		else
		{
			HashSet<STRIPSFact> set = new HashSet<STRIPSFact>();
			set.add((STRIPSFact) this.effect);
			return set;
		}
	}

	@Override
	public GroundFact ground(Map<Variable, PDDLObject> varMap)
	{
		Fact groundCondition = ((UngroundFact) this.condition).ground(varMap);
		Fact groundEffect = ((UngroundFact) this.effect).ground(varMap);

		ConditionalEffect ground = new ConditionalEffect(groundCondition,
				groundEffect);
		return ground;
	}

	@Override
	public boolean isTrue(State s)
	{
		return ((GroundFact) this.condition).isTrue(s);
	}

	/**
	 * Returns the EFFECT of this class. Condition can only be accessed through
	 * {@link #getCondition()}.
	 */
	@Override
	public Set<Fact> getFacts()
	{
		HashSet<Fact> s = new HashSet<Fact>();
		s.add(this.effect);
		return s;
	}

	public Fact getCondition()
	{
		return condition;
	}

	public void setCondition(Fact condition)
	{
		this.condition = condition;
	}

	public Fact getEffect()
	{
		return effect;
	}

	public void setEffect(Fact effect)
	{
		this.effect = effect;
	}

	@Override
	public Set<NamedFunction> getComparators()
	{
		return ((GroundFact) this.condition).getComparators();
	}

	@Override
	public GroundFact staticify()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void apply(State s)
	{
		if (((GroundFact) this.condition).isTrue(s))
		{
			((GroundFact) this.effect).apply(s);
		}
	}

	@Override
	public void applyAdds(State s)
	{
		((GroundFact) this.effect).applyAdds(s);
	}

	@Override
	public void applyDels(State s)
	{
		((GroundFact) this.effect).applyDels(s);
	}

	@Override
	public Set getOperators()
	{
		return ((GroundFact) this.condition).getOperators();
	}

	@Override
	public Set getStaticPredicates()
	{
		return ((UngroundFact) this.condition).getStaticPredicates();
	}

	@Override
	public UngroundFact minus(UngroundFact effect)
	{
		return ((UngroundFact) this.condition).minus(effect);
	}

	@Override
	public boolean effects(PredicateSymbol ps)
	{
		return ((UngroundFact) this.condition).effects(ps);
	}

	@Override
	public UngroundFact effectsAdd(UngroundFact cond)
	{
		return ((UngroundFact) this.condition).effectsAdd(cond);
	}
}
