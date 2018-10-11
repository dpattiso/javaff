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

import javaff.data.strips.Operator;
import javaff.data.metric.NamedFunction;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

public abstract class PDDLPrinter
{
	public static void printToString(PDDLPrintable p, PrintStream ps,
			boolean typed, boolean bracketed, int indent)
	{
		printIndent(ps, indent);
		printToString(p, ps, typed, bracketed);
	}

	public static void printToString(PDDLPrintable p, PrintStream ps,
			boolean typed, boolean bracketed)
	{
		if (bracketed)
			ps.print("(");
		if (typed)
			ps.print(p.toStringTyped());
		else
			ps.print(p.toString());
		if (bracketed)
			ps.print(")");
	}

	public static void printIndent(PrintStream ps, int indent)
	{
		for (int i = 0; i < indent; ++i)
		{
			ps.print("\t");
		}
	}

	public static void printToString(Collection c, PrintStream ps,
			boolean typed, boolean bracketed, int indent)
	{
		Iterator it = c.iterator();
		while (it.hasNext())
		{
			printIndent(ps, indent);
			PDDLPrintable p = (PDDLPrintable) it.next();
			printToString(p, ps, typed, bracketed);
			if (it.hasNext())
				ps.println();
		}
	}

	public static void printToString(Collection c, PrintStream ps,
			boolean typed, boolean bracketed)
	{
		printToString(c, ps, typed, bracketed, 0);
	}

	public static void printToString(Collection c, String label,
			PrintStream ps, boolean typed, boolean bracketed, int indent)
	{
		ps.println();
		printIndent(ps, indent);
		ps.print("(");
		ps.println(label);
		printToString(c, ps, typed, bracketed, indent + 1);
		ps.print(")");
	}

	public static void printToString(Collection c, String label,
			PrintStream ps, boolean typed, boolean bracketed)
	{
		printToString(c, label, ps, typed, bracketed, 0);
	}

	public static void printDomainFile(UngroundProblem p,
			java.io.PrintStream pstream)
	{
		pstream.println("(define (domain " + p.DomainName + ")");

		pstream.print("\t(:requirements");
		p.requirements.PDDLPrint(pstream, 1);
		pstream.print(")");

		printToString(p.types, ":types", pstream, true, false, 1);
		if (!p.constants.isEmpty())
			printToString(p.constants, ":constants", pstream, true, false, 1);
		printToString(p.predSymbols, ":predicates", pstream, true, true, 1);
		if (!p.funcSymbols.isEmpty())
			printToString(p.funcSymbols, ":functions", pstream, true, true, 1);

		for (Operator o : p.actions)
		{
			pstream.println();
			o.PDDLPrint(pstream, 1);
		}

		pstream.println(")");
	}

	public static void printProblemFile(UngroundProblem p,
			java.io.PrintStream pstream)
	{
		pstream.println("(define (problem " + p.ProblemName + ")");
		pstream.print("\t(:domain " + p.ProblemDomainName + ")");

		printToString(p.objects, ":objects", pstream, true, false, 1);
		pstream.println();
		printIndent(pstream, 1);
		pstream.print("(");
		pstream.println(":init");
		printToString(p.initial, pstream, false, true, 2);
		Iterator it = p.funcValues.keySet().iterator();
		while (it.hasNext())
		{
			NamedFunction nf = (NamedFunction) it.next();
			pstream.println();
			printIndent(pstream, 2);
			pstream.print("(= ");
			printToString(nf, pstream, false, false);
			pstream.print(" " + p.funcValues.get(nf) + ")");
		}
		pstream.print(")");

		pstream.print("\n\t(:goal ");
		p.goal.PDDLPrint(pstream, 2);
		pstream.println(")");
		printIndent(pstream, 1);
		if (p.metric != null)
			p.metric.PDDLPrint(pstream, 1);
		pstream.print(")");
	}
}
