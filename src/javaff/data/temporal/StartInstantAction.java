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

package javaff.data.temporal;

import javaff.data.strips.Proposition;
import javaff.planning.TemporalMetricState;

public class StartInstantAction extends SplitInstantAction
{

	public SplitInstantAction getSibling()
	{
		return parent.endAction;
	}

	public void applySplit(TemporalMetricState ts)
	{
		ts.invariants.addAll(parent.invariant.getFacts());
		ts.openActions.add(parent);
		ts.getActions().remove(this);
		ts.getActions().add(getSibling());
	}

	public boolean exclusivelyInvariant(Proposition p)
	{
		return !parent.startCondition.getFacts().contains(p)
				|| !parent.startEffect.getFacts().contains(p)
				|| !parent.startEffect.getFacts().contains(p);
	}
	
	@Override
	public Object clone()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
