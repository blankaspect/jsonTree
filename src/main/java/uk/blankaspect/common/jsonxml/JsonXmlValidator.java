/*====================================================================*\

JsonXmlValidator.java

Class: XML Schema validator for JSON-XML.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.jsonxml;

//----------------------------------------------------------------------


// IMPORTS


import java.io.BufferedReader;
import java.io.InputStream;

import java.lang.invoke.MethodHandles;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamSource;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Element;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;
import uk.blankaspect.common.exception2.LocationException;

//----------------------------------------------------------------------


// CLASS: XML SCHEMA VALIDATOR FOR JSON-XML


/**
 * This class provides some {@code static} methods for validating a tree of JSON-XML elements against an XML Schema
 * (XSD).
 * <ul>
 *   <li>
 *     A {@linkplain #validate(Element, boolean) method} that validates a JSON-XML tree against one of two predefined
 *     schemas according to a flag that is an argument of the method.  One schema has a {@linkplain #NAMESPACE_NAME
 *     target namespace}; the other does not have a target namespace.
 *   </li>
 *   <li>
 *     A {@linkplain #validate(Class, String, Element) method} that validates a JSON-XML tree against a schema that is
 *     created from a resource at a specified location.
 *   </li>
 *   <li>
 *     A {@linkplain #validate(Path, Element) method} that validates a JSON-XML tree against a schema that is created
 *     from a file at a specified file-system location.
 *   </li>
 * </ul>
 */

public class JsonXmlValidator
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The name of the namespace in the XML Schema for JSON-XML that has a target namespace. */
	public static final		String	NAMESPACE_NAME	= "http://ns.blankaspect.uk/jsonXml-1";

	/** The filename of the XML Schema with no target namespace. */
	private static final	String	XSD_FILENAME	= "jsonXml.xsd";

	/** The filename of the XML Schema with a target namespace. */
	private static final	String	XSD_FILENAME_NS	= "jsonXmlNS.xsd";

	/** Error messages. */
	private interface ErrorMsg
	{
		String	FAILED_TO_CREATE_SCHEMA		= "Failed to create an XML schema.";
		String	ERROR_VALIDATING_ELEMENT	= "An error occurred when validating the XML element against a schema.";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private JsonXmlValidator()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Validates a tree of JSON-XML elements whose root is the specified element against one of two predefined XML
	 * Schemas (XSD) according to the specified flag.  One schema has a {@linkplain #NAMESPACE_NAME target namespace};
	 * the other does not have a target namespace.
	 *
	 * @param  element
	 *           the root of the tree of JSON-XML elements that will be validated against one of two predefined schemas
	 *           according to {@code hasTargetNamespace}.
	 * @param  hasTargetNamespace
	 *           if {@code true}, the JSON-XML tree will be validated against a schema that has a target namespace;
	 *           if {@code false}, the JSON-XML tree will be validated against a schema that does not have a target
	 *           namespace.
	 * @throws BaseException
	 *           if an error occurs when validating the tree of JSON-XML elements.
	 */

	public static void validate(
		Element	element,
		boolean	hasTargetNamespace)
		throws BaseException
	{
		// Get pathname of schema resource
		Class<?> cls = MethodHandles.lookup().lookupClass();
		String filename = hasTargetNamespace ? XSD_FILENAME_NS : XSD_FILENAME;
		String pathname = "/" + cls.getPackageName().replace('.', '/') + "/" + filename;

		// Validate tree of JSON-XML elements against schema
		validate(cls, pathname, element);
	}

	//------------------------------------------------------------------

	/**
	 * Validates a tree of JSON-XML elements whose root is the specified element against an XML Schema (XSD) that is
	 * created from a resource at the specified location.
	 *
	 * @param  cls
	 *           the class with which the schema-file resource is associated.  If it is {@code null}, the system class
	 *           loader will be used to find the resource.
	 * @param  xsdPathname
	 *           the pathname of the schema-file resource.
	 * @param  element
	 *           the root of the tree of JSON-XML elements that will be validated.
	 * @throws BaseException
	 *           if an error occurs when
	 *           <ul>
	 *             <li>loading the schema-file resource,</li>
	 *             <li>creating an XML Schema from the resource, or</li>
	 *             <li>validating the tree of JSON-XML elements.</li>
	 *           </ul>
	 */

	public static void validate(
		Class<?>	cls,
		String		xsdPathname,
		Element		element)
		throws BaseException
	{
		// Create schema from XSD file
		Schema schema = null;
		try (InputStream stream = (cls == null) ? ClassLoader.getSystemResourceAsStream(xsdPathname)
												: cls.getResourceAsStream(xsdPathname))
		{
			schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(stream));
		}
		catch (Exception e)
		{
			throw new LocationException(ErrorMsg.FAILED_TO_CREATE_SCHEMA, e, xsdPathname);
		}

		// Validate tree of JSON-XML elements against schema
		validate(schema, element);
	}

	//------------------------------------------------------------------

	/**
	 * Validates a tree of JSON-XML elements whose root is the specified element against an XML Schema (XSD) that is
	 * created from a file at the specified file-system location.
	 *
	 * @param  xsdFile
	 *           the file-system location of the schema file.
	 * @param  element
	 *           the root of the tree of JSON-XML elements that will be validated.
	 * @throws BaseException
	 *           if an error occurs when
	 *           <ul>
	 *             <li>reading the schema file,</li>
	 *             <li>creating an XML Schema from the contents of the file, or</li>
	 *             <li>validating the tree of JSON-XML elements.</li>
	 *           </ul>
	 */

	public static void validate(
		Path	xsdFile,
		Element	element)
		throws BaseException
	{
		// Create schema from XSD file
		Schema schema = null;
		try (BufferedReader reader = Files.newBufferedReader(xsdFile))
		{
			schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(reader));
		}
		catch (Exception e)
		{
			throw new FileException(ErrorMsg.FAILED_TO_CREATE_SCHEMA, e, xsdFile);
		}

		// Validate tree of JSON-XML elements against schema
		validate(schema, element);
	}

	//------------------------------------------------------------------

	/**
	 * Validates a tree of JSON-XML elements whose root is the specified element against the specified schema.
	 *
	 * @param  schema
	 *           the schema against which the tree of JSON-XML elements will be validated.
	 * @param  element
	 *           the root of the tree of JSON-XML elements that will be validated against {@code schema}.
	 * @throws BaseException
	 *           if an error occurs when validating the tree of JSON-XML elements.
	 */

	private static void validate(
		Schema	schema,
		Element	element)
		throws BaseException
	{
		try
		{
			schema.newValidator().validate(new DOMSource(element));
		}
		catch (Exception e)
		{
			throw new BaseException(ErrorMsg.ERROR_VALIDATING_ELEMENT, e);
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
