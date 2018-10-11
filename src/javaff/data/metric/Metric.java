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

package javaff.data.metric;

import javaff.data.PDDLPrintable;

import java.io.PrintStream;

public class Metric implements PDDLPrintable
{
	public MetricType type;
	public Function func;

	public Metric(MetricType t, Function f)
	{
		type = t;
		func = f;
	}

	public void PDDLPrint(PrintStream p, int indent)
	{
		p.print("(:metric ");
		p.print(toString());
		p.print(")");
	}

	public String toString()
	{
		String str = "";
		if (type == MetricType.Maximize)
			str += "maximize ";
		else if (type == MetricType.Minimize)
			str += "minimize ";
		str += func.toString();
		return str;
	}

	public String toStringTyped()
	{
		String str = "";
		if (type == MetricType.Maximize)
			str += "maximize ";
		else if (type == MetricType.Minimize)
			str += "minimize ";
		str += func.toStringTyped();
		return str;
	}
}
