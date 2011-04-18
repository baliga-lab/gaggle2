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
 * A matrix-like, two-dimensional Gaggle data structure where the values
 * in each column have the same data type.
 * These data types can be:
 * - int
 * - double
 * - boolean
 * - String
 */
public class Table implements GaggleData {
    /**
     * Abstract super class for table columns.
     */
    static abstract class TableColumn implements Serializable {
        private String name;
        /** Default constructor. */
        public TableColumn() { this(""); }
        /**
         * Constructor.
         * @param name the column name
         */
        public TableColumn(String name) { this.name = name; }
        /**
         * Returns the column name.
         * @return column name
         */
        public String getName() { return name; }
        /**
         * Sets the column name.
         * @param name the new column name
         */
        public void setName(String name) { this.name = name; }
       
        /**
         * Returns the column class.
         * @return column class
         */
        public abstract Class getColumnClass();
        /**
         * Returns the number of rows.
         * @return number of rows
         */
        public abstract int getRowCount();

        /**
         * boolean value at the specified row.
         * @param row index
         * @return boolean value at the specified row
         */
        public boolean booleanValueAt(int row) {
            throw new UnsupportedOperationException("this column does not hold bool values");
        }

        /**
         * int value at the specified row.
         * @param row index
         * @return int value at the specified row
         */
        public int intValueAt(int row) {
            throw new UnsupportedOperationException("this column does not hold int values");
        }

        /**
         * String value at the specified row.
         * @param row index
         * @return String value at the specified row
         */
        public String stringValueAt(int row) {
            throw new UnsupportedOperationException("this column does not hold string values");
        }

        /**
         * double value at the specified row.
         * @param row index
         * @return double value at the specified row
         */
        public double doubleValueAt(int row) {
            throw new UnsupportedOperationException("this column does not hold double values");
        }
    }

    /**
     * Table column that stores only boolean values.
     */
    static class BooleanTableColumn extends TableColumn {
        private static final long serialVersionUID = 1L;
        private boolean[] values;

        /**
         * Constructor.
         * @param name column name
         * @param values boolean values
         */
        public BooleanTableColumn(String name, boolean[] values) {
            super(name);
            this.values = values;
        }
        /** Default constructor. */
        public BooleanTableColumn() { this(null, new boolean[0]); }

        /** {@inheritDoc} */
        public Class getColumnClass() { return boolean.class; }

        /** {@inheritDoc} */
        public int getRowCount() { return values.length; }

        /** {@inheritDoc} */
        @Override public boolean booleanValueAt(int row) { return this.values[row]; }

        /**
         * Sets the values of this column.
         * @param values the values
         */
        public void setValues(boolean[] values) { this.values = values; }
    }
    /**
     * Table column that stores only String values.
     */
    static class StringTableColumn extends TableColumn {
        private static final long serialVersionUID = 1L;
        private String[] values;

        /**
         * Constructor.
         * @param name column name
         * @param values String values
         */
        public StringTableColumn(String name, String[] values) {
            super(name);
            this.values = values;
        }
        /** Default constructor. */
        public StringTableColumn() { this(null, new String[0]); }

        /** {@inheritDoc} */
        public Class getColumnClass() { return String.class; }
        /** {@inheritDoc} */
        public int getRowCount() { return values.length; }
        /** {@inheritDoc} */
        @Override public String stringValueAt(int row) { return this.values[row]; }

        /**
         * Sets the values of this column.
         * @param values the values
         */
        public void setValues(String[] values) { this.values = values; }
    }
    /**
     * Table column that stores only int values.
     */
    static class IntTableColumn extends TableColumn {
        private static final long serialVersionUID = 1L;
        private int[] values;

        /**
         * Constructor.
         * @param name column name
         * @param values int values
         */
        public IntTableColumn(String name, int[] values) {
            super(name);
            this.values = values;
        }
        /** Default constructor. */
        public IntTableColumn() { this(null, new int[0]); }

        /** {@inheritDoc} */
        public Class getColumnClass() { return int.class; }
        /** {@inheritDoc} */
        public int getRowCount() { return values.length; }
        /** {@inheritDoc} */
        @Override public int intValueAt(int row) { return this.values[row]; }

        /**
         * Sets the values of this column.
         * @param values the values
         */
        public void setValues(int[] values) { this.values = values; }
    }
    /**
     * Table column that stores only double values.
     */
    static class DoubleTableColumn extends TableColumn {
        private static final long serialVersionUID = 1L;
        private double[] values;

