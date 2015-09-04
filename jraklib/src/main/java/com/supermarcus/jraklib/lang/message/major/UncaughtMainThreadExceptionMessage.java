package com.supermarcus.jraklib.lang.message.major;

public class UncaughtMainThreadExceptionMessage extends MainThreadExceptionMessage {
    public UncaughtMainThreadExceptionMessage(Thread thread, Throwable throwable) {
        super(thread, throwable);
    }
}
