package javaff.graph;

import org.jgrapht.graph.DefaultEdge;

import javaff.data.Action;
import javaff.data.Fact;

public class ActionEdge extends DefaultEdge
{
	public enum ActionEdgeType
	{
		None,
		Natural,
		Necessary,
		GreedyNecessary,
		Unknown,
	}

	private ActionEdgeType edgeType;
	private Action action;

	public ActionEdge(Action a, ActionEdgeType type)
	{
		super();
//		this.action = a;
		this.edgeType = type;
	}
	
	public ActionEdge(Action a)
	{
		super();
//		this.action = a;
		this.edgeType = ActionEdgeType.None;
	}

	public Action getAction()
	{
		return this.action;
	}

	public void setAction(Action data)
	{
		this.action = data;
	}

	public ActionEdgeType getEdgeType()
	{
		return edgeType;
	}

	public void setEdgeType(ActionEdgeType edgeType)
	{
		this.edgeType = edgeType;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + ": "+this.action.toString();
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (super.equals(other) == false)
			return false;
		
		return this.edgeType.equals(((ActionEdge)other).edgeType);
	}
}
