package javaff.scheduling;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.Parameter;
import javaff.data.metric.NamedFunction;
import javaff.data.strips.And;
import javaff.data.strips.Not;
import javaff.data.strips.OperatorName;
import javaff.data.strips.STRIPSInstantAction;
import javaff.planning.State;

/**
 * This class is used during scheduling to achieve preconditions which have not yet been 
 * met by preceding actions effects. As such, they have no preconditions themselves, and 
 * {@link #setCondition(GroundFact)} will just return instantly.
 * 
 * @author David Pattison
 *
 */
public class UnobservedInstantAction extends STRIPSInstantAction
{
	public UnobservedInstantAction()
	{
		super("UnobservedAction");
	}
	
	public UnobservedInstantAction(GroundFact achieves)
	{
		this();
		
		super.setEffect(achieves);
	}
		
	@Override
	public Object clone()
	{
		return new UnobservedInstantAction((GroundFact) this.getEffect().clone());
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode() ^ 17;
	}


	public boolean equals(Object obj)
	{
		if (obj instanceof UnobservedInstantAction == false)
			return false;
		
		return super.equals(obj);
	}
	
	@Override
	public boolean isApplicable(State s)
	{
		return true;
	}
	
	@Override
	public boolean requires(Fact f)
	{
		return false;
	}
	
	 @Override
	public void setParameters(List<Parameter> parameters)
	{
		return;
	}
	 
	 @Override
	public void setName(OperatorName name)
	{
		return;
	}
	
	public void addEffect(GroundFact eff)
	{
		And a = new And(super.getEffect());
		a.add(eff);
		
		super.setEffect(a);
	}
	 
	/**
	 * Stub method, does nothing. Unobserved actions have no preconditions, they only have effects.
	 */
	@Override
	public void setCondition(GroundFact condition)
	{
		return;
	}
}
