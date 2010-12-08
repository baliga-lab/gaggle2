package org.systemsbiology.gaggle.bridge;

import javax.swing.*;
import netscape.javascript.*;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/**
 * The BridgeApplet is intended to start an invisible component in the browser
 * which acts as a surrogate to the Boss. Requests to its embedded RMI server
 * are relayed to the Javascript based Boss contained in the web page this applet
 * was started from.
 * An RMI registry is created on localhost, port RMI_PORT, in case it exists, the boss will
 * registered with the existing Registry.
 * Note: The applet <b>must</b> be signed in order to use RMI functionality.
 */
public class BridgeApplet extends JApplet {

    private static final String SERVICE_NAME = "gaggle";
    private static final int RMI_PORT = 1099;
    private BridgeBoss boss;
    private boolean bossWasBound;

    private void shutdownCurrentBoss() {
        try {
            if (bossWasBound) {
                Naming.unbind(SERVICE_NAME);
                boolean result = UnicastRemoteObject.unexportObject(boss, true);
                if (result) {
                    System.out.println("removed current boss service from RMI");
                } else {
                    System.out.println("could not remove boss service from RMI");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean bossWasBound() { return bossWasBound; }

    @Override public void destroy() {
        System.out.println("BridgeApplet.destroy()");
        shutdownCurrentBoss();
    }

    @Override public void start() {
        JSObject win = JSObject.getWindow(this);
        JSObject doc = (JSObject) win.getMember("document");
        try {
            try {
                System.out.printf("creating new registry on port %d...", RMI_PORT);
                LocateRegistry.createRegistry(RMI_PORT);
                System.out.println("done.");
            } catch (java.rmi.server.ExportException ex) {
                System.out.println("registry exists, we are using the existing one.");
            }
            try {
                bossWasBound = false;
                System.out.print("register bridge boss service...");
                boss = new BridgeBoss(doc);
                Naming.bind(SERVICE_NAME, boss);
                bossWasBound = true;
                System.out.println("done.");
                System.out.println("Gaggle Boss Bridge ready to go !");
            } catch (java.rmi.AlreadyBoundException ex) { 
                System.out.println("Gaggle Boss already exists (probably GUI Boss ?)");
            } catch (MalformedURLException ex) {
                System.out.println("error, reason: ");
                ex.printStackTrace();
            } catch (RemoteException ex) {
                System.out.println("error, reason: ");
                ex.printStackTrace();
            }
        } catch (RemoteException ex) {
            // any errors we did not think about
            ex.printStackTrace();
        }
    }

    // unused Applet lifecycle functions, put in here for diagnostics reasons
    @Override public void init() {
        System.out.println("BridgeApplet.init()");
    }

    @Override public void stop() {
        System.out.println("BridgeApplet.stop()");
    }

}
