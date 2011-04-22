/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.boss;

import java.io.*;
import java.util.*;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.systemsbiology.gaggle.util.*;

/**
 * A class to handle run-time configuration.
 */
public class BossConfig {

    private String argSpecificationString = "p:n:";
    private String[] commandLineArguments;
    private String propsFilename;
    private String nameHelperURI = null;
    private File projectFileDirectoryAbsolute;

    private Properties props;
    private boolean startInvisibly = false;
    private boolean startMinimized = false;

    public BossConfig() { this(new String[0]); }

    public BossConfig(String[] args) {
        commandLineArguments = new String[args.length];
        System.arraycopy(args, 0, commandLineArguments, 0, args.length);
        parseArgs();
        props = readProperties();
    }

    public Properties getProperties() { return props; }

    private void parseArgs() {
        boolean argsError = false;
        String tmp;

        if (commandLineArguments == null || commandLineArguments.length == 0)
            return;

        LongOpt[] longopts = new LongOpt[2];
        longopts[0] = new LongOpt("startInvisibly", LongOpt.NO_ARGUMENT, null, '~');
        longopts[1] = new LongOpt("startMinimized", LongOpt.NO_ARGUMENT, null, '`');
        Getopt g = new Getopt("GaggleBoss", commandLineArguments,
                              argSpecificationString, longopts);
        g.setOpterr(false); // We'll do our own error handling

        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case'p':
                    propsFilename = g.getOptarg();
                    break;
                case'n':
                    nameHelperURI = g.getOptarg();
                    break;
                case'?':
                    // ignore
                    break;
                case'~':
                    startInvisibly = true;
                    break;
                case'`':
                    startMinimized = true;
                    break;
                default:
                    argsError = true;
                    break;
            }
        }
    }

    public String[] getPluginNames() {
        String[] keys = (String[]) props.keySet().toArray(new String[0]);
        List<String> result = new ArrayList<String>();
        for (String propertyName : keys) {
            if (propertyName.toLowerCase().startsWith("plugin"))
                result.add(props.get(propertyName).toString());
        }
        return result.toArray(new String[0]);
    }

    public String getNameHelperURI() { return nameHelperURI; }
    public String getPropsFilename() { return propsFilename; }
    public boolean startInvisibly() { return startInvisibly; }
    public boolean startMinimized() { return startMinimized; }

    private Properties readProperties() {
        if (propsFilename == null) return new Properties();
        System.out.println("BossConfig about to read from " + propsFilename);
        Properties projectProps = readPropertyFileAsText(propsFilename);
        System.out.println("props: " + projectProps);
        return projectProps;
    }

    public Properties readPropertyFileAsText(String filename) {
        String rawText = "";

        try {
            if (filename.trim().startsWith("jar://")) {
                TextJarReader reader = new TextJarReader(filename);
                reader.read();
                rawText = reader.getText();
            } else {
                File projectPropsFile =
                    new File(absolutizeFilename(projectFileDirectoryAbsolute, filename));
                TextFileReader reader = new TextFileReader(projectPropsFile.getPath());
                reader.read();
                rawText = reader.getText();
            }
        } catch (Exception e0) {
            System.err.println("-- Exception while reading properties file " + filename);
            e0.printStackTrace();
        }

        // the Properties class contains its own parser, so it makes the most sense
        // to massage our text into a form suitable for that loader
        byte[] byteText = rawText.getBytes();
        InputStream is = new ByteArrayInputStream(byteText);
        Properties newProps = new Properties();
        try {
            newProps.load(is);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return newProps;
    }

    private String absolutizeFilename(File parentDirectory, String filename) {
        if (filename.trim().startsWith("/")) return filename;
        else return (new File(parentDirectory, filename)).getPath();
    }
}


