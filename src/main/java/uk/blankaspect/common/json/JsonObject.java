/*====================================================================*\

JsonObject.java

Interface: JSON object.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.json;

//----------------------------------------------------------------------


// INTERFACE: JSON OBJECT


/**
 * This interface defines constants that relate to JSON objects.
 */

public interface JsonObject
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The character that denotes the start of a JSON object. */
	char	START_CHAR	= '{';

	/** The character that denotes the end of a JSON object. */
	char	END_CHAR	= '}';

	/** The character that separates the name and value of a property of a JSON object. */
	char	NAME_VALUE_SEPARATOR_CHAR	= ':';

	/** The character that separates adjacent properties of a JSON object. */
	char	PROPERTY_SEPARATOR_CHAR		= ',';

}

//----------------------------------------------------------------------
