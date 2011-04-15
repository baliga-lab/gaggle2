package org.systemsbiology.gaggle.core;

public interface Boss2 {
    /**
     * Broadcasts a string in JSON format.
     * @param sourceGoose name of the source goose
     * @param targetGoose name of target goose. If this is "boss", all listening geese will
     * @param json a string in JSON format
     * receive the broadcast
     */
    public void broadcastJson(String sourceGoose, String targetGoose, String json);
}