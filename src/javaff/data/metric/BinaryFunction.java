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

import javaff.planning.MetricState;
import javaff.scheduling.MatrixSTN;

import java.math.BigDecimal;
import java.util.Map;

public class BinaryFunction implements Function
{

	public Function first, second;
	public int type;

	protected BinaryFunction()
	{
	}

	public BinaryFunction(String s, Function f1, Function f2)
	{
		type = MetricSymbolStore.getType(s);
		first = f1;
		second = f2;
	}

	public BinaryFunction(int t, Function f1, Function f2)
	{
		type = t;
		first = f1;
		second = f2;
	}

	public boolean effectedBy(ResourceOperator ro)
	{
		return (first.effectedBy(ro) || second.effectedBy(ro));
	}

	public Function replace(ResourceOperator ro)
	{
		return new BinaryFunction(type, first.replace(ro), second.replace(ro));
	}

	public boolean isStatic()
	{
		return (first.isStatic() && second.isStatic());
	}

	public Function staticify(Map fValues)
	{
		first = first.staticify(fValues);
		second = second.staticify(fValues);
		if (isStatic())
		{
			return new NumberFunction(getValue(null));
		} else
			return this;
	}

	public Function makeOnlyDurationDependent(MetricState s)
	{
		BinaryFunction bf = new BinaryFunction(type, first
				.makeOnlyDurationDependent(s), second
				.makeOnlyDurationDependent(s));
		Function f = null;
		if (bf.first instanceof NumberFunction
				&& bf.second instanceof NumberFunction)
		{
			f = new NumberFunction(bf.getValue(s));
		} else
			f = bf;
		return f;
	}

	public Function ground(Map varMap)
	{
		return new BinaryFunction(type, first.ground(varMap), second
				.ground(varMap));
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof BinaryFunction)
		{
			BinaryFunction bf = (BinaryFunction) obj;
			if (bf.type == this.type && first.equals(bf.first)
					&& second.equals(bf.second))
				return true;
			else if (((bf.type == MetricSymbolStore.PLUS && this.type == MetricSymbolStore.PLUS) || (this.type == MetricSymbolStore.MULTIPLY && bf.type == MetricSymbolStore.MULTIPLY))
					&& (first.equals(bf.second) && second.equals(bf.first)))
				return true;
			else
				return false;
		} else
			return false;
	}

	public String toString()
	{
		return "(" + MetricSymbolStore.getSymbol(type) + " " + first.toString()
				+ " " + second.toString() + ")";
	}

	public String toStringTyped()
	{
		return "(" + MetricSymbolStore.getSymbol(type) + " "
				+ first.toStringTyped() + " " + second.toStringTyped() + ")";
	}

	public BigDecimal getValue(MetricState s)
	{
		BigDecimal fbd = first.getValue(s);
		BigDecimal sbd = second.getValue(s);
		if (type == MetricSymbolStore.PLUS)
			return fbd.add(sbd);
		else if (type == MetricSymbolStore.MINUS)
			return fbd.subtract(sbd);
		else if (type == MetricSymbolStore.MULTIPLY)
			return fbd.multiply(sbd);
		else if (type == MetricSymbolStore.DIVIDE)
			return fbd.divide(sbd, MetricSymbolStore.SCALE,
					MetricSymbolStore.ROUND);
		else
			return null;
	}

	public BigDecimal getMaxValue(MatrixSTN stn)
	{
		if (type == MetricSymbolStore.PLUS)
			return first.getMaxValue(stn).add(second.getMaxValue(stn));
		else if (type == MetricSymbolStore.MINUS)
			return first.getMaxValue(stn).subtract(second.getMinValue(stn));
		else if (type == MetricSymbolStore.MULTIPLY)
			return first.getMaxValue(stn).multiply(second.getMaxValue(stn));
		else if (type == MetricSymbolStore.DIVIDE)
			return first.getMaxValue(stn).divide(second.getMinValue(stn),
					MetricSymbolStore.SCALE, MetricSymbolStore.ROUND);
		else
			return null;
	}

	public BigDecimal getMinValue(MatrixSTN stn)
	{
		if (type == MetricSymbolStore.PLUS)
			return first.getMinValue(stn).add(second.getMinValue(stn));
		else if (type == MetricSymbolStore.MINUS)
			return first.getMinValue(stn).subtract(second.getMaxValue(stn));
		else if (type == MetricSymbolStore.MULTIPLY)
			return first.getMinValue(stn).multiply(second.getMinValue(stn));
		else if (type == MetricSymbolStore.DIVIDE)
			return first.getMinValue(stn).divide(second.getMaxValue(stn),
					MetricSymbolStore.SCALE, MetricSymbolStore.ROUND);
		else
			return null;
	}
}
