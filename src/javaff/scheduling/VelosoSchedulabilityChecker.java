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

package javaff.scheduling;

import javaff.data.strips.InstantAction;
import javaff.data.temporal.SplitInstantAction;
import javaff.data.temporal.StartInstantAction;
import javaff.data.temporal.EndInstantAction;
import javaff.planning.TemporalMetricState;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.math.BigDecimal;

public class VelosoSchedulabilityChecker implements SchedulabilityChecker,
		Cloneable
{
	protected Set entries;
	protected boolean allGood = true;

	public VelosoSchedulabilityChecker()
	{
		entries = new HashSet();
	}

	public Object clone()
	{
		VelosoSchedulabilityChecker v = new VelosoSchedulabilityChecker();
		Iterator eit = entries.iterator();
		while (eit.hasNext())
		{
			EnvelopeEntry ee = (EnvelopeEntry) eit.next();
			v.entries.add(ee.clone());
		}
		v.allGood = allGood;
		return v;
	}

	public boolean addAction(InstantAction a, TemporalMetricState s)
	{
		// if end, close envelope
		if (a instanceof EndInstantAction)
		{
			Iterator eit2 = entries.iterator();
			Set over = new HashSet();
			while (eit2.hasNext())
			{
				EnvelopeEntry e = (EnvelopeEntry) eit2.next();
				if (e.end.equals(a))
					over.add(e);
			}
			entries.removeAll(over);
		}

		// check each envelope to see if it must go in
		Iterator eit = entries.iterator();
		while (allGood && eit.hasNext())
		{
			EnvelopeEntry e = (EnvelopeEntry) eit.next();
			add(e, a, s);
			// if so, check for consistency
			allGood = e.check();
		}

		if (!allGood)
			return false;

		// if start, create a(ny) new envelope,
		if (a instanceof StartInstantAction)
		{
			HashSet es = new HashSet();
			Iterator eit2 = entries.iterator();
			while (eit2.hasNext() && allGood)
			{
				EnvelopeEntry e = (EnvelopeEntry) eit2.next();
				if (checkOrder(a, e.end))
				{
					StartInstantAction sa = ((StartInstantAction) a);
					EnvelopeEntry ne = new EnvelopeEntry(e.start, sa
							.getSibling());
					ne.maxEnv = sa.parent.getMaxDuration(s).add(e.maxEnv);
					ne.minEnv = sa.parent.getMinDuration(s).add(e.minEnv);
					ne.constraints.addAll(TemporalConstraint.getBounds(sa, sa
							.getSibling(), sa.parent.getMaxDuration(s),
							sa.parent.getMinDuration(s)));
					ne.constraints.addAll(e.constraints);
					ne.followsStart.addAll(e.followsStart);

					Iterator fit = e.followsStart.iterator();
					while (fit.hasNext())
					{
						InstantAction ia = (InstantAction) fit.next();
						if (checkOrder(ia, ne.end))
							ne.addPreceder(ia, s);
					}

					ne.constraints.add(TemporalConstraint.getConstraint(sa,
							e.end));

					es.add(ne);

					allGood = ne.check();

				}
			}
			entries.addAll(es);
			entries.add(new EnvelopeEntry(((StartInstantAction) a), s));

		}
		return allGood;

	}

	private void add(EnvelopeEntry e, InstantAction a, TemporalMetricState s)
	{
		if (checkOrder(e.start, a))
			e.addFollower(a, s);
		if (checkOrder(a, e.end))
			e.addPreceder(a, s);

		Set fs = new HashSet();
		Iterator fit = e.followsStart.iterator();
		while (fit.hasNext())
		{
			InstantAction f = (InstantAction) fit.next();
			if (checkOrder(f, a))
				fs.add(f);
		}
		fit = fs.iterator();
		while (fit.hasNext())
		{
			InstantAction f = (InstantAction) fit.next();
			e.addFollowerOrder(f, a, s);
		}

		/*
		 * Set ps = new HashSet(); Iterator pit = e.precedesEnd.iterator();
		 * while (pit.hasNext()) { InstantAction p = (InstantAction) pit.next();
		 * if (checkOrder(a,p)) ps.add(p); } pit = ps.iterator(); while
		 * (pit.hasNext()) { InstantAction p = (InstantAction) pit.next();
		 * e.addPrecederOrder(a,p,s); }
		 */

	}

	private boolean checkOrder(InstantAction a, InstantAction b)
	{
		if (a.equals(b))
			return false;
		else if (a instanceof SplitInstantAction
				&& b instanceof SplitInstantAction)
		{
			if (((SplitInstantAction) a).parent
					.equals(((SplitInstantAction) b).parent))
				return false;
		}

		Set addA = a.getAddPropositions();
		addA.retainAll(b.getPreconditions());

		if (!addA.isEmpty())
			return true;

		Set condA = a.getPreconditions();
		condA.retainAll(b.getDeletePropositions());
		if (!condA.isEmpty())
			return true;

		Set delA = a.getDeletePropositions();
		delA.retainAll(b.getAddPropositions());
		if (!delA.isEmpty())
			return true;

		return false;
	}

	private class EnvelopeEntry implements Cloneable
	{
		public InstantAction start, end;
		public List followsStart, precedesEnd;
		public Set constraints;
		public SimpleTemporalNetwork stn;
		BigDecimal maxEnv, minEnv;

		public EnvelopeEntry(StartInstantAction s, TemporalMetricState tms)
		{
			this(s, s.getSibling());
			maxEnv = s.parent.getMaxDuration(tms);
			minEnv = s.parent.getMinDuration(tms);
			constraints.addAll(TemporalConstraint.getBounds(s, s.getSibling(),
					maxEnv, minEnv));
		}

		public EnvelopeEntry(InstantAction s, InstantAction e)
		{
			start = s;
			end = e;
			followsStart = new ArrayList();
			precedesEnd = new ArrayList();
			constraints = new HashSet();
		}

		public void addFollower(InstantAction a, TemporalMetricState s)
		{
			addFollowerOrder(start, a, s);
		}

		public void addFollowerOrder(InstantAction f, InstantAction s,
				TemporalMetricState tms)
		{
			followsStart.add(s);
			constraints.add(TemporalConstraint.getConstraint(f, s));
			if (s instanceof StartInstantAction)
			{
				StartInstantAction sa = (StartInstantAction) s;
				// followsStart.add(sa.getSibling());
				constraints.addAll(TemporalConstraint.getBounds(sa, sa
						.getSibling(), sa.parent.getMaxDuration(tms), sa.parent
						.getMinDuration(tms)));
			}
		}

		public void addPreceder(InstantAction a, TemporalMetricState s)
		{
			addPrecederOrder(a, end, s);
		}

		public void addPrecederOrder(InstantAction f, InstantAction s,
				TemporalMetricState tms)
		{
			precedesEnd.add(f);
			constraints.add(TemporalConstraint.getConstraint(f, s));
			if (f instanceof EndInstantAction)
			{
				EndInstantAction ea = (EndInstantAction) f;
				// precedesEnd.add(ea.getSibling());
				constraints.addAll(TemporalConstraint.getBounds(
						ea.getSibling(), ea, ea.parent.getMaxDuration(tms),
						ea.parent.getMinDuration(tms)));
			}
		}

		public boolean check()
		{
			Set testSet = new HashSet(followsStart);
			testSet.retainAll(precedesEnd);
			if (testSet.isEmpty())
				return true;
			else
				return stnCheck();
		}

		public boolean stnCheck()
		{
			if (stn == null)
				stn = new GraphSTN();
			stn.addConstraints(constraints);
			constraints = new HashSet();
			return stn.consistentSource(end);
		}

		public Object clone()
		{
			EnvelopeEntry e = new EnvelopeEntry(start, end);
			e.followsStart.addAll(followsStart);
			e.precedesEnd.addAll(precedesEnd);
			e.constraints.addAll(constraints);
			if (stn != null)
				e.stn = (SimpleTemporalNetwork) ((GraphSTN) stn).clone();
			e.maxEnv = maxEnv;
			e.minEnv = minEnv;
			return e;
		}
	}
}
