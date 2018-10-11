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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class NullFilter implements Filter
{
	private static NullFilter nf = null;

	private NullFilter()
	{
	}

	public static NullFilter getInstance()
	{
		if (nf == null)
			nf = new NullFilter(); // Singleton design pattern - return one
									// central instance
		return nf;
	}

	public List<Action> getActions(State S)
	{
		Set<Action> actionsFromS = S.getActions(); // get the logically appicable
											// actions in S
		List ns = new ArrayList();
		Iterator ait = actionsFromS.iterator(); // Get an iterator over these
												// actions
		while (ait.hasNext())
		{
			Action a = (Action) ait.next();
			if (a.isApplicable(S))
				ns.add(a); // Check they are applicable (will check
							// numeric/temporal constraints)
		}
		return ns;
	}

}