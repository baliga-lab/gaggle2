/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.boss.plugins.tigrAnno;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

class SearchResultsTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"NAME", "Gene Symbol", "Function" };
    private List<String[]> data;

    public SearchResultsTableModel() { clearData(); }

    public void clearData() {
        data = new ArrayList<String[]>();
    }

    public void addSearchResult(String name, String geneSymbol, String geneFunction) {
        data.add(new String[] {name, geneSymbol, geneFunction});
    }

    public int getColumnCount() { return COLUMN_NAMES.length; }
    public int getRowCount() { return data.size(); }
    public String getColumnName(int col) { return COLUMN_NAMES[col]; }

    public Object getValueAt(int row, int col) {
        try {
            int lastIndex = data.size() - 1;
            if (row > lastIndex) return null;
  
            String[] rowContents = data.get(row);
            if (rowContents == null) return null;
  
            lastIndex = rowContents.length - 1;
            if (col > lastIndex) return null;
            return rowContents [col];
        } catch (Exception ex0) {
            return null;
        }
    }
    public boolean isCellEditable(int row, int col) { return false; }
}
