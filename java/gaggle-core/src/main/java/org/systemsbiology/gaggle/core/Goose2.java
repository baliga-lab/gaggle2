package org.systemsbiology.gaggle.core;

import java.rmi.*;
import org.systemsbiology.gaggle.core.datatypes.*;

/**
 * Extension of the Goose interface to handle more data types without
 * breaking backwards compatibility.
 */
public interface Goose2 extends Goose {
    /**
     * Handles a table data object.
     * @param source source goose name
     * @param table the table to handle
     * @throws RemoteException on remote error
     */
    public void handleTable(String source, Table table) throws RemoteException;
}
