/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.boss;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.table.TableColumnModel;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.rmi.RemoteException;
import java.security.Security;

import org.systemsbiology.gaggle.boss.plugins.*;
import org.systemsbiology.gaggle.util.*;
import org.systemsbiology.gaggle.core.*;
import org.systemsbiology.gaggle.core.datatypes.*;

import java.util.logging.*;

public final class GuiBoss implements BossUI {

    private static final Logger Log = Logger.getLogger("Boss UI");
    private static final String FRAME_TITLE = "Gaggle Boss v.4360";
    private static final String ABOUT_MESSAGE =
        "<html><center>" +
        "<h3>The Gaggle is developed by the Baliga Laboratory<br>" +
        "at the Institute for Systems Biology.<br></h3>" +
        "<a href=\"http://www.systemsbiology.org\">http://www.systemsbiology.org</a><br>" +
        "Software Developers: Paul Shannon, Wei-ju Wu<br>" +
        "Principal Investigator: Nitin S. Baliga<br><br>" +
        "Supported by research grants from NSF, DoE and DoD<br><br>" +
        "For more information visit the Baliga Laboratory: <br>" +
        "<a href=\"http://baliga.systemsbiology.net\">" +
        "http://baliga.systembiology.net</a></center></html>";

    private JFrame frame;
    private JTable gooseTable;
    private GaggleBossTableModel gooseTableModel;

    private List<GaggleBossPlugin> plugins = new ArrayList<GaggleBossPlugin>();

    private JTabbedPane tabbedPanel;
    private JButton frameSizeToggleButton;
    private boolean bodyVisible = true;
    private BossConfig config;
    private BossImpl bossImpl;

