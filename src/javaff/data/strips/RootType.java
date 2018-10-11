/*******************************************************************************
 * Strathclyde Planning Group, Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey Copyright 2008, Andrew Coles and Amanda Smith
 * 
 * (Questions/bug reports now to be sent to Andrew Coles)
 * 
 * This file is part of JavaFF.
 * 
 * JavaFF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * JavaFF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * JavaFF. If not, see <http://www.gnu.org/licenses/>.
 * 
 ******************************************************************************/

// A Singleton to represent the default "object" type for the root of the type
// hierarchy
package javaff.data.strips;

import javaff.data.Type;

/**
 * A Root type is the object from which all types derive. Appropriately this is called "object", and behaves
 * in the same way as Object in an OO hierarchy.
 * @author David Pattison
 *
 */
public class RootType extends SimpleType
{
	private static RootType r;

	private RootType()
	{
		name = "object";
		superType = null;
	}

	public static RootType getInstance()
	{
		if (r == null)
			r = new RootType();
		return r;
	}

	public boolean isOfType(Type t)
	{
		return t.equals(this);
	}

	public void setSuperType(Type pSuperType)
	{
		return;
	}
}
