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

public class TextFileReader {
    private String filename;
    private BufferedReader bufferedReader;
    private StringBuilder strbuf;

    public TextFileReader(String filename) {
        this.filename = filename;
        try {
            bufferedReader = new BufferedReader(new FileReader(filename));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        strbuf = new StringBuilder();       
    }

    public int read() {
        String newLineOfText;
 
        try {
            while ((newLineOfText = bufferedReader.readLine()) != null) {
                strbuf.append(newLineOfText + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return strbuf.length();
    }

    public String getText() { return strbuf.toString(); }
}


