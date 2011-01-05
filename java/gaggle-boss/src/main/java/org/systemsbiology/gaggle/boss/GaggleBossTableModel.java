// GaggleBossTableModel.java
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss;

import org.systemsbiology.gaggle.core.Boss;

import javax.swing.*;
import javax.swing.table.*;
import java.util.ArrayList;

public class GaggleBossTableModel extends AbstractTableModel {

    private GuiBoss parentApp;
    private ArrayList applicationNames = new ArrayList();
    private ArrayList listeningState = new ArrayList();
    private ArrayList selectionCounts = new ArrayList();

    private String[] appNames;
    private String[] columnNames = {"Geese", "Listening?"};

    public GaggleBossTableModel(GuiBoss parentApp) {
        this.parentApp = parentApp;

    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getRowCount() { return applicationNames.size(); }

    public int getColumnCount() { return columnNames.length; }

    public void setValueAt(Object value, int row, int column) {
        try {
            if (column == 0) {
                String proposedName = (String) value;
                String uniquifiedName =
                    parentApp.actuallyRenameGoose((String) applicationNames.get(row),
                                                  proposedName);
                if (uniquifiedName != null)
                    applicationNames.set(row, uniquifiedName);
            } else if (column == 1) listeningState.set(row, value);
        } catch (Exception ex0) {
            System.out.println("GaggleBossTableModel.setValueAt exception: " +
                               ex0.getMessage());
            String msg = "Failed to contact goose to rename!";
            JOptionPane.showMessageDialog(parentApp.frame, msg);
        }
    }

    public Object getValueAt(int row, int column) {
        Object result = null;

        if (column == 0 && row < applicationNames.size())
            result = (String) applicationNames.get(row);
        else if (column == 1 && row < listeningState.size())
            result = (Boolean) listeningState.get(row);

        return result;
    }

    public boolean isCellEditable(int row, int column) { return true; }

    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    protected int getGooseRow(String gooseName) {
        for (int row = 0; row < getRowCount(); row++) {
            String name = (String) getValueAt(row, 0);
            if (name.equals(gooseName)) return row;
        }
        throw new IllegalArgumentException("could not find row for goose named '" +
                                           gooseName + "'");
    }

    public boolean isListening(String gooseName) {
        int row = getGooseRow(gooseName);
        return ((Boolean) getValueAt(row, 1)).booleanValue();
    }

    public void setListeningState(String gooseName, boolean newValue) {
        int row = getGooseRow(gooseName);
        setValueAt(new Boolean(newValue), row, 1);
        fireTableStructureChanged();
    }

    public void addClient(String newClientName) {
        applicationNames.add(newClientName);
        listeningState.add(new Boolean(true));
        JButton button = (new JButton(" "));
        selectionCounts.add(new Integer(0));
        fireTableStructureChanged();

    }

    public void removeGoose(String gooseName) {
        int row = getGooseRow(gooseName);
        applicationNames.remove(row);
        listeningState.remove(row);
        selectionCounts.remove(row);
        fireTableStructureChanged();
    }

    public void setAppNameAtRow(Object value, int row) {
        applicationNames.set(row, value);
    }

    public void setSelectionCount(String gooseName, int selectionCount) {
        int row = getGooseRow(gooseName);
    }

    public String[] getAppNames() {
        return (String[]) applicationNames.toArray(new String[0]);
    }
}
