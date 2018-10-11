package javaff.planning;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.Parameter;
import javaff.data.metric.NamedFunction;
import javaff.data.strips.InstantAction;
import javaff.data.strips.Not;
import javaff.data.strips.OperatorName;
import javaff.data.strips.STRIPSInstantAction;


/**
 * Wrapper class for comparing helpful actions. In the classic FF sense, helpful actions are those
 * which are applicable at layer 0 of the RPG and add at-least one goal literal at layer 1. Note that these
 * are not the "final" goals, but rather intermediate goals which appear during relaxed plan extraction in the RPG.
 * 
 * This wrapper allows helpful actions to be sorted, based upon how many goal literals they achieve. If another
 * helpful action achieves the same number of goals, they are sorted alphabetically. If they both have the same
 * result of calling toString(), the world implodes.
 * 
 * @author David Pattison
 *
 */

public class HelpfulAction extends STRIPSInstantAction
{
	private int goalsAchieved;
	private InstantAction action;
	private int hash; //forced to override hash caching behaviour because this is a 1:1 wrapper.
	
	public HelpfulAction(InstantAction a, int goalsAchieved)
	{
		super();
		this.setHelpfulAction(a);
		this.goalsAchieved = goalsAchieved;
		
		this.updateHash();
	}

	public int getGoalsAchieved()
	{
		return goalsAchieved;
	}

	public void setGoalsAchieved(int goalsAchieved)
	{
		this.goalsAchieved = goalsAchieved;
	}
	
	public String toString()
	{
		return getHelpfulAction().toString();
	}

	public boolean isApplicable(State s)
	{
		return getHelpfulAction().isApplicable(s);
	}

	public void apply(State s)
	{
		getHelpfulAction().apply(s);
	}

	public Set<Fact> getPreconditions()
	{
		return getHelpfulAction().getPreconditions();
	}

	public Set<Fact> getAddPropositions()
	{
		return getHelpfulAction().getAddPropositions();
	}

	public Set<Not> getDeletePropositions()
	{
		return getHelpfulAction().getDeletePropositions();
	}

	public Set<NamedFunction> getComparators()
	{
		return getHelpfulAction().getComparators();
	}

	public Set getOperators()
	{
		return getHelpfulAction().getOperators();
	}

	public void staticify(Map fValues)
	{
		getHelpfulAction().staticify(fValues);
	}

	public boolean deletes(Fact f)
	{
		return getHelpfulAction().deletes(f);
	}

	public boolean adds(Fact f)
	{
		return getHelpfulAction().adds(f);
	}

	public boolean requires(Fact f)
	{
		return getHelpfulAction().requires(f);
	}

	public boolean equals(Object obj)
	{
		return getHelpfulAction().equals(obj) && this.getGoalsAchieved() == ((HelpfulAction)obj).getGoalsAchieved();
	}

	public int hashCode()
	{
		return this.hash;
	}
	
	@Override
	protected int updateHash()
	{
		int h = 31;
		if (this.getHelpfulAction() != null)
			h ^= super.hashCode();
		
		return h;
	}

	public Object clone()
	{
		HelpfulAction clone = new HelpfulAction((InstantAction) this.getHelpfulAction().clone(), this.getGoalsAchieved());
		return clone;
	}

	public List<Parameter> getParameters()
	{
		return getHelpfulAction().getParameters();
	}

	public void setParameters(List<Parameter> parameters)
	{
		getHelpfulAction().setParameters(parameters);
	}

	public OperatorName getName()
	{
		return getHelpfulAction().getName();
	}

	public void setName(OperatorName name)
	{
		getHelpfulAction().setName(name);
	}
	
	/**
	 * Compares 2 helpful actions. If this helpful action achieves a higher number of goals than the other, -1 is
	 * returned, or +1 if the opposite is true. If both achieve the same number of goals, the result is
	 * dictated by {@link String#compareTo(String)}, where the strings compared are the result of calling toString() on 
	 * both actions. In practice, it is assumed that both actions will never have the same result of calling
	 * toString(), i.e. that 0 is returned. 
	 * @see #getGoalsAchieved()
	 * @see String#compareTo(String)
	 */
	@Override
	public int compareTo(Action o)
	{
		if (o instanceof HelpfulAction)
		{
			if (this.getGoalsAchieved() > ((HelpfulAction) o).getGoalsAchieved())
				return -1;
			else if (this.getGoalsAchieved() == ((HelpfulAction) o).getGoalsAchieved())
			{
				int res = this.toString().compareTo(o.toString());
				
				//in an ADL-world, we may have a single ADL action which was decompiled
				//into multiple STRIPS actions -- which would probably have the same action signature
				//Therefore, if 0 is returned (action signatures are the same) we have to do a full comparison
				//of the entire action structure. The easiest way to do this is just to generate the hashCode() for
				//each action and compare these instead. This is a relatively slow process so we only do it as-needed,
				//rather than being the default comparison method.
				if (res == 0)
				{
					Integer aHash = this.hashCode();
					Integer bHash = o.hashCode();
					int fullRes = aHash.compareTo(bHash);
					
					return fullRes;
				}
				else
					return res;
			}
			else 
				return +1;
		}
		else
		{
			return super.compareTo(o);
		}
	}

	protected InstantAction getHelpfulAction()
	{
		return action;
	}

	protected void setHelpfulAction(InstantAction action)
	{
		this.action = action;
	}

	public BigDecimal getCost()
	{
		return action.getCost();
	}

	public void setCost(BigDecimal cost)
	{
		action.setCost(cost);
	}

	public void setCondition(GroundFact condition)
	{
		action.setCondition(condition);
	}

	public GroundFact getCondition()
	{
		return action.getCondition();
	}

	public void setEffect(GroundFact effect)
	{
		action.setEffect(effect);
	}

//	public GroundFact getEffect()
//	{
//		return action.getEffect();
//	}
}
