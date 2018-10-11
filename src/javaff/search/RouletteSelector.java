//
//  BestSuccessorSelector.java
//  JavaFF
//
//  Created by Andrew Coles on Thu Jan 31 2008.
//

package javaff.search;

import javaff.JavaFF;
import javaff.planning.State;
import java.util.Iterator;
import java.util.Set;
import java.math.BigDecimal;

public class RouletteSelector implements SuccessorSelector
{
	private static RouletteSelector rs = null;

	public static RouletteSelector getInstance()
	{
		if (rs == null)
			rs = new RouletteSelector(); // Singleton, as in NullFilter
		return rs;
	}

	public State choose(Set toChooseFrom)
	{
		//Let f_i be the fitness value of individual I and let f^ 
		//be the average population fitness i.e. f^ =(1/N)(Sum(1,N)f_i) 
		if (toChooseFrom.isEmpty())
			return null;
		if (toChooseFrom.size() == 1)
			return (State)(toChooseFrom.iterator().next());

		BigDecimal bestHeuristic; // best heuristic seen
		//BigDecimal total = new BigDecimal(0);
		double total = 0;
		
		double[] runningTotal = new double[toChooseFrom.size()+1];
		runningTotal[0] = 0;
		int index = 1;
		
		Iterator itr = toChooseFrom.iterator();
		
		while (itr.hasNext())
		{
			State cur = (State)itr.next();
			double hval = cur.getHValue().doubleValue();
//			System.out.println("total = "+total+" + "+(1d/hval));
			total = total + (1d / hval);
			runningTotal[index] = runningTotal[index-1] + (1d / hval);
			index++;
		}
//		System.out.print("Rewards array is ");
//		for (int i = 0; i < runningTotal.length; i++)
//			System.out.print(runningTotal[i]+", ");
//		System.out.println();
		
		int selection = 0;
		double r = JavaFF.generator.nextDouble() * total;//.doubleValue();
		for (int i = 0; i < toChooseFrom.size() - 1; i++)
		{
			//System.out.println("r is "+r+", checking between "+runningTotal[i]+" and "+runningTotal[i+1]);
			if (r >= runningTotal[i] && r <= runningTotal[i+1])
			{
				selection = i+1;
				break;
			}
		}
		
		State selectedState = null;
		int count = 0;
		itr = toChooseFrom.iterator();
		while (itr.hasNext())
		{
			State cur = (State)itr.next();
			if (count == selection)
			{
				selectedState = cur;
			}
			else
				count++;
		}
		
		//System.out.println("selectedstate is "+selectedState);
		return selectedState;
	}
}
