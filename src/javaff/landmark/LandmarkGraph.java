package javaff.landmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.EdgeFactory;

import javaff.data.Action;
import javaff.data.Fact;
import javaff.data.strips.Proposition;
import javaff.graph.ActionEdge;
import javaff.graph.StandardGraph;
import javaff.graph.ActionEdge.ActionEdgeType;

public class LandmarkGraph extends StandardGraph<Fact, ActionEdge>
{
//	public LandmarkGraph(EdgeFactory<Fact, ActionEdge> edgeFactory)
//	{
//		super(edgeFactory);
//	}
	
	public LandmarkGraph()
	{
		super(ActionEdge.class);
	}
	
	
	@Override
	public LandmarkGraph clone()
	{
		LandmarkGraph g = new LandmarkGraph();
		
		for (Fact v : super.vertexSet())
			g.addVertex(v);
		
		for (ActionEdge e : super.edgeSet())
			g.addEdge(super.getEdgeSource(e), super.getEdgeTarget(e), e);
		
		return g;
	}
	

	/**
	 * Generates a dot-parseable graph.
	 * @param graph
	 * @param dotFile
	 */
    public void generateDotGraph(File dotFile)
    {
    	super.generateDotGraph(dotFile);
    	
//    	FileWriter writer;
//    	BufferedWriter bufWriter;
//    	try
//    	{
//	    	writer = new FileWriter(dotFile);
//	    	bufWriter =  new BufferedWriter(writer);
//	    	
//	    	bufWriter.write("digraph Tree {\n\tnode [shape=circle, fontsize=14, color=black, fillcolor=white, fontcolor=black];\n\t edge [style=solid, color=black];\n");
//	    	
//	    	System.out.println("vertex size is "+this.vertices().size());
//	    	System.out.println("edge size is "+this.edges().size());
////	    	
////	    	int counter = 0;
////	    	for (Object v : graph.vertices())
////	    	{
////	    		String vert = ((Vertex)v).getObject().toString().replace(' ', '_');
////
////    			bufWriter.write(counter++ +" [label=\""+vert+"\"];\n");
////	    	}
//	    	
//	    	for (Edge<Fact,E> e : super.edges)
//	    	{
////    			bufWriter.write(counter++ +" [label=\""+e+"\"];\n");
//	    		//System.out.println("Vertex: "+p);
////	    		Iterator<RelationshipEdge> outEdgesIter = this.graph.outgoingEdgesOf(p).iterator();
////	    		while (outEdgesIter.hasNext())
////	    		{
////	    			RelationshipEdge e = outEdgesIter.next();
//	    			//System.out.println("Outgoing edge: "+e);
//
//	    		String startVert = e.getStart().getObject().toString().replace(' ', '_');
//	    		String endVert = e.getEnd().getObject().toString().replace(' ', '_');
//	    		startVert = startVert.replace('-', '_');
//	    		endVert = endVert.replace('-', '_');
//	    		startVert = startVert.replace('#', '_');
//	    		endVert = endVert.replace('#', '_');	    		
//	    		bufWriter.write(startVert+" -> "+endVert+";\n");
////	    		}
//	    	}
//	    	
//	    	bufWriter.write("}\n");
//	    	
//    		//writer.close();
//    		bufWriter.close();
//    		
//    		System.out.println("writing file "+dotFile.getAbsolutePath());
//    		//Process p = Runtime.getRuntime().exec("dot -Tpng \'"+dotFile.getAbsolutePath()+"_dot\' > \'./test.png/'");
//    		//p.waitFor();
//    	}
//    	catch (IOException ioe)
//    	{
//    		System.out.println("Cannot create file: "+ioe.getMessage());
//    		ioe.printStackTrace();
//    	}
//		catch (InterruptedException e)
//		{
//    		System.out.println("Cannot create file: "+e.getMessage());
//			e.printStackTrace();
//		}
//    	finally
//    	{
//    	}
    }
}