        /**
         * Constructor.
         * @param name column name
         * @param values double values
         */
        public DoubleTableColumn(String name, double[] values) {
            super(name);
            this.values = values;
        }
        /** Default constructor. */
        public DoubleTableColumn() { this(null, new double[0]); }

        /** {@inheritDoc} */
        public Class getColumnClass() { return double.class; }
        /** {@inheritDoc} */
        public int getRowCount() { return values.length; }
        /** {@inheritDoc} */
        @Override public double doubleValueAt(int row) { return this.values[row]; }

        /**
         * Sets the values of this column.
         * @param values the values
         */
        public void setValues(double[] values) { this.values = values; }
    }

    private static final long serialVersionUID = 1L;
    private String name;
    private Tuple metadata;
    private String species;
    private TableColumn[] columns;

    /** Default constructor. */
    public Table() { this(null, "unknown", null, new TableColumn[0]); }

    /**
     * Constructor.
     * @param name table name
     * @param species species
     * @param metadata metadata
     * @param columns table columns
     */
    public Table(String name, String species, Tuple metadata,
                 TableColumn[] columns) {
        this.name     = name;
        this.species  = species;
        this.metadata = metadata;
        this.columns  = columns;
    }

    /**
     * Returns the table name.
     * @return table name
     */
    public String getName() { return name; }
    /**
     * Sets the table name.
     * @param name table name
     */
    public void setName(String name) { this.name = name; }
    /**
     * Returns the species.
     * @return species
     */
    public String getSpecies() { return species; }
    /**
     * Sets the species.
     * @param species thes species
     */
    public void setSpecies(String species) { this.species = species; }
    /**
     * Returns the metadata.
     * @return metadata
     */
    public Tuple getMetadata() { return metadata; }
    /**
     * Sets the metadata.
     * @param metadata metadata
     */
    public void setMetadata(Tuple metadata) { this.metadata = metadata; }
    /**
     * Sets the table columns.
     * @param columns table columns
     */
    public void setColumns(TableColumn[] columns) { this.columns = columns; }

    /**
     * Returns the number of columns.
     * @return number of columns
     */
    public int getColumnCount() { return columns == null ? 0 : columns.length; }

    /**
     * Returns the number of rows.
     * @return number of rows
     */
    public int getRowCount() {
        if (columns == null) return 0;
        else return columns.length == 0 || columns[0] == null ? 0 : columns[0].getRowCount();
    }

    /**
     * Returns the column name.
     * @param column column index
     * @return column name
     */
    public String getColumnName(int column) { return columns[column].getName(); }

    /**
     * Returns the column class.
     * @param column column index
     * @return column class
     */
    public Class getColumnClass(int column) { return columns[column].getColumnClass(); }
    /**
     * Returns the int value at the specified position.
     * @param row row index
     * @param column column index
     * @return int value
     */
    public int intValueAt(int row, int column) { return columns[column].intValueAt(row); }
    /**
     * Returns the String value at the specified position.
     * @param row row index
     * @param column column index
     * @return String value
     */
    public String stringValueAt(int row, int column) { return columns[column].stringValueAt(row); }
    /**
     * Returns the boolean value at the specified position.
     * @param row row index
     * @param column column index
     * @return boolean value
     */
    public boolean booleanValueAt(int row, int column) { return columns[column].booleanValueAt(row); }
    /**
     * Returns the double value at the specified position.
     * @param row row index
     * @param column column index
     * @return double value
     */
    public double doubleValueAt(int row, int column) { return columns[column].doubleValueAt(row); }

    // **** Factory methods ***
    /**
     * Creates an int type table column.
     * @param name column name
     * @param values column values
     * @return table column object
     */
    public static TableColumn createIntColumn(String name, int[] values) {
        return new IntTableColumn(name, values);
    }
    /**
     * Creates an boolean type table column.
     * @param name column name
     * @param values column values
     * @return table column object
     */
    public static TableColumn createBooleanColumn(String name, boolean[] values) {
        return new BooleanTableColumn(name, values);
    }
    /**
     * Creates an String type table column.
     * @param name column name
     * @param values column values
     * @return table column object
     */
    public static TableColumn createStringColumn(String name, String[] values) {
        return new StringTableColumn(name, values);
    }
    /**
     * Creates an double type table column.
     * @param name column name
     * @param values column values
     * @return table column object
     */
    public static TableColumn createDoubleColumn(String name, double[] values) {
        return new DoubleTableColumn(name, values);
    }
}
