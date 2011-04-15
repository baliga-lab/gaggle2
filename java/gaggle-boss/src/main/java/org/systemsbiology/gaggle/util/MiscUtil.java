// MiscUtil.java
// miscellaneous utilities
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.util;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.systemsbiology.gaggle.core.Boss;

public class MiscUtil {

    private static final String GAGGLE_ICON =
        "http://gaggle.systemsbiology.net/images/icons/gaggle_icon_32x32.gif";

    static public void placeInCenter(JFrame frame) {
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        int screenHeight = (int) gc.getBounds().getHeight();
        int screenWidth = (int) gc.getBounds().getWidth();
        int windowWidth = frame.getWidth();
        int windowHeight = frame.getHeight();
        frame.setLocation ((screenWidth-windowWidth)/2, (screenHeight-windowHeight)/2);
    }

    static public void displayWebPage(String uriString) {
        try {
            displayWebPage(new URI(uriString));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static public void displayWebPage(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    static public String getPage(String urlString) throws Exception {
        return getPage(new URL(urlString));
    }

    static public void setApplicationIcon(JFrame frame) {
        try {
            frame.setIconImage(Toolkit.getDefaultToolkit()
                               .getImage(new URL(GAGGLE_ICON)));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    static public String getPage(URL url) throws Exception {
        int characterCount = 0;
        StringBuilder result            = new StringBuilder();
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        int responseCode                = urlConnection.getResponseCode();
        //String contentType              = urlConnection.getContentType();
        //int contentLength               = urlConnection.getContentLength();
        //String contentEncoding          = urlConnection.getContentEncoding();

        if (responseCode != HttpURLConnection.HTTP_OK)
            throw new IOException("\nHTTP response code: " + responseCode);

        BufferedReader theHTML = new BufferedReader 
            (new InputStreamReader(urlConnection.getInputStream()));
        String thisLine;
        while ((thisLine = theHTML.readLine()) != null) {
            result.append(thisLine);
            result.append(" ");
        }
        return result.toString();
    }

    /**
     *  this is a nop for java < 1.5
     */
    static public void setJFrameAlwaysOnTop(JFrame frame, boolean newValue) {
        frame.setAlwaysOnTop (newValue);
    }
    /**
     *  re-populate the chooser with all geese known to the boss, 'Boss', and any
     *  pseudo-geese the calling goose wants to add.  leave out the name of the
     *  calling goose.  alphabetize the names.
     * todo - remove when all geese implement update()
     * @deprecated - use updateGooseChooser() instead
     */
    static public void updateGooseChooserOLD(Boss boss,
                                             JComboBox chooser,
                                             String callingGoose, 
                                             String[] pseudoGeese) {
        try {
            DefaultComboBoxModel model = (DefaultComboBoxModel) chooser.getModel();
            model.removeAllElements();
            if (boss == null) {
                model.addElement("Not Connected to Boss");
                return;
            }
            model.addElement("Boss");

            String[] geese = null;
            try {
                geese = boss.getGooseNames();
            } catch (Exception e) {
                model.removeAllElements();
                model.addElement("Couldn't connect to Boss");
                throw e;
            }
            List<String> tmp = new ArrayList<String>();
            for (int i = 0; i < geese.length; i++) tmp.add(geese[i]);
            if (pseudoGeese != null && pseudoGeese.length > 0) {
                for (int i = 0; i < pseudoGeese.length; i++) {
                    tmp.add(pseudoGeese[i]);
                }
            }
            String[] allGeese = tmp.toArray(new String[0]);
            Arrays.sort(allGeese);
            for (int i = 0; i < allGeese.length; i++) {
                if (allGeese[i].equals(callingGoose)) continue;
                else model.addElement(allGeese[i]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Updates the UI to show the current list of geese. Removes the name of the
     * calling goose from the list, and sorts the remainder. Tries to preserve the
     * original selection in the list, if it still exists in the newly received
     * current goose list.
     * @param gooseChooser The UI element containing the list of geese
     * @param callingGoose The name of the calling goose
     * @param allGeese The list of all currently active geese
     */
    public static void updateGooseChooser(JComboBox gooseChooser,
                                          String callingGoose,
                                          String[] allGeese) {
        if (gooseChooser == null || allGeese == null) return;

        String savedItem = (String) gooseChooser.getSelectedItem();

        Arrays.sort(allGeese);
        DefaultComboBoxModel model = (DefaultComboBoxModel) gooseChooser.getModel();
        model.removeAllElements();
        model.addElement("Boss");

        for (String gooseName : allGeese) {
            if (!gooseName.equals(callingGoose)) {
                model.addElement(gooseName);
            }
        }
        // this will attempt to set selected item to what it was before.
        // if that item is no longer in the list, it will silently fail
        // and therefore Boss, as the first item, will be selected by default.
        gooseChooser.setSelectedItem(savedItem);
    }

    static public int countOccurences (String textToSearch, String pattern) {
        int base = 0;
        int count = 0;
        boolean done = false;
        while (!done) {
            base = textToSearch.indexOf (pattern, base);
            if (base > 0) {
                count++;
                base += 3;
            } else {
                done = true;
            }
        }
        return count;
    }
}
