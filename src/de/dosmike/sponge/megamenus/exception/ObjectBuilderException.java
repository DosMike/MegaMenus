package de.dosmike.sponge.megamenus.exception;

/** This Exception is meant to be thrown if a Builder or builder-like method
 * can not finish returning a valid Object due to missconfiguration of the builder
 * caused by the application/user. */
public class ObjectBuilderException extends RuntimeException {
    public ObjectBuilderException() {
        super();
    }

    public ObjectBuilderException(String message) {
        super(message);
    }

    public ObjectBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectBuilderException(Throwable cause) {
        super(cause);
    }
}
