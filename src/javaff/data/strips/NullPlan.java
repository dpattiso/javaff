package javaff.data.strips;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.Plan;

public class NullPlan implements Plan
{
	private Fact goal; 
	
	public NullPlan()
	{
		this.goal = null;
	}
	
	public NullPlan(Fact goal)
	{
		this();
		
		this.goal = goal;
	}
	
	/**
	 * Returns zero.
	 */
	@Override
	public BigDecimal getCost()
	{
		return BigDecimal.ZERO;
	}
	
	@Override
	public boolean addAction(Action a)
	{
		return false;
	}

	@Override
	public int getPlanLength()
	{
		return 0;
	}

	@Override
	public Iterator<Action> iterator()
	{
		return Collections.emptyIterator();
	}

	@Override
	public void print(PrintStream p)
	{
		p.print("NullPlan, goal "+this.getGoal().toString());
	}

	@Override
	public void print(PrintWriter p)
	{
		p.print("NullPlan, goal "+this.getGoal().toString());
	}

	@Override
	public List<Action> getActions()
	{
		return Collections.EMPTY_LIST;
	}

	@Override
	public int getActionCount()
	{
		return 0;
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

	@Override
	public NullPlan clone()
	{
		return new NullPlan((Fact) this.getGoal().clone());
	}
}
