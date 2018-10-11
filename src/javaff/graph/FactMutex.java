package javaff.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javaff.data.Fact;
import javaff.data.strips.Proposition;


/**
 * Represents a mutex between a proposition action and 1-n others.
 * 
 * @author David Pattison
 *
 */
public class FactMutex implements Mutex<Fact>
{
	private Fact owner;
	private Collection<Fact> others;
	
	public FactMutex(Fact owner)
	{
		this.owner = owner;
		this.others = new HashSet<Fact>();
	}
	
	public FactMutex(Fact owner, Collection<Fact> others)
	{
		this.owner = owner;
		this.others = others;
	}
	
	public FactMutex(Fact owner, Fact other)
	{
		this.owner = owner;
		this.others = new ArrayList<Fact>();
		this.others.add(other);
	}
	
	@Override
	public Object clone()
	{
		FactMutex clone = new FactMutex((Fact) this.owner.clone());
		for (Fact o : this.others)
		{
			Fact c = (Fact) o.clone();
			clone.addMutex(c);
		}
		
		return clone;
	}

	/* (non-Javadoc)
	 * @see javaff.learning.graph.Mutex#addMutexProposition(javaff.data.strips.Proposition)
	 */
	public void addMutex(Fact other)
	{
		this.others.add(other);
	}
	
	/**
	 * Get the size of this mutex.
	 * @return
	 */
	public int size()
	{
		return this.others.size() + 1;
	}
	
	/* (non-Javadoc)
	 * @see javaff.learning.graph.Mutex#addMutexProposition(javaff.data.strips.Proposition)
	 */
	public void addMutex(Collection<Fact> others)
	{
		for (Fact p : others)
			this.addMutex(p);
	}
	
	/* (non-Javadoc)
	 * @see javaff.learning.graph.Mutex#removeMutexProposition(javaff.data.strips.Proposition)
	 */
	public boolean removeMutex(Fact other)
	{
		return this.others.remove(other);
	}
	/* (non-Javadoc)
	 * @see javaff.learning.graph.Mutex#hasMutexes()
	 */
	public boolean hasMutexes()
	{
		return this.others.size() > 0;
	}
	
	/* (non-Javadoc)
	 * @see javaff.learning.graph.Mutex#isMutexWith(javaff.data.strips.Proposition)
	 */
	public boolean isMutexWith(Fact a)
	{
		return this.others.contains(a);
	}

	/* (non-Javadoc)
	 * @see javaff.learning.graph.Mutex#getOwner()
	 */
	public Fact getOwner()
	{
		return owner;
	}
	
	public Collection<Fact> getAll()
	{
		HashSet<Fact> facts = new HashSet<Fact>(this.others);
		facts.add(this.owner);
		return facts;
	}

	/* (non-Javadoc)
	 * @see javaff.learning.graph.Mutex#setOwner(javaff.data.strips.Proposition)
	 */
	public void setOwner(Fact owner)
	{
		this.owner = owner;
	}

	/* (non-Javadoc)
	 * @see javaff.learning.graph.Mutex#getOthers()
	 */
	public Collection<Fact> getOthers()
	{
		return others;
	}

	/* (non-Javadoc)
	 * @see javaff.learning.graph.Mutex#setOthers(java.util.Collection)
	 */
	public void setOthers(Collection<Fact> others)
	{
		this.others = others;
	}
	
	@Override
	public String toString()
	{
		return this.owner.toString()+": ["+this.others+"]";
	}

	public void clear()
	{
		this.others.clear();
	}
}
