/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.schema.cmd;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ResourceResolver implements LSResourceResolver {

    private CatalogResolver catRes = new CatalogResolver();

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
	InputSource resolveEntity = catRes.resolveEntity(publicId, systemId);
	resolveEntity.setEncoding("UTF-8");
	DOMImplementationLS domImplementation;
	try {
	    domImplementation = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
	} catch (ClassCastException e) {
	    throw new RuntimeException(e);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	} catch (InstantiationException e) {
	    throw new RuntimeException(e);
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e);
	}
	LSInput lsInput = domImplementation.createLSInput();
	lsInput.setEncoding("UTF-8");
	lsInput.setByteStream(resolveEntity.getByteStream());
	lsInput.setCharacterStream(resolveEntity.getCharacterStream());
	return lsInput;
    }
}
