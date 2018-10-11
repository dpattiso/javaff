package javaff.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import javaff.data.Type;
import javaff.graph.Path;
import javaff.graph.StandardGraph;

public class TypeGraph
{
	private StandardGraph<Type, DefaultEdge> graph;
	
	public TypeGraph()
	{
		this.graph = new StandardGraph<Type, DefaultEdge>(DefaultEdge.class);
		this.graph.addVertex(Type.rootType);
	}
	
	public void addType(Type t)
	{
//		Vertex<Type> v = this.graph.containsVertex(t);
//		if (v == null)
//		{
//			v = this.graph.addVertex(t);
//		}
		
		this.addType(t, Type.rootType);
	}
	
	public void addType(Type t, Type superType)
	{
		boolean v = this.graph.containsVertex(t);
		if (v == false)
		{
			v = this.graph.addVertex(t);
		}
		
		boolean parent = this.graph.containsVertex(superType);
		if (parent != false)
		{
			DefaultEdge e = this.graph.addEdge(t, superType);
		}
	}
	
	public boolean isOfType(Type t, Type typeWanted)
	{
		List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(this.graph, t, typeWanted);
		return path != null;
	}
	
	public Collection<Type> getTypes()
	{
		return this.graph.vertexSet();
	}
	
	/**
	 * Gets child types of the specified type. Note this does not include the parent.
	 * @param t
	 * @return
	 */
	public Set<Type> getChildTypes(Type t)
	{
		boolean tVert = this.graph.containsVertex(t);
		if (tVert == false)
			throw new NullPointerException("Type "+t+" not found");
		
		HashSet<Type> children = new HashSet<Type>();
		LinkedList<Type> toExpand = new LinkedList<Type>();
		toExpand.addAll(this.graph.getOutgoingVertices(t));
		while (toExpand.isEmpty() == false)
		{
			Type child = toExpand.poll();
			children.add(child);
			
			toExpand.addAll(this.graph.getOutgoingVertices(child));
		}
		
		return children;
	}
	
	public Object clone()
	{
		StandardGraph<Type, DefaultEdge> g = (StandardGraph<Type, DefaultEdge>) this.graph.clone();
		
		TypeGraph t = new TypeGraph();
		t.graph = g;
		
		return t;
	}

	public StandardGraph<Type, DefaultEdge> getGraph()
	{
		return graph;
	}

	public void setGraph(StandardGraph<Type, DefaultEdge> graph)
	{
		this.graph = graph;
	}
}
