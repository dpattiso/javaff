package javaff.data.strips;

import java.util.List;

import javaff.data.Fact;
import javaff.data.Fact;
import javaff.data.PDDLPrintable;
import javaff.data.Parameter;

//TODO move this functionality up to STRIPSFact level, same with ADLFact etc
public interface SingleLiteral extends STRIPSFact
{
	public PredicateSymbol getPredicateSymbol();

	public void setPredicateSymbol(PredicateSymbol n);
	
	public List<Parameter> getParameters();
	
	public void setParameters(List<Parameter> params);
	
	public Object clone();
}
