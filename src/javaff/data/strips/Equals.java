package javaff.data.strips;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.Parameter;
import javaff.data.UngroundFact;
import javaff.data.adl.ADLFact;
import javaff.data.adl.Quantifier;
import javaff.data.metric.NamedFunction;
import javaff.planning.State;

public class Equals implements GroundFact, UngroundFact//, ADLFact //Equality really is ADL, but it makes no sense to convert it to STRIPS
{
	protected static final Set EmptySet = new HashSet();
	
	private List<Parameter> parameters;
	
	public Equals()
	{
		this.parameters = new ArrayList<Parameter>();
	}
	
	public Equals(Parameter a, Parameter b)
	{
		this();
		
		this.addParameter(a);
		this.addParameter(b);
	}
	
	public Equals(Collection<Parameter> params)
	{
		this();
		
		for (Parameter p : params)
			this.addParameter(p);
	}
	
	@Override
	public int compareTo(Fact o)
	{
		return this.toString().compareTo(o.toString());
	}
	
	@Override
	public Object clone()
	{
		Equals e = new Equals();
		for (Parameter p : this.parameters)
		{
			e.addParameter((Parameter) p.clone());
		}
		
		return e;
	}
	
	public void addParameter(Parameter p)
	{
		this.parameters.add(p);
	}

	protected boolean areEqual()
	{
//		return this.a.equals(this.b);
		Iterator<Parameter> it = this.parameters.iterator();
		Parameter f = it.next();
		while (it.hasNext())
		{
			Parameter next = it.next();
			if (f.equals(next) == false)
				return false;
		}
		
		return true;
	}
	
	public int size()
	{
		return this.parameters.size();
	}

	@Override
	public void apply(State s)
	{
		
	}

	@Override
	public void applyAdds(State s)
	{
		
	}

	@Override
	public void applyDels(State s)
	{
		
	}

	@Override
	public Set<NamedFunction> getComparators()
	{
		return EmptySet;
	}

	@Override
	public Set<Fact> getFacts()
	{
		Set<Fact> s = new HashSet<Fact>();
		s.add(this);
		return s;
	}

	@Override
	public Set getOperators()
	{
		return EmptySet;
	}

	@Override
	public boolean isTrue(State s)
	{
		boolean equalParams = this.areEqual();
		
		return equalParams;
	}

	@Override
	public GroundFact staticify()
	{
		return this;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	/**
	 * Does nothing
	 */
	public void setStatic(boolean value)
	{
		
	}

	@Override
	public void PDDLPrint(PrintStream p, int indent)
	{
		p.print(this.toStringTyped());
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer("(= ");
		for (Parameter p : this.parameters)
		{
			buf.append(p.toString()+ " ");
		}
		buf.append(")");
		
		return buf.toString();
	}
	
	@Override
	public String toStringTyped()
	{
		StringBuffer buf = new StringBuffer("(= ");
		for (Parameter p : this.parameters)
		{
			buf.append(p.toStringTyped()+ " ");
		}
		buf.append(")");
		
		return buf.toString();
	}

	@Override
	public boolean effects(PredicateSymbol ps)
	{
		return false;
	}

	@Override
	public UngroundFact effectsAdd(UngroundFact cond)
	{
		return this;
	}

	@Override
	public Set<Fact> getStaticPredicates()
	{
		return EmptySet;
	}

	/**
	 * Grounding out Equals will return either a {@link TrueCondition} on a {@link TrueCondition} wrapped
	 * within a {@link Not}. That is, either all the relevant parameters passed in are equal, or they are not.
	 * If they are, then there is no need to even consider the equality test, so just return True. Of they 
	 * are not equal, then the fact can never be true, so return False. 
	 */
	@Override
	public GroundFact ground(Map<Variable, PDDLObject> varMap)
	{
		Equals equals = new Equals();
		PDDLObject first = varMap.values().iterator().next();
		for (Parameter p : this.parameters)
		{
			PDDLObject o = varMap.get(p);
			if (o.equals(first) == false)
				return new Not(TrueCondition.getInstance());
			
			equals.addParameter(o);
		}
		
		return TrueCondition.getInstance();
//		return equals;
	}

	@Override
	public UngroundFact minus(UngroundFact effect)
	{
		return this;
	}

}
