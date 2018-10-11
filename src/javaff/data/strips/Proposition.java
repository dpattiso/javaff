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
import javaff.data.GroundFact;

import javaff.data.Literal;
import javaff.planning.State;
import javaff.planning.STRIPSState;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class Proposition extends javaff.data.Literal implements GroundFact, SingleLiteral, STRIPSFact
{
	private int hash;
	
	public Proposition(PredicateSymbol p)
	{
		super();
		name = p;
		
		this.updateHash();
	}
	
	protected Proposition()
	{
		super();
		
		name = new PredicateSymbol();
		
		this.updateHash();
	}
	
	@Override
	protected int updateHash()
	{
		this.hash = super.updateHash();
		return hash;
		
//		this.hash = super.updateHash() ^ 31;
//		
//		if (this.name != null)
//			System.out.println("Updated Proposition hash of \""+this.toString()+"\" to be "+hash);
//		else
//			System.out.println("Updated Proposition hash to be "+hash);
//		
//		return this.hash;
	}
	
	@Override
	public Set<Fact> getFacts()
	{
		Set<Fact> s = new HashSet<Fact>(1);
		s.add(this);
		return s;
	}
	
	public Object clone()
	{
		Proposition p = new Proposition(this.name);
		p.parameters = new ArrayList(this.parameters);
		
		p.updateHash();
		
		return p;
	}

	public boolean isTrue(State s) // returns whether this conditions is true
									// is State S
	{
		STRIPSState ss = (STRIPSState) s;
		boolean t = ss.isTrue(this);
		return t;
	}

	public void apply(State s) // carry out the effects of this on State s
	{
		applyDels(s);
		applyAdds(s);
	}

	public void applyAdds(State s)
	{
		STRIPSState ss = (STRIPSState) s;
		ss.addFact(this);
	}

	public void applyDels(State s)
	{
		STRIPSState ss = (STRIPSState) s;
		ss.removeFact(new Not(this)); //TODO hack for removing negated version of propositions
	}

	public boolean isStatic()
	{
		return name.isStatic();
	}

	public GroundFact staticify()
	{
		if (isStatic())
			return TrueCondition.getInstance();
		else
			return this;
	}

	public Set getOperators()
	{
		return super.EmptySet;
	}

	public Set getComparators()
	{
		return super.EmptySet;
	}
	
	@Override
	public int hashCode()
	{
		return this.hash;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Proposition)
		{
			Proposition p = (Proposition) obj;
			boolean eq = (name.equals(p.name) && parameters.equals(p.parameters));
			return eq;
			
			/*18/03/14 Crazy hashCode() behaviour! If "return this.hashCode() == obj.hashCode()", the
			  runtimes are crazy high when profiled. Change it to "boolean s = this.hashCode() == p.hashCode()"
			  and it just breezes through it! Seems to be the casting to a Proposition which does this. If 
			  just "obj.hashCode()" is used, the above behaviour is exhibited.
			  java version "1.7.0_51"
			  OpenJDK Runtime Environment (IcedTea 2.4.4) (7u51-2.4.4-0ubuntu0.12.10.2)
			  OpenJDK 64-Bit Server VM (build 24.45-b08, mixed mode)
			 */

//			boolean h = this.hashCode() == p.hashCode();
//			return h;
//			assert(h == eq);
//			return h && eq;
		} 
		else
			return false;
	}
}
