package io.fabric8.api.container;

/**
 * LifecycleException
 *
 * @author Thomas.Diesler@jboss.com
 * @since 26-Feb-2014
 */
public class LifecycleException extends Exception {
    private static final long serialVersionUID = 1L;

    public LifecycleException(String message) {
        super(message);
    }

    public LifecycleException(String message, Throwable cause) {
        super(message, cause);
    }

}