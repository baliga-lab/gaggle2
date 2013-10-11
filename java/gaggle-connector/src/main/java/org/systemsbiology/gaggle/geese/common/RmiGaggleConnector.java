package org.systemsbiology.gaggle.geese.common;

import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.*;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.security.Security;

import org.systemsbiology.gaggle.core.*;


/**
 * Handles connecting and disconnecting from the Gaggle Boss and exporting the Goose
 * as an RMI remote object. Listeners can be registered which will be notified of
 * connect and disconnect events.
 * 
 * TODO: better exception handling.
 * 
 * @author cbare
 */
public class RmiGaggleConnector {
    /**
     * todo - Make sure this is the correct URL if you update the API.
     * todo - factor this out to the GaggleConstants interface
     */
    private Goose goose;
    private JSONGoose jsonGoose;
    private Boss2 boss;
    private final static String DEFAULT_HOSTNAME = "localhost";
    private String serviceName = "gaggle";
    private String hostname = DEFAULT_HOSTNAME;
    private String uri = "rmi://" + hostname + "/" + serviceName;
    private Set<GaggleConnectionListener> listeners =
        new CopyOnWriteArraySet<GaggleConnectionListener>();

    private boolean exported  = false;
    private boolean autoStartBoss = true;
    private long timerInterval = 200L; //milliseconds
    private long timerTimeout = 15000L; // 15 seconds

    // a hack to avoid seeing unwanted stack traces. We should think about using log4j. -jcb
    private boolean verbose = true;
    private static Logger Log = Logger.getLogger("RmiGaggleConnector"); 

    public RmiGaggleConnector(Goose goose) {
        this(goose, null);
    }

    public RmiGaggleConnector(JSONGoose jsonGoose) {
        this(null, jsonGoose);
    }

    /**
     * @param goose a non-null goose
     */
    private RmiGaggleConnector(Goose goose, JSONGoose jsonGoose) {
        Security.setProperty("networkaddress.cache.ttl","0");
        Security.setProperty("networkaddress.cache.negative.ttl","0");
        Log.info("ttl settings changed in goose");
        
        if (goose == null && jsonGoose == null)
            throw new NullPointerException("RmiGaggleConnector requires a non-null goose.");
        this.goose = goose;
        this.jsonGoose = jsonGoose;

        if (goose != null && goose instanceof GaggleConnectionListener) {
        	addListener((GaggleConnectionListener)goose);
        }
        if (jsonGoose != null && jsonGoose instanceof GaggleConnectionListener) {
        	addListener((GaggleConnectionListener)jsonGoose);
        }
    }

    private synchronized void exportObject(Goose goose) throws Exception {
        try {
            UnicastRemoteObject.exportObject(goose, 0);
            exported = true;
        } catch (Exception e) {
            Log.severe("RmiGaggleConnector failed to export remote object: "
                       + e.getMessage());
            throw e;
        }
    }
    private synchronized void exportObject(JSONGoose jsonGoose) throws Exception {
        try {
            UnicastRemoteObject.exportObject(jsonGoose, 0);
            exported = true;
        } catch (Exception e) {
            Log.severe("RmiGaggleConnector failed to export remote object: "
                       + e.getMessage());
            throw e;
        }
    }

    public synchronized void connectToGaggle(String hostname) throws Exception {
        this.hostname = hostname;
        uri = "rmi://" + hostname + "/" + serviceName;
        connectToGaggle();
    }



    /**
     * connect to the Gaggle Boss, performing RMI exportObject if necessary.
     * @throws Exception if connection cannot be performed
     */
    public synchronized void connectToGaggle() throws Exception {
        Log.info("connectToGaggle(%s)".format(uri));

        try {
            // if goose is not already a live RMI object, make it so
            if (!exported && goose != null) exportObject(goose);
            else if (!exported && jsonGoose != null) exportObject(jsonGoose);

            // connect to the Boss
            if (autoStartBoss && hostname.equals(DEFAULT_HOSTNAME)) {
                Log.info("AUTOSTART BOSS & DEFAULT HOST");
                try {
                    boss = (Boss2) Naming.lookup(uri);
                } catch (Exception ex) {
                    Log.info("EXCEPT MESSAGE: " + ex.getMessage());
                    if (ex.getMessage().startsWith("Connection refused to host:")) {
                        System.out.println("Couldn't find a boss, trying to start one....");
                        tryToStartBoss();
                    }
                }

            } else {
                Log.info("NO AUTOSTART, CONNECT TO EXIST");
                boss = (Boss2) Naming.lookup(uri);
            }
            registerGoose();
            fireConnectionEvent(true);
        } catch (NullPointerException npe) {
            Log.warning("Boss isn't quite ready yet, trying again...");
            npe.printStackTrace();
        } catch (Exception e) {
            if (!autoStartBoss) {
                Log.severe("failed to connect to gaggle at " + uri + ": " + e.getMessage());
                if (verbose) e.printStackTrace();
            }
            boss = null;
            fireConnectionEvent(false);
            throw e;
        }
    }

