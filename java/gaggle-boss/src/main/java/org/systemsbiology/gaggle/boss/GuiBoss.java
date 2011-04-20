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

public final class GuiBoss
implements WindowStateListener, Serializable {

    private static final String BOSS_REVISION = "4360";

    private JFrame frame;
    private JTable gooseTable;
    private JScrollPane scrollPane;
    private JTextField searchBox;
    private GaggleBossTableModel gooseTableModel;
    private NewNameHelper nameHelper;

    private List<GaggleBossPlugin> plugins = new ArrayList<GaggleBossPlugin>();

    private JPanel outerPanel;
    private JTabbedPane tabbedPanel;
    private JButton frameSizeToggleButton;
    private boolean bodyVisible = true;
    private BossConfig config;
    private BossImpl bossImpl = new BossImpl();

    public GuiBoss() { this(new String[0]); }

    public GuiBoss(String[] args) {
        Security.setProperty("networkaddress.cache.ttl","0");
        Security.setProperty("networkaddress.cache.negative.ttl","0");
        System.out.println("ttl settings changed in boss");

        config = new BossConfig(args);
        String nameHelperUri = config.getNameHelperUri();
        if (nameHelperUri != null && nameHelperUri.length() > 0) {
            try {
                nameHelper = new NewNameHelper(nameHelperUri);
            } catch (Exception ex0) {
                String msg = "Error reading name helper file from " +
                    nameHelperUri + "\n" + ex0.getMessage();
                JOptionPane.showMessageDialog(frame, msg);
            }
        }
        System.out.println("start invisibly? " + config.startInvisibly());
        System.out.println("start minimized? " + config.startMinimized());

        frame = new JFrame(createFrameTitle());
        frame.addWindowStateListener(this);

        MiscUtil.setApplicationIcon(frame);

        try {
            bossImpl.bind();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Gaggle Port already in use.  Exiting....");
            System.exit(0);
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowStateListener(this);
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
    public NewNameHelper getNameHelper() { return nameHelper; }
    public JFrame getFrame() { return frame; }

    public void windowStateChanged(WindowEvent e) { }

    private String createFrameTitle() {
        return "Gaggle Boss v." + BOSS_REVISION;
    }

    /**
     * return the names of the geese currently selected in the goose table
     */
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
        int[] selectedRows = gooseTable.getSelectedRows();
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

    protected void broadcastToPlugins(String[] names) {
        for (int i = 0; i < plugins.size(); i++) {
            GaggleBossPlugin plugin = plugins.get(i);
            plugin.select(names);
        }
    }

    public void cleanUpOnExit(String appName) { }


    public void show(String gooseName) {
        if (gooseName.equalsIgnoreCase("boss")) {
            if (getFrame().getExtendedState() != java.awt.Frame.NORMAL)
                getFrame().setExtendedState(java.awt.Frame.NORMAL);
            getFrame().setAlwaysOnTop(true);
            MiscUtil.setJFrameAlwaysOnTop(getFrame(), true);
            getFrame().setVisible(true);
            getFrame().setAlwaysOnTop(false);
            MiscUtil.setJFrameAlwaysOnTop(getFrame(), false);
            return;
        }

        final Goose goose = bossImpl.getGoose(gooseName);
        if (goose == null) return;

        try {
            goose.doShow();
        } catch (Exception ex0) {
            System.err.println("error in show request to " + gooseName + ": " +
                               ex0.getMessage());
            ex0.printStackTrace();
        }
    }

    public void askForShow(String gooseName) {
        getFrame().toBack();
        getFrame().toFront();
        getFrame().setVisible(true);
    }

    public void toggleVisibility() {
        bodyVisible = !bodyVisible;
        String label = "Shrink";
        if (!bodyVisible)
            label = "Boss";
        frameSizeToggleButton.setText(label);
        tabbedPanel.setVisible(bodyVisible);
        frame.pack();
    }

    protected JPanel createGui() {
        outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        frameSizeToggleButton = new JButton("Shrink");
        frameSizeToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toggleVisibility();
            }
        });
        toolbar.add(frameSizeToggleButton);
        outerPanel.add(toolbar, BorderLayout.NORTH);

        JButton aboutButton = new JButton("About");
        aboutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringBuffer sb = new StringBuffer();
                sb.append("<html><center>");
                sb.append("<h3>");
                sb.append("The Gaggle is developed by the Baliga Laboratory<br>");
                sb.append("at the Institute for Systems Biology.<br>");
                sb.append("</h3>");
                sb.append("<font color='blue'>");
                sb.append("http://www.systemsbiology.org<br>");
                sb.append("</font>");
                sb.append("<br>");
                sb.append("Software engineer:  Paul Shannon<br>");
                sb.append("Principal Investigator: Nitin S. Baliga<br>");
                sb.append("<br>");
                sb.append("Supported by research grants from NSF, DoE and DoD<br>");
                sb.append("<br>");
                sb.append("For more information visit the Baliga Laboratory: <br>");
                sb.append("<font color='blue' size='-1'>");
                sb.append("http://www.systembiology.org/Scientists_and_Research/Faculty_Groups/Baliga_Group");
                sb.append("</font>");
                sb.append("</center></html>");
                String msg = sb.toString();
                String title = "About the Gaggle";
                int messageType = JOptionPane.INFORMATION_MESSAGE;
                Icon icon = null;
                JOptionPane.showMessageDialog(frame, msg, title, messageType, icon);
            }
        });
        toolbar.add(aboutButton);
        outerPanel.add(toolbar, BorderLayout.NORTH);

        tabbedPanel = new JTabbedPane();
        outerPanel.add(tabbedPanel, BorderLayout.CENTER);
        tabbedPanel.add(createGaggleControlPanel(), "Gaggle");

        String[] pluginClassNames = config.getPluginNames();
        for (int i = 0; i < pluginClassNames.length; i++) {
            System.out.println("about to load boss plugin: " + pluginClassNames[i]);
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

    protected JPanel createGaggleControlPanel() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        int topBorder = 20;
        int bottomBorder = 10;
        int sideBorder = 20;

        tablePanel.setBorder(BorderFactory.createEmptyBorder(topBorder, sideBorder,
                bottomBorder, sideBorder));
        JPanel tableOuterPanel = new JPanel();
        tableOuterPanel.setLayout(new BorderLayout());
        tableOuterPanel.add(tablePanel, BorderLayout.CENTER);
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BorderLayout());
        controlsPanel.add(createButtonsInPanel(), BorderLayout.CENTER);
        // controlsPanel.add (createSearchBoxInPanel (), BorderLayout.NORTH);
        tableOuterPanel.add(controlsPanel, BorderLayout.SOUTH);


        gooseTableModel = new GaggleBossTableModel(this);
        gooseTable = new JTable(gooseTableModel);
        gooseTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        gooseTable.setShowGrid(true);
        gooseTable.setGridColor(Color.gray);
        gooseTable.setDefaultRenderer(JButton.class, new ButtonCellRenderer(
                gooseTable.getDefaultRenderer(JButton.class)));
        setTableColumnWidths();

        gooseTable.setPreferredScrollableViewportSize(new Dimension(400, 200));
        gooseTable.addMouseListener(new GaggleMouseListener(gooseTable));
        scrollPane = new JScrollPane(gooseTable);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tableOuterPanel;
    }

    protected JPanel createButtonsInPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        JPanel actionButtonPanel = new JPanel();
        JPanel upperActionButtonPanel = new JPanel();
        JPanel lowerActionButtonPanel = new JPanel();
        JPanel quitButtonPanel = new JPanel();

        actionButtonPanel.setLayout(new GridLayout(2, 1));
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

        showButton.addActionListener(new ShowAction());
        showOthersButton.addActionListener(new ShowOthersAction());
        hideButton.addActionListener(new HideAction());
        hideOthersButton.addActionListener(new HideOthersAction());
        selectAllButton.addActionListener(new SelectAllAction());
        refreshButton.addActionListener(new RefreshAction());
        listenAllButton.addActionListener(new ListenAllAction());
        listenNoneButton.addActionListener(new ListenNoneAction());
        terminateButton.addActionListener(new TerminateAction());
        quitButton.addActionListener(new QuitAction());

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

    protected void setTableColumnWidths() { }

    class SelectAllAction extends AbstractAction {

        SelectAllAction() { super(""); }
        public void actionPerformed(ActionEvent e) {
            gooseTable.selectAll();
        }
    }

    class ShowAction extends AbstractAction {

        ShowAction() { super(""); }

        public void actionPerformed(ActionEvent e) {
            String[] names = getSelectedGooseNames();
            for (int i = 0; i < names.length; i++) {
                try {
                    show(names[i]);
                }
                catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            }
        }
    }

    class ShowOthersAction extends AbstractAction {

        ShowOthersAction() { super(""); }
        public void actionPerformed(ActionEvent e) {
            String[] names = getUnselectedGooseNames();
            for (int i = 0; i < names.length; i++) {
                try {
                    show(names[i]);
                } catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            }
        }
    }

    class HideAction extends AbstractAction {

        HideAction() { super(""); }
        public void actionPerformed(ActionEvent e) {
            String[] names = getSelectedGooseNames();
            for (int i = 0; i < names.length; i++) {
                try {
                    bossImpl.hide(names[i]);
                } catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            }
        }
    }

    class HideOthersAction extends AbstractAction {

        HideOthersAction() { super(""); }
        public void actionPerformed(ActionEvent e) {
            String[] names = getUnselectedGooseNames();
            for (int i = 0; i < names.length; i++) {
                try {
                    bossImpl.hide(names[i]);
                } catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            }
        }
    }

    protected String[] getListeningGeese() {
        String[] allGeese = bossImpl.getGooseNames();
        List<String> tmp = new ArrayList<String>();
        for (int i = 0; i < allGeese.length; i++) {
            if (listening(allGeese[i])) tmp.add(allGeese[i]);
        }
        return tmp.toArray(new String[0]);

    }

    protected boolean listening(String gooseName) {
        return gooseTableModel.isListening(gooseName);
    }

    protected void setSelectionCount(String gooseName, int count) {
        gooseTableModel.setSelectionCount(gooseName, count);
    }

    public void refresh() { refresh(false); }

    public void refresh(boolean resetTableColumnWidths) {
        // todo - remove the following? is it used?
        if (resetTableColumnWidths) setTableColumnWidths();
    }

    class RefreshAction extends AbstractAction {

        RefreshAction() {
            super("");
            putValue(AbstractAction.SHORT_DESCRIPTION,
                    "Remove disconnected geese from list.");
        }
        public void actionPerformed(ActionEvent e) {
            bossImpl.unregisterIdleGeeseAndUpdate();
            refresh(true);
        }
    }

    class ListenAllAction extends AbstractAction {

        ListenAllAction() { super(""); }
        public void actionPerformed(ActionEvent e) {
            String[] gooseNames = bossImpl.getGooseNames();
            for (int i = 0; i < gooseNames.length; i++) {
                String name = gooseNames[i];
                gooseTableModel.setListeningState(name, true);
            }
        }
    }

    class ListenNoneAction extends AbstractAction {

        ListenNoneAction() { super(""); }
        public void actionPerformed(ActionEvent e) {
            String[] gooseNames = bossImpl.getGooseNames();
            for (int i = 0; i < gooseNames.length; i++) {
                String name = gooseNames[i];
                gooseTableModel.setListeningState(name, false);
            }
        }
    }

    class TileAction extends AbstractAction {

        TileAction() { super(""); }
        public void actionPerformed(ActionEvent e) { }
    }

    class StaggerAction extends AbstractAction {

        StaggerAction() { super(""); }

        public void actionPerformed(ActionEvent e) {
            System.out.println("StaggerAction");
        }

    }

    class TerminateAction extends AbstractAction {

        TerminateAction() { super(""); }
        
        public void actionPerformed(ActionEvent e) {
            refresh();
            String[] names = getSelectedGooseNames();
            for (int i = 0; i < names.length; i++) {
                try {
                    bossImpl.terminate(names[i]);
                } catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            }
            refresh(true);
        }
    }

    class QuitAction extends AbstractAction {
        QuitAction() { super(""); }

        public void actionPerformed(ActionEvent e) {
            int dialogResult =
                JOptionPane.showConfirmDialog(frame, "Really Quit?",
                                              "Exit the Gaggle Boss?",
                                              JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.NO_OPTION) return;

            try {
                bossImpl.unbind();
            } catch (Exception ex0) {
                ex0.printStackTrace();
            }
            System.exit(0);
        }
    }

    class GaggleMouseListener extends MouseAdapter {
        private JTable table;

        public GaggleMouseListener(JTable table) {
            this.table = table;
        }

        private void forwardEventToButton(MouseEvent e) {
            TableColumnModel columnModel = table.getColumnModel();
            int column = columnModel.getColumnIndexAtX(e.getX());
            int row = e.getY() / table.getRowHeight();
            Object value;
            JButton button;
            MouseEvent buttonEvent;
            if (row >= table.getRowCount() || row < 0 ||
                    column >= table.getColumnCount() || column < 0)
                return;
            value = table.getValueAt(row, column);
            if (!(value instanceof JButton)) return;
            button = (JButton) value;
            buttonEvent = (MouseEvent) SwingUtilities.convertMouseEvent(table, e, button);
            button.doClick();
            //button.dispatchEvent (buttonEvent);
            table.repaint();
        }
        public void mouseReleased(MouseEvent e) { forwardEventToButton(e); }
    }

    public void windowClosed(WindowEvent event) { }
    public void windowClosing(WindowEvent event) { }
    public void windowOpened(WindowEvent event) { }
    public void windowIconified(WindowEvent event) { }
    public void windowDeiconified(WindowEvent event) { }
    public void windowActivated(WindowEvent event) { }
    public void windowDeactivated(WindowEvent event) { }
    public void windowGainedFocus(WindowEvent event) { }
    public void windowLostFocus(WindowEvent event) { }

    class ButtonCellRenderer implements TableCellRenderer {

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

    public void addNewGoose(String name, Goose goose) {
        gooseMap.put(name, goose);
        gooseTableModel.addClient(name);
        setTableColumnWidths();
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
            Constructor[] ctors = pluginClass.getConstructors();
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
        try {
            GuiBoss app = new GuiBoss(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
