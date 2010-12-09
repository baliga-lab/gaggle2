// Interaction.java
//  source node to target, with an interaction type
//----------------------------------------------------------------------------------------
// RCS: $Revision:1365 $   
// $Date:2007-04-12 15:45:50 -0700 (Thu, 12 Apr 2007) $ 
// $Author:dtenenba $
//-----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

/**
 * Encapsulates a network interaction (two nodes and an edge). Used by the top-level
 * GaggleData object Network.
 * @see Network
 */
package org.systemsbiology.gaggle.core.datatypes;

import java.io.*;

public class Interaction implements Serializable {
    private static final long serialVersionUID = -3634738795512456706L;
    private String source;
    private String target;
    private String interactionType;
    private boolean directed;

    public Interaction(String source, String target, String interactionType, boolean directed) {
        this.source = source;
        this.interactionType = interactionType;
        this.target = target;
        this.directed = directed;
    }

    public Interaction(String source, String target, String interactionType) {
        this(source, target, interactionType, false);
    }

    public String getSource() { return source; }
    public String getType() { return interactionType; }
    public String getTarget() { return target; }

    public boolean isDirected() { return directed; }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(source);
        sb.append(" (");
        sb.append(interactionType);
        sb.append(") ");
        sb.append(target);
        return sb.toString();
    }

    public boolean equals(Interaction other) {
        return (other.toString().equals(toString()));
    }
}


