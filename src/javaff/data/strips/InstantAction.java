/************************************************************************
 * Strathclyde Planning Group,
 * Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK
 * http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey
 * Copyright 2008, Andrew Coles and Amanda Smith
 * Copyright 2015, David Pattison
 *
 * This file is part of JavaFF.
 * 
 * JavaFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JavaFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JavaFF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ************************************************************************/

package javaff.data.strips;

import javaff.data.Fact;
import javaff.data.Fact;
import javaff.data.GroundFact;

import javaff.data.Action;
import javaff.data.metric.NamedFunction;
import javaff.planning.State;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

//TODO this class could be massively sped up by caching the hash. The problem is that
//it is so embedded in JavaFF that ironing out any bugs caused by changing the values of 
//fields and not then calling updatehash() would be a massive task.
public abstract class InstantAction extends Action
{
	//private because we want to go through setupAddDeletes() every time the effect is modified
	private GroundFact condition;
	private GroundFact effect;
	
	private HashSet<Fact> addEffects;
	private HashSet<Not> deleteEffects; //local lookup of adds and deletes
	
	private int hash;

	public InstantAction()
	{
		super();
		this.condition = TrueCondition.getInstance();
		this.effect = TrueCondition.getInstance();
		this.addEffects = new HashSet<Fact>();
		this.deleteEffects = new HashSet<Not>();
		
		this.updateHash();
	}
	
	public InstantAction(String name)
	{
		super(name);
		this.condition = TrueCondition.getInstance();
		this.effect = TrueCondition.getInstance();
		this.addEffects = new HashSet<Fact>();
		this.deleteEffects = new HashSet<Not>();
		
		this.updateHash();
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean supeq = super.equals(obj);
		if (!supeq)
			return false;
		
		InstantAction other = (InstantAction) obj;
		if (this.getCondition().equals(other.getCondition()) == false || this.getEffect().equals(other.getEffect()) == false)
			return false;
		
		return true;
	}
	
	protected int updateHash()
	{
		this.hash = 31 ^ this.getAddPropositions().hashCode() ^ this.getDeletePropositions().hashCode() ^ this.getCondition().hashCode() ^ 
				this.getCost().hashCode();
		return this.hash;
	}
	
	@Override
	public int hashCode()
	{
		return this.hash;
	}
	
	public boolean isApplicable(State s)
	{
		return this.getCondition().isTrue(s) && s.checkAvailability(this);
	}

	public void apply(State s)
	{
		getEffect().applyDels(s);
		getEffect().applyAdds(s);
		
//		this.setupAddDeletes();
	}
	
	/**
	 * Add a positive effect.
	 * @param add
	 */
	public void addAddProposition(Fact add)
	{
		this.addEffectFact(add);
	}
	
	/**
	 * Add a negative effect.
	 * @param del
	 */
	public void addDeleteProposition(Fact del)
	{
		this.addEffectFact(del);
	}

	
	/**
	 * Adds the specified effects. If a fact is of type {@link Not} then it is added to the 
	 * list of negative effects. If it is a positive effect, it is added to the list of
	 * positive effects. 
	 * @param f
	 */
	protected void addEffectFacts(Set<Fact> facts)
	{
		for (Fact f : facts)
			this.addEffectFact(f);
		
		this.updateHash();
	}
	
	protected void setEffectFacts(Set<Fact> facts)
	{
		this.clearAddEffects();
		this.clearDeleteEffects();
		
		this.addEffectFacts(facts);
		this.updateHash();
	}
	
	protected void clearAddEffects()
	{
		this.addEffects.clear();
		this.updateHash();
	}
	
	protected void clearDeleteEffects()
	{
		this.deleteEffects.clear();
		this.updateHash();
	}
	
	
	/**
	 * Adds the specified effect. If this is of type {@link Not} then it is added to the 
	 * list of negative effects. If it is a positive effect, it is added to the list of
	 * positive effects. 
	 * @param f
	 */
	protected boolean addEffectFact(Fact f)
	{
		boolean res;
		if (f instanceof Not)
		{
			res = this.deleteEffects.add((Not) f);
		}
		else
		{
			res = this.addEffects.add(f);
		}
		this.updateHash();
		
		return res;
	}
	
	/**
	 * Returns an unmodifiable set of the preconditions associated with this action.
	 */
	public Set<Fact> getPreconditions()
	{
		return Collections.unmodifiableSet(this.getCondition().getFacts());
	}

	/**
	 * Returns an unmodifiable set of the add effects associated with this action.
	 */
	@Override
	public Set<Fact> getAddPropositions()
	{
//		return effect.getFacts();
		return Collections.unmodifiableSet(this.addEffects);
	}

	/**
	 * Returns an unmodifiable set of the delete effects associated with this action.
	 */
	@Override
	public Set<Not> getDeletePropositions()
	{
//		return effect.getFacts();
		return Collections.unmodifiableSet(this.deleteEffects);
	}

	/**
	 * Returns an unmodifiable set of the comparators associated with this action.
	 */
	public Set<NamedFunction> getComparators()
	{
		return Collections.unmodifiableSet(getCondition().getComparators());
	}

	public Set getOperators()
	{
		Set addset = getEffect().getOperators();
		addset.addAll(getEffect().getOperators());
		return addset;
	}

	public void staticify(Map fValues)
	{
		setCondition(getCondition().staticify());
		setEffect(getEffect().staticify());
	}

	public void setCondition(GroundFact condition)
	{
		this.condition = condition;
	}

	public GroundFact getCondition()
	{
		return condition;
	}

	public void setEffect(GroundFact effect)
	{
		this.effect = effect;
		this.setEffectFacts(this.effect.getFacts());
	}

	protected GroundFact getEffect()
	{
		return effect;
	}

}
