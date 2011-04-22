/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.boss.plugins;

import java.util.*;
import java.lang.reflect.*;
import org.systemsbiology.gaggle.boss.GuiBoss;

public class PluginLoader {

    public PluginLoader(GuiBoss gaggleBoss, String[] classNames) {
        for (String className : classNames) loadPlugin(className, gaggleBoss);
    }

    private void loadPlugin(String className, GuiBoss gaggleBoss) {
        try {
            Class pluginClass = Class.forName(className);
            Class[] argClasses = new Class[1];
            argClasses [0] = gaggleBoss.getClass();
            Object[] args = new Object[1];
            args[0] = gaggleBoss;
            Constructor ctor = pluginClass.getConstructor(argClasses);
            ctor.newInstance(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }
    }
}
