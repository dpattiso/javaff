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

import javaff.data.Action;
import javaff.data.Plan;
import javaff.data.TimeStampedPlan;
import javaff.data.strips.InstantAction;
import javaff.data.strips.STRIPSInstantAction;
import javaff.data.strips.OperatorName;
import javaff.data.temporal.StartInstantAction;
import javaff.data.temporal.EndInstantAction;
import javaff.data.temporal.DurativeAction;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.math.BigDecimal;

public class MatrixSTN implements SimpleTemporalNetwork
{
	static BigDecimal EPSILON = javaff.JavaFF.EPSILON;
	static BigDecimal ZERO = new BigDecimal(0);
	static BigDecimal INF = new BigDecimal(100000);
	static BigDecimal NEG_EPSILON = EPSILON.negate();
	static int SCALE = 2;
	static int ROUND = BigDecimal.ROUND_HALF_EVEN;

	BigDecimal[][] TheArray;
	ArrayList Timepoints;
	Plan pop;
	int Size;

	InstantAction START = new STRIPSInstantAction();

	public MatrixSTN(Plan plan)
	{
		pop = plan;
		START.setName(new OperatorName("TIME_ZERO"));

		Timepoints = new ArrayList();
		Timepoints.add(START);
		Timepoints.addAll(pop.getActions());

		ZERO = ZERO.setScale(SCALE, ROUND);
		INF = INF.setScale(SCALE, ROUND);
		NEG_EPSILON = NEG_EPSILON.setScale(SCALE, ROUND);

		Size = Timepoints.size();
		TheArray = new BigDecimal[Size][Size];
		TheArray[0][0] = ZERO;
		for (int i = 1; i < Size; ++i)
		{
			TheArray[0][i] = INF;
			TheArray[i][0] = NEG_EPSILON;

			for (int j = 1; j < Size; ++j)
			{
				if (i == j)
					TheArray[i][j] = ZERO;
				else
					TheArray[i][j] = INF;
			}
		}
	}

	public void addConstraints(Set constraints)
	{
		Iterator oit = constraints.iterator();
		while (oit.hasNext())
		{
			TemporalConstraint c = (TemporalConstraint) oit.next();
			addConstraint(c);
		}
	}

	public void addConstraint(TemporalConstraint c)
	{
		int firstpos = Timepoints.indexOf(c.y);
		int secondpos = Timepoints.indexOf(c.x);
		TheArray[firstpos][secondpos] = TheArray[firstpos][secondpos].min(c.b)
				.setScale(SCALE, ROUND);
	}

	public void constrain()
	{
		for (int k = 0; k < Size; ++k)
		{
			for (int i = 0; i < Size; ++i)
			{
				for (int j = 0; j < Size; ++j)
				{
					if (TheArray[i][j].compareTo(TheArray[i][k]
							.add(TheArray[k][j])) > 0)
					{
						TheArray[i][j] = TheArray[i][k].add(TheArray[k][j])
								.setScale(SCALE, ROUND);
					}
				}
			}
		}
	}

	public boolean check()
	{
		for (int i = 0; i < Size; ++i)
		{
			if (TheArray[i][i].compareTo(ZERO) < 0)
			{
				return false;
			}
		}
		return true;
	}

	public boolean consistent()
	{
		constrain();
		return check();
	}

	public boolean consistentSource(InstantAction a)
	{
		return consistent();
	}

	public TimeStampedPlan getTimes()
	{
		TimeStampedPlan plan = new TimeStampedPlan(null);
		Iterator ait = Timepoints.iterator();
		while (ait.hasNext())
		{
			InstantAction a = (InstantAction) ait.next();
			;
			if (a instanceof StartInstantAction)
			{
				DurativeAction da = ((StartInstantAction) a).parent;
				BigDecimal time = TheArray[Timepoints.indexOf(a)][0].negate()
						.setScale(SCALE, ROUND);
				BigDecimal dur = TheArray[Timepoints.indexOf(da.endAction)][0]
						.negate().subtract(time).setScale(SCALE, ROUND);
				plan.addAction(da, time, dur);
			} else if (a instanceof STRIPSInstantAction && a != START)
			{
				BigDecimal time = TheArray[Timepoints.indexOf(a)][0].negate()
						.setScale(SCALE, ROUND);
				plan.addAction(a, time);
			}
		}
		return plan;
	}

	public boolean B(Action a, Action b)
	{
		BigDecimal v = TheArray[Timepoints.indexOf(b)][Timepoints.indexOf(a)];
		return (v.compareTo(ZERO) < 0);
	}

	public boolean BS(Action a, Action b)
	{
		BigDecimal v = TheArray[Timepoints.indexOf(b)][Timepoints.indexOf(a)];
		return (v.compareTo(ZERO) <= 0);
	}

