package javaff.data;

import java.util.Collection;
import java.util.List;

/**
 * A relaxed plan is one in which the planning constraints have been relaxed in some way. Typically this means to 
 * ignore the delete effects of actions when applying them. This causes the plan to only be valid in the relaxed-plan-space.
 * As a normal Plan returns a set of totally-ordered actions, this interface allows for partially-ordered actions to be returned.
 * Note that this is of course not the same as a {@link PartialOrderPlan}!
 * @author David Pattison
 *
 */
public interface RelaxedPlan extends Plan 
{
	/**
	 * Get the actions which make up this relaxed plan. These will have a context specific to the underlying implementation.
	 * @return
	 */
	public List<Collection<Action>> getRelaxedPlan();


}
