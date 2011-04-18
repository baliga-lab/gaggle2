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
 * Table is implemented as an immutable data structure to.
 */
public class Table implements GaggleData {
    abstract class TableColumn {
        private String name;
        public TableColumn(String name) { this.name = name; }
        abstract int getRowCount();
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
    class BooleanTableColumn extends TableColumn {
        private boolean[] values;
        public BooleanTableColumn(String name, boolean[] values) {
            super(name);
            this.values = values;
        }
        public int getRowCount() { return values.length; }
        @Override public boolean booleanValueAt(int row) { return this.values[row]; }
    }
    class StringTableColumn extends TableColumn {
        private String[] values;
        public StringTableColumn(String name, String[] values) {
            super(name);
            this.values = values;
        }
        public int getRowCount() { return values.length; }
        @Override public String stringValueAt(int row) { return this.values[row]; }
    }
    class IntTableColumn extends TableColumn {
        private int[] values;
        public IntTableColumn(String name, int[] values) {
            super(name);
            this.values = values;
        }
        public int getRowCount() { return values.length; }
        @Override public int intValueAt(int row) { return this.values[row]; }
    }
    class DoubleTableColumn extends TableColumn {
        private double[] values;
        public DoubleTableColumn(String name, double[] values) {
            super(name);
            this.values = values;
        }
        public int getRowCount() { return values.length; }
        @Override public double doubleValueAt(int row) { return this.values[row]; }
    }

    private static final long serialVersionUID = 1L;
    private String name;
    private Tuple metadata;
    private String species = "unknown";
    private TableColumn[] columns;

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
        else return columns[0] == null ? 0 : columns[0].getRowCount();
    }
}
