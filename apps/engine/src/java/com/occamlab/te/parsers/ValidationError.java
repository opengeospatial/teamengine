package com.occamlab.te.parsers;

/**
 * Encapsulates information pertaining to a validation error. Instances of this
 * class are immutable.
 *
 * @author rmartell
 * @version $Rev: 1 $
 */
public class ValidationError {

    /**
     * A warning (e.g., a condition that does not cause the instance to be
     * non-conforming.
     */
    public static final short WARNING = 1;

    /** An error (e.g., the instance is invalid). */
    public static final short ERROR = 2;

    /** A fatal error (e.g., the instance is not well-formed). */
    public static final short FATAL_ERROR = 3;

    /** The error message. */
    private String message;

    /** The severity level. */
    private short severity;

    /**
     * Constructs an immutable error object.
     *
     * @param severity
     *            the severity level (Warning, Error, Fatal)
     * @param message
     *            a descriptive message
     */
    public ValidationError(short severity, String message) {
        if (null == message) {
            message = "No details available";
        }
        this.message = message;
        this.severity = severity;
    }

    /**
     * Returns the message describing this error.
     *
     * @return the details about this error
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the severity code (a <code>short</code> value) for this error.
     *
     * @return the severity code for this error
     */
    public short getSeverity() {
        return severity;
    }
}
