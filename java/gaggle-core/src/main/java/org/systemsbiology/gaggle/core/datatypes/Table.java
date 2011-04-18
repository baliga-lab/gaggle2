/*
 * Copyright (C) 2011 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.core.datatypes;

import java.util.*;
import java.io.*;

/**
 * Table is implemented as an immutable data structure.
 */
public class Table implements GaggleData {
    static abstract class TableColumn implements Serializable {
        private String name;
        public TableColumn(String name) { this.name = name; }
        public String getName() { return name; }
        public abstract Class getColumnClass();
        public abstract int getRowCount();
        public boolean booleanValueAt(int row) {
            throw new UnsupportedOperationException("this column does not hold bool values");
        }
        public int intValueAt(int row) {
            throw new UnsupportedOperationException("this column does not hold int values");
        }
        public String stringValueAt(int row) {
            throw new UnsupportedOperationException("this column does not hold string values");
        }
        public double doubleValueAt(int row) {
            throw new UnsupportedOperationException("this column does not hold double values");
        }
    }
    private static class BooleanTableColumn extends TableColumn {
        private static final long serialVersionUID = 1L;
        private boolean[] values;
        public BooleanTableColumn(String name, boolean[] values) {
            super(name);
            this.values = values;
        }
        public Class getColumnClass() { return boolean.class; }
        public int getRowCount() { return values.length; }
        @Override public boolean booleanValueAt(int row) { return this.values[row]; }
    }
    private static class StringTableColumn extends TableColumn {
        private static final long serialVersionUID = 1L;
        private String[] values;
        public StringTableColumn(String name, String[] values) {
            super(name);
            this.values = values;
        }
        public Class getColumnClass() { return String.class; }
        public int getRowCount() { return values.length; }
        @Override public String stringValueAt(int row) { return this.values[row]; }
    }
    private static class IntTableColumn extends TableColumn {
        private static final long serialVersionUID = 1L;
        private int[] values;
        public IntTableColumn(String name, int[] values) {
            super(name);
            this.values = values;
        }
        public Class getColumnClass() { return int.class; }
        public int getRowCount() { return values.length; }
        @Override public int intValueAt(int row) { return this.values[row]; }
    }
    private static class DoubleTableColumn extends TableColumn {
        private static final long serialVersionUID = 1L;
        private double[] values;
        public DoubleTableColumn(String name, double[] values) {
            super(name);
            this.values = values;
        }
        public Class getColumnClass() { return double.class; }
        public int getRowCount() { return values.length; }
        @Override public double doubleValueAt(int row) { return this.values[row]; }
    }

    private static final long serialVersionUID = 1L;
    private String name;
    private Tuple metadata;
    private String species = "unknown";
    private TableColumn[] columns;

    public Table() {
    }
    public Table(String name, String species, Tuple metadata,
                 TableColumn[] columns) {
        this.name     = name;
        this.species  = species;
        this.metadata = metadata;
        this.columns  = columns;
    }

    public String getName() { return name; }
    public String getSpecies() { return species; }
    public Tuple getMetadata() { return metadata; }
    public int getColumnCount() { return columns == null ? 0 : columns.length; }
    public int getRowCount() {
        if (columns == null) return 0;
        else return columns.length == 0 || columns[0] == null ? 0 : columns[0].getRowCount();
    }
    public String getColumnName(int column) { return columns[column].getName(); }
    public Class getColumnClass(int column) { return columns[column].getColumnClass(); }

    // **** Factory methods ***
    public static TableColumn createIntColumn(String name, int[] values) {
        return new IntTableColumn(name, values);
    }
    public static TableColumn createBooleanColumn(String name, boolean[] values) {
        return new BooleanTableColumn(name, values);
    }
    public static TableColumn createStringColumn(String name, String[] values) {
        return new StringTableColumn(name, values);
    }
    public static TableColumn createDoubleColumn(String name, double[] values) {
        return new DoubleTableColumn(name, values);
    }
}
