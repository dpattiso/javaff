package javaff.graph;

import java.util.ArrayList;
import java.util.List;

public class Path<T>
{
	private List<T> vertices;
	private double distance;

	public Path()
	{
		this.vertices = new ArrayList<T>();
		this.distance = 0;
	}
	
	public Path(List<T> verts, double distance)
	{
		this.vertices = new ArrayList<T>(verts);
		this.distance = distance;
	}
	
	/**
	 * Returns the length of this path. Note this is NOT the distance of the path.
	 * @return
	 */
	public int length()
	{
		return this.vertices.size();
	}

	public List<T> getVertices()
	{
		return vertices;
	}
	
	public boolean isEmpty()
	{
		return this.vertices.size() == 0;
	}

	public double getDistance()
	{
		return distance;
	}
	
	public T getStart()
	{
		if (distance > 0)
			return this.vertices.get(0);
		else
			return null;
	}
	
	public T getEnd()
	{
		if (this.vertices.size() > 0)
			return this.vertices.get(this.vertices.size() - 1);
		else
			return null;
	}
	
	public T getVertex(int index)
	{
		return this.vertices.get(index);
	}
	
	@Override
	public String toString()
	{
		return "Path: distance = "+distance+", waypoints are "+this.vertices.toString();
	}

	public void append(Path other)
	{
		this.distance += other.distance;
		this.vertices.addAll(other.vertices);
	}
}
