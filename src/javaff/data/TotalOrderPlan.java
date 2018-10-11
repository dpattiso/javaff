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

package javaff.data;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;

/**
 * Represents a totally-ordered plan, containing discrete actions.
 * @author David Pattison
 *
 */
public class TotalOrderPlan implements Plan, Cloneable, Iterable<Action>
{
	private List<Action> plan;
	private Fact goal;
	
	public TotalOrderPlan(Fact goal)
	{
		this.goal = goal;
		this.plan = new ArrayList<Action>();
	}
	
	@Override
	public Iterator<Action> iterator()
	{
		return this.getPlan().iterator();
	}
	
	@Override
	public Fact getGoal()
	{
		return this.goal;
	}
	
	@Override
	public void setGoal(Fact g)
	{
		this.goal = g;
	}

	public Plan clone()
	{
		Fact gClone = null;
		if (this.getGoal() != null)
		{
			gClone = (Fact) this.getGoal().clone();
		}
		
		TotalOrderPlan rTOP = new TotalOrderPlan(gClone);
		for (Action a : this.getPlan())
		{
			rTOP.getPlan().add((Action) a.clone());
		}
		
		return rTOP;
	}
	
	/**
	 * Returns the sum of all action-costs in this plan.
	 */
	@Override
	public BigDecimal getCost()
	{
		BigDecimal cost = new BigDecimal(0);
		for (Action a : this)
		{
			cost = cost.add(a.getCost());
		}
		
		return cost;
	}

	public boolean addAction(Action a)
	{
		return getPlan().add(a);
	}

	public int getPlanLength()
	{
		return getPlan().size();
	}
	
	public int getActionCount()
	{
		return this.getPlan().size();
	}

	public ListIterator<Action> listIteratorEnd()
	{
		return getPlan().listIterator(getPlan().size());
	}

	public ListIterator<Action> listIterator(Action a)
	{
		return getPlan().listIterator(getPlan().indexOf(a));
	}

	@Override
	public List<Action> getActions()
	{
		return getPlan();
	}
	
//	public List<Action> getActions()
//	{
//		return plan;
//	}
	
	public void clear()
	{
		this.getPlan().clear();
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof TotalOrderPlan)
		{
			TotalOrderPlan p = (TotalOrderPlan) obj;
			return (getPlan().equals(p.getPlan()));
		} else
			return false;
	}

	public int hashCode()
	{
		return getPlan().hashCode() ^ goal.hashCode() ^ 31;
	}

	public void print(PrintStream ps)
	{
		Iterator pit = getPlan().iterator();
		while (pit.hasNext())
		{
			ps.println("(" + pit.next() + ")");
		}
	}

	public void print(PrintWriter pw)
	{
		Iterator pit = getPlan().iterator();
		while (pit.hasNext())
		{
			pw.println("(" + pit.next() + ")");
		}
	}
	
	@Override
	public String toString() 
	{
		return getPlan().toString();
	}

	//TODO make read only?
	protected List<Action> getPlan()
	{
		return plan;
	}

	protected void setPlan(List<Action> plan)
	{
		this.plan = plan;
	}
}
