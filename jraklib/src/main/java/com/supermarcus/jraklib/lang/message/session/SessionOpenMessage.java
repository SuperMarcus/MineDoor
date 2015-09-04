package com.supermarcus.jraklib.lang.message.session;

import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.lang.message.MessageLevel;

public class SessionOpenMessage extends SessionMessage {
    public SessionOpenMessage(Session session) {
        super(MessageLevel.INFO, session);
    }
}