	public boolean U(Action a, Action b)
	{
		BigDecimal v1 = TheArray[Timepoints.indexOf(b)][Timepoints.indexOf(a)];
		BigDecimal v2 = TheArray[Timepoints.indexOf(b)][Timepoints.indexOf(a)];
		return (v1.compareTo(ZERO) > 0 && v2.compareTo(ZERO) > 0);
	}

	public Action getEarliest(Set s)
	{
		Iterator sit = s.iterator();
		Action c = null;
		while (sit.hasNext())
		{
			Action a = (Action) sit.next();
			if (c == null)
				c = a;
			else if (TheArray[Timepoints.indexOf(c)][0]
					.compareTo(TheArray[Timepoints.indexOf(a)][0]) > 0)
				c = a;
		}
		return c;
	}

	public BigDecimal getMinimum(DurativeAction da)
	{
		return TheArray[Timepoints.indexOf(da.endAction)][Timepoints
				.indexOf(da.startAction)].negate();

	}

	public BigDecimal getMaximum(DurativeAction da)
	{
		return TheArray[Timepoints.indexOf(da.startAction)][Timepoints
				.indexOf(da.endAction)];
	}

	public void increaseMin(DurativeAction da, BigDecimal diff)
	{
		BigDecimal v1 = TheArray[Timepoints.indexOf(da.endAction)][Timepoints
				.indexOf(da.startAction)];
		BigDecimal v2 = v1.subtract(diff);
		TheArray[Timepoints.indexOf(da.endAction)][Timepoints
				.indexOf(da.startAction)] = v1.min(v2);
	}

	public void decreaseMax(DurativeAction da, BigDecimal diff)
	{
		BigDecimal v1 = TheArray[Timepoints.indexOf(da.startAction)][Timepoints
				.indexOf(da.endAction)];
		BigDecimal v2 = v1.subtract(diff);
		TheArray[Timepoints.indexOf(da.startAction)][Timepoints
				.indexOf(da.endAction)] = v1.min(v2);
	}

	public void maximize(DurativeAction da)
	{
		TheArray[Timepoints.indexOf(da.endAction)][Timepoints
				.indexOf(da.startAction)] = TheArray[Timepoints
				.indexOf(da.startAction)][Timepoints.indexOf(da.endAction)]
				.negate();
	}

	public void minimize(DurativeAction da)
	{
		TheArray[Timepoints.indexOf(da.startAction)][Timepoints
				.indexOf(da.endAction)] = TheArray[Timepoints
				.indexOf(da.endAction)][Timepoints.indexOf(da.startAction)]
				.negate();
	}

	public void minimizeTime()
	{
		Iterator ait = Timepoints.iterator();
		while (ait.hasNext())
		{
			InstantAction a = (InstantAction) ait.next();
			;
			if (a instanceof EndInstantAction)
			{
				int i = Timepoints.indexOf(a);
				if (TheArray[i][0].compareTo(TheArray[0][i].negate()) != 0)
				{
					TheArray[0][i] = TheArray[i][0].negate();
					constrain();
				}
			}
		}

	}

	public void minimizeDuration()
	{
		Iterator ait = Timepoints.iterator();
		while (ait.hasNext())
		{
			InstantAction a = (InstantAction) ait.next();
			;
			if (a instanceof StartInstantAction)
			{
				DurativeAction da = ((StartInstantAction) a).parent;
				minimize(da);
			}
		}
	}

	public void printArray()
	{
		System.out.print("                                       ");
		for (int i = 0; i < Size; ++i)
		{
			String istr = (new Integer(i)).toString();
			istr += " ";
			istr = "  " + istr.substring(0, 2) + " ";
			System.out.print(istr);
		}
		System.out.println();
		for (int i = 0; i < Size; ++i)
		{
			String istr = (new Integer(i)).toString();
			istr += " ";
			istr = "  " + istr.substring(0, 2) + " ";
			System.out
					.print((Timepoints.get(i).toString() + "                                                 ")
							.substring(0, 35)
							+ istr);
			for (int j = 0; j < Size; ++j)
			{
				if (TheArray[i][j].compareTo(INF) == 0)
					System.out.print("INF  ");
				else
					System.out.print(TheArray[i][j] + "  ");
			}
			System.out.print("\n");
		}
	}

	public Object clone() throws CloneNotSupportedException
	{
		MatrixSTN STN = (MatrixSTN) super.clone();
		STN.Size = Size;
		STN.Timepoints = Timepoints;
		STN.START = START;
		for (int i = 0; i < Size; ++i)
		{
			for (int j = 0; j < Size; ++j)
			{
				STN.TheArray[i][j] = (TheArray[i][j]).setScale(
						SCALE, ROUND);
			}
		}
		return STN;
	}

}
