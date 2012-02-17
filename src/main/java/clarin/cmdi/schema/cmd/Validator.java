package clarin.cmdi.schema.cmd;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import clarin.cmdi.xml.Saxon;
import java.io.File;
import java.io.InputStream;
import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The Validator class handles the XSD and Schematron validation of CMD profile and component specifications.
 * 
 * The class caches thread safe versions of the XSD Schema and the Schematron XSLT.
 * So multiple instances of this Validator class can be used in parallel and use the same cached schema and transformer.
 * Although a single instance of the Validator class can't be accessed in parallel it can be used to validate multiple CMD profiles/components in sequence.
 *
 * @author menwin
 * @author twagoo
 */
public class Validator {

    /**
     * Default location of the CMD schema
     */
    static final String CMD_SCHEMA_URL = "http://www.clarin.eu/cmd/general-component-schema.xsd";
    /**
     * The immutable location of the CMD schema that is used in this instance
     */
    private final URL cmdSchemaUri;
    /**
     * The "immutable, and therefore thread-safe," "compiled form of [the Schematron] stylesheet".
     */
    private XsltExecutable cmdSchematron = null;
    /**
     * The "immutable in-memory representation of [the XSD] grammar".
     */
    private Schema cmdSchema = null;
    /**
     * The list of validation messages compiled a the last run of the validator.
     */
    private List<Message> msgList = null;
    /**
     * The Schematron SVRL validation report
     */
    private XdmNode validationReport = null;

    /**
     * Creates a Validator that uses a specific schema specified by its URL
     * @param cmdSchemaUri Schema URI to use
     * @see #CMD_SCHEMA_URL
     */
    public Validator(URL cmdSchemaUri) {
	this.cmdSchemaUri = cmdSchemaUri;
    }

    /**
     * Convenience method to build a XSLT transformer from a resource.
     * 
     * @param uri The location of the resource
     * @return An executable XSLT
     * @throws Exception 
     */
    static XsltExecutable buildTransformer(File file) throws SaxonApiException {
	XdmNode xslDoc = Saxon.buildDocument(new javax.xml.transform.stream.StreamSource(file));
	return Saxon.buildTransformer(xslDoc);
    }

    /**
     * Convenience method to build a XSLT transformer from a resource.
     * 
     * @param uri The location of the resource
     * @return An executable XSLT
     * @throws Exception 
     */
    static XsltExecutable buildTransformer(URL url) throws SaxonApiException {
	XdmNode xslDoc = Saxon.buildDocument(new javax.xml.transform.stream.StreamSource(url.toExternalForm()));
	return Saxon.buildTransformer(xslDoc);
    }

    /**
     * Convenience method to build a XSLT transformer from a resource.
     * 
     * @param uri The location of the resource
     * @return An executable XSLT
     * @throws Exception 
     */
    static XsltExecutable buildTransformer(InputStream stream) throws SaxonApiException {
	XdmNode xslDoc = Saxon.buildDocument(new javax.xml.transform.stream.StreamSource(stream));
	return Saxon.buildTransformer(xslDoc);
    }

    /**
     * Returns the CMD XSD schema, and loads it just-in-time.
     * 
     * @return An in-memory representation of the grammar
     * @throws Exception 
     */
    private synchronized Schema getSchema() throws IOException, SAXException {
	if (cmdSchema == null) {
	    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	    // Load the CMD XSD.
	    Source schemaFile = new StreamSource(cmdSchemaUri.openStream());
	    cmdSchema = factory.newSchema(schemaFile);

	}
	return cmdSchema;
    }

    /**
     * Validation of a loaded CMD profile/component against the XSD schema.
     * 
     * Unfortunately we can't use the Saxon XSD validator as that is limited to a commercial version of Saxon.
     * 
     * @param src The loaded CMD profile/component
     * @return Is the CMD profile/component valid or not?
     * @throws Exception 
     */
    public boolean validateXSD(XdmNode src) throws ParserConfigurationException, SaxonApiException, IOException {
	Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	DOMDestination dst = new DOMDestination(doc);
	Saxon.getProcessor().writeXdmValue(src, dst);

	try {
	    // Create a Validator object, which can be used to validate
	    // an instance document.
	    javax.xml.validation.Validator validator = getSchema().newValidator();

	    // Validate the DOM tree.
	    validator.validate(new DOMSource(doc));

	} catch (SAXException e) {
	    Message msg = new Message();
	    msg.error = true;
	    msg.text = e.getMessage();
	    msgList.add(msg);
	    return false;
	}

	return true;
    }

    /**
     * Returns the CMD Schematron XSLT, and loads it just-in-time.
     * 
     * @return The compiled Schematron XSLT
     * @throws Exception 
     */
    private synchronized XsltExecutable getSchematron() throws SaxonApiException, IOException {
	if (cmdSchematron == null) {
	    // Load the schema
	    XdmNode schema = Saxon.buildDocument(new javax.xml.transform.stream.StreamSource(cmdSchemaUri.openStream()));
	    // Load the Schematron XSL to extract the Schematron rules;
	    XsltTransformer extractSchXsl = buildTransformer(Validator.class.getResource("/schematron/ExtractSchFromXSD-2.xsl")).load();
	    // Load the Schematron XSLs to 'compile' Schematron rules;
	    XsltTransformer includeSchXsl = buildTransformer(Validator.class.getResource("/schematron/iso_dsdl_include.xsl")).load();
	    XsltTransformer expandSchXsl = buildTransformer(Validator.class.getResource("/schematron/iso_abstract_expand.xsl")).load();
	    XsltTransformer compileSchXsl = buildTransformer(Validator.class.getResource("/schematron/iso_svrl_for_xslt2.xsl")).load();
	    // Setup the pipeline
	    XdmDestination destination = new XdmDestination();
	    extractSchXsl.setSource(schema.asSource());
	    extractSchXsl.setDestination(includeSchXsl);
	    includeSchXsl.setDestination(expandSchXsl);
	    expandSchXsl.setDestination(compileSchXsl);
	    compileSchXsl.setDestination(destination);
	    // Extract the Schematron rules from the schema        
	    extractSchXsl.transform();
	    // Compile the Schematron rules XSL
	    cmdSchematron = Saxon.buildTransformer(destination.getXdmNode());
	}
	return cmdSchematron;
    }

