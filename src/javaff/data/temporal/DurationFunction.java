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

import javaff.data.metric.Function;
import javaff.data.metric.NamedFunction;
import javaff.data.metric.NumberFunction;
import javaff.planning.MetricState;
import javaff.scheduling.MatrixSTN;

import java.util.Map;
import java.math.BigDecimal;

public class DurationFunction extends NamedFunction
{
	public DurativeAction durativeAction;
	UngroundDurativeAction ungroundDurativeAction; // horrible hack

	public DurationFunction(DurativeAction da)
	{
		durativeAction = da;
	}

	public DurationFunction(UngroundDurativeAction uda)
	{
		ungroundDurativeAction = uda;
	}

	public BigDecimal getValue(MetricState ms)
	{
		return durativeAction.getDuration(ms);
	}

	public BigDecimal getMaxValue(MatrixSTN stn)
	{
		return stn.getMaximum(durativeAction);
	}

	public BigDecimal getMinValue(MatrixSTN stn)
	{
		return stn.getMinimum(durativeAction);
	}

	public Function staticify(Map fValues)
	{
		if (durativeAction.staticDuration())
		{
			BigDecimal d = getValue(null);
			return new NumberFunction(d);
		} else
			return this;
	}

	public Function makeOnlyDurationDependent(MetricState s)
	{
		return this;
	}

	public Function ground(Map varMap)
	{
		return (Function) varMap.get(this);
	}

	public String toString()
	{
		return "?duration";
	}

	public String toStringTyped()
	{
		return "?duration";
	}

	public int hashCode()
	{
		int hash = 7;
		if (durativeAction != null)
			hash = 31 * hash ^ durativeAction.hashCode();
		else
			hash = 31 * hash ^ ungroundDurativeAction.hashCode();
		return hash;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof DurationFunction)
		{
			DurationFunction f = (DurationFunction) obj;
			if (f.durativeAction != null && durativeAction != null)
				return durativeAction.equals(f.durativeAction);
			else if (f.ungroundDurativeAction != null
					&& ungroundDurativeAction != null)
				return ungroundDurativeAction.equals(f.ungroundDurativeAction);
			else
				return false;
		} else
			return false;
	}

}
