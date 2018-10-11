package javaff.data;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class MutexSet
{
	protected TreeSet<Fact> facts;

	public MutexSet()
	{
		this.facts = new TreeSet<Fact>(new Comparator<Fact>()
		{
			public int compare(Fact a, Fact b)
			{
				return a.toString().compareTo(b.toString());
			}
		});
	}

	public MutexSet(Fact f)
	{
		this();
		this.facts.add(f);
	}
	
	public MutexSet(Collection<Fact> facts)
	{
		this();
		this.facts.addAll(facts);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this.facts.equals(((MutexSet)obj).facts);
	}
	
	@Override
	public int hashCode()
	{
		int hash = 9991 ^ this.facts.hashCode();
		return hash;
	}
	
	@Override
	public String toString()
	{
		return this.facts.toString();
	}
	
	public boolean addFact(Fact f)
	{
		return this.facts.add(f);
	}

	public boolean removeFact(Fact f)
	{
		return this.facts.remove(f);
	}

	public boolean removeAll(Collection<Fact> f)
	{
		return this.facts.removeAll(f);
	}

	public boolean contains(Fact f)
	{
		return this.facts.contains(f);		
	}
	
	public boolean containsAll(Collection<Fact> facts)
	{
		return this.facts.containsAll(facts);		
	}

	public boolean areMutex(Fact a, Fact b)
	{
		return this.contains(a) && this.contains(b);
	}
	
	public int size()
	{
		return this.facts.size();
	}

	public Collection<Fact> getFacts()
	{
		return facts;
	}


	public void setFacts(Collection<Fact> facts)
	{
		this.facts.clear();
		this.facts.addAll(facts);
	}

	public void merge(MutexSet ms)
	{
		this.facts.addAll(ms.facts);
	}
	
	public void addAll(Collection<Fact> facts)
	{
		this.facts.addAll(facts);
	}
	
}
