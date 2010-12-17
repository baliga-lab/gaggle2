/*
 * BridgeApplet.java
 * Copyright (C) 2010 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.bridge;

import java.io.Serializable;
import java.util.List;
import javax.swing.*;
import netscape.javascript.*;
import java.security.*;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import org.systemsbiology.gaggle.core.datatypes.*;

/**
 * The BridgeApplet is intended to start an invisible component in the browser
 * which acts as a surrogate to the Boss. Requests to its embedded RMI server
 * are relayed to the Javascript based Boss contained in the web page this applet
 * was started from.
 * An RMI registry is created on localhost, port RMI_PORT, in case it exists, the boss will
 * registered with the existing Registry, if SERVICE_NAME is not bound. If it is bound, it
 * is assumed, that the service was already started somewhere else (this could be the old
 * Boss or the start page was opened twice).
 * <b>Note:</b> The applet <b>must</b> be signed in order to use RMI functionality.
 */
public class BridgeApplet extends JApplet {

    private static final String SERVICE_NAME = "gaggle";
    private static final int RMI_PORT = 1099;
    private BridgeBoss boss;
    public BridgeBoss getBoss() { return boss; }

    private void shutdownCurrentBoss() {
        try {
            if (boss != null && boss.isMaster()) {
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
        } finally {
            boss = null;
        }
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        System.out.println("BridgeApplet.destroy()");
        shutdownCurrentBoss();
    }

    /**
     * We are initializing from a Javascript function, so we need to do this in
     * privileged mode to prevent the SecurityManager complaining about the
     * Javascript context on the stack.
     */
    public void initBridge() {
        AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    doInitBridge();
                    return null;
                }
            });
    }

    /**
     * Initialize the RMI bridge. We let the client initialize the bridge expicitly
     * to avoid unnecessary thread synchronization.
     */
    private void doInitBridge() {
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
                System.out.print("register bridge boss service...");
                boss = new BridgeBoss(doc);
                Naming.bind(SERVICE_NAME, boss);
                System.out.println("done.");
                System.out.println("Gaggle Boss Bridge ready to go !");
            } catch (java.rmi.AlreadyBoundException ex) { 
                System.out.println("Gaggle Boss already exists (probably GUI Boss ?)");
            } catch (MalformedURLException ex) {
                // should not happen
                System.out.println("error, reason: ");
                ex.printStackTrace();
            }
        } catch (RemoteException ex) {
            // any errors we did not think about
            ex.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override public void start() {
        System.out.println("BridgeApplet.start()");
    }

    // unused Applet lifecycle functions, put in here for diagnostics reasons
    /** {@inheritDoc} */
    @Override public void init() {
        System.out.println("BridgeApplet.init()");
    }

    /** {@inheritDoc} */
    @Override public void stop() {
        System.out.println("BridgeApplet.stop()");
    }

    // Provide factory methods for all core Gaggle RMI data types.
    // Creation of Java custom objects seems to be different on different
    // browsers, so we use the Applet as a portable factory
    public Single createSingle(String name, Serializable value) {
        return new Single(name, value);
    }
    public Tuple createTuple(String name, List<Single> singleList) {
        return new Tuple(name, singleList);
    }
    public GaggleTuple createGaggleTuple() { return new GaggleTuple(); }
    public Namelist createNamelist(String name, String species, String[] names) {
        return new Namelist(name, species, names);
    }
    public Cluster createCluster(String name, String species, String[] rowNames,
                                 String[] colNames) {
        return new Cluster(name, species, rowNames, colNames);
    }
    public Network createNetwork() { return new Network(); }
    public Interaction createInteraction(String source, String target, String interactionType,
                                         boolean directed) {
        return new Interaction(source, target, interactionType, directed);
    }
    public DataMatrix createDataMatrix(String uri) {
        return new DataMatrix(uri);
    }
}
