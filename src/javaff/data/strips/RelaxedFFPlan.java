package javaff.data.strips;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.RelaxedPlan;
import javaff.data.TotalOrderPlan;

public class RelaxedFFPlan implements RelaxedPlan
{
	private List<Collection<Action>> relaxedPlan;
	private Fact goal;

	public RelaxedFFPlan(Fact goal)
	{
		this.goal = goal;
		this.relaxedPlan = new ArrayList<Collection<Action>>();
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

	@Override
	public Iterator<Action> iterator()
	{
		return this.getActions().iterator();
	}

	@Override
	public List<Collection<Action>> getRelaxedPlan()
	{
		return this.relaxedPlan;
	}

	public RelaxedFFPlan clone()
	{
		Fact gClone = null;
		if (this.getGoal() != null)
		{
			gClone = (Fact) this.getGoal().clone();
		}

		RelaxedFFPlan rplanClone = new RelaxedFFPlan(gClone);

		for (Collection<Action> list : this.getRelaxedPlan())
		{
			ArrayList<Action> cloneLayer = new ArrayList<Action>();
			for (Action a : list)
			{
				cloneLayer.add((Action) a.clone());
			}
			rplanClone.addActions(cloneLayer);
		}

		return rplanClone;
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
	
	/**
	 * Adds a new action layer to the relaxed plan containing a single action.
	 */
	@Override
	public boolean addAction(Action a)
	{
		HashSet<Action> newLayer = new HashSet<Action>();
		newLayer.add(a);
		return this.relaxedPlan.add(newLayer);
	}
	
	/**
	 * Get the number of relaxed action groups in this plan. This is equivalent to the number of layers in the RPG which 
	 * this plan was extracted from.
	 * @return
	 */
	public int getRelaxedActionGroupCount()
	{
		return this.getRelaxedPlan().size();
	}
	
	/**
	 * Adds a single action to an existing action layer in the relaxed plan.
	 * @param a The action to add.
	 * @param index The layer to add the action to (0-valued).
	 * @throws IndexOutOfBoundsException Thrown if the index is outside the range of the layers already present in the plan.
	 * @see #getRelaxedActionGroupCount() Returns the upper bound.
	 */
	public void addAction(Action a, int index)
	{
		if (index >= this.getRelaxedActionGroupCount())
		{
			throw new IndexOutOfBoundsException("Relaxed plan length is less than index specified");
		}

		relaxedPlan.get(index).add(a);
	}

	/**
	 * Add a layer of actions to the relaxed plan. These are appended to the
	 * existing relaxed plan.
	 * 
	 * @param layer
	 *            A set of actions which all exist on the same layer
	 */
	public void addActions(Collection<Action> layer)
	{
		this.relaxedPlan.add(layer);
	}

	/**
	 * Get the relaxed plan length.
	 * @see #getActionCount()
	 * @return
	 */
	public int getPlanLength()
	{
		return this.getActionCount();
	}


	/**
	 * Get the relaxed plan length.
	 * 
	 * @see #getRelaxedPlanLength()
	 */
	public int getActionCount()
	{
//		return this.relaxedPlan.size();
		return this.getActions().size();
	}

	public ListIterator<Collection<Action>> listIteratorEnd()
	{
		return relaxedPlan.listIterator(relaxedPlan.size());
	}

	public ListIterator<Collection<Action>> listIterator(Action a)
	{
		return relaxedPlan.listIterator(relaxedPlan.indexOf(a));
	}

	/**
	 * Returns all individual actions which make up this relaxed plan. Note that as actions are grouped together by layers, there is partial ordering in the resulting totally-ordered
	 * list of actions returned.
	 * @return
	 */
	@Override
	public List<Action> getActions()
	{
		ArrayList<Action> all = new ArrayList<Action>();
		for (Collection<Action> l : this.relaxedPlan)
		{
			all.addAll(l);
		}
		return all;
	}

	// public List<Action> getActions()
	// {
	// return plan;
	// }

	public void clear()
	{
		this.relaxedPlan.clear();
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof RelaxedFFPlan)
		{
			RelaxedFFPlan p = (RelaxedFFPlan) obj;
			return (this.relaxedPlan.equals(p.relaxedPlan));
		}
		else
			return false;
	}

	public int hashCode()
	{
		return relaxedPlan.hashCode() ^ goal.hashCode() ^ 31;
	}

	public void print(PrintStream ps)
	{
		Iterator pit = relaxedPlan.iterator();
		while (pit.hasNext())
		{
			ps.println("(" + pit.next() + ")");
		}
	}

	public void print(PrintWriter pw)
	{
		Iterator pit = relaxedPlan.iterator();
		while (pit.hasNext())
		{
			pw.println("(" + pit.next() + ")");
		}
	}

	@Override
	public String toString()
	{
		return relaxedPlan.toString();
	}

}
