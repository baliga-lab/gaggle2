/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.util;

import java.io.*;
import java.util.*;
import java.net.*;

public class TextHttpReader {
    private StringBuilder sb;
    private String uri;
    public TextHttpReader(String URI) throws Exception {
        // TODO change to url encode
        uri = URI.replaceAll(" ", "%20");
        sb = new StringBuilder();
    }

    public int read() throws Exception {
        sb.append(getPage(uri));
        return sb.length();
    }

    public String getText() {
        return sb.toString();
    }

    static public String getPage(String urlString) throws Exception {
        return getPage(new URL(urlString)); 
    }

    static public String getPage(URL url) throws Exception {
        int characterCount = 0;
        StringBuilder result = new StringBuilder();

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        int responseCode = urlConnection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK)
            throw new IOException("\nHTTP response code: " + responseCode + " url: " + url.toString() + "\n");

        BufferedReader theHTML = new BufferedReader 
            (new InputStreamReader(urlConnection.getInputStream()));
        String thisLine;
        while ((thisLine = theHTML.readLine()) != null) {
            result.append(thisLine);
            result.append("\n");
        }
        return result.toString();
    }
}


