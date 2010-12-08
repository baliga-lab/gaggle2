package org.systemsbiology.gaggle.bridge;

import javax.swing.*;
import netscape.javascript.*;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;

/**
 * The BridgeApplet is intended to start an invisible component in the browser
 * which acts as a surrogate to the Boss. Requests to its embedded RMI server
 * are relayed to the Javascript based Boss contained in the web page this applet
 * was started from.
 */
public class BridgeApplet extends JApplet {

    class BridgeBoss extends UnicastRemoteObject implements Boss {
        public BridgeBoss() throws RemoteException {
            super();
        }

        // registration
        public String[] getGooseNames() {
            System.out.println("getGooseNames()");
            return new String[] { "goose1" };
        }
        public String renameGoose(String oldName, String newName) {
            System.out.printf("renameGoose from '%s' to '%s'\n", oldName, newName);
            return newName;
        }
        public String register(Goose goose) throws RemoteException {
            System.out.printf("register() goose '%s'\n", goose.getName());
            return goose.getName();
        }
        public void unregister(String gooseName) {
            System.out.printf("unregister() goose '%s'\n", gooseName);
        }

        // application control
        public void show(String gooseName) {
            System.out.printf("show goose '%s'\n", gooseName);
        }
        public void hide(String gooseName) {
            System.out.printf("hide goose '%s'\n", gooseName);
        }
        public void terminate(String gooseName) {
            System.out.printf("terminate goose '%s'\n", gooseName);
        }

        // broadcasting
        public void broadcastNetwork(String source, String target, Network network) {
            System.out.printf("broadcastNetwork() from '%s' to '%s'\n", source, target);
        }
        public void broadcastCluster(String source, String target, Cluster cluster) {
            System.out.printf("broadcastCluster() from '%s' to '%s'\n", source, target);
        }
        public void broadcastTuple(String source, String target, GaggleTuple tuple) {
            System.out.printf("broadcastTuple() from '%s' to '%s'\n", source, target);
        }
        public void broadcastMatrix(String source, String target, DataMatrix matrix) {
            System.out.printf("broadcastMatrix() from '%s' to '%s'\n", source, target);
        }
        public void broadcastNamelist(String source, String target, Namelist namelist) {
            System.out.printf("broadcastNamelist() from '%s' to '%s'\n", source, target);
        }
    }

    private static final String SERVICE_NAME = "gaggle";

    public void init() {
        JSObject win = JSObject.getWindow(this);
        System.out.println("window is: " + win);
    }

    public void start() {
        try {
            BridgeBoss boss = new BridgeBoss();
            LocateRegistry.createRegistry(1099);
            Naming.rebind(SERVICE_NAME, boss);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        System.out.println("Started the RMI Server");
    }
}
