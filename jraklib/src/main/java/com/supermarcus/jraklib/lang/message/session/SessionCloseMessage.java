package com.supermarcus.jraklib.lang.message.session;

import com.supermarcus.jraklib.Session;
import com.supermarcus.jraklib.lang.message.MessageLevel;

public class SessionCloseMessage extends SessionMessage {
    private Reason reason;

    public SessionCloseMessage(Reason reason, Session session) {
        super(MessageLevel.INFO, session);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        TIMEOUT,
        CLIENT_DISCONNECT
    }
}
