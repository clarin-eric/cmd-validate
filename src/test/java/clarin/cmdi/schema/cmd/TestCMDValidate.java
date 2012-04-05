/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.schema.cmd;

import clarin.cmdi.schema.cmd.Validator.Message;
import java.util.List;
import javax.xml.transform.Source;
import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 * @author menwin
 */
public class TestCMDValidate {

    private Validator cmdValidator;

    @Before
    public void setUp() {
	cmdValidator = new Validator(getClass().getResource("/schema/general-component-schema.xsd"));
	cmdValidator.setResourceResolver(new ResourceResolver());
    }

    @After
    public void tearDown() {
	cmdValidator = null;
    }

    protected boolean validate(String prof) throws Exception {
	System.out.print("Test CMD validation [" + prof + "] ");
	Source src = new javax.xml.transform.stream.StreamSource(new java.io.File(TestCMDValidate.class.getResource("/docs/" + prof).toURI()));
	boolean valid = cmdValidator.validateProfile(src);
	if (valid) {
	    System.out.println("valid");
	} else {
	    System.out.println("invalid");
	}
	cmdValidator.printMessages(System.out);
	return valid;
    }

    @Test
    public void valid() throws Exception {
	assertTrue(validate("CLARINWebService.xml"));
	assertEquals(0, cmdValidator.getMessages().size());
    }

    @Test
    public void invalid_1() throws Exception {
	assertFalse(validate("CLARINWebService_faulty-1.xml"));

	final List<Message> messages = cmdValidator.getMessages();
	assertEquals(1, messages.size());
	Message message = messages.get(0);
	assertTrue(message.error);
	assertEquals("/CMD_ComponentSpec[1]/CMD_Component[1]/CMD_Component[1]/AttributeList[1]/Attribute[1]", message.location);
	assertEquals("not(Name=('ref','ComponentId'))", message.test);
	assertNotNull(message.text);
    }

    @Test
    public void invalid_2() throws Exception {
	assertFalse(validate("CLARINWebService_faulty-2.xml"));

	final List<Message> messages = cmdValidator.getMessages();
	assertEquals(1, messages.size());
	Message message = messages.get(0);
	assertTrue(message.error);
	assertEquals("/CMD_ComponentSpec[1]/CMD_Component[1]/CMD_Component[1]/AttributeList[1]/Attribute[1]/ValueScheme[1]/enumeration[1]/item[2]", message.location);
	assertEquals("empty(preceding-sibling::item[.=current()])", message.test);
	assertNotNull(message.text);
    }

    @Test
    public void invalid_3() throws Exception {
	assertFalse(validate("CLARINWebService_faulty-3.xml"));

	final List<Message> messages = cmdValidator.getMessages();
	assertEquals(1, messages.size());
	Message message = messages.get(0);
	assertTrue(message.error);
	assertEquals("/CMD_ComponentSpec[1]/CMD_Component[1]/CMD_Component[1]/AttributeList[1]/Attribute[2]", message.location);
	assertEquals("empty(preceding-sibling::Attribute[Name=current()/Name])", message.test);
	assertNotNull(message.text);
    }

    @Test
    public void invalid_4() throws Exception {
	assertFalse(validate("CLARINWebService_faulty-4.xml"));

	final List<Message> messages = cmdValidator.getMessages();
	assertEquals(1, messages.size());
	Message message = messages.get(0);
	assertTrue(message.error);
	assertEquals("/CMD_ComponentSpec[1]/CMD_Component[1]/CMD_Component[1]/CMD_Component[2]", message.location);
	assertEquals("empty(preceding-sibling::*[@name=current()/@name])", message.test);
	assertNotNull(message.text);
    }

    @Test
    public void invalid_5() throws Exception {
	assertFalse(validate("CLARINWebService_faulty-5.xml"));

	final List<Message> messages = cmdValidator.getMessages();
	assertEquals(1, messages.size());
	Message message = messages.get(0);
	assertTrue(message.error);
	assertEquals("/CMD_ComponentSpec[1]/CMD_Component[1]/CMD_Component[1]/CMD_Element[3]", message.location);
	assertEquals("empty(preceding-sibling::*[@name=current()/@name])", message.test);
	assertNotNull(message.text);
    }

    @Test
    public void invalid_6() throws Exception {
	assertFalse(validate("CLARINWebService_faulty-6.xml"));

	final List<Message> messages = cmdValidator.getMessages();
	assertEquals(1, messages.size());
	Message message = messages.get(0);
	assertTrue(message.error);
	assertEquals("/CMD_ComponentSpec[1]/CMD_Component[1]/CMD_Component[1]/CMD_Component[1]", message.location);
	assertEquals("empty(preceding-sibling::*[@name=current()/@name])", message.test);
	assertNotNull(message.text);
    }

    @Test
    public void invalid_7() throws Exception {
	assertFalse(validate("CLARINWebService_faulty-7.xml"));

	final List<Message> messages = cmdValidator.getMessages();
	assertEquals(1, messages.size());
	Message message = messages.get(0);
	assertTrue(message.error);
	assertNull(message.location);
	assertNull(message.test);
	assertNotNull(message.text);
    }
}
