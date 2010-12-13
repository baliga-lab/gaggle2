package org.systemsbiology.gaggle.geese.common;

import javax.swing.JComboBox;
import org.systemsbiology.gaggle.util.MiscUtil;


public class GaggleUtil {

    /**
     * Updates the UI to show the current list of geese. Removes the name of the
     * calling goose from the list, and sorts the remainder. Tries to preserve the
     * original selection in the list, if it still exists in the newly received
     * current goose list.
     * @param gooseChooser The UI element containing the list of geese
     * @param callingGoose The name of the calling goose
     * @param allGeese The list of all currently active geese
     */
    public static void updateGooseChooser(JComboBox gooseChooser, String callingGoose, String[] allGeese) {
        // simply delegates to MiscUtil (jnlp should only be an issue in case of displayWebPage())
        MiscUtil.updateGooseChooser(gooseChooser, callingGoose, allGeese);
    }

}
