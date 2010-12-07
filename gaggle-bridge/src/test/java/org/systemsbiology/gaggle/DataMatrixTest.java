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

        matrix.setName("name");
        matrix.setSpecies("species");
        matrix.setMetadata(metadata);
        matrix.setShortName("short");
        matrix.setDataTypeBriefName("brief");
     
        assertEquals("name", matrix.getName());
        assertEquals("species", matrix.getSpecies());
        assertEquals(metadata, matrix.getMetadata());
        assertEquals("short", matrix.getShortName());
        assertEquals("brief", matrix.getDataTypeBriefName());
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

    @Test public void testMakeMatrixDefault() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(3, 4);
        assertEquals(3, matrix.getRowCount());
        assertEquals(4, matrix.getColumnCount());
        assertAllValuesAre(matrix, 0.0);
    }

    @Test public void testMakeMatrixOverrideDefault() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        matrix.setSize(3, 4);
        matrix.setDefault(13.0);
        assertAllValuesAre(matrix, 13.0);
    }
}