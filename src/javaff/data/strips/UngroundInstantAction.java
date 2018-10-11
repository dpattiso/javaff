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

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.UngroundFact;

import javaff.data.PDDLPrinter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class UngroundInstantAction extends Operator
{
	public UngroundFact condition;
	public UngroundFact effect;
	
	public UngroundInstantAction()
	{
		this.condition = TrueCondition.getInstance();
		this.effect = TrueCondition.getInstance();
	}

	public boolean effects(PredicateSymbol ps)
	{
		return effect.effects(ps);
	}

	public Action ground(Map<Variable, PDDLObject> varMap)
	{
		return ground(varMap, new STRIPSInstantAction());
	}

	public Action ground(Map<Variable, PDDLObject> varMap, InstantAction a)
	{
		a.setName(this.name);

		Iterator pit = params.iterator();
		while (pit.hasNext())
		{
			Variable v = (Variable) pit.next();
			PDDLObject o = (PDDLObject) varMap.get(v);
			a.getParameters().add(o);
		}
		
		a.setCondition(condition.ground(varMap));
		a.setEffect(effect.ground(varMap));
		
		return a;
	}

	public Set<Fact> getStaticConditionPredicates()
	{
		return condition.getStaticPredicates();
	}

	public void PDDLPrint(java.io.PrintStream p, int indent)
	{
		p.println();
		PDDLPrinter.printIndent(p, indent);
		p.print("(:action ");
		p.print(name);
		p.println();
		PDDLPrinter.printIndent(p, indent + 1);
		p.print(":parameters(\n");
		PDDLPrinter.printToString(params, p, true, false, indent + 2);
		p.println(")");
		PDDLPrinter.printIndent(p, indent + 1);
		p.print(":precondition");
		condition.PDDLPrint(p, indent + 2);
		p.println();
		PDDLPrinter.printIndent(p, indent + 1);
		p.print(":effect");
		effect.PDDLPrint(p, indent + 2);
		p.print(")");
	}

	@Override
	public Object clone()
	{
		UngroundInstantAction a = new UngroundInstantAction();
		a.name = this.name;
		a.params = this.params;
		a.condition = this.condition;
		a.effect = this.effect;
		
		return a;
	}
}
