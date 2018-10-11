package javaff.landmark;

import javaff.data.Parameter;
import javaff.data.strips.Proposition;

import java.util.ArrayList;
import java.util.List;

public class PDDLObjectMapping
{
	private Proposition proposition;
	private List<Parameter> objects;

	public PDDLObjectMapping(Proposition p)
	{
		this.proposition = p;
		this.objects = new ArrayList<Parameter>();
	}
	
	public PDDLObjectMapping(Proposition p, List<Parameter> objs)
	{
		this.proposition = p;
		this.objects = objs;
	}
	
	public void addObject(Parameter o)
	{
		if (this.objects.contains(o) == false)
			this.objects.add(o);
	}
	
	public boolean removeObject(Parameter o)
	{
		return this.objects.remove(o);
	}
	
	public void clearObjects()
	{
		this.objects.clear();
	}

	public Proposition getProposition()
	{
		return proposition;
	}

	public void setProposition(Proposition proposition)
	{
		this.proposition = proposition;
	}

	public List<Parameter> getObjects()
	{
		return objects;
	}

	public void setObjects(List<Parameter> objects)
	{
		this.objects = objects;
	}
	
}
