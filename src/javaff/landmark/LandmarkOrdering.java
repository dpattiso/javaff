package javaff.landmark;

import java.util.*;
import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.Fact;
import javaff.data.strips.Proposition;
import javaff.graph.ActionEdge.ActionEdgeType;

/**
 * Represents an ordering from one grounded proposition to another via a certain action
 * 
 * @author David Pattison
 *
 */
public class LandmarkOrdering
{
	private ArrayList<Action> actions;
	private Fact previous, successor;
	private ActionEdgeType ordering;
	
	
	public LandmarkOrdering(Fact prev, Action action, Fact succ)
	{
		//if (from.name.equals(to.name))
		//	throw new IllegalArgumentException("Start and end actions are the same");
		
		this.actions = new ArrayList<Action>();
		this.previous = prev;
		this.successor = succ;
		this.ordering = ActionEdgeType.Unknown;
	}
	
	public LandmarkOrdering(Action action, Fact succ)
	{
		this(null, action, succ);
	}
	
	public LandmarkOrdering(Fact succ)
	{
		this(null, null, succ);
	}
	
	public String toString()
	{
		return "Prev: "+previous + "; Action: "+actions.toString()+"; Succ: "+successor.toString();
	}


	public boolean addAction(Action a)
	{
		return this.actions.add(a);
	}


	public ArrayList<Action> getActions()
	{
		return this.actions;
	}


	public Fact getPrevious()
	{
		return previous;
	}


	public void setPrevious(Fact previous)
	{
		this.previous = previous;
	}


	public Fact getSuccessor()
	{
		return successor;
	}


	public void setSuccessor(Fact successor)
	{
		this.successor = successor;
	}

	public ActionEdgeType getOrdering()
	{
		return ordering;
	}

	public void setOrdering(ActionEdgeType ordering)
	{
		this.ordering = ordering;
	}


}
