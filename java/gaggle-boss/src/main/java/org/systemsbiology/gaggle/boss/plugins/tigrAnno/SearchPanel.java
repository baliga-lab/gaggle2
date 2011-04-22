/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.boss.plugins.tigrAnno;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.*;


import java.rmi.*;
import java.util.*;

import org.systemsbiology.gaggle.boss.GuiBoss;
import org.systemsbiology.gaggle.util.*;
import org.systemsbiology.gaggle.boss.plugins.GaggleBossPlugin;
import org.systemsbiology.gaggle.core.datatypes.Namelist;

public class SearchPanel extends GaggleBossPlugin {

    private static final String PLUGIN_NAME = "TIGR Search Panel";
    private JTextField searchBox;
    private SearchResultsTableModel tableModel;
    private JTable resultsTable;

    private JTextField resultSetSizeReadout;
    private JButton broadcastSelectionsButton;
    private JButton selectAllRowsButton;
    private JButton clearAllSelectionsButton;
    private JButton clearTableButton;
    private JTextField conditionCounterTextField;

    private GuiBoss gaggleBoss;
    private Map<String, String> searchMap = new HashMap<String, String>();
    private Map<String, String[]> summaryMap = new HashMap<String, String[]>();
  
    public SearchPanel(GuiBoss boss) {
        super ("Annotation Search");
        gaggleBoss = boss;
        System.out.println("got annotationUri from props: " + annotationURI());
        createGui();
        if (annotationURI() != null) readAnnotationData(annotationURI());
    }

    private String species() {
        String result = gaggleBoss.getConfig().getProperties().getProperty("species");
        return result == null ? "unknown" : result;
    }
    private String annotationURI() {
        return gaggleBoss.getConfig().getProperties().getProperty("annotation");
    }

    private void readAnnotationData(String uri) {
        String[] lines = new String[0];

        try {
            TextHttpReader reader = new TextHttpReader(uri);
            System.out.println("about to read: " + uri);
            reader.read();
            System.out.println("reading done");
            String rawText = reader.getText();
            lines = rawText.split("\n");
            System.out.println(" * annotation lines read: " + lines.length);
        } catch (Exception ex0) {
            String msg = "<html>Error reading annotation from <br>" + annotationURI()
                + ":<br>" + ex0.getMessage() + "</html>";
            JOptionPane.showMessageDialog(this, msg);
        }

        for (int i = 1; i < lines.length; i++) {
            String [] tokens = lines [i].split("\t");
            if (tokens.length > 2) {
                String orf = tokens [0];
                String geneSymbol = tokens[1];
                String function = tokens[2];
                String uniquifier = String.valueOf(i);
                searchMap.put(orf, orf);
                searchMap.put(geneSymbol, orf);
                searchMap.put(function + uniquifier, orf);
                summaryMap.put(orf, new String [] {geneSymbol, function});
            }
        }
    }

