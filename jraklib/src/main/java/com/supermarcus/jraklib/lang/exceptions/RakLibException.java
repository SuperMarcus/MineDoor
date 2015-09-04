package com.supermarcus.jraklib.lang.exceptions;

abstract public class RakLibException extends Exception {
    private int serverId = 0;

    public RakLibException(String message, int serverId){
        this(message);
        this.serverId = serverId;
    }

    public RakLibException(String message, Throwable cause, int serverId){
        this(message, cause);
        this.serverId = serverId;
    }

    public RakLibException(Throwable t, int serverId){
        this(t);
        this.serverId = serverId;
    }

    public RakLibException(String message){
        super(message);
    }

    public RakLibException(String message, Throwable cause){
        super(message, cause);
    }

    public RakLibException(Throwable t){
        super(t);
    }

    public int getInterfaceId(){
        return this.serverId;
    }
}
