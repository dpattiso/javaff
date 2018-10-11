package javaff.data;

import java.io.PrintStream;


/**
 * Encapsulates the :requirements tag of a PDDL domain. This is essentially a wrapper for the Requirements
 * enum.
 * 
 * @author David Pattison
 *
 */
public class DomainRequirements implements PDDLPrintable
{
	private int flags;
	
	/**
	 * Create an empty set of domain flags. 
	 */
	public DomainRequirements()
	{
		this.flags = (Requirement.None).getIntegerValue();
	}
	
	/**
	 * Create a set of domain flags from the specified binary flag.
	 * @param binaryFlags An integer which will have its binary value used as a set of Requirement flags.
	 */
	public DomainRequirements(int binaryFlags)
	{
		this.flags = binaryFlags;
	}
	
	/**
	 * Create a set of flags from the binary form of the specified integer, which itself
	 * is treated as a binary string.
	 * @param flags
	 */
	public DomainRequirements(Requirement flags)
	{
		this();
		this.flags = flags.getIntegerValue();
	}

	/**
	 * Create a set of flags from an existing set of flags.
	 * @param existingFlags
	 */
	public DomainRequirements(DomainRequirements existingFlags)
	{
		this(existingFlags.flags);
	}
	
	/**
	 * Add the specified flag to this DomainFlags object.
	 * @param domainFlag
	 */
	public void addRequirement(Requirement domainFlag)
	{
//		this.flags.addRequirement(domainFlag);
		this.flags = this.flags | domainFlag.getIntegerValue();
	}
	
	public Object clone()
	{
		return new DomainRequirements(this.flags);
	}
	
	/**
	 * Check whether this and another set of flags are exactly equal.
	 * See {@link #subsumes} for checking whether a set of 
	 * flags are wholly encompassed by this DomainFlags object. 
	 */
	public boolean equals(Object other)
	{
		return this.flags == ((DomainRequirements)other).flags;
	}
	
	/**
	 * Does this set of flags encompass another set of flags.
	 * @param otherFlags
	 * @return
	 */
	public boolean subsumes(DomainRequirements otherFlags)
	{
		return DomainRequirements.contains(this, otherFlags);
	}

//	/**
//	 * Does this set of flags encompass another set of flags.
//	 * @param otherFlags
//	 * @return
//	 */
//	public boolean subsumes(Requirement otherFlags)
//	{
//		return DomainRequirements.contains(this.flags, otherFlags.flag);
//	}

	/**
	 * Does one set of flags encompass another set of flags.
	 * @param otherFlags
	 * @return
	 */
	public static boolean contains(DomainRequirements superset, DomainRequirements subset)
	{
		return DomainRequirements.supports(superset.flags, subset.flags);
	}

//	/**
//	 * Does one set of flags encompass another set of flags.
//	 * @param otherFlags
//	 * @return
//	 */
//	public static boolean contains(Requirement superset, Requirement subset)
//	{
//		return ((superset.getIntegerValue() & subset.getIntegerValue()) == subset.getIntegerValue());
//	}
		
	/**
	 * Returns the integer value of the flags represented by this object.
	 */
	@Override
	public int hashCode()
	{
		return this.flags;
	}
	
	/**
	 * Get the Requirements representation of this set of flags.
	 * @return
	 */
	public int getIntegerValue()
	{
		return this.flags;
	}
	
	/**
	 * Checks whether this set of domain flags support the specified set of flags.
	 * @param flagsToCheck
	 * @return
	 */
	public boolean contains(DomainRequirements flagsToCheck)
	{
		return this.contains(flagsToCheck);
	}

	/**
	 * Checks whether this set of domain flags support the specified set of flags.
	 * @param flagsToCheck
	 * @return
	 */
	public boolean contains(Requirement flagsToCheck)
	{
		return DomainRequirements.supports(this.flags, flagsToCheck.getIntegerValue());
	}
	
	/**
	 * Tests whether Requirements.Fluents or Requirements.NumericFluents is present. Note that Fluents (:fluents)
	 * is present for legacy reasons. Until PDDL 3 this would have been an indication of numeric fluents being present, but
	 * now it should really mean that Object fluents are present, with :numeric-fluents being the appropriate replacement. 
	 * Therefore, developers should be careful of relying upon this method in a post PDDL 3 world.
	 * 
	 * @return
	 */
	public boolean isMetric()
	{
		return this.contains(Requirement.Fluents) || 
			   this.contains(Requirement.NumericFluents);
	}
	
	/**
	 * Tests whether :durative-actions, :duration-inequalities or :timed-initial-literals appears in this domain requirements 
	 * object.
	 * @return
	 */
	public boolean isTemporal()
	{
		return this.contains(Requirement.DurativeActions) ||
				this.contains(Requirement.DurationInequalities) ||
				this.contains(Requirement.TimedInitialLiterals);				
	}
	
