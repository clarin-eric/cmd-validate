package clarin.cmdi.xml;

import javax.xml.transform.Source;
import net.sf.saxon.s9api.*;

/**
 * This class contains some convenience methods for Saxon.
 * 
 * @author menwin
 */
public class Saxon {

    /**
     * The Saxon processor from which there should be only one.
     * Any Saxon related instance, e.g., an XML document or an XSLT transform, should share this processor.
     * Otherwise Saxon will complain as it can't used shared constructs, like the NamePool.
     */
    static private Processor sxProcessor = null;
    /**
     * The Saxon XSLT compiler.
     */
    static private XsltCompiler sxXsltCompiler = null;
    /**
     * The Saxon XPath compiler.
     */
    static private XPathCompiler sxXPathCompiler = null;

    /**
     * Get a Saxon processor, i.e., just-in-time create the Singleton.
     * 
     * @return  The Saxon processor
     */
    static synchronized public Processor getProcessor() {
	if (sxProcessor == null) {
	    sxProcessor = new Processor(false);
	}
	return sxProcessor;
    }

    /**
     * Load an XML document.
     * 
     * @param src The source of the document.
     * @return A Saxon XDM node
     * @throws SaxonApiException 
     */
    static public XdmNode buildDocument(Source src) throws SaxonApiException {
	return getProcessor().newDocumentBuilder().build(src);
    }

    /**
     * Compile an XLST document. To use compiled XSLT document use the load() method to turn it into a XsltTransformer.
     * 
     * @param xslStylesheet
     * @return An Saxon XSLT executable, which can be shared.
     * @throws SaxonApiException 
     */
    static public XsltExecutable buildTransformer(XdmNode xslStylesheet) throws SaxonApiException {
	if (sxXsltCompiler == null) {
	    sxXsltCompiler = getProcessor().newXsltCompiler();
	}
	return sxXsltCompiler.compile(xslStylesheet.asSource());
    }

    /**
     * Declare an XML namespace to be used in XPath expressions.
     * 
     * @param nsPrefix The prefix used by the XPath expression to refer to the namespace.
     * @param nsUri The actual namespace URI.
     */
    static public void declareXPathNamespace(String nsPrefix, String nsUri) {
	if (sxXPathCompiler == null) {
	    sxXPathCompiler = getProcessor().newXPathCompiler();
	}
	sxXPathCompiler.declareNamespace(nsPrefix, nsUri);
    }

    /**
     * Compile an XPath expression. Use evaluate(), evaluateSingle() or iterator() to actually execute the XPath expression.
     * 
     * @param xp The XPath expression.
     * @return A compiled XPath expression.
     * @throws SaxonApiException 
     */
    static public XPathSelector compileXPath(String xp) throws SaxonApiException {
	if (sxXPathCompiler == null) {
	    sxXPathCompiler = getProcessor().newXPathCompiler();
	}
	return sxXPathCompiler.compile(xp).load();
    }

    /**
     * Compile an XPath expression and set the context item. Use evaluate(), evaluateSingle() or iterator() to actually execute the XPath expression.
     * 
     * @param ctxt The context item.
     * @param xp The XPath expression.
     * @return A compiled XPath expression.
     * @throws SaxonApiException 
     */
    static public XPathSelector evaluateXPath(XdmItem ctxt, String xp) throws SaxonApiException {
	XPathSelector sxXPathSelector = compileXPath(xp);
	sxXPathSelector.setContextItem(ctxt);
	return sxXPathSelector;
    }
}
