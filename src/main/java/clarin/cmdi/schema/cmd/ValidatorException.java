/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.schema.cmd;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ValidatorException extends Exception {

    public ValidatorException() {
    }

    public ValidatorException(String message) {
	super(message);
    }

    public ValidatorException(Throwable cause) {
	super(cause);
    }

    public ValidatorException(String message, Throwable cause) {
	super(message, cause);
    }
    
}
