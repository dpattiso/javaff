package javaff.data.adl;

import java.util.Map;
import java.util.Set;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.Literal;
import javaff.data.UngroundProblem;
import javaff.data.strips.PDDLObject;
import javaff.data.strips.Variable;

public interface Quantifier extends ADLFact
{
	public Set<PDDLObject> getQuantifiedObjects();
	
	public void setQuantifiedObjects(Set<PDDLObject> quantifiedObjects);

	public Fact getCondition();
	
	public void setCondition(Fact c);
	
	public Variable getVariable();
	
	public void setVariable(Variable v);
	
	public GroundFact ground(Map<Variable, PDDLObject> map);

	public Object clone();
}
