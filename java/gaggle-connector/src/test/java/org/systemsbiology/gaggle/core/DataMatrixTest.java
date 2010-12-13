package org.systemsbiology.gaggle;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.datatypes.Tuple;

public class DataMatrixTest {
    private static final String TESTURI = "http://www.test.com/db/testfile.txt";

    @Test public void testCreateEmpty() {
        DataMatrix matrix = new DataMatrix();
        matrix.setDefault(1.0); // does nothing
        assertNull(matrix.getName());
        assertEquals("unknown", matrix.getSpecies());
        assertNull(matrix.getMetadata());

        assertEquals("", matrix.getURI());
        assertEquals("", matrix.getFullName());
        assertEquals("", matrix.getShortName());
        assertEquals("", matrix.getFileExtension());
        assertEquals("", matrix.getDataTypeBriefName());
        assertEquals(0, matrix.getRowCount());
        assertEquals(0, matrix.getColumnCount());
        assertEquals("DataMatrix", matrix.getRowTitlesTitle());
        assertEquals(0, matrix.getRowTitles().length);
        assertEquals(0, matrix.getColumnTitles().length);
        assertEquals(0, matrix.get(0).length);
        assertNull(matrix.get());
        assertEquals(0, matrix.getColumn(0).length);
        assertEquals("DataMatrix\n", matrix.toString());
    }

