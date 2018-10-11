package javaff.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * Simple directed-edge graph.
 * 
 * @author pattison
 *
 */
public class StandardGraph<V, E> extends DirectedMultigraph<V, E>
{
	public StandardGraph(Class<? extends E> edgeFactory)
	{
		super(edgeFactory);
	}
	
	public StandardGraph(EdgeFactory<V, E> edgeFactory)
	{
		super(edgeFactory);
	}
	
	/**
	 * Constructs a graph from an existing graph -- that is, the existing edges and
	 * vertices are copied into this graph.
	 * 
	 * @param existingGraph
	 */
	protected StandardGraph(StandardGraph<V, E> existingGraph)
	{
		super(existingGraph.getEdgeFactory());
		
		for (V v : super.vertexSet())
			existingGraph.addVertex(v);
		
		for (E e : super.edgeSet())
			existingGraph.addEdge(super.getEdgeSource(e), super.getEdgeTarget(e), e);
		
	}
	
	@Override
	public boolean equals(Object obj)
	{
		StandardGraph<V, E> other = (StandardGraph<V, E>) obj;

		if (super.edgeSet().equals(other.edgeSet()) == false)
			return false;
		if (super.vertexSet().equals(other.vertexSet()) == false)
			return false;
		
		return true;
	}
	
	public E addEdge(V sourceVertex, V targetVertex) 
	{
		if (super.containsVertex(sourceVertex) == false)
			this.addVertex(sourceVertex);

		if (super.containsVertex(targetVertex) == false)
			this.addVertex(targetVertex);
		
		return super.addEdge(sourceVertex, targetVertex);
	}
	
	public boolean addEdge(V sourceVertex, V targetVertex, E e) 
	{
//		int source = sourceVertex.hashCode();
//		for (V v : super.vertexSet())
//		{
//			int dest = v.hashCode();
//			dest = v.hashCode();
//		}
		
		if (super.containsVertex(sourceVertex) == false)
			this.addVertex(sourceVertex);

		if (super.containsVertex(targetVertex) == false)
			this.addVertex(targetVertex);
		
		return super.addEdge(sourceVertex, targetVertex, e);
	}
	
	public Collection<V> getOutgoingVertices(V v)
	{
		if (this.containsVertex(v) == false)
			throw new NullPointerException("Vertex "+v+" not found");
		
		HashSet<V> set = new HashSet<V>();
		for (E e : super.outgoingEdgesOf(v))
		{
			set.add(super.getEdgeTarget(e));
		}
		
		return set;
	}	
	
	public Collection<V> getIncomingVertices(V v)
	{
		if (this.containsVertex(v) == false)
			throw new NullPointerException("Vertex "+v+" not found");
		
		HashSet<V> set = new HashSet<V>();
		for (E e : super.incomingEdgesOf(v))
		{
			set.add(super.getEdgeSource(e));
		}
		
		return set;
	}
	
	
	public Collection<V> getConnectedVertices(V v)
	{
		if (this.containsVertex(v) == false)
			throw new NullPointerException("Vertex "+v+" not found");
		
		HashSet<V> set = new HashSet<V>();
		set.addAll(this.getOutgoingVertices(v));
		set.addAll(this.getIncomingVertices(v));
		
		return set;
	}
	
	/* (non-Javadoc)
	 * @see javaff.graph.IGraph#clone()
	 */
	public Object clone()
	{
		StandardGraph<V, E> g = new StandardGraph<V, E>(super.getEdgeFactory());
		for (V v : super.vertexSet())
			g.addVertex(v);
		
		for (E e : super.edgeSet())
			g.addEdge(super.getEdgeSource(e), super.getEdgeTarget(e), e);
		
		return g;
	}
	
	public void addAllVertices(Collection<V> col) throws NullPointerException
	{
		for (V v : col)
		{
			super.addVertex(v);
		}
	}
	
	/**
	 * Simple check which permits edges to exist. Default implementation only allows
	 * 1 edge between nodes.
	 * @param start
	 * @param end
	 */
	public boolean isEdgeAllowed(V start, V end)
	{
		return super.containsEdge(start, end) && super.isAllowingMultipleEdges();
	}
	
	/**
	 * Generates a dot-parseable graph.
	 * @param graph
	 * @param dotFile
	 */
    public void generateDotGraph(File dotFile)
    {
    	FileWriter writer;
    	BufferedWriter bufWriter;
    	try
    	{
	    	writer = new FileWriter(dotFile);
	    	bufWriter =  new BufferedWriter(writer);
	    	
	    	bufWriter.write("digraph Tree {\n\tnode [shape=circle, fontsize=14, color=black, fillcolor=white, fontcolor=black];\n\t edge [style=solid, color=black];\n");
	    	
	    	System.out.println("vertex size is "+this.vertexSet().size());
	    	System.out.println("edge size is "+this.edgeSet().size());
	    	
	    	int counter = 0;
	    	for (V v : this.vertexSet())
	    	{
	    		String vert = v.toString().replace(' ', '_');

    			bufWriter.write(counter++ +" [label=\""+vert+"\"];\n");
	    	}
	    	
	    	for (E e : this.edgeSet())
	    	{
//    			bufWriter.write(counter++ +" [label=\""+e+"\"];\n");
	    		//System.out.println("Vertex: "+p);
//	    		Iterator<RelationshipEdge> outEdgesIter = this.graph.outgoingEdgesOf(p).iterator();
//	    		while (outEdgesIter.hasNext())
//	    		{
//	    			RelationshipEdge e = outEdgesIter.next();
	    			//System.out.println("Outgoing edge: "+e);

	    		String startVert = this.getEdgeSource(e).toString().replace(' ', '_');
	    		String endVert = this.getEdgeTarget(e).toString().replace(' ', '_');
	    		startVert = startVert.replace('-', '_');
	    		endVert = endVert.replace('-', '_');	    
	    		startVert = startVert.replace('#', '_');
	    		endVert = endVert.replace('#', '_');	    				
	    		bufWriter.write(startVert+" -> "+endVert+";\n");
//	    		}
	    	}
	    	
	    	bufWriter.write("}\n");
	    	
    		//writer.close();
    		bufWriter.close();
    		
    		System.out.println("writing file "+dotFile.getAbsolutePath());
//    		Process p = Runtime.getRuntime().exec("dot -Tpng \'"+dotFile.getAbsolutePath()+"_dot\' > \'test.png/'");
//    		p.waitFor();
    	}
    	catch (IOException ioe)
    	{
    		System.out.println("Cannot create file: "+ioe.getMessage());
    		ioe.printStackTrace();
    	}
//		catch (InterruptedException e)
//		{
//    		System.out.println("Cannot create file: "+e.getMessage());
//			e.printStackTrace();
//		}
    	finally
    	{
    	}
    }
    
    @Override
    public String toString()
    {
    	return "Nodes: "+this.vertexSet().size()+", Edges: "+this.edgeSet().size();
    }

	public void clear()
	{
		super.removeAllEdges(super.edgeSet());
		super.removeAllVertices(super.vertexSet());
	}
}
