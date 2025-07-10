/*====================================================================*\

N0Source.java

Class: source of natural numbers including zero.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.jsonxmltest;

//----------------------------------------------------------------------


// IMPORTS


import java.util.random.RandomGenerator;

import java.util.stream.Stream;

//----------------------------------------------------------------------


// CLASS: SOURCE OF NATURAL NUMBERS INCLUDING ZERO


/**
 * This is the abstract base class of a source of natural numbers including zero, &#x2115;<sub>0</sub> (N0).
 */

public abstract class N0Source
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** Miscellaneous strings. */
	private static final	String	LAMBDA_OUT_OF_BOUNDS_STR	= "Lambda must not be negative";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private N0Source()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the next value from this source.  The value is in the set of natural numbers including zero,
	 * &#x2115;<sub>0</sub> (N0).
	 *
	 * @param  prng
	 *           a generator of pseudo-random numbers that may be used by this source.  It may be {@code null} if this
	 *           source doesn't require a generator of pseudo-random numbers.
	 * @return a value in the set of natural numbers including zero, &#x2115;<sub>0</sub> (N0).
	 */

	public abstract int next(
		RandomGenerator	prng);

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: KIND OF SOURCE OF NATURAL NUMBERS INCLUDING ZERO


	/**
	 * This is an enumeration of the available kinds of source of natural numbers including zero, &#x2115;<sub>0</sub>
	 * (N0).
	 */

	public enum Kind
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * A fixed, specified value (a one-point distribution).
		 */
		FIXED
		(
			"fixed",
			1
		),

		/**
		 * A value from a discrete uniform distribution whose lower and upper bounds are specified.
		 */
		UNIFORM
		(
			"uniform",
			2
		),

		/**
		 * A value from a Poisson distribution whose lambda parameter is specified.
		 */
		POISSON
		(
			"poisson",
			1
		),

		/**
		 * A value from a zero-truncated Poisson distribution whose lambda parameter is specified.
		 */
		ZT_POISSON
		(
			"ztp",
			1
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The key that is associated with this kind of number source. */
		private	String	key;

		/** The number of parameters of this kind of number source. */
		private	int		numParams;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a kind of source of natural numbers including zero,
		 * &#x2115;<sub>0</sub> (N0).
		 *
		 * @param key
		 *          the key that will be associated with the kind of number source.
		 * @param numParams
		 *          the number of parameters of the kind of number source.
		 */

		private Kind(
			String	key,
			int		numParams)
		{
			// Initialise instance variables
			this.key = key;
			this.numParams = numParams;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the kind of number source that is associated with the specified key.
		 *
		 * @param  key
		 *           the key whose associated kind of number source is desired.
		 * @return the kind of number source that is associated with {@code key}, or {@code null} if there is no such
		 *         kind of number source.
		 */

		public static Kind forKey(
			String	key)
		{
			return Stream.of(values())
					.filter(value -> value.key.equals(key))
					.findFirst()
					.orElse(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the key that is associated with this kind of number source.
		 *
		 * @return the key that is associated with this kind of number source.
		 */

		public String key()
		{
			return key;
		}

		//--------------------------------------------------------------

		/**
		 * Returns the number of parameters of this kind of number source.
		 *
		 * @return the number of parameters of this kind of number source.
		 */

		public int numParams()
		{
			return numParams;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: FIXED VALUE


	/**
	 * This class implements a number source that always supplies the same specified value from the set of natural
	 * numbers including zero, &#x2115;<sub>0</sub> (N0).  This is known as a <i>one-point distribution</i>.
	 */

	public static class Fixed
		extends N0Source
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The fixed value that is supplied by this number source. */
		private	int	value;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a number source that always supplies the specified value.
		 *
		 * @param value
		 *          the non-negative integer that will be supplied by the number source.
		 */

		public Fixed(
			int	value)
		{
			// Validate argument
			if (value < 0)
				throw new IllegalArgumentException("Value must not be negative");

			// Initialise instance variables
			this.value = value;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public int next(
			RandomGenerator	prng)
		{
			return value;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public boolean equals(
			Object	obj)
		{
			if (this == obj)
				return true;

			return (obj instanceof Fixed other) && (value == other.value);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public int hashCode()
		{
			return value;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: RANDOM, UNIFORM DISTRIBUTION


	/**
	 * This class implements a source of pseudo-random values from a discrete uniform distribution whose lower and upper
	 * bounds are specified.
	 */

	public static class Uniform
		extends N0Source
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The lower bound of the discrete uniform distribution. */
		private	int	lowerBound;

		/** The upper bound of the discrete uniform distribution. */
		private	int	upperBound;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a source of pseudo-random values from a discrete uniform distribution with the
		 * specified lower and upper bounds.
		 *
		 * @param lowerBound
		 *          the lower bound of the discrete uniform distribution.
		 * @param upperBound
		 *          the upper bound of the discrete uniform distribution.
		 */

		public Uniform(
			int	lowerBound,
			int	upperBound)
		{
			// Validate arguments
			if (lowerBound < 0)
				throw new IllegalArgumentException("Lower bound must not be negative");
			if (upperBound < 0)
				throw new IllegalArgumentException("Upper bound must not be negative");
			if (lowerBound > upperBound)
				throw new IllegalArgumentException("Bounds are out of order");

			// Initialise instance variables
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public int next(
			RandomGenerator	prng)
		{
			return lowerBound + prng.nextInt(upperBound - lowerBound + 1);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public boolean equals(
			Object	obj)
		{
			if (this == obj)
				return true;

			return (obj instanceof Uniform other) && (lowerBound == other.lowerBound)
					&& (upperBound == other.upperBound);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public int hashCode()
		{
			return 31 * lowerBound + upperBound;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: RANDOM, POISSON DISTRIBUTION


	/**
	 * This class implements a source of pseudo-random values from a Poisson distribution whose lambda parameter,
	 * <i>&#x3BB;</i>, is specified.
	 */

	public static class Poisson
		extends N0Source
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The lambda parameter of the Poisson distribution. */
		private	int	lambda;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a source of pseudo-random values from a Poisson distribution with the specified
		 * lambda parameter.
		 *
		 * @param lambda
		 *          the lambda parameter of the Poisson distribution.
		 */

		public Poisson(
			int	lambda)
		{
			// Validate arguments
			if (lambda < 0)
				throw new IllegalArgumentException(LAMBDA_OUT_OF_BOUNDS_STR);

			// Initialise instance variables
			this.lambda = lambda;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public int next(
			RandomGenerator	prng)
		{
			int k = 0;
			double fpLambda = (double)lambda;
			double eMinusLambda = Math.exp(-fpLambda);
			double a = eMinusLambda;
			double b = a;
			double u = prng.nextDouble();
			while (b < u)
			{
				++k;
				a *= fpLambda / (double)k;
				b += a;
			}

			return k;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public boolean equals(
			Object	obj)
		{
			if (this == obj)
				return true;

			return (obj instanceof Poisson other) && (lambda == other.lambda);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public int hashCode()
		{
			return lambda;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: RANDOM, ZERO-TRUNCATED POISSON DISTRIBUTION


	/**
	 * This class implements a source of pseudo-random values from a zero-truncated Poisson (ZTP) distribution whose
	 * lambda parameter, <i>&#x3BB;</i>, is specified.
	 */

	public static class ZtPoisson
		extends N0Source
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The lambda parameter of the zero-truncated Poisson distribution. */
		private	int	lambda;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a source of pseudo-random values from a zero-truncated Poisson distribution with
		 * the specified lambda parameter.
		 *
		 * @param lambda
		 *          the lambda parameter of the zero-truncated Poisson distribution.
		 */

		public ZtPoisson(
			int	lambda)
		{
			// Validate arguments
			if (lambda < 0)
				throw new IllegalArgumentException(LAMBDA_OUT_OF_BOUNDS_STR);

			// Initialise instance variables
			this.lambda = lambda;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public int next(
			RandomGenerator	prng)
		{
			int k = 1;
			double fpLambda = (double)lambda;
			double eMinusLambda = Math.exp(-fpLambda);
			double a = fpLambda * eMinusLambda / (1.0 - eMinusLambda);
			double b = a;
			double u = prng.nextDouble();
			while (b < u)
			{
				++k;
				a *= fpLambda / (double)k;
				b += a;
			}

			return k;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public boolean equals(
			Object	obj)
		{
			if (this == obj)
				return true;

			return (obj instanceof ZtPoisson other) && (lambda == other.lambda);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public int hashCode()
		{
			return lambda;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
