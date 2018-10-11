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

package javaff.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javaff.data.metric.NamedFunction;
import javaff.data.strips.Not;
import javaff.data.strips.OperatorName;
import javaff.planning.State;

public class TimeStampedAction extends Action
{
	private Action action;
	private BigDecimal time;
//	private BigDecimal majorTime; //as in, "ground control to..."
	private BigDecimal duration;

	public TimeStampedAction(Action a, BigDecimal t, BigDecimal d)
	{
		setAction(a);
		setTime(t);
//		setMajorTime(getTime().setScale(0, RoundingMode.FLOOR));
		setDuration(d);
	}

	public String toString()
	{
		String str = getTime() + ": (" + getAction() + ")";
		if (getDuration() != null)
			str += " [" + getDuration() + "]";
		return str;
	}

	public int compareTo(Action o)
	{
		if (o instanceof TimeStampedAction)
		{
			TimeStampedAction that = (TimeStampedAction) o;
			if (this.getTime().compareTo(that.getTime()) < 0)
			{
				return -1;
			}
			else if (this.getTime().compareTo(that.getTime()) > 0)
			{
				return +1;
			}
			else
			{
				//actions have same timestamp, so compare action hashcodes
				return Integer.compare(this.getAction().hashCode(), that.getAction().hashCode());
			}
		}
		else
		{
			return super.compareTo(o);
		}
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof TimeStampedAction)
		{
			TimeStampedAction other = (TimeStampedAction) obj;
			if (this.getCost().equals(other.getCost()) == false || this.getTime().equals(other.getTime()) == false)
				return false;

			Action a = other.getAction();
			return this.getAction().equals(a);	
		}	
			
		return false;
	}

	public boolean isApplicable(State s)
	{
		return getAction().isApplicable(s);
	}

	public void apply(State s)
	{
		getAction().apply(s);
	}

	public Set<Fact> getPreconditions()
	{
		return getAction().getPreconditions();
	}

	public Set<Fact> getAddPropositions()
	{
		return getAction().getAddPropositions();
	}

	public Set<Not> getDeletePropositions()
	{
		return getAction().getDeletePropositions();
	}

	public Set<NamedFunction> getComparators()
	{
		return getAction().getComparators();
	}

	public Set getOperators()
	{
		return getAction().getOperators();
	}

	public void staticify(Map fValues)
	{
		getAction().staticify(fValues);
	}

	public boolean deletes(Fact f)
	{
		return getAction().deletes(f);
	}

	public boolean adds(Fact f)
	{
		return getAction().adds(f);
	}

	public boolean requires(Fact f)
	{
		return getAction().requires(f);
	}

	public int hashCode()
	{
		return this.getAction().hashCode() ^ this.getTime().hashCode() ^ this.getDuration().hashCode();
	}

	public Object clone()
	{
		TimeStampedAction clone = new TimeStampedAction((Action) this.action.clone(), this.time, this.duration);
//		clone.majorTime = this.majorTime;
		
		return clone;
	}

	/**
	 * Returns the major time this action is scheduled for. The major time is the
	 * number preceding the floating point, i.e. 2.0001 has major time 2.
	 * @return
	 */
	public BigDecimal getMajorTime()
	{
//		return majorTime;
		return this.getTime().setScale(1, RoundingMode.DOWN);
	}

	/**
	 * Get the action which this TSA encapsulates.
	 * @return
	 */
	public Action getAction()
	{
		return action;
	}

	/**
	 * Set the action which this TSA encapsulates.
	 * @return
	 */
	public void setAction(Action action)
	{
		this.action = action;
	}

	/**
	 * Returns the exact time that this action is scheduled for.
	 * @return
	 */
	public BigDecimal getTime()
	{
		return time;
	}

	/**
	 * Sets the exact time this action starts. The major time of this action is also set as the leading integer
	 * of this number, i.e. 2.00043 has major time 2.
	 * @param time
	 */
	public void setTime(BigDecimal time)
	{
		this.time = time;
//		this.setMajorTime(this.time.setScale(0, RoundingMode.FLOOR));
	}

//	protected void setMajorTime(BigDecimal majorTime)
//	{
//		this.majorTime = majorTime;
//	}

	public BigDecimal getDuration()
	{
		return duration;
	}

	public void setDuration(BigDecimal duration)
	{
		this.duration = duration;
	}

	public List<Parameter> getParameters()
	{
		return action.getParameters();
	}

	public void setParameters(List<Parameter> parameters)
	{
		action.setParameters(parameters);
	}

	public OperatorName getName()
	{
		return action.getName();
	}

	public void setName(OperatorName name)
	{
		action.setName(name);
	}
}
