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

import java.math.BigDecimal;
import java.util.ArrayList;

import javaff.data.GroundFact;
import javaff.data.Parameter;

public class STRIPSInstantAction extends InstantAction
{
	public STRIPSInstantAction()
	{
		super();
	}
	
	public STRIPSInstantAction(String name)
	{
		super(name);
	}
	
	@Override
	public Object clone()
	{
		STRIPSInstantAction clone = new STRIPSInstantAction();
		clone.setCondition((GroundFact) super.getCondition().clone());
		clone.setEffect((GroundFact) super.getEffect().clone());
		clone.setName((OperatorName) super.getName().clone());
		clone.setParameters(new ArrayList<Parameter>());
		for (Parameter p : super.getParameters())
			clone.getParameters().add((Parameter) p.clone());
		clone.setCost(new BigDecimal(0).add(super.getCost()));
		
		return clone;
	}
	
	
}
