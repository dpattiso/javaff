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

package javaff.scheduling;

import javaff.data.Fact;
import javaff.data.TotalOrderPlan;
import javaff.data.PartialOrderPlan;
import javaff.data.GroundProblem;
import javaff.data.Action;
import javaff.data.strips.Not;
import javaff.data.strips.Proposition;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

public class GreedyPartialOrderLifter implements PartialOrderLifter
{
	// This could probably make use of the data structures in the relaxed plan
	// graph
	public static PartialOrderPlan lift(TotalOrderPlan top,
			GroundProblem problem)
	{
		/*
		 * WARNING - this code does not work, but should have something in
		 * instead //Must create new instances for where the same action is
		 * performed more than once List TOPlan = new ArrayList(); Iterator pit =
		 * pTOPlan.iterator(); while (pit.hasNext()) { InstantAction a =
		 * (InstantAction) pit.next(); TOPlan.add(a.clone()); }
		 */

		PartialOrderPlan pop = new PartialOrderPlan(problem.getGoal());
		pop.addActions(new HashSet(top.getActions()));

		Set goals = new HashSet(problem.getGoal().getFacts());

		ListIterator toit = top.listIteratorEnd();
		while (toit.hasPrevious())
		{
			Action a = (Action) toit.previous();

			getAOrderings(a, top, pop);
			getBOrderings(a, top, pop);
			getCOrderings(a, top, pop, goals);

			goals.addAll(a.getPreconditions());
		}

		return pop;
	}

	public static void getAOrderings(Action a, TotalOrderPlan top,
			PartialOrderPlan pop)
	{
		Iterator cit = a.getPreconditions().iterator();
		while (cit.hasNext())
		{
			Proposition c = (Proposition) cit.next();
			ListIterator bit = top.listIterator(a);
			while (bit.hasPrevious())
			{
				Action b = (Action) bit.previous();
				Set achieves = b.getAddPropositions();
				if (achieves.contains(c))
				{
					pop.addOrder(b, a, c);
					break;
				}
			}
		}
	}

	public static void getBOrderings(Action a, TotalOrderPlan top,
			PartialOrderPlan pop)
	{
		Iterator dit = a.getDeletePropositions().iterator();
		while (dit.hasNext())
		{
			Fact n = (Fact) dit.next();
			Proposition d = null;
			if (n instanceof Proposition)
				d = (Proposition) n;
			else if (n instanceof Not)
				d = (Proposition) ((Not)n).getLiteral();
			else
				throw new ClassCastException("Unknown delete type");
			
			ListIterator bit = top.listIterator(a);
			while (bit.hasPrevious())
			{
				Action b = (Action) bit.previous();
				Set conds = b.getPreconditions();
				if (conds.contains(d))
				{
					pop.addOrder(b, a, d);
				}
			}
		}
	}

	public static void getCOrderings(Action a, TotalOrderPlan top,
			PartialOrderPlan pop, Set goals)
	{
		Set as = a.getAddPropositions();
		as.retainAll(goals);
		Iterator adit = as.iterator();
		while (adit.hasNext())
		{
			Proposition ad = (Proposition) adit.next();
			ListIterator bit = top.listIterator(a);
			while (bit.hasPrevious())
			{
				Action b = (Action) bit.previous();
				Set dels = b.getDeletePropositions();
				if (dels.contains(ad))
				{
					pop.addOrder(b, a, ad);
				}
			}
		}
	}

}
