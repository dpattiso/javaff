package javaff.graph;
//package javaff.graph;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//
//import javaff.data.strips.Proposition;
//
//public class DepthFirstIterator
//{
//	private StandardGraph graph;
//	
//	public DepthFirstIterator(StandardGraph g)
//	{
//		this.graph = g;
//	}
//	
//	public ArrayList<ArrayList<Proposition>> findPaths() 
//	{
//		ArrayList<ArrayList<Proposition>> linearPaths = new ArrayList<ArrayList<Proposition>>();
//		ArrayList<Proposition> currentList = new ArrayList<Proposition>();
//		
//		for (Vertex v : graph.vertices()) //get root nodes of landmark graph
//		{
//			if (graph.getInDegree(v) == 0)
//			{
//				linearPaths.add(this.findSinglePath(v));
//			}
//		}
//		
//		
//		return linearPaths;
//	}
//	
//	public ArrayList<ArrayList<Proposition>> findSinglePath(Vertex start)
//	{
//		return this.findSinglePath(start, new ArrayList<Proposition>());
//	}
//	
//	public ArrayList<ArrayList<Proposition>> findSinglePath(Vertex start, ArrayList<Proposition> currentList)
//	{
//		ArrayList<Proposition> copy = new ArrayList<Proposition>(currentList);
//		copy.add(start.getProposition());
//	}
//}
//
//
//
//
//
//
//
//
//