    /**
     * Validation of a loaded CMD profile/component against the Schematron XSLT
     * 
     * @param src The loaded CMD profile/component
     * @return Is the CMD profile/component valid or not?
     * @throws Exception 
     */
    public boolean validateSchematron(XdmNode src) throws SaxonApiException, IOException {
	XsltTransformer schematronXsl = getSchematron().load();
	schematronXsl.setSource(src.asSource());
	XdmDestination destination = new XdmDestination();
	schematronXsl.setDestination(destination);
	schematronXsl.transform();

	validationReport = destination.getXdmNode();

	Saxon.declareXPathNamespace("svrl", "http://purl.oclc.org/dsdl/svrl");
	return ((net.sf.saxon.value.BooleanValue) Saxon.evaluateXPath(validationReport, "empty(//svrl:failed-assert[(preceding-sibling::svrl:fired-rule)[last()][empty(@role) or @role!='warning']])").evaluateSingle().getUnderlyingValue()).getBooleanValue();
    }

    /**
     * Validation of a loaded CMD profile/component against both the XSD and the Schematron XSLT
     * 
     * After validation any messages can be accessed using the {@link getMessages()} method.
     * Notice that even if a CMD profile/component is valid there might be warning messages.
     * 
     * @param prof The CMD profile/component
     * @return Is the CMD profile/component valid or not?
     * @throws Exception 
     */
    public boolean validateProfile(Source prof) throws SaxonApiException, ParserConfigurationException, IOException {
	// Initalize
	msgList = new java.util.ArrayList<Message>();
	validationReport = null;

	// load the document
	XdmNode doc = Saxon.buildDocument(prof);

	// step 1: validate against XML Schema
	if (!this.validateXSD(doc)) {
	    return false;
	}

	// step 2: validate Schematron rules
	return validateSchematron(doc);
    }

    /**
     * Get the list of messages accumulated in the last validation run.
     * 
     * @return The list of messages
     * @throws Exception 
     */
    public synchronized List<Message> getMessages() throws SaxonApiException {
	if (validationReport != null) {
	    for (XdmItem assertion : Saxon.evaluateXPath(validationReport, "//svrl:failed-assert")) {
		Message msg = new Message();
		msg.context = Saxon.evaluateXPath(assertion, "(preceding-sibling::svrl:fired-rule)[last()]/@context").evaluateSingle().getStringValue();
		msg.test = ((XdmNode) assertion).getAttributeValue(new QName("test"));
		msg.location = ((XdmNode) assertion).getAttributeValue(new QName("location"));
		msg.error = !((net.sf.saxon.value.BooleanValue) Saxon.evaluateXPath(assertion, "(preceding-sibling::svrl:fired-rule)[last()]/@role='warning'").evaluateSingle().getUnderlyingValue()).getBooleanValue();
		msg.text = assertion.getStringValue();
		msgList.add(msg);
	    }
	    validationReport = null;
	}
	return msgList;
    }

    /**
     * Print the list of messages accumulated in the last validation run.
     * 
     * @param out
     * @throws Exception 
     */
    public void printMessages(java.io.PrintStream out) throws Exception {
	for (Message msg : getMessages()) {
	    out.println("" + (msg.isError() ? "ERROR" : "WARNING") + (msg.getLocation() != null ? " at " + msg.getLocation() : ""));
	    out.println("  " + msg.getText());
	}
    }

    /**
     * @param args One or more CMD profile/component files to validate.
     */
    public static void main(String[] args) {
	try {
	    Validator cmdValidator = new Validator(new URL(CMD_SCHEMA_URL));
	    for (int i = 1; i < args.length; i++) {
		String f = args[i];
		System.out.print("CMD validate[" + f + "] ");
		try {
		    Source src = new javax.xml.transform.stream.StreamSource(new java.io.File(f));
		    if (cmdValidator.validateProfile(src)) {
			System.out.println("valid");
		    } else {
			System.out.println("invalid");
		    }
		    cmdValidator.printMessages(System.out);
		} catch (Exception e) {
		    System.err.println("failed:");
		    e.printStackTrace(System.out);
		}
	    }
	} catch (MalformedURLException e) {
	    System.err.println("failed:");
	    e.printStackTrace(System.out);
	}
    }

    /**
     * Public inner class to represent validation messages.
     */
    public final class Message {

	/**
	 * Is the message and error or an warning?
	 */
	boolean error = false;
	/**
	 * The context of the message (might be null).
	 */
	String context = null;
	/**
	 * The test that triggered the message (might be null).
	 */
	String test = null;
	/**
	 * The location that triggered the test (might be null).
	 */
	String location = null;
	/**
	 * The actual message.
	 */
	String text = null;

	/**
	 * @return the error
	 */
	public boolean isError() {
	    return error;
	}

	/**
	 * @return the context
	 */
	public String getContext() {
	    return context;
	}

	/**
	 * @return the test
	 */
	public String getTest() {
	    return test;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
	    return location;
	}

	/**
	 * @return the text
	 */
	public String getText() {
	    return text;
	}
    }
}
