package org.systemsbiology.gaggle;

import org.junit.Test;
import static org.junit.Assert.*;
import org.systemsbiology.gaggle.core.datatypes.Single;

public class SingleTest {
    @Test public void testCreateEmpty() {
        Single single = new Single();
        assertNull("name of empty Single should be null", single.getName());
        assertNull("value of empty Single should be null", single.getValue());        
    }
}
