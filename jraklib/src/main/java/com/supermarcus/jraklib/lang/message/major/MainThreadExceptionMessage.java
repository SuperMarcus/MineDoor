package com.supermarcus.jraklib.lang.message.major;

import com.supermarcus.jraklib.lang.message.MessageLevel;

public class MainThreadExceptionMessage extends MajorMessage {
    private Thread thread;

    private Throwable throwable;

    public MainThreadExceptionMessage(Thread thread, Throwable throwable){
        super(MessageLevel.FATAL);
        this.thread = thread;
        this.throwable = throwable;
    }

    public Thread getThread(){
        return thread;
    }

    public Throwable getThrowable(){
        return throwable;
    }
}
