package javaff.data.adl;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javaff.data.CompoundLiteral;
import javaff.data.Fact;
import javaff.data.Fact;
import javaff.data.GroundFact;

import javaff.data.Literal;
import javaff.data.PDDLPrinter;
import javaff.data.UngroundFact;

import javaff.data.strips.AbstractCompoundLiteral;
import javaff.data.strips.And;
import javaff.data.strips.Not;
import javaff.data.strips.NullFact;
import javaff.data.strips.PDDLObject;
import javaff.data.strips.PredicateSymbol;
import javaff.data.strips.STRIPSFact;
import javaff.data.strips.TrueCondition;
import javaff.data.strips.Variable;
import javaff.planning.State;

public class Or extends AbstractCompoundLiteral implements ADLFact
{
	private int hash;
	
	public Or()
	{
		super();
		this.hash = this.updateHash();
	}	
	
	public Or(Collection<Fact> props)
	{
		super(props);
		this.hash = this.updateHash();
	}
	

	
	@Override
	public void add(Fact o)
	{
		if (o instanceof Or)
			this.addAll((Collection<Fact>) o.getFacts());
		else
			super.add(o);
	}
	
	
	@Override
	protected int updateHash()
	{
		this.hash = super.hashCode() ^ 23; //23 distinguishes from And
		return this.hash;
	}
	
	@Override
	public int compareTo(Fact o)
	{
		return this.toString().compareTo(o.toString());
	}
	
	public Object clone()
	{
		Or or = new Or();
		or.addAll(this.getFacts());
		or.updateHash();
		return or;
	}


	public boolean isStatic()
	{
		Iterator<Fact> it = getFacts().iterator();
		while (it.hasNext())
		{
			Fact c = (Fact) it.next();
			if (!c.isStatic())
				return false;
		}
		return true;
	}
	
	public void setStatic(boolean value)
	{
		for (Fact f : this.getFacts())
			f.setStatic(value);
	}

	public GroundFact staticifyEffect(Map fValues)
	{
		Set<Fact> newlit = new HashSet<Fact>(getFacts().size());
		Iterator<Fact> it = getFacts().iterator();
		while (it.hasNext())
		{
			GroundFact e = (GroundFact) it.next();
			if (!(e instanceof NullFact))
				newlit.add(e.staticify());
		}
		super.setFacts(newlit);
		if (getFacts().isEmpty())
			return NullFact.getInstance();
		else
			return this;
	}


	public boolean effects(PredicateSymbol ps)
	{
		boolean rEff = false;
		Iterator<Fact> lit = getFacts().iterator();
		while (lit.hasNext() && !(rEff))
		{
			UngroundFact ue = (UngroundFact) lit.next();
			rEff = ue.effects(ps);
		}
		return rEff;
	}

	public UngroundFact minus(UngroundFact effect)
	{
		Or a = new Or();
		Iterator<Fact> lit = getFacts().iterator();
		while (lit.hasNext())
		{
			UngroundFact p = (UngroundFact) lit.next();
			a.add(p.minus(effect));
		}
		return a;
	}

	public UngroundFact effectsAdd(UngroundFact cond)
	{
		Iterator<Fact> lit = getFacts().iterator();
		UngroundFact c = null;
		while (lit.hasNext())
		{
			UngroundFact p = (UngroundFact) lit.next();
			UngroundFact d = p.effectsAdd(cond);
			if (!d.equals(cond))
				c = d;
		}
		if (c == null)
			return cond;
		else
			return c;
	}

	/**
	 * Grounding this disjunction of literals. If any of the literals associated with this disjunction
	 * compile to True, then a {@link TrueCondition} is simply returned.
	 */
	public GroundFact ground(Map<Variable, PDDLObject> varMap)
	{
		Or o = new Or();
		Iterator lit = getFacts().iterator();
		while (lit.hasNext())
		{
			UngroundFact p = (UngroundFact) lit.next();
			GroundFact g = p.ground(varMap);
			
			if (TrueCondition.isSimpleTrue(g) == 1)
				return TrueCondition.getInstance();
			
			if (g instanceof Or)
			{
				for (Fact f : g.getFacts())
				{
					o.add(f);
				}				
			}
			else
				o.add(g);
		}
		return o;
	}

	public boolean isTrue(State s)
	{
		for (Fact l : this.getFacts())
		{
			GroundFact c = (GroundFact) l;
			if (c.isTrue(s))
				return true;
		}
		return false;
	}

	public void apply(State s)
	{
		applyDels(s);
		applyAdds(s);
	}

	public void applyAdds(State s)
	{
		Iterator eit = getFacts().iterator();
		while (eit.hasNext())
		{
			GroundFact e = (GroundFact) eit.next();
			e.applyAdds(s);
		}
	}

	public void applyDels(State s)
	{
		Iterator eit = getFacts().iterator();
		while (eit.hasNext())
		{
			GroundFact e = (GroundFact) eit.next();
			e.applyDels(s);
		}
	}

//	public Set getConditionalPropositions()
//	{
//		Set rSet = new HashSet();
//		Iterator eit = literals.iterator();
//		while (eit.hasNext())
//		{
//			GroundFact e = (GroundFact) eit.next();
//			rSet.addAll(e.getConditionalPropositions());
//		}
//		return rSet;
//	}
//
//	public Set getAddPropositions()
//	{
//		Set rSet = new HashSet();
//		Iterator eit = literals.iterator();
//		while (eit.hasNext())
//		{
//			GroundFact e = (GroundFact) eit.next();
//			rSet.addAll(e.getAddPropositions());
//		}
//		return rSet;
//	}
//
//	public Set getDeletePropositions()
//	{
//		Set rSet = new HashSet();
//		Iterator eit = literals.iterator();
//		while (eit.hasNext())
//		{
//			GroundFact e = (GroundFact) eit.next();
//			rSet.addAll(e.getDeletePropositions());
//		}
//		return rSet;
//	}


	public boolean equals(Object obj)
	{
		if (obj instanceof Or)
		{
			Or a = (Or) obj;
			return (getFacts().equals(a.getFacts()));
		} else
			return false;
	}

	public int hashCode()
	{
		return this.hash;
	}

	public void PDDLPrint(PrintStream p, int indent)
	{
		PDDLPrinter.printToString(getFacts(), "or", p, false, true, indent);
	}

	public String toString()
	{
		String str = "(or";
		Iterator it = getFacts().iterator();
		while (it.hasNext())
		{
			Object next = it.next();
			if (next instanceof TrueCondition || next instanceof NullFact)
				continue;
			
			str += " (" + next+") ";
		}
		str += ")";
		return str;
	}

	public String toStringTyped()
	{
		String str = "(or";
		Iterator<Fact> it = getFacts().iterator();
		while (it.hasNext())
		{
			Object next = it.next();
			if (next instanceof Not)
			{
				Not l = (Not) next;
				str += " (" + l.toStringTyped()+")";
			}
			else if (next instanceof TrueCondition || next instanceof NullFact)
			{
			}
			else
			{
				Literal l = (Literal) next;
				str += " (" + l.toStringTyped()+")";
			}
		}
		str += ")";
		return str;

	}

	/**
	 * Returns each of the literals which this OR encapsulates as a set of individual literals. That is,
	 * the STRIPS version of an Or is just the individual literals.
	 */
	@Override
	public Collection<? extends STRIPSFact> toSTRIPS(Set<Fact> staticFacts)
	{
		Collection<STRIPSFact> facts = new HashSet<STRIPSFact>();
		for (Fact f: this.getFacts())
		{
			facts.add(new And(f)); //TODO maybe find a better way of doing this
		}
		
		return facts;
	}

}
