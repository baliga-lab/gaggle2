package org.systemsbiology.gaggle.core;

import java.rmi.*;

/**
 * Extension of the Boss interface.
 * @author Wei-ju Wu
 */
public interface Boss2 extends Boss {
    /**
     * Broadcasts a string in JSON format.
     * @param sourceGoose name of the source goose
     * @param targetGoose name of target goose. If this is "boss", all listening geese will
     * @param json a string in JSON format
     * receive the broadcast
     */
    public void broadcastJson(String sourceGoose, String targetGoose, String json) throws RemoteException;

    /**
     * Registers a JSONGoose.
     * @param goose a JSON goose
     * @return the unique goose name
     */
    public String register(JSONGoose goose) throws RemoteException;
}
