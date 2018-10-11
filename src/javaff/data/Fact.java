package javaff.data;

import java.util.Set;

import javaff.data.strips.PredicateSymbol;


/**
 * This is a PDDL fact, which can be a conjunction, single literal, OR, Imply, function etc.
 * @author David Pattison
 *
 */
public interface Fact extends PDDLPrintable, Cloneable, Comparable<Fact>
{
	/**
	 * Is this fact static.
	 * @return
	 */
	public boolean isStatic(); // returns whether this condition is static
	
	/**
	 * Is this fact static.
	 * @return
	 */
	public void setStatic(boolean value); // returns whether this condition is static
	
	/**
	 * Clone this object. All objects should offer deep-copy functionality.
	 * @return
	 */
	public Object clone();

	/**
	 * Returns all literals held in this fact. It is up to the implementation to decide
	 * quite what this means...
	 * @return
	 */
	public Set<? extends Fact> getFacts();
		
	
}
