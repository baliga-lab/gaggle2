package org.systemsbiology.gaggle.bridge;

import javax.swing.*;
import netscape.javascript.*;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * The BridgeApplet is intended to start an invisible component in the browser
 * which acts as a surrogate to the Boss. Requests to its embedded RMI server
 * are relayed to the Javascript based Boss contained in the web page this applet
 * was started from.
 * Note: The applet <b>must</b> be signed in order to use RMI functionality.
 */
public class BridgeApplet extends JApplet {

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
