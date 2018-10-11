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

import java.util.List;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;

/**
 * Represents a plan, which is a set of {@link Action}s. The underlying order is 
 * left to the implementing class.
 * @author David Pattison
 *
 */
public interface Plan extends Cloneable, Iterable<Action>
{
	/**
	 * Add the specified action to the plan.
	 * @param a The action to add.
	 * @return True if the addition was successful, false otherwise.
	 */
	public boolean addAction(Action a);
	
	public Plan clone();
	
	/**
	 * Gets the cost of this plan.
	 * @return
	 */
	public BigDecimal getCost();
	
	/**
	 * Print this plan to the specified {@link PrintStream}
	 * @param p
	 */
	public abstract void print(PrintStream p);

	/**
	 * Print this plan to the specified {@link PrintWriter}.
	 * @param p
	 */
	public abstract void print(PrintWriter p);

	/**
	 * Get the actions in this plan.
	 * @return
	 */
	public abstract List<Action> getActions();
	
	/**
	 * Get the number of discrete actions in this plan.
	 * @return
	 */
	public abstract int getActionCount();

	/**
	 * Get the goal which this plan achieves.
	 * @return
	 */
	public Fact getGoal();
	
	/**
	 * Set the goal which this plan achieves.
	 * @param g
	 */
	public void setGoal(Fact g);

	/**
	 * Get the length of the plan, which is implementation-dependent.
	 * @return
	 * @see #getActionCount() For the number of discrete actions in the plan.
	 */
	public int getPlanLength();
}
