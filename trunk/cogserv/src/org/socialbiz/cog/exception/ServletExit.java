package org.socialbiz.cog.exception;

/**
 * This Throwable object is NOT used as an exception, but rather as a way
 * to jump out of execution of a current line of processing.
 * For example, in a servlet, you can send a "redirect" to the browser,
 * you then need to quit the logic, and not write anything else.
 * This is useful for "ASSERT" type logic where a condition is tested,
 * the browser is redirected, and you just want ot exit out of the rest.
 * It is useful for cases where an exception has been caught at the
 * top level, recorded, and then this is thrown to get the rest of the
 * way out.
 *
 * Throwing a ServletExit is like exiting a program, or quitting a program
 * only it does not kill the entire JVM, instead it it just quits the
 * handling of this particular browser request.
 *
 * This does not carry a message.  This class should never be used to wrap
 * another exception.  An exception should be handled or recorded, and
 * then this can be used to get the rest of the way out of the call stack.
 */
@SuppressWarnings("serial")
public class ServletExit extends Exception
{

    public ServletExit() {
        super();
    }


}
