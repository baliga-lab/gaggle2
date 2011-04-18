package org.systemsbiology.gaggle.core.datatypes;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.Table.TableColumn;

public class TableTest {
    @Test public void testCreateEmptyTable() {
        Tuple metadata = new Tuple();
        TableColumn[] columns = new TableColumn[0];
        Table table = new Table("table", "species", metadata, columns);
        assertEquals("table", table.getName());
        assertEquals("species", table.getSpecies());
        assertEquals(metadata, table.getMetadata());
        assertEquals(0, table.getRowCount());
        assertEquals(0, table.getColumnCount());
    }
    @Test public void testCreateNonEmptyTable() {
        Tuple metadata = new Tuple();
        TableColumn[] columns = {
            Table.createIntColumn("intcol", new int[] { 1, 2, 3})
        };
        Table table = new Table("table", "species", metadata, columns);
        assertEquals("table", table.getName());
        assertEquals("species", table.getSpecies());
        assertEquals(metadata, table.getMetadata());
        assertEquals(3, table.getRowCount());
        assertEquals(1, table.getColumnCount());
        assertEquals("intcol", table.getColumnName(0));
        assertEquals(int.class, table.getColumnClass(0));
    }
    @Test public void testCreateAllTypesTable() {
        Tuple metadata = new Tuple();
        TableColumn[] columns = {
            Table.createIntColumn("intcol",        new int[] { 1, 2, 3}),
            Table.createStringColumn("stringcol",  new String[] { "foo", "bar", "bar"}),
            Table.createBooleanColumn("boolcol",   new boolean[] { true, false, true}),
            Table.createDoubleColumn("doublecol",  new double[] { 1.0, 2.0, 3.0})
        };
        Table table = new Table("table", "species", metadata, columns);
        assertEquals("table",       table.getName());
        assertEquals("species",     table.getSpecies());
        assertEquals(metadata,      table.getMetadata());
        assertEquals(3,             table.getRowCount());
        assertEquals(4,             table.getColumnCount());
        assertEquals("intcol",      table.getColumnName(0));
        assertEquals(int.class,     table.getColumnClass(0));
        assertEquals("stringcol",   table.getColumnName(1));
        assertEquals(String.class,  table.getColumnClass(1));
        assertEquals("boolcol",     table.getColumnName(2));
        assertEquals(boolean.class, table.getColumnClass(2));
        assertEquals("doublecol",   table.getColumnName(3));
        assertEquals(double.class,  table.getColumnClass(3));
    }
}