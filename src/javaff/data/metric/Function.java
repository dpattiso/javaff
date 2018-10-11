/************************************************************************
 * Strathclyde Planning Group,
 * Department of Computer and Information Sciences,
 * University of Strathclyde, Glasgow, UK
 * http://planning.cis.strath.ac.uk/
 * 
 * Copyright 2007, Keith Halsey
 * Copyright 2008, Andrew Coles and Amanda Smith
 * Copyright 2015, David Pattison
 *
 * This file is part of JavaFF.
 * 
 * JavaFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * JavaFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JavaFF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ************************************************************************/

package javaff.data.metric;

import javaff.planning.MetricState;
import java.math.BigDecimal;
import java.util.Map;
import javaff.scheduling.MatrixSTN;

public interface Function
{
	public BigDecimal getValue(MetricState s);

	public boolean isStatic();

	public String toStringTyped();

	public Function ground(Map varMap);

	public Function staticify(Map fValues);

	public boolean effectedBy(ResourceOperator ro);

	public Function replace(ResourceOperator ro); // replaces the resource for
													// the change

	public Function makeOnlyDurationDependent(MetricState s);

	public BigDecimal getMaxValue(MatrixSTN stn);

	public BigDecimal getMinValue(MatrixSTN stn);
}
