package org.systemsbiology.gaggle.core;

import java.rmi.*;
import org.systemsbiology.gaggle.core.datatypes.Table;

/**
 * Extension of the Boss interface.
 * @author Wei-ju Wu
 */
public interface Boss2 extends Boss {

    /**
     * Broadcasts a Gaggle table.
     * @param sourceGoose name of source goose
     * @param targetGoose name of target goose
     * @param table table to broadcast
     */
    public void broadcastTable(String sourceGoose, String targetGoose,
                               Table table) throws RemoteException;

    /**
     * Broadcasts a string in JSON format.
     * @param sourceGoose name of the source goose
     * @param targetGoose name of target goose. If this is "boss", all listening geese will
     * @param json a string in JSON format
     * receive the broadcast
     */
    public void broadcastJSON(String sourceGoose, String targetGoose,
                              String json) throws RemoteException;

    /**
     * Registers a JSONGoose.
     * @param goose a JSON goose
     * @return the unique goose name
     */
    public String register(JSONGoose goose) throws RemoteException;
}
