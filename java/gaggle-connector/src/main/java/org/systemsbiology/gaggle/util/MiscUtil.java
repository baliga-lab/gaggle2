/*
 * MiscUtil.java
 * miscellaneous utilities
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.util;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URI;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.Desktop;

public class MiscUtil {
    public static void placeInCenter(JFrame frame) {
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        int screenHeight         = (int) gc.getBounds().getHeight();
        int screenWidth          = (int) gc.getBounds().getWidth();
        int windowWidth          = frame.getWidth();
        int windowHeight         = frame.getHeight();
        frame.setLocation((screenWidth - windowWidth)/2, (screenHeight - windowHeight)/2);
    }

    public static void displayWebPage(String urlString) {
        try {
            displayWebPage(new URI(urlString));
        } catch (Exception ex0) {
            ex0.printStackTrace();
        }
    }
    public static void displayWebPage(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (Exception ex) {
            ex.printStackTrace();
            openURL(uri.toString());
        }
    }

    public static String getPage(String urlString) throws Exception {
        return getPage(new URL(urlString));
    }

    public static void setApplicationIcon(JFrame frame) {
        try {
            frame.setIconImage(Toolkit.getDefaultToolkit()
                               .getImage(new URL("http://gaggle.systemsbiology.net/images/icons/gaggle_icon_32x32.gif")));
        } catch (MalformedURLException ignore) {
            ignore.printStackTrace();
        }
    }
    public static String getPage(URL url) throws Exception {
        StringBuilder result = new StringBuilder();
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        int responseCode = urlConnection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK)
            throw new IOException ("\nHTTP response code: " + responseCode);

        BufferedReader theHTML = 
            new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String thisLine;
        while ((thisLine = theHTML.readLine()) != null) {
            result.append(thisLine);
            result.append(" ");
        }
        return result.toString();   
    }

    /**
     * TODO: This is pre-1.6 code, we can now use Java Desktop API which is
     * cleaner and more user-friendly.
     */
    public static void openURL(String url)  {
        String UNIX_PATH = "gnome-moz-remote";
        String WINDOWS_PATH = "cmd.exe /c start";
        String MAC_PATH = "open";
        String osName = System.getProperty("os.name" );

        try {
            String cmd;
            if (osName.startsWith ("Windows"))  cmd =  WINDOWS_PATH + " " + url;
            else if (osName.startsWith ("Mac")) cmd = MAC_PATH + " " + url;
            else cmd = UNIX_PATH + " " + url;
            
            Process p = Runtime.getRuntime().exec(cmd);
            try {
                int exitCode = p.waitFor();
                if (exitCode != 0) {
                    //System.out.println("cmd failed, start new browser");
                    cmd = UNIX_PATH + " "  + url;
                    Runtime.getRuntime().exec(cmd);
                }
            } catch (InterruptedException ignore) { ignore.printStackTrace(); }
        } catch (IOException ignore) { ignore.printStackTrace(); }
    }

    public static void setJFrameAlwaysOnTop(JFrame frame, boolean newValue) {
        frame.setAlwaysOnTop(newValue);
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
                                          String callingGoose, String[] allGeese) {
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
        if (savedItem != null) gooseChooser.setSelectedItem(savedItem);
    }

    public static int countOccurences(String textToSearch, String pattern) {
        int base = 0;
        int count = 0;
        boolean done = false;
        while (!done) {
            base = textToSearch.indexOf (pattern, base);
            if (base > 0) {
                count++;
                base += 3;
            } else
                done = true;
        }
        return count;
    }
}