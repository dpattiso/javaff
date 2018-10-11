//
//  BestSuccessorSelector.java
//  JavaFF
//
//  Created by Andrew Coles on Thu Jan 31 2008.
//

package javaff.search;

import javaff.planning.State;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.math.BigDecimal;

public class BestSuccessorSelector implements SuccessorSelector
{

	private static BestSuccessorSelector ss = null;

	public static BestSuccessorSelector getInstance()
	{
		if (ss == null)
			ss = new BestSuccessorSelector(); // Singleton, as in NullFilter
		return ss;
	}

	public State choose(Set toChooseFrom)
	{

		if (toChooseFrom.isEmpty())
			return null;

		HashSet jointBest = new HashSet(); // set to store the joint-best
											// states
		BigDecimal bestHeuristic; // best heuristic seen

		Iterator itr = toChooseFrom.iterator();
		State curr = (State) itr.next();
		jointBest.add(curr); // first successor is the best seen so far
		bestHeuristic = curr.getHValue(); // and has the best heuristic

		while (itr.hasNext())
		{
			curr = (State) itr.next();
			if (curr.getHValue().compareTo(bestHeuristic) < 0)
			{ // if it has a better heuristic value
				jointBest = new HashSet();
				jointBest.add(curr); // it is the joint best, with only
										// itself
				bestHeuristic = curr.getHValue();
			} else if (curr.getHValue().compareTo(bestHeuristic) == 0)
			{ // if it has an equally good h
				jointBest.add(curr); // then it is joint best with the others
			}
		}
		int nextChosen = javaff.JavaFF.generator.nextInt(jointBest.size()); 

		Iterator skipThrough = jointBest.iterator();
		while (nextChosen > 0)
		{ // skip over the appropriate number of items
			skipThrough.next();
			--nextChosen;
		}

		return ((State) (skipThrough.next())); // return tmstate from set

	};

};