    public GuiBoss(String[] args) {
        Security.setProperty("networkaddress.cache.ttl","0");
        Security.setProperty("networkaddress.cache.negative.ttl","0");
        Log.info("ttl settings changed in boss");

        config = new BossConfig(args);
        try {
            this.bossImpl = new BossImpl(this, config.getNameHelperURI());
        } catch (Exception ex0) {
            displayErrorMessage("Error reading name helper file from " +
                                config.getNameHelperURI() + "\n" + ex0.getMessage());
        }
        Log.info("start invisibly? " + config.startInvisibly());
        Log.info("start minimized? " + config.startMinimized());

        frame = new JFrame(FRAME_TITLE);
        MiscUtil.setApplicationIcon(frame);

        try {
            bossImpl.bind();
        } catch (Exception e) {
            displayErrorMessage("Gaggle Port already in use.  Exiting....");
            System.exit(0);
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(createGui());
        frame.pack();

        if (!config.startInvisibly()) {
            frame.setVisible(true);
            frame.toFront();
        }
        if (config.startMinimized()) {
            frame.setVisible(true);
            frame.setState(Frame.ICONIFIED);
        }

    }

    public BossConfig getConfig() { return config; }
    public JFrame getFrame() { return frame; }

    private String[] getSelectedGooseNames() {
        List<String> list = new ArrayList<String>();
        int[] selectedRows = gooseTable.getSelectedRows();
        String[] gooseNamesInTable = gooseTableModel.getAppNames();

        for (int i = 0; i < selectedRows.length; i++) {
            list.add(gooseNamesInTable[selectedRows[i]]);
        }
        return list.toArray(new String[0]);
    }

    private String[] getUnselectedGooseNames() {
        List<String> list = new ArrayList<String>();
        String[] selectedGooseNames = getSelectedGooseNames();
        String[] allNames = bossImpl.getGooseNames();

        Arrays.sort(selectedGooseNames);
        Arrays.sort(allNames);
        for (int i = 0; i < allNames.length; i++) {
            if (Arrays.binarySearch(selectedGooseNames, allNames[i]) < 0) {
                list.add(allNames[i]);
            }
        }
        return list.toArray(new String[0]);
    }

    public void broadcastToPlugins(String[] names) {
        for (GaggleBossPlugin plugin : plugins) plugin.select(names);
    }
    public void broadcastNamelist(String sourceGoose, String targetGoose,
                                  Namelist nameList) {
        bossImpl.broadcastNamelist(sourceGoose, targetGoose, nameList);
    }

    public String renameGooseDirectly(String oldName,
                                      String proposedName) throws RemoteException {
        return bossImpl.renameGooseDirectly(oldName, proposedName);
    }

    public void show() {
        if (getFrame().getExtendedState() != java.awt.Frame.NORMAL)
            getFrame().setExtendedState(java.awt.Frame.NORMAL);
        getFrame().setAlwaysOnTop(true);
        MiscUtil.setJFrameAlwaysOnTop(getFrame(), true);
        getFrame().setVisible(true);
        getFrame().setAlwaysOnTop(false);
        MiscUtil.setJFrameAlwaysOnTop(getFrame(), false);
    }

    public void hide() { frame.setVisible(false); }

    public void askForShow(String gooseName) {
        getFrame().toBack();
        getFrame().toFront();
        getFrame().setVisible(true);
    }

    public void toggleVisibility() {
        bodyVisible = !bodyVisible;
        frameSizeToggleButton.setText(bodyVisible ? "Shrink" : "Boss");
        tabbedPanel.setVisible(bodyVisible);
        frame.pack();
    }

    private JPanel createGui() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        frameSizeToggleButton = new JButton("Shrink");
        frameSizeToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { toggleVisibility(); }
        });
        toolbar.add(frameSizeToggleButton);
        outerPanel.add(toolbar, BorderLayout.NORTH);

        JButton aboutButton = new JButton("About");
        aboutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, ABOUT_MESSAGE, "About the Gaggle",
                                              JOptionPane.INFORMATION_MESSAGE, null);
            }
        });
        toolbar.add(aboutButton);
        outerPanel.add(toolbar, BorderLayout.NORTH);

        tabbedPanel = new JTabbedPane();
        outerPanel.add(tabbedPanel, BorderLayout.CENTER);
        tabbedPanel.add(createGaggleControlPanel(), "Gaggle");

        String[] pluginClassNames = config.getPluginNames();
        for (int i = 0; i < pluginClassNames.length; i++) {
            Log.info("about to load boss plugin: " + pluginClassNames[i]);
            GaggleBossPlugin plugin = loadPlugin(pluginClassNames[i], this);
            if (plugin == null) {
                String msg = "Could not locate plugin '" + pluginClassNames[i] + "'";
                JOptionPane.showMessageDialog(frame, msg);
                continue;
            }
            plugins.add(plugin);
            tabbedPanel.add(plugin, plugin.getName());
        }
        return outerPanel;
    }

    private JPanel createGaggleControlPanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        JPanel tableOuterPanel = new JPanel(new BorderLayout());
        tableOuterPanel.add(tablePanel, BorderLayout.CENTER);
        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.add(createButtonsInPanel(), BorderLayout.CENTER);
        tableOuterPanel.add(controlsPanel, BorderLayout.SOUTH);

        gooseTableModel = new GaggleBossTableModel(this);
        gooseTable = new JTable(gooseTableModel);
        gooseTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        gooseTable.setShowGrid(true);
        gooseTable.setGridColor(Color.gray);
        gooseTable.setDefaultRenderer(JButton.class, new ButtonCellRenderer(
                gooseTable.getDefaultRenderer(JButton.class)));

        gooseTable.setPreferredScrollableViewportSize(new Dimension(400, 200));
        gooseTable.addMouseListener(new GaggleMouseListener());

        tablePanel.add(new JScrollPane(gooseTable), BorderLayout.CENTER);

        return tableOuterPanel;
    }

    private JPanel createButtonsInPanel() {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel actionButtonPanel = new JPanel(new GridLayout(2, 1));
        JPanel upperActionButtonPanel = new JPanel();
        JPanel lowerActionButtonPanel = new JPanel();
        JPanel quitButtonPanel = new JPanel();

        actionButtonPanel.add(upperActionButtonPanel);
        actionButtonPanel.add(lowerActionButtonPanel);

        JButton showButton = new JButton("Show");
        JButton showOthersButton = new JButton("Show Others");
        JButton hideButton = new JButton("Hide");
        JButton hideOthersButton = new JButton("Hide Others");
        JButton selectAllButton = new JButton("Select All");
        JButton refreshButton = new JButton("Refresh");
        JButton listenAllButton = new JButton("Listen All");
        JButton listenNoneButton = new JButton("Listen None");
        JButton terminateButton = new JButton("Terminate");
        JButton quitButton = new JButton("Quit");

        showButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    showSelectedGeese();
                }
            });
        showOthersButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    showNonSelectedGeese();
                } 
            });
        hideButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    hideSelectedGeese();
                }
            });
        hideOthersButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    hideNonSelectedGeese();
                }
            });
        selectAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    gooseTable.selectAll();
                }
            });
        refreshButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    bossImpl.unregisterIdleGeeseAndUpdate();
                }
            });
        listenAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    listenAll();
                }
            });
        listenNoneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    listenNone();
                }
            });
        terminateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    terminateSelectedGeese();
                }
            });
        quitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) { quit(); }
            });

        upperActionButtonPanel.add(showButton);
        upperActionButtonPanel.add(showOthersButton);
        upperActionButtonPanel.add(hideButton);
        upperActionButtonPanel.add(hideOthersButton);

        lowerActionButtonPanel.add(selectAllButton);
        lowerActionButtonPanel.add(refreshButton);
        lowerActionButtonPanel.add(listenAllButton);
        lowerActionButtonPanel.add(listenNoneButton);
        lowerActionButtonPanel.add(terminateButton);

        quitButtonPanel.add(quitButton);

        buttonPanel.add(actionButtonPanel, BorderLayout.CENTER);
        buttonPanel.add(quitButtonPanel, BorderLayout.SOUTH);

        return buttonPanel;
    }

    private void listenAll() {
        for (String name : bossImpl.getGooseNames()) {
            gooseTableModel.setListeningState(name, true);
        }
    }

    private void listenNone() {
        for (String name : bossImpl.getGooseNames()) {
            gooseTableModel.setListeningState(name, false);
        }
    }

    private void terminateSelectedGeese() {
        for (String name : getSelectedGooseNames()) {
            bossImpl.terminate(name);
        }
    }

    private void quit() {
        int dialogResult =
            JOptionPane.showConfirmDialog(frame, "Really Quit?",
                                          "Exit the Gaggle Boss?",
                                          JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            try {
                bossImpl.unbind();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        }
    }

    private void showSelectedGeese() {
        for (String name : getSelectedGooseNames()) bossImpl.show(name);
    }

    private void showNonSelectedGeese() {
        for (String name : getUnselectedGooseNames()) bossImpl.show(name);
    }
    private void hideSelectedGeese() {
        for (String name : getSelectedGooseNames()) bossImpl.hide(name);
    }
    private void hideNonSelectedGeese() {
        for (String name : getUnselectedGooseNames()) bossImpl.hide(name);
    }

    public String[] getListeningGeese() {
        List<String> result = new ArrayList<String>();
        for (String goose : bossImpl.getGooseNames()) {
            if (isListening(goose)) result.add(goose);
        }
        return result.toArray(new String[0]);

    }

    class GaggleMouseListener extends MouseAdapter {
        private void forwardEventToButton(MouseEvent e) {
            TableColumnModel columnModel = gooseTable.getColumnModel();
            int column = columnModel.getColumnIndexAtX(e.getX());
            int row = e.getY() / gooseTable.getRowHeight();
            Object value;
            JButton button;

            if (row >= gooseTable.getRowCount() || row < 0 ||
                column >= gooseTable.getColumnCount() || column < 0) return;
            value = gooseTable.getValueAt(row, column);
            if (!(value instanceof JButton)) return;
            button = (JButton) value;
            button.doClick();
            gooseTable.repaint();
        }
        public void mouseReleased(MouseEvent e) { forwardEventToButton(e); }
    }

    static class ButtonCellRenderer implements TableCellRenderer {

        private TableCellRenderer defaultRenderer;

        public ButtonCellRenderer(TableCellRenderer renderer) {
            defaultRenderer = renderer;
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            if (value instanceof Component) {
                return (Component) value;
            } else {
                return defaultRenderer.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
            }
        }
    }

    private void placeInCenter() {
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        int screenHeight = (int) gc.getBounds().getHeight();
        int screenWidth = (int) gc.getBounds().getWidth();
        int windowWidth = frame.getWidth();
        int windowHeight = frame.getHeight();
        frame.setLocation((screenWidth - windowWidth) / 2, (screenHeight - windowHeight) / 2);

    }

    public void gooseAdded(String name) { gooseTableModel.addClient(name); }

    public void gooseUnregistered(String gooseName) {
        gooseTableModel.removeGoose(gooseName);
    }

    public void gooseRenamed(String oldName, String uniqueName) {
        String[] appNames = GuiBoss.this.gooseTableModel.getAppNames();
        for (int i = 0; i < appNames.length; i++) {
            if (appNames[i].equals(oldName)) {
                GuiBoss.this.gooseTableModel.setAppNameAtRow(uniqueName, i);
                GuiBoss.this.gooseTableModel.fireTableDataChanged();
            }
        }
    }

    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public boolean isListening(String gooseName) {
        return gooseTableModel.isListening(gooseName);
    }

    public GaggleBossPlugin loadPlugin(String className, GuiBoss gaggleBoss) {
        try {
            Class pluginClass = Class.forName(className);
            Class[] argClasses = new Class[1];
            argClasses[0] = gaggleBoss.getClass();
            Object[] args = new Object[1];
            args[0] = gaggleBoss;
            Constructor ctor = pluginClass.getConstructor(argClasses);
            Object plugin = ctor.newInstance(args);
            return (GaggleBossPlugin) plugin;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return null;
        }

    }

    public static void main(String[] args) throws Exception {
        new GuiBoss(args);
    }
}
