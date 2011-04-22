/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.boss.plugins.sbeams;

import javax.swing.table.*;
import java.util.ArrayList;
import java.util.List;

public class SbeamsResultSetTableModel extends AbstractTableModel {

    private String[] COLUMN_NAMES = {"ORF", "Gene", "EC Numbers", "Gene Name/Function", "Domains"};
    private List<Annotation> data = new ArrayList<Annotation>();

    public void clearData() {
        data.clear();
    }

    public void addSearchResult(String orf, String geneSymbol, String ecNumbers, 
                                String geneFunction, String domainHitsURL) {
        data.add(new Annotation(orf, geneSymbol, ecNumbers, geneFunction, domainHitsURL));
    }

    public void addSearchResults(List<Annotation> annotations) {
        data.addAll(annotations);
    }
  
    public int getColumnCount() { return COLUMN_NAMES.length; }
    public int getRowCount() { return data.size(); }  
    public String getColumnName(int col) { return COLUMN_NAMES[col]; }

    public Object getValueAt(int row, int col) {
        try {
            if (row >= data.size()) return null;
    
            Annotation annotation = data.get(row);
            if (annotation == null) return null;
      
            if (col == 0) return annotation.orf();
            else if (col == 1) return annotation.geneSymbol();
            else if (col == 2) return annotation.ecNumbers();
            else if (col == 3) return annotation.annotation();
            else if (col == 4) return annotation.domainHitsURL();
            else return null;  
        } catch (Exception ex) {
            return null;
        }
    }
  
    public boolean isCellEditable(int row, int col) { return false; }
}
