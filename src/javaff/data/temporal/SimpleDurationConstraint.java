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

import javaff.data.metric.MetricSymbolStore;
import javaff.data.metric.Function;
import javaff.data.PDDLPrinter;
import javaff.planning.MetricState;

import java.util.Map;
import java.io.PrintStream;
import java.math.BigDecimal;

public class SimpleDurationConstraint extends DurationConstraint
{
	protected int type;
	protected DurationFunction variable;
	protected Function value;

	public SimpleDurationConstraint(DurationFunction v, Function f, int t)
	{
		type = t;
		variable = v;
		value = f;
	}

	public DurationConstraint ground(Map varMap)
	{
		return new SimpleDurationConstraint((DurationFunction) variable
				.ground(varMap), value.ground(varMap), type);
	}

	public BigDecimal getDuration(MetricState ms)
	{
		return value.getValue(ms);
	}

	// could put stuff about < and > using epsilonAccuracy
	public BigDecimal getMaxDuration(MetricState ms)
	{
		if (type == MetricSymbolStore.LESS_THAN_EQUAL)
			return value.getValue(ms);
		else if (type == MetricSymbolStore.GREATER_THAN_EQUAL)
			return javaff.JavaFF.MAX_DURATION;
		else if (type == MetricSymbolStore.EQUAL)
			return value.getValue(ms);

		else
			return null;
	}

	public BigDecimal getMinDuration(MetricState ms)
	{
		if (type == MetricSymbolStore.LESS_THAN_EQUAL)
			return new BigDecimal(0);
		else if (type == MetricSymbolStore.GREATER_THAN_EQUAL)
			return value.getValue(ms);
		else if (type == MetricSymbolStore.EQUAL)
			return value.getValue(ms);
		else
			return null;
	}

	public boolean staticDuration()
	{
		// return value.isStatic();
		return (type == MetricSymbolStore.EQUAL);
	}

	public void addConstraint(SimpleDurationConstraint sdc)
	{

	}

	public void PDDLPrint(PrintStream p, int indent)
	{
		PDDLPrinter.printToString(this, p, true, false, indent);
	}

	public String toString()
	{
		String str = "(" + MetricSymbolStore.getSymbol(type) + " "
				+ variable.toString() + " " + value.toString() + ")";
		return str;
	}

	public String toStringTyped()
	{
		String str = "(" + MetricSymbolStore.getSymbol(type) + " "
				+ variable.toStringTyped() + " " + value.toStringTyped() + ")";
		return str;
	}

	public int hashCode()
	{
		int hash = 7;
		hash = 31 * hash ^ type;
		hash = 31 * hash ^ variable.hashCode();
		hash = 31 * hash ^ value.hashCode();
		return hash;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof SimpleDurationConstraint)
		{
			SimpleDurationConstraint c = (SimpleDurationConstraint) obj;
			return (type == c.type && variable.equals(c.variable) && value
					.equals(c.value));
		} else
			return false;
	}

}
