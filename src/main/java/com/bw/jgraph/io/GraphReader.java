package com.bw.jgraph.io;

public class GraphReader {
    
    public boolean isFieldNull() {
        return true;
    }

    public void skip() {
    }

    public boolean isFieldNumeric() {
        return false;
    }

    public Number readNumber() {
        return 0;
    }

    public boolean isFieldObject() {
        return false;
    }

    public boolean hasNextField() {
        return false;
    }

    public void startObject() {
    }

    public String readString() {
        return "";
    }

    public Object readObject() {
        return null;
    }
}
