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

package javaff.planning;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.Parameter;
import javaff.data.TotalOrderPlan;
import javaff.data.metric.NamedFunction;
import javaff.data.strips.Not;
import javaff.data.strips.OperatorName;
import javaff.data.strips.Proposition;
import javaff.data.strips.STRIPSInstantAction;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class HelpfulFilter implements Filter
{
	private static HelpfulFilter hf = null;

	private HelpfulFilter()
	{
	}

	public static HelpfulFilter getInstance()
	{
		if (hf == null)
			hf = new HelpfulFilter(); // Singleton, as in NullFilter
		return hf;
	}

	public List<Action> getActions(State S)
	{
		STRIPSState SS = (STRIPSState) S;
		SS.calculateRP(); // get the relaxed plan to the goal, to make sure
							// helpful actions exist for S

		SortedSet<HelpfulAction> allHelpful = SS.getRPG().getHelpfulActions(); //returned in sorted order!
		ArrayList<Action> allHelpfulApplicable = new ArrayList<Action>();
		
		//most helpful are at the start of the iterator
		for (Action a : allHelpful)
		{
			if (a.isApplicable(SS))
				allHelpfulApplicable.add(a);
		}

		return allHelpfulApplicable;
	}
	



}