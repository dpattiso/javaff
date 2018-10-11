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

import javaff.scheduling.MatrixSTN;
import javaff.planning.MetricState;

import java.math.BigDecimal;
import java.util.Map;

public class NumberFunction implements Function
{
	final protected BigDecimal num;

	public NumberFunction(double d)
	{
		num = new BigDecimal(d);
	}

	public NumberFunction(BigDecimal d)
	{
		if (d != null)
			num = d;
		else
			num = new BigDecimal(0);
	}

	public BigDecimal getValue(MetricState s)
	{
		return num;
	}

	public BigDecimal getMaxValue(MatrixSTN stn)
	{
		return getValue(null);
	}

	public BigDecimal getMinValue(MatrixSTN stn)
	{
		return getValue(null);
	}

	public Function staticify(Map fValues)
	{
		return this;
	}

	public Function makeOnlyDurationDependent(MetricState s)
	{
		return this;
	}

	public String toString()
	{
		return num.toString();
	}

	public String toStringTyped()
	{
		return toString();
	}

	public boolean isStatic()
	{
		return true;
	}

	public boolean effectedBy(ResourceOperator ro)
	{
		return false;
	}

	public Function replace(ResourceOperator ro)
	{
		return this;
	}

	public Function ground(Map varMap)
	{
		return this;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof NumberFunction)
		{
			NumberFunction nf = (NumberFunction) obj;
			return num.equals(nf.num);
		} else
			return false;
	}
}
