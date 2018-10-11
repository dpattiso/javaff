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
import javaff.data.Literal;
import javaff.data.Parameter;
import javaff.data.UngroundFact;

import javaff.data.GroundFact;


import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class Predicate extends Literal implements UngroundFact, SingleLiteral
{
	private int hash;
	
	public Predicate(PredicateSymbol p)
	{
		name = p;
		
		this.updateHash();
	}
	
	public Object clone()
	{
		PredicateSymbol sym = new PredicateSymbol();
		sym.name = new String(this.name.name);
		sym.params = new ArrayList(this.name.params);
		sym.staticValue = this.name.staticValue;
		
		Predicate p = new Predicate(sym);
		
		return p;
	}
	
	@Override
	public Set<? extends Fact> getFacts()
	{
		Set<Fact> s = new HashSet<Fact>(1);
		s.add(this);
		return s;
	}

	public boolean effects(PredicateSymbol ps)
	{
		return name.equals(ps);
	}

	public UngroundFact minus(UngroundFact effect)
	{
		return effect.effectsAdd(this);
	}

	public UngroundFact effectsAdd(UngroundFact cond)
	{
		if (this.equals(cond))
			return TrueCondition.getInstance();
		else
			return cond;
	}

	public Set getStaticPredicates()
	{
		Set rSet = new HashSet();
		if (name.isStatic())
			rSet.add(this);
		return rSet;
	}

	public Proposition ground(Map<Variable, PDDLObject> varMap)
	{
		Proposition p = new Proposition(name);
		for (Parameter o : this.parameters)
		{
			PDDLObject po;
			if (o instanceof PDDLObject)
				po = (PDDLObject) o;
			else
			{
				Variable v = (Variable) o;
				po = (PDDLObject) varMap.get(v);
			}

			p.addParameter(po);
		}
		return p;
	}

//	public GroundFact ground(Map<Variable, PDDLObject> varMap)
//	{
//		return ground(varMap);
//	}

//	public GroundFact groundEffect(Map<Variable, PDDLObject> varMap)
//	{
//		return ground(varMap);
//	}
	
	
	@Override
	public int hashCode()
	{
		return this.hash;
	}

	protected int updateHash()
	{
		this.hash = 5 ^ super.hashCode();
		return this.hash;
	}

	
}
