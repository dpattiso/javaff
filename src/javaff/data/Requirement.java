package javaff.data;

import java.io.PrintStream;

/**
 * Represents a single or multiple requirements from a PDDL domain definition.
 * @author David Pattison
 *
 */
public enum Requirement implements PDDLPrintable
{
	Typing(1),
	Strips(2),
	Fluents(4),
	DurativeActions(8),
	DurationInequalities(16),
	NumericFluents(32),
	ActionCosts(64),
	ADL(128),
	NegativePreconditions(256),
	DisjunctivePreconditions(512),
	ExistentialPreconditions(1024),
	UniversalPreconditions(2048),
	QuantifiedPreconditions(4096),
	ConditionalEffects(8192),
	TimedInitialLiterals(16384),
	DomainAxioms(32768),
	Constraints(65536),
	Preferences(131072),
	DerivedPredicates(262144),
	Equality(524288),
	None(1048576);
	
	private final int flag;
	
	private Requirement(int flag)
	{
		this.flag = flag;
	}
	
	public int getIntegerValue()
	{
		return this.flag;
	}
	
	
	
//	public void addRequirement(Requirements r)
//	{
//		this.flag = (this.flag | r.flag);
//	}
//	
//	public void removeRequirement(Requirements r)
//	{
//		if ((this.flag & r.flag) == r.flag)
//		{
//			this.flag = (this.flag - r.flag);
//		
//		
//		}
//	}
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		if ((this.flag & Typing.flag) == Typing.flag)
			buf.append(":typing ");
		if ((this.flag & Fluents.flag) == Fluents.flag)
			buf.append(":fluents ");
		if ((this.flag & DurativeActions.flag) == DurativeActions.flag)
			buf.append(":durative-actions ");
		if ((this.flag & DurationInequalities.flag) == DurationInequalities.flag)
			buf.append(":duration-inequalities ");
		if ((this.flag & NumericFluents.flag) == NumericFluents.flag)
			buf.append(":numeric-fluents ");
		if ((this.flag & ActionCosts.flag) == ActionCosts.flag)
			buf.append(":action-costs ");
		if ((this.flag & ADL.flag) == ADL.flag)
			buf.append(":adl ");
		if ((this.flag & NegativePreconditions.flag) == NegativePreconditions.flag)
			buf.append(":negative-preconditions ");
		if ((this.flag & DisjunctivePreconditions.flag) == DisjunctivePreconditions.flag)
			buf.append(":disjunctive-preconditions ");
		if ((this.flag & ExistentialPreconditions.flag) == ExistentialPreconditions.flag)
			buf.append(":existential-preconditions ");
		if ((this.flag & UniversalPreconditions.flag) == UniversalPreconditions.flag)
			buf.append(":universal-preconditions ");
		if ((this.flag & QuantifiedPreconditions.flag) == QuantifiedPreconditions.flag)
			buf.append(":quantified-preconditions ");
		if ((this.flag & ConditionalEffects.flag) == ConditionalEffects.flag)
			buf.append(":conditional-effects ");			
		if ((this.flag & TimedInitialLiterals.flag) == TimedInitialLiterals.flag)
			buf.append(":timed-initial-literals ");
		if ((this.flag & DomainAxioms.flag) == DomainAxioms.flag)
			buf.append(":domain-axioms ");
		if ((this.flag & Constraints.flag) == Constraints.flag)
			buf.append(":constraints ");
		if ((this.flag & Preferences.flag) == Preferences.flag)
			buf.append(":preferences ");
		if ((this.flag & DerivedPredicates.flag) == DerivedPredicates.flag)
			buf.append(":derived-predicates ");
		if ((this.flag & Strips.flag) == Strips.flag)
			buf.append(":strips ");
		if ((this.flag & Equality.flag) == Equality.flag)
			buf.append(":equality ");
		
		return buf.toString();
	}

	@Override
	public void PDDLPrint(PrintStream p, int indent)
	{
		p.print(this.toString());
	}

	@Override
	public String toStringTyped()
	{
		return this.toString();
	}
}