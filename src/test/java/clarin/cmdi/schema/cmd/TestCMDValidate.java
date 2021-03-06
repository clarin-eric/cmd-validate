package clarin.cmdi.schema.cmd;

import clarin.cmdi.schema.cmd.Validator.Message;
import java.util.List;
import javax.xml.transform.Source;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * TODO: update tests and test resources to CMDI 1.2.
 *
 * @author menwin
 */
public class TestCMDValidate {

    private Validator cmdValidator;

    @Before
    public void setUp() {
        cmdValidator = new Validator(getClass().getResource("/schema/cmd-component.xsd"));
        cmdValidator.setResourceResolver(new ResourceResolver());
    }

    @After
    public void tearDown() {
        cmdValidator = null;
    }

    protected boolean validate(String prof) throws Exception {
        return validate(prof, null);
    }

    protected boolean validate(String prof, String phase) throws Exception {
        System.out.print("Test CMD validation [" + prof + "] ");
        Source src = new javax.xml.transform.stream.StreamSource(new java.io.File(TestCMDValidate.class.getResource("/docs/" + prof).toURI()));
        if (phase != null) {
            cmdValidator.setSchematronPhase(phase);
        }
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
    public void valid_0() throws Exception {
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
        assertNotNull(message.text);
    }

    @Test
    public void invalid_2() throws Exception {
        assertFalse(validate("CLARINWebService_faulty-2.xml"));

        final List<Message> messages = cmdValidator.getMessages();
        assertEquals(2, messages.size());
        {
            Message message = messages.get(0);
            assertTrue(message.error);
            assertEquals("/ComponentSpec[1]/Component[1]/Component[1]/AttributeList[1]/Attribute[1]/ValueScheme[1]/Vocabulary[1]/enumeration[1]/item[1]", message.location);
            assertEquals("count(key('enums', current(), current()/parent::enumeration)) eq 1", message.test);
            assertNotNull(message.text);
        }
        {
            Message message = messages.get(1);
            assertTrue(message.error);
            assertEquals("/ComponentSpec[1]/Component[1]/Component[1]/AttributeList[1]/Attribute[1]/ValueScheme[1]/Vocabulary[1]/enumeration[1]/item[2]", message.location);
            assertEquals("count(key('enums', current(), current()/parent::enumeration)) eq 1", message.test);
            assertNotNull(message.text);
        }
    }

    @Test
    public void invalid_3() throws Exception {
        assertFalse(validate("CLARINWebService_faulty-3.xml"));

        final List<Message> messages = cmdValidator.getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);
        assertTrue(message.error);
        assertEquals("/ComponentSpec[1]/Component[1]/Component[1]/AttributeList[1]/Attribute[2]", message.location);
        assertEquals("empty(preceding-sibling::Attribute[@name = current()/@name])", message.test);
        assertNotNull(message.text);
    }

    @Test
    public void invalid_4() throws Exception {
        assertFalse(validate("CLARINWebService_faulty-4.xml"));

        final List<Message> messages = cmdValidator.getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);
        assertTrue(message.error);
        assertEquals("/ComponentSpec[1]/Component[1]/Component[1]/Component[2]", message.location);
        assertEquals("empty(preceding-sibling::*[@name = current()/@name])", message.test);
        assertNotNull(message.text);
    }

    @Test
    public void invalid_5() throws Exception {
        assertFalse(validate("CLARINWebService_faulty-5.xml"));

        final List<Message> messages = cmdValidator.getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);
        assertTrue(message.error);
        assertEquals("/ComponentSpec[1]/Component[1]/Component[1]/Element[3]", message.location);
        assertEquals("empty(preceding-sibling::*[@name = current()/@name])", message.test);
        assertNotNull(message.text);
    }

    @Test
    public void invalid_6() throws Exception {
        assertFalse(validate("CLARINWebService_faulty-6.xml"));

        final List<Message> messages = cmdValidator.getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);
        assertTrue(message.error);
        assertEquals("/ComponentSpec[1]/Component[1]/Component[1]/Component[1]", message.location);
        assertEquals("empty(preceding-sibling::*[@name = current()/@name])", message.test);
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

    @Test
    public void invalid_8() throws Exception {
        assertFalse("There should be invalid cardinality values", validate("CLARINWebService_faulty-8.xml"));
        final List<Message> messages = cmdValidator.getMessages();
        assertEquals("There should be 5 invalid cardinality sets", 5, messages.size());
        assertEquals("UNK, number, maximum ne 0", "/ComponentSpec[1]/Component[1]/Element[3]", messages.get(0).location);
        assertEquals("number, UNK, minimum le 1", "/ComponentSpec[1]/Component[1]/Element[6]", messages.get(1).location);
        assertEquals("number, number, minimum le maximum", "/ComponentSpec[1]/Component[1]/Element[8]", messages.get(2).location);
        assertEquals("unbounded, UNK", "/ComponentSpec[1]/Component[1]/Element[10]", messages.get(3).location);
        assertEquals("unbounded, 1", "/ComponentSpec[1]/Component[1]/Element[11]", messages.get(4).location);
    }

    @Test
    public void invalid_9() throws Exception {
        assertFalse(validate("CLARINWebService_faulty-9.xml"));

        final List<Message> messages = cmdValidator.getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);
        assertTrue(message.error);
        assertEquals("/ComponentSpec[1]/Component[1]/Component[1]/Component[2]/Component[1]/Component[2]/Component[3]", message.location);
        assertEquals("empty(preceding-sibling::Component[@ComponentRef = current()/@ComponentRef])", message.test);
        assertNotNull(message.text);
    }

    @Test
    public void invalid_10() throws Exception {
        assertFalse(validate("CLARINWebService_faulty-10.xml"));

        final List<Message> messages = cmdValidator.getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);
        assertTrue(message.error);
        assertEquals("/ComponentSpec[1]", message.location);
        assertEquals("normalize-space(Header/ID) != ''", message.test);
        assertNotNull(message.text);
    }

    @Test
    public void valid_10_preRegPhase() throws Exception {
        final String phase = "preRegistration";
        assertTrue("Should be valid with phase " + phase, validate("CLARINWebService_faulty-10.xml", phase));
    }

    //add test for schematron phase
}