    @Test public void testCreateWithURI() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        assertEquals(TESTURI, matrix.getURI());
        assertEquals(TESTURI, matrix.getFullName());
        assertEquals("testfile.txt", matrix.getShortName());
        assertEquals("txt", matrix.getFileExtension());
        assertEquals("txt", matrix.getDataTypeBriefName());
    }

    @Test public void testCreateWithNonPathURI() {
        DataMatrix matrix = new DataMatrix("LABEL");
        assertEquals("LABEL", matrix.getURI());
        assertEquals("LABEL", matrix.getFullName());
        assertEquals("LABEL", matrix.getShortName());
        assertEquals("", matrix.getFileExtension());
        assertEquals("", matrix.getDataTypeBriefName());
    }

    /**
     * An unusual case, which is nevertheless handled.
     */
    @Test public void testCreateWithSlashURI() {
        DataMatrix matrix = new DataMatrix("/");
        assertEquals("/", matrix.getURI());
        assertEquals("/", matrix.getFullName());
        assertEquals("/", matrix.getShortName());
        assertEquals("", matrix.getFileExtension());
        assertEquals("", matrix.getDataTypeBriefName());
    }

    @Test public void testSimpleSetters() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        Tuple metadata = new Tuple();
        String[] rowTitles = new String[] { "row1", "row2" };
        String[] colTitles = new String[] { "col1", "col2" };

        matrix.setName("name");
        matrix.setSpecies("species");
        matrix.setMetadata(metadata);
        matrix.setShortName("short");
        matrix.setDataTypeBriefName("brief");
        matrix.setRowTitlesTitle("title");
        matrix.setRowTitles(rowTitles);
        matrix.setColumnTitles(colTitles);

        assertEquals("name", matrix.getName());
        assertEquals("species", matrix.getSpecies());
        assertEquals(metadata, matrix.getMetadata());
        assertEquals("short", matrix.getShortName());
        assertEquals("brief", matrix.getDataTypeBriefName());
        assertEquals("title", matrix.getRowTitlesTitle());
        assertEquals(rowTitles, matrix.getRowTitles());
        assertEquals(colTitles, matrix.getColumnTitles());
    }

    @Test public void testSetFullName() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setFullName("/path/to/testfile.txt");
     
        assertEquals("/path/to/testfile.txt", matrix.getFullName());
        assertEquals("testfile.txt", matrix.getShortName());
        assertEquals("txt", matrix.getFileExtension());
    }

    // Data matrix values
    private void assertAllValuesAre(DataMatrix matrix, double expected) {
        for (int row = 0; row < matrix.getRowCount(); row++) {
            for (int col = 0; col < matrix.getColumnCount(); col++) {
                assertEquals(expected, matrix.get(row, col), 0.001);
            }
        }
    }
    private void assertAllRowValuesAre(DataMatrix matrix, int row, double expected) {
        for (int col = 0; col < matrix.getColumnCount(); col++) {
            assertEquals(expected, matrix.get(row, col), 0.001);
        }
    }

    @Test public void testZeroColSetup() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(3, 0);
        assertEquals(0, matrix.getColumnCount());
        matrix.setSize(0, 0);
        assertEquals(0, matrix.getColumnCount());
    }

    @Test public void testSetMatrixDirect() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        double[][] data = new double[3][2];
        matrix.set(data);
        assertEquals(2, matrix.getColumnCount());
        assertEquals(3, matrix.getRowCount());
        assertEquals(data, matrix.get());
    }


    @Test public void testMakeMatrixDefault() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(3, 4);
        assertEquals(3, matrix.getRowCount());
        assertEquals(4, matrix.getColumnCount());
        assertAllValuesAre(matrix, 0.0);
        assertEquals(4, matrix.get(0).length);
        assertEquals(3, matrix.getColumn(0).length);
    }

    @Test public void testMakeMatrixOverrideDefault() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(3, 4);
        matrix.setDefault(13.0);
        assertAllValuesAre(matrix, 13.0);
    }

    @Test public void testSetDataValue() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(3, 4);
        matrix.set(0, 1, 1.2);
        assertEquals(1.2, matrix.get(0, 1), 0.001);
    }

    @Test public void testSetDataRow() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(3, 4);
        matrix.set(0, new double[] { 3.0, 3.0, 3.0, 3.0 });
        assertAllRowValuesAre(matrix, 0, 3.0);
    }

    @Test public void testAddDataRow() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(3, 4);
        matrix.addRow("name", new double[] { 3.0, 3.0, 3.0, 3.0 });
        assertEquals(4, matrix.getRowCount());
        assertAllRowValuesAre(matrix, 3, 3.0);

        try {
            matrix.addRow("name2", new double[] { 2.0, 2.0 });
            fail("adding row with wrong column count should throw an exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("new row must have only 4 values; you supplied 2", ex.getMessage());
        }
    }

    @Test public void testGetDataByName() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(3, 4);
        matrix.setRowTitles(new String[] {"row1", "row2", "row3"});
        matrix.setColumnTitles(new String[] {"col1", "col2", "col3", "col4"});
        
        assertEquals(0, matrix.getRowNumber("row1"));
        assertEquals(1, matrix.getColumnNumber("col2"));
        assertEquals(4, matrix.get("row1").length);
        assertEquals(3, matrix.getColumn("col2").length);
        
        try {
            matrix.getRowNumber("norow");
            fail("getRowNumber() should throw an exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("no row named norow", ex.getMessage());
        }
        try {
            matrix.getColumnNumber("nocol");
            fail("getColumnNumber() should throw an exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("no column named nocol", ex.getMessage());
        }
        try {
            matrix.get("norow");
            fail("get() should throw an exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("no data for 'norow'", ex.getMessage());
        }
    }
    @Test public void testEquals() {
        // note that we currently have to set up a complete data matrix
        // to make equals() work 
        DataMatrix matrix1 = new DataMatrix("matrix1");
        DataMatrix matrix2 = new DataMatrix("matrix2");
        matrix1.setSize(2, 2);
        matrix2.setSize(3, 2);
        matrix1.setRowTitles(new String[] {"row1", "row2"});
        matrix1.setColumnTitles(new String[] {"col1", "col2"});
        matrix2.setRowTitles(new String[] {"row1", "row2", "row3"});
        matrix2.setColumnTitles(new String[] {"col1", "col2"});

        assertTrue(matrix1.equals(matrix1));
        assertFalse(String.format("%s", matrix1), matrix1.equals(matrix2));
    }

    @Test public void testToString() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(2, 2);
        matrix.setRowTitles(new String[] {"row1", "row2"});
        matrix.setColumnTitles(new String[] {"col1", "col2"});
        assertEquals("DataMatrix\tcol1\tcol2\nrow1\t0.0\t0.0\nrow2\t0.0\t0.0\n", matrix.toString());
    }

    @Test public void testSortByRowName() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(2, 2);
        matrix.setRowTitles(new String[] {"row02", "row01"});
        matrix.setColumnTitles(new String[] {"col1", "col2"});
        matrix.set(0, new double[] { 3.0, 3.0 });
        matrix.set(1, new double[] { 1.0, 1.0 });
        matrix.sortByRowName();

        assertEquals("row01", matrix.getRowTitles()[0]);
        assertEquals("row02", matrix.getRowTitles()[1]);
        assertAllRowValuesAre(matrix, 0, 1.0);
        assertAllRowValuesAre(matrix, 1, 3.0);
    }

    @Test public void testSortByRowNameInvalid() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(2, 2);
        matrix.setRowTitles(new String[] {"row02", "row02"});
        matrix.setColumnTitles(new String[] {"col1", "col2"});
        matrix.set(0, new double[] { 3.0, 3.0 });
        matrix.set(1, new double[] { 1.0, 1.0 });
        try {
            matrix.sortByRowName();
            fail("sorting with duplicate row names should throw an exception");
        } catch (IllegalArgumentException ex) {
            assertEquals("Encountered duplicate row names in matrix!", ex.getMessage());
        }
    }
}