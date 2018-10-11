package javaff.data.strips;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javaff.data.CompoundLiteral;
import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.UngroundFact;

/**
 * Convenience class for representing PDDL facts which are comprised of multiple facts, e.g. And and Or.
 * @author David Pattison
 *
 */
public abstract class AbstractCompoundLiteral implements CompoundLiteral, GroundFact, UngroundFact, STRIPSFact
{
	private int hash;
	private Set<Fact> literals;

	public AbstractCompoundLiteral()
	{
		super();
		this.literals = new HashSet<Fact>();
		this.updateHash();
	}
	
	public AbstractCompoundLiteral(Collection<Fact> facts)
	{
		this();
		this.literals = new HashSet<Fact>();
		this.addAll(facts);
		this.updateHash();
	}
	
	/**
	 * Get the number of grouped literals in this compound literal.
	 * @return
	 */
	public int size()
	{
		return this.literals.size();
	}
	
	public abstract Object clone();
	
	@Override
	public int hashCode()
	{
		return this.hash;
	}
	
	public void clearFacts()
	{
		this.literals.clear();
		this.updateHash();
	}

	public void add(Fact o)
	{
		if (o instanceof CompoundLiteral)
		{
			for (Fact f : ((CompoundLiteral) o).getCompoundFacts())
			{
				this.add(f);
			}
		}
		else
		{
			this.literals.add(o);
		}
		this.updateHash();
	}
	

	public void addAll(Collection<Fact> c)
	{
		for (Fact l : c)
			this.add(l);
		
		this.updateHash();
	}

	public boolean isStatic()
	{
		for (Fact c : this.literals)
		{
			if (!c.isStatic())
				return false;
		}
		return true;
	}

	/**
	 * Sets all the literals in this AND to be static.
	 */
	public void setStatic(boolean value)
	{
		for (Fact c : this.literals)
		{
			c.setStatic(value);
		}
	}

	public GroundFact staticify()
	{
		Set<Fact> newlit = new HashSet<Fact>(literals.size());
		for (Fact c : this.literals)
		{
			if ((c instanceof TrueCondition))
			{
				continue;
				// newlit.add(((TrueCondition)c).staticify());
			}
			else if (!(c instanceof NullFact) && c instanceof GroundFact)
			{
				Fact f = ((GroundFact) c).staticify();
				if (f instanceof TrueCondition == false)
				{
					newlit.add(f);
				}
			}
		}
		literals = newlit;
		if (literals.isEmpty())
			return TrueCondition.getInstance();
		else
			return this;
	}

	//
	// public GroundFact staticifyEffect(Map fValues)
	// {
	// Set newlit = new HashSet(literals.size());
	// Iterator it = literals.iterator();
	// while (it.hasNext())
	// {
	// GroundFact e = (GroundFact) it.next();
	// }
	// literals = newlit;
	// if (literals.isEmpty())
	// return NullEffect.getInstance();
	// else
	// return this;
	// }

	public Set getOperators()
	{
		Set rSet = new HashSet();
		Iterator eit = literals.iterator();
		while (eit.hasNext())
		{
			GroundFact e = (GroundFact) eit.next();
			rSet.addAll(e.getOperators());
		}
		return rSet;
	}

	public Set getComparators()
	{
		Set rSet = new HashSet();
		Iterator eit = literals.iterator();
		while (eit.hasNext())
		{
			GroundFact e = (GroundFact) eit.next();
			rSet.addAll(e.getComparators());
		}
		return rSet;
	}

	public Set getStaticPredicates()
	{
		Set rSet = new HashSet();
		Iterator it = literals.iterator();
		while (it.hasNext())
		{
			UngroundFact c = (UngroundFact) it.next();
			rSet.addAll(c.getStaticPredicates());
		}
		return rSet;
	}

	/**
	 * Returns and unmodifiable set of facts contained in this compound literal
	 */
	@Override
	public Set<Fact> getFacts()
	{
		return Collections.unmodifiableSet(this.literals);
	}


	/**
	 * Returns and unmodifiable set of facts contained in this compound literal
	 */
	@Override
	public Set<Fact> getCompoundFacts()
	{
		return this.getFacts();
	}

	@Override
	public boolean remove(Fact f)
	{
		boolean res = this.literals.remove(f);
		this.updateHash();
		return res;
	}

	@Override
	public boolean removeAll(Collection<Fact> c)
	{
		boolean res = this.literals.removeAll(c);
		this.updateHash();
		return res;
	}

	public void setFacts(Set<Fact> literals)
	{
		this.literals = literals;
		this.updateHash();
	}
	
	protected int updateHash()
	{
		int h = this.getFacts().hashCode() ^ 31;
		this.hash = h;
		return hash;
	}

}