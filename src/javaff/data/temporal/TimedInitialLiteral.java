package javaff.data.temporal;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.Literal;
import javaff.data.Parameter;
import javaff.data.strips.NullFact;
import javaff.data.strips.PredicateSymbol;
import javaff.data.strips.Proposition;
import javaff.planning.State;

/**
 * A Timed Initial Literal (TIL) is a single fact which becomes true at a pre-determined time during
 * plan execution.
 * 
 * @author David Pattison
 *
 */
public class TimedInitialLiteral extends javaff.data.Literal
{
	private Literal fact;
	private BigDecimal time; 
	private int hash;

	protected TimedInitialLiteral()
	{
		super();
		
		this.fact = NullFact.getInstance();
		this.time = new BigDecimal(-1);
		
		this.updateHash();
	}
	
	
	@Override
	public int hashCode()
	{
		return this.hash;
	}
	
	public TimedInitialLiteral(Literal p, BigDecimal time)
	{
		this();
		this.fact = p;
		this.time = time;
		
		this.updateHash();
	}

//	public void setPredicateSymbol(PredicateSymbol n)
//	{
//		fact.setPredicateSymbol(n);
//	}

	public Object clone()
	{
		TimedInitialLiteral clone = new TimedInitialLiteral();
		
		BigDecimal t = new BigDecimal(this.time.toString());
		Literal f = (Literal) fact.clone();
		
		clone.fact = f;
		clone.time = t;
		
		clone.updateHash();
		
		return clone;
	}

	public String toString()
	{
		return "(at "+this.time+" (" + fact.toString() + ")";
	}

	public boolean isStatic()
	{
		return fact.isStatic();
	}

	public String toStringTyped()
	{
		return "(at "+this.time+" (" + fact.toStringTyped() + ")";
	}

	public Literal staticify()
	{
		return this; //fact.staticify();
	}


	public Set<? extends Fact> getFacts()
	{
		return fact.getFacts();
	}

	public void setPredicateSymbol(PredicateSymbol n)
	{
		fact.setPredicateSymbol(n);
		
		this.updateHash();
	}

	public PredicateSymbol getPredicateSymbol()
	{
		return fact.getPredicateSymbol();
	}

	public List<Parameter> getParameters()
	{
		return fact.getParameters();
	}

	public void setParameters(List<Parameter> params)
	{
		fact.setParameters(params);
		
		this.updateHash();
	}

	public void addParameter(Parameter p)
	{
		fact.addParameter(p);
		
		this.updateHash();
	}

	public void addParameters(List<Parameter> l)
	{
		fact.addParameters(l);
		
		this.updateHash();
	}

	@Override
	protected int updateHash()
	{
		this.hash = super.hashCode() ^ this.time.hashCode() ^ this.fact.hashCode();
		return this.hash;
	}

	public boolean equals(Object obj)
	{
		TimedInitialLiteral other = (TimedInitialLiteral) obj;
	
		return this.fact.equals(other.fact) && this.time.equals(other.time);
	}

	public void setStatic(boolean value)
	{
		fact.setStatic(value);
		
		this.updateHash();
	}

	public void PDDLPrint(PrintStream p, int indent)
	{
//		fact.PDDLPrint(p, indent);
		p.print(this.toString());
	}

}
