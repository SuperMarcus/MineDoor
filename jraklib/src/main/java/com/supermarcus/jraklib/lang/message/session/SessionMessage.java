package com.supermarcus.jraklib.lang.message.session;

import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.lang.message.MessageLevel;
import com.supermarcus.jraklib.lang.message.RakLibMessage;

abstract public class SessionMessage extends RakLibMessage {
    private Session session;

    public SessionMessage(MessageLevel level, Session session) {
        super(level);
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
