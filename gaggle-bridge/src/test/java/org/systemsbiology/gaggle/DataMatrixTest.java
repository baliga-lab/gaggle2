package org.systemsbiology.gaggle;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.datatypes.Tuple;

public class DataMatrixTest {
    private static final String TESTURI = "http://www.test.com/db/testfile.txt";

    @Test public void testCreateEmpty() {
        DataMatrix matrix = new DataMatrix();
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
    }

    @Test public void testCreateWithURI() {
        DataMatrix matrix = new DataMatrix(TESTURI);
        assertEquals(TESTURI, matrix.getURI());
        assertEquals(TESTURI, matrix.getFullName());
        assertEquals("testfile.txt", matrix.getShortName());
        assertEquals("txt", matrix.getFileExtension());
        assertEquals("txt", matrix.getDataTypeBriefName());
    }
}