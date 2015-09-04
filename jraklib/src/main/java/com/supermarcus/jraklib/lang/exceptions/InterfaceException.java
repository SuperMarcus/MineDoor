package com.supermarcus.jraklib.lang.exceptions;

abstract public class InterfaceException extends RakLibException {
    public InterfaceException(String message, int serverId) {
        super(message, serverId);
    }

    public InterfaceException(String message, Throwable cause, int serverId) {
        super(message, cause, serverId);
    }

    public InterfaceException(Throwable t, int serverId) {
        super(t, serverId);
    }

    public InterfaceException(String message) {
        super(message);
    }

    public InterfaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterfaceException(Throwable t) {
        super(t);
    }
}
