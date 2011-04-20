package org.systemsbiology.gaggle.core;

import java.rmi.*;

/**
 * JSONGoose interface does not inherit from the Goose interface, but
 * offers the mo
 */
public interface JSONGoose extends Remote {
    void handleJSON(String source, String json) throws RemoteException;
    String getName() throws RemoteException;
    void setName(String name) throws RemoteException;
    void update(String[] gooseNames) throws RemoteException;
}