	/**
	 * Checks whether the flags passed in include a set of other flags.
	 * @param flags The flags to check against.
	 * @param flagsToCheck The flags to check for.
	 * @return True if all the flags were found, false otherwise.
	 */
	public static boolean supports(int flags, int flagsToCheck)
	{
		int res = flags & flagsToCheck;
		
//		System.out.println(Integer.toBinaryString(flags) + " AND "+Integer.toBinaryString(flagsToCheck)+" = "+Integer.toBinaryString(res));
		return res == flagsToCheck;
	}
	
	public static String toBinaryString(Requirement flags)
	{
		return Integer.toBinaryString(flags.getIntegerValue());
	}
	
	public static String toBinaryString(DomainRequirements flags)
	{
		return Integer.toBinaryString(flags.getIntegerValue());
	}
	
	/**
	 * Converts this object's flags to a binary string.
	 * @return
	 */
	public String toBinaryString()
	{
		return DomainRequirements.toBinaryString(this);
	}
	
	@Override
	public String toString()
	{
//		return this.flagss+"";
		StringBuffer buf = new StringBuffer();
		if ((this.flags & Requirement.Typing.getIntegerValue()) == Requirement.Typing.getIntegerValue())
			buf.append(":typing ");
		if ((this.flags & Requirement.Fluents.getIntegerValue()) == Requirement.Fluents.getIntegerValue())
			buf.append(":fluents ");
		if ((this.flags & Requirement.DurativeActions.getIntegerValue()) == Requirement.DurativeActions.getIntegerValue())
			buf.append(":durative-actions ");
		if ((this.flags & Requirement.DurationInequalities.getIntegerValue()) == Requirement.DurationInequalities.getIntegerValue())
			buf.append(":duration-inequalities ");
		if ((this.flags & Requirement.NumericFluents.getIntegerValue()) == Requirement.NumericFluents.getIntegerValue())
			buf.append(":numeric-fluents ");
		if ((this.flags & Requirement.ActionCosts.getIntegerValue()) == Requirement.ActionCosts.getIntegerValue())
			buf.append(":action-costs ");
		if ((this.flags & Requirement.ADL.getIntegerValue()) == Requirement.ADL.getIntegerValue())
			buf.append(":adl ");
		if ((this.flags & Requirement.NegativePreconditions.getIntegerValue()) == Requirement.NegativePreconditions.getIntegerValue())
			buf.append(":negative-preconditions ");
		if ((this.flags & Requirement.DisjunctivePreconditions.getIntegerValue()) == Requirement.DisjunctivePreconditions.getIntegerValue())
			buf.append(":disjunctive-preconditions ");
		if ((this.flags & Requirement.ExistentialPreconditions.getIntegerValue()) == Requirement.ExistentialPreconditions.getIntegerValue())
			buf.append(":existential-preconditions ");
		if ((this.flags & Requirement.UniversalPreconditions.getIntegerValue()) == Requirement.UniversalPreconditions.getIntegerValue())
			buf.append(":universal-preconditions ");
		if ((this.flags & Requirement.QuantifiedPreconditions.getIntegerValue()) == Requirement.QuantifiedPreconditions.getIntegerValue())
			buf.append(":quantified-preconditions ");
		if ((this.flags & Requirement.ConditionalEffects.getIntegerValue()) == Requirement.ConditionalEffects.getIntegerValue())
			buf.append(":conditional-effects ");			
		if ((this.flags & Requirement.TimedInitialLiterals.getIntegerValue()) == Requirement.TimedInitialLiterals.getIntegerValue())
			buf.append(":timed-initial-literals ");
		if ((this.flags & Requirement.DomainAxioms.getIntegerValue()) == Requirement.DomainAxioms.getIntegerValue())
			buf.append(":domain-axioms ");
		if ((this.flags & Requirement.Constraints.getIntegerValue()) == Requirement.Constraints.getIntegerValue())
			buf.append(":constraints ");
		if ((this.flags & Requirement.Preferences.getIntegerValue()) == Requirement.Preferences.getIntegerValue())
			buf.append(":preferences ");
		if ((this.flags & Requirement.DerivedPredicates.getIntegerValue()) == Requirement.DerivedPredicates.getIntegerValue())
			buf.append(":derived-predicates ");
		if ((this.flags & Requirement.Strips.getIntegerValue()) == Requirement.Strips.getIntegerValue())
			buf.append(":strips ");
		if ((this.flags & Requirement.Equality.getIntegerValue()) == Requirement.Equality.getIntegerValue())
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

//	/**
//	 * Validates that the flags stored within this object are correct PDDL flags.
//	 * @return True if this object is valid, false otherwise.
//	 */
//	public boolean isValid()
//	{
//		return DomainFlags.isValid(this);
//	}
//	
//	public static boolean isValid(DomainFlags flags)
//	{
//		return DomainFlags.isValid(flags.getFlags());
//	}
//	
//	public static boolean isValid(int flags)
//	{
//		//too much effort!
//	}
	
}
