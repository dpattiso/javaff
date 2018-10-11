package javaff.landmark;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javaff.data.Fact;
import javaff.data.GroundFact;
import javaff.data.strips.Proposition;
import javaff.data.strips.SingleLiteral;

public interface ILandmarkGenerator 
{
	public void generateLandmarks(java.util.Collection<SingleLiteral> goals);
	
	public void clearLandmarks();

    public Collection<Fact> getLandmarks();
    
    public LandmarkGraph getLandmarkGraph();
    
}