    private void createGui() {
        setLayout(new BorderLayout());
        JPanel innerPanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel();
  
        JButton searchButton = new JButton("Search");
        searchButton.setToolTipText("Find every gene matching (in name or annotation)");
        searchButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { performSearch(); }});

        searchBox = new JTextField(20);
        searchBox.setToolTipText("one or more search terms; use semi-colon delimitors; substrings okay");
        searchBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    performSearch();
                }
            });

        searchBox.addKeyListener (new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyText(e.getKeyCode()).equals("Enter")) performSearch();
                }
            });

        JButton clearSearchBoxButton = new JButton("Clear");
        clearSearchBoxButton.setToolTipText("clear the search box of all text");
        clearSearchBoxButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    searchBox.setText("");
                }
            });
        searchPanel.add(searchButton);
        searchPanel.add(searchBox);
        searchPanel.add(clearSearchBoxButton);
  
        selectAllRowsButton = new JButton("Select All");
        clearAllSelectionsButton = new JButton("Deselect All");
        clearTableButton = new JButton("Clear All");
        broadcastSelectionsButton = new JButton("Broadcast");

        selectAllRowsButton.setEnabled(false);
        selectAllRowsButton.setToolTipText("Select all rows in current search result set");
        clearAllSelectionsButton.setEnabled(false);
        clearAllSelectionsButton.setToolTipText("Deselect all rows");
   
        clearTableButton.setEnabled(false);
        clearTableButton.setToolTipText("Remove all rows");
        broadcastSelectionsButton.setEnabled(false);

        selectAllRowsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    resultsTable.selectAll();
                }
            });
        clearAllSelectionsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    resultsTable.clearSelection();
                }
            });
        clearTableButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tableModel.clearData();
                    tableModel.fireTableStructureChanged();
                    setTableColumnWidths();
                    resultSetSizeReadout.setText("0");
                }
            });

        broadcastSelectionsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    broadcastSelection();
                }
            });
        broadcastSelectionsButton.setToolTipText("Broadcast all ORF names to all geese");

        innerPanel.add(searchPanel, BorderLayout.NORTH);
        innerPanel.add(createSearchResultsTable(), BorderLayout.CENTER);
        JPanel broadcastButtonPanel = new JPanel();

        resultSetSizeReadout = new JTextField("0", 4);
        resultSetSizeReadout.setToolTipText("Number of genes found in last search");
        resultSetSizeReadout.setEditable(false);

        broadcastButtonPanel.add(resultSetSizeReadout);
        broadcastButtonPanel.add(selectAllRowsButton);
        broadcastButtonPanel.add(clearAllSelectionsButton);
        broadcastButtonPanel.add(clearTableButton);
        broadcastButtonPanel.add(broadcastSelectionsButton);

        innerPanel.add(broadcastButtonPanel, BorderLayout.SOUTH);
        add (innerPanel);
        ToolTipManager.sharedInstance().setInitialDelay (0);
    }

    private void setTableColumnWidths() {
        int narrow = 80;
        int broad = 400;
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(narrow);
        resultsTable.getColumnModel().getColumn(0).setMaxWidth(narrow);

        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(narrow);
        resultsTable.getColumnModel().getColumn(1).setMaxWidth(narrow);

        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(broad);
    }

    private JPanel createSearchResultsTable() {
        tableModel = new SearchResultsTableModel();
        resultsTable = new JTable(tableModel);
        setTableColumnWidths();

        resultsTable.setShowHorizontalLines (true);
        resultsTable.setShowVerticalLines (true);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private void broadcastSelection() {
        int[] selectedRows = resultsTable.getSelectedRows();

        List<String> selectedOrfs = new ArrayList<String>();
        for (int i = 0; i < selectedRows.length; i++) {
            String orfName = (String) tableModel.getValueAt(selectedRows[i], 0);
            orfName = orfName.toUpperCase();
            selectedOrfs.add(orfName);
        }
        Namelist nameList = new Namelist();
        nameList.setSpecies(species());
        nameList.setNames(selectedOrfs.toArray(new String[0]));
        gaggleBoss.broadcastNamelist(PLUGIN_NAME, "all", nameList);
    }

    private String rationalizeSearchText(final String searchText) {
        return searchText.replaceAll("\\s+", ";").replaceAll(":", ";");
    }

    protected void performSearch() {
        String searchText = searchBox.getText().trim();
        if (searchText.length() < 1) return;

        searchText = rationalizeSearchText(searchText);    
        String[] searchMapKeys = searchMap.keySet().toArray(new String[0]);

        Set<String> hitsList = new HashSet<String>();

        String soughtForText = searchText.toLowerCase();
        String[] arrows = soughtForText.split(";");

        // 'arrows' are supplied by the user.  we see if any strike a 'target' -- a key in
        // the searchMap
        for (int i = 0; i < searchMapKeys.length; i++) {
            String targetText = searchMapKeys[i].toLowerCase();
            for (int a = 0; a < arrows.length; a++) {
                String arrow = arrows[a].trim();
                if (arrow.length() > 0 && targetText.indexOf(arrow) >= 0) {
                    hitsList.add(searchMap.get(searchMapKeys[i]));
                }
            }
        }

        String[] hits = hitsList.toArray(new String[0]);
        resultSetSizeReadout.setText(String.valueOf(hits.length));

        if (hits.length == 0) return;

        broadcastSelectionsButton.setEnabled(true);
        selectAllRowsButton.setEnabled(true);
        clearTableButton.setEnabled(true);
        clearAllSelectionsButton.setEnabled(true);
        tableModel.clearData();

        for (int i = 0; i < hits.length; i++) {
            String orf = hits[i];
            String[] searchValue = summaryMap.get(orf);
            tableModel.addSearchResult(orf, searchValue[0], searchValue[1]);
        }
        tableModel.fireTableStructureChanged();
        setTableColumnWidths();
    }

    public void select(String[] names) {
        StringBuilder sb = new StringBuilder();
        sb.append (searchBox.getText());
        for (String name : names) {
            if (sb.length() > 0) sb.append(";");
            sb.append(name);
        }
        searchBox.setText(sb.toString());  
    }
}