    private void registerGoose() throws java.rmi.RemoteException {
        if (goose != null) {
            String gooseName = boss.register(goose);
            goose.setName(gooseName);
        } else if (jsonGoose != null) {
            String gooseName = boss.register(jsonGoose);
            jsonGoose.setName(gooseName);
        }
    }

    class WaitForBossStart extends TimerTask {
        long startTime = System.currentTimeMillis();

        public void run() {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timerTimeout) {
                Log.info("Didn't hear from the boss for 15 seconds, timing out.");
                this.cancel();
            }

            try {
                Naming.lookup(uri);
                connectToGaggle();
                this.cancel();
            } catch(ConnectException ce) {
                Log.severe("ConnectException in WaitForBossStart");
            } catch (ClassNotFoundException cnfe) {
                try {
                   connectToGaggle();
                   this.cancel();
                } catch (Exception ex) {
                    Log.severe("exception trying to connect using boss autostart: " + ex.getMessage());
                }
            } catch (Exception ex) {
                // reduce noise by not printing anything here. This will be
                // caught on every timer event until a boss is found or until
                // timeout
                //Log.log(Level.WARNING, "unknown Exception in WaitForBossStart: " + ex.getMessage());
                //System.out.println("general exception trying to autostart boss: " + ex.getMessage());
                // ex.printStackTrace();
            }
        }
    }

    private void tryToStartBoss() {
        //String command = System.getProperty("java.home");
        //command += File.separator +  "bin" + File.separator + "javaws " + GaggleConstants.BOSS_URL;

        String jwsdir = System.getProperty("java.home");
        jwsdir = jwsdir.replace("\\", "\\\\");
        jwsdir += File.separator + "bin";
        try {
            ProcessBuilder pb = new ProcessBuilder("javaws", GaggleConstants.BOSS_URL);
            pb.directory(new File(jwsdir));
            //Runtime.getRuntime().exec(command);
            Process p = pb.start();
            Timer timer = new Timer();
            timer.schedule(new WaitForBossStart(), 0, timerInterval);
        } catch (Exception e) {
            Log.severe("Failed to start boss process first time!");
            e.printStackTrace();
        }
    }

    /**
     * remove this goose from the Boss and unexport.
     * You can suppress the stack trace if you are calling from a shutdown
     * hook and the boss is not running.
     * @param printStackTrace allows a stack trace to be printed if the call fails
     */
    public synchronized void disconnectFromGaggle(boolean printStackTrace) {
        if (goose != null) disconnectJavaGooseFromGaggle(printStackTrace);
        else if (jsonGoose != null) disconnectJSONGooseFromGaggle(printStackTrace);
    }

    private void disconnectJavaGooseFromGaggle(boolean printStackTrace) {
        if (boss != null) {
            try {
                Log.info("received disconnect request from " + goose.getName());
                boss.unregister(goose.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            boss = null;
        }

        if (exported) {
            try {
                Log.info("received disconnect request from " + goose.getName());
                UnicastRemoteObject.unexportObject(goose, true);
            } catch (Exception e) {
                if (printStackTrace) e.printStackTrace();
            }
            exported = false;
        }
        fireConnectionEvent(false);
    }

    private void disconnectJSONGooseFromGaggle(boolean printStackTrace) {
        if (boss != null) {
            try {
                Log.info("received disconnect request from " + jsonGoose.getName());
                boss.unregister(jsonGoose.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            boss = null;
        }

        if (exported) {
            try {
                Log.info("received disconnect request from " + jsonGoose.getName());
                UnicastRemoteObject.unexportObject(jsonGoose, true);
            } catch (Exception e) {
                if (printStackTrace) e.printStackTrace();
            }
            exported = false;
        }
        fireConnectionEvent(false);
    }

    /**
     * listeners will be notified on connect and disconnect.
     * @param listener The listener to add
     */
    public synchronized void addListener(GaggleConnectionListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(GaggleConnectionListener listener) {
        listeners.remove(listener);
    }

    private synchronized void fireConnectionEvent(boolean connected) {
        for (GaggleConnectionListener listener : listeners) {
            try {
                listener.setConnected(connected, boss);
            } catch (Exception e) {
                //listener may have gone away
                e.printStackTrace();
            }
        }
    }
    
    public synchronized boolean isConnected() { return boss != null; }

    /**
     * Determines whether we should try and start a boss if a boss cannot
     * be found; true by default.
     * @param autoStartBoss whether to try and start a boss if no boss is found
     */
    public void setAutoStartBoss(boolean autoStartBoss) {
        this.autoStartBoss = autoStartBoss;
    }

    public boolean getAutoStartBoss() { return autoStartBoss; }

    /**
     * @return Boss if connected or null otherwise.
     */
    public synchronized Boss getBoss() { return boss; }

    public boolean isVerbose() { return verbose; }

    public void setVerbose(boolean verbose) { this.verbose = verbose; }

    public void setTimerTimeout(long timerTimeout) {
        this.timerTimeout = timerTimeout;
    }
}
