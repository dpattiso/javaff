package javaff.search;

import java.util.Collection;
import java.util.HashSet;

import javaff.data.Fact;

public class UnreachableGoalException extends Exception
{
	private Collection<Fact> unreachables;

	public UnreachableGoalException()
	{
		super();
		this.unreachables = new HashSet<Fact>(0);
	}
	
	public UnreachableGoalException(Collection<Fact> unreachables)
	{
		this.unreachables = unreachables;
	}

	public UnreachableGoalException(Fact unreachable)
	{
		this.unreachables = new HashSet<Fact>();
		this.unreachables.add(unreachable);
	}

	public UnreachableGoalException(Fact unreachable, String message)
	{
		super(message);
		this.unreachables = new HashSet<Fact>();
		this.unreachables.add(unreachable);
	}
	
	public UnreachableGoalException(Collection<Fact> unreachables, String message)
	{
		super(message);
		this.unreachables = unreachables;
	}

	public Collection<Fact> getUnreachables() {
		return unreachables;
	}

	public UnreachableGoalException(Throwable cause)
	{
		super(cause);
	}

	public UnreachableGoalException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
