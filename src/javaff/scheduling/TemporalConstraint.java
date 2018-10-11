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

import javaff.data.strips.InstantAction;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

public class TemporalConstraint extends Constraint
{
	InstantAction x, y;
	BigDecimal b;

	public TemporalConstraint(InstantAction X, InstantAction Y, BigDecimal B)
	{
		x = X;
		y = Y;
		b = B;
	}

	public static TemporalConstraint getConstraint(InstantAction first,
			InstantAction second)
	{
		return new TemporalConstraint(first, second, javaff.JavaFF.EPSILON
				.negate());
	}

	public static TemporalConstraint getConstraintEqual(InstantAction first,
			InstantAction second)
	{
		return new TemporalConstraint(first, second, new BigDecimal(0));
	}

	public static TemporalConstraint getConstraintMax(InstantAction first,
			InstantAction second, BigDecimal max)
	{
		return new TemporalConstraint(second, first, max);
	}

	public static TemporalConstraint getConstraintMin(InstantAction first,
			InstantAction second, BigDecimal min)
	{
		return new TemporalConstraint(first, second, min.negate());
	}

	public static List getExactly(InstantAction first, InstantAction second,
			BigDecimal value)
	{
		List rList = new ArrayList(2);
		rList.add(getConstraintMax(first, second, value));
		rList.add(getConstraintMin(first, second, value));
		return rList;
	}

	public static List getBounds(InstantAction first, InstantAction second,
			BigDecimal max, BigDecimal min)
	{
		List rList = new ArrayList(2);
		rList.add(getConstraintMax(first, second, max));
		rList.add(getConstraintMin(first, second, min));
		return rList;
	}

	public String toString()
	{
		return (x.toString() + " - " + y.toString() + " <= " + b.toString());
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof TemporalConstraint)
		{
			TemporalConstraint c = (TemporalConstraint) obj;
			return (c.x.equals(x) && c.y.equals(y) && c.b.equals(b));
		}
		return false;
	}

	public int hashCode()
	{
		int hash = 2;
		hash = 31 * hash ^ x.hashCode();
		hash = 31 * hash ^ y.hashCode();
		hash = 31 * hash ^ b.hashCode();
		return hash;
	}
}
