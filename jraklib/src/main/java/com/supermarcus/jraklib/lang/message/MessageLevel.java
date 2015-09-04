package com.supermarcus.jraklib.lang.message;

public enum MessageLevel {
    FATAL(3), ERROR(2), WARN(1), INFO(0);

    private int level;

    MessageLevel(int level){
        this.level = level;
    }

    public int getLevel(){
        return level;
    }
}
