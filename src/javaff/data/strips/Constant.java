package javaff.data.strips;

import javaff.data.Parameter;
import javaff.data.Type;

/**
 * Constants show properties of both a PDDLObject and Variable, but are still their own type. This class inherits from Parameter just
 * as PDDLObject and Variable do, and also holds a copy of both these representations.
 * 
 * @author David Pattison
 *
 */
public class Constant extends PDDLObject
{
//	protected PDDLObject pddlObject;
//	protected Variable variable;

	public Constant(String n)
	{
		super(n);
//		this.generatePDDLObjectandVariable();
	}

	public Constant(String n, Type t)
	{
		super(n, t);
//		this.generatePDDLObjectandVariable();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Constant && super.equals(obj);
	}
	
//	public PDDLObject getPddlObject()
//	{
//		return pddlObject;
//	}
//	
//	public Variable getVariable()
//	{
//		return variable;
//	}
 
//	@Override
//	public void setName(String n)
//	{
//		super.setName(n);
//		this.generatePDDLObjectandVariable();
//	}
	
//	@Override
//	public void setType(Type t)
//	{
//		super.setType(t);
//		this.generatePDDLObjectandVariable();
//	}
	
//	protected void generatePDDLObjectandVariable()
//	{
//		this.pddlObject = new PDDLObject(this.name, this.type);
//		this.variable = new Variable(this.name, this.type);
//	}

	public int hashCode()
	{
		int hash = 999;
		hash = 31 * hash ^ name.hashCode();
		hash = 31 * hash ^ type.hashCode();
		return hash;
	}
	
	@Override
	public Object clone()
	{		
		return new Constant(new String(this.name), (Type) this.type.clone());
	}
}
