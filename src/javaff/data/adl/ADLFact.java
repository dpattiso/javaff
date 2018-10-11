package javaff.data.adl;

import java.util.Collection;
import java.util.Set;

import javaff.data.Fact;
import javaff.data.strips.STRIPSFact;

/**
 * Represents a fact which exists as part of the ADL subset of PDDL.
 * 
 * @author David Pattison
 *
 */
public interface ADLFact extends Fact
{
	/**
	 * Converts this ADL fact into an equivalent STRIPS representation. Note that while the members of the collection returned will be derived
	 * from a STRIPSFact, the facts contained within these may not be. That is, the returned STRIPSFact may just encompass another ADLFact etc.
	 * The method must have a set of static facts passed in, which are true in the initial state and can never be deleted. This 
	 * allows the set of returned facts to be minimised under certain circumstances.
	 * @return A set of N disjoint fact sets representing the ADL fact.
	 * @param staticFacts A set of static facts which are true in the initial state.
	 */
	public Collection<? extends STRIPSFact> toSTRIPS(Set<Fact> staticFacts);
}
