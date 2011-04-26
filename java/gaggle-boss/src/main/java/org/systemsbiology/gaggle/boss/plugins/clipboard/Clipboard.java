/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.boss.plugins.clipboard;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import java.rmi.*;

import org.systemsbiology.gaggle.boss.GuiBoss;
import org.systemsbiology.gaggle.boss.plugins.GaggleBossPlugin;
import org.systemsbiology.gaggle.core.datatypes.Namelist;

public class Clipboard extends GaggleBossPlugin {

    private static final Border EMPTY_BORDER = new EmptyBorder(10, 10, 10, 10);
    private static final String PLUGIN_NAME = "Clipboard";
    private JTextArea textArea;
    private GuiBoss gaggleBoss;
    private String species = "unknown";
  
    public Clipboard(GuiBoss boss) {
        super(PLUGIN_NAME);
        gaggleBoss = boss;
        createGui();
    }

    private void createGui() {
        setLayout(new BorderLayout());
        JPanel innerPanel = new JPanel(new BorderLayout());
  
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        innerPanel.setBorder(EMPTY_BORDER);
        innerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel broadcastButtonPanel = new JPanel();

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    textArea.setText("");
                }});

        JButton broadcastButton = new JButton("Broadcast");
        broadcastButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    broadcast();
                }});

        broadcastButtonPanel.add(clearButton);
        broadcastButtonPanel.add(broadcastButton);

        innerPanel.add(broadcastButtonPanel, BorderLayout.SOUTH);
        add (innerPanel);
        ToolTipManager.sharedInstance().setInitialDelay (0);
    }

    private void broadcast() {
        String s = textArea.getText();
        s =  s.replaceAll("\\n", " ");
        String [] tokens = s.split("\\s+");
        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(tokens);
        gaggleBoss.broadcastNamelist(getName(), "all", nameList);
    }

    public void select(String[] names) {
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(name);
        }
        textArea.setText(sb.toString());  
    }
}
