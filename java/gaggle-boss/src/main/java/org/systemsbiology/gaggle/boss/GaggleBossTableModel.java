/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.boss;

import javax.swing.*;
import javax.swing.table.*;
import java.util.List;
import java.util.ArrayList;

public class GaggleBossTableModel extends AbstractTableModel {

    private GuiBoss parentApp;
    private List<String> applicationNames = new ArrayList<String>();
    private List<Boolean> listeningState = new ArrayList<Boolean>();

    private String[] columnNames = {"Geese", "Listening?"};

    public GaggleBossTableModel(GuiBoss parentApp) {
        this.parentApp = parentApp;
    }

    public String getColumnName(int column) { return columnNames[column]; }
    public int getRowCount() { return applicationNames.size(); }
    public int getColumnCount() { return columnNames.length; }

    public void setValueAt(Object value, int row, int column) {
        try {
            if (column == 0) {
                String proposedName = (String) value;
                String uniquifiedName = parentApp.renameGooseDirectly(applicationNames.get(row), proposedName);
                if (uniquifiedName != null)
                    applicationNames.set(row, uniquifiedName);
            } else if (column == 1) {
                listeningState.set(row, (Boolean) value);
            }
        } catch (Exception ex0) {
            System.out.println("GaggleBossTableModel.setValueAt exception: " + ex0.getMessage());
            String msg = "Failed to contact goose to rename!";
            JOptionPane.showMessageDialog(parentApp.getFrame(), msg);
        }
    }

    public Object getValueAt(int row, int column) {
        Object result = null;

        if (column == 0 && row < applicationNames.size())
            result = applicationNames.get(row);
        else if (column == 1 && row < listeningState.size())
            result = listeningState.get(row);

        return result;
    }

    public boolean isCellEditable(int row, int column) { return true; }

    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    private int getGooseRow(String gooseName) {
        for (int row = 0; row < getRowCount(); row++) {
            if (gooseName.equals(applicationNames.get(row))) return row;
        }
        throw new IllegalArgumentException("could not find row for goose named '" +
                                           gooseName + "'");
    }

    public boolean isListening(String gooseName) {
        return listeningState.get(getGooseRow(gooseName));
    }

    public void setListeningState(String gooseName, boolean newValue) {
        int row = getGooseRow(gooseName);
        listeningState.set(row, newValue);
        fireTableDataChanged();
    }

    public void addClient(String newClientName) {
        applicationNames.add(newClientName);
        listeningState.add(true);
        fireTableDataChanged();
    }

    public void removeGoose(String gooseName) {
        int row = getGooseRow(gooseName);
        applicationNames.remove(row);
        listeningState.remove(row);
        fireTableDataChanged();
    }

    public void setAppNameAtRow(Object value, int row) {
        applicationNames.set(row, value.toString());
    }

    public String[] getAppNames() {
        return applicationNames.toArray(new String[0]);
    }
}
