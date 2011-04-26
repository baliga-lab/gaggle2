/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.boss.plugins.sbeams;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PasswordDialog extends JDialog {

    private boolean pressed_OK = false;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton okButton;
    private JButton cancelButton;
    private JLabel nameLabel;
    private JLabel passLabel;

    public PasswordDialog() { this(null, null); }
    public PasswordDialog(String title) { this(null, title); }
    public PasswordDialog(Frame parent) { this(parent, null); }
    public PasswordDialog(Frame parent, String title) {
        super(parent, title, true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if (title == null) {
            setTitle("User Login");
        }
        if (parent != null){
            setLocationRelativeTo(parent);
        }
    }

    protected void dialogInit() {
        usernameField = new JTextField("", 20);
        passwordField = new JPasswordField("", 20);
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        nameLabel = new JLabel("User Name ");
        passLabel = new JLabel("Password ");
        super.dialogInit();

        KeyListener keyListener = new KeyAdapter() {
                public void keyPressed(KeyEvent e){
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
                        (e.getSource() == cancelButton
                         && e.getKeyCode() == KeyEvent.VK_ENTER)){
                        pressed_OK = false;
                        PasswordDialog.this.setVisible(false);
                    }
                    if (e.getSource() == okButton &&
                        e.getKeyCode() == KeyEvent.VK_ENTER){
                        pressed_OK = true;
                        PasswordDialog.this.setVisible(false);
                    }
                }
            };
        addKeyListener(keyListener);

        passwordField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyText(e.getKeyCode()).equals("Enter")) {
                        pressed_OK = true;
                        PasswordDialog.this.setVisible(false);
                    }}});

        ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    Object source = e.getSource();
                    if (source == usernameField){
                        usernameField.transferFocus();
                    } else {
                        pressed_OK = (source == passwordField || source == okButton);
                        PasswordDialog.this.setVisible(false);
                        PasswordDialog.this.dispose();
                    }
                }
            };


        // Layout
        GridBagLayout gridbag = new GridBagLayout();
        JPanel panel = new JPanel();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.top = 5;
        constraints.insets.bottom = 5;

        JPanel pane = new JPanel(gridbag);
        pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

        constraints.anchor = GridBagConstraints.EAST;
        gridbag.setConstraints(nameLabel, constraints);
        pane.add(nameLabel);

        gridbag.setConstraints(usernameField, constraints);
        usernameField.addActionListener(actionListener);
        usernameField.addKeyListener(keyListener);
        pane.add(usernameField);

        constraints.gridy = 1;
        gridbag.setConstraints(passLabel, constraints);
        pane.add(passLabel);

        // Listeners
        gridbag.setConstraints(passwordField, constraints);
        passwordField.addActionListener(actionListener);
        passwordField.addKeyListener(keyListener);
        pane.add(passwordField);

        // Gridy
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.CENTER;

        // Buttons
        okButton.addActionListener(actionListener);
        okButton.addKeyListener(keyListener);
        panel.add(okButton);
        cancelButton.addActionListener(actionListener);
        cancelButton.addKeyListener(keyListener);
        panel.add(cancelButton);
        gridbag.setConstraints(panel, constraints);
        pane.add(panel);

        GraphicsConfiguration gc = getGraphicsConfiguration ();
        int screenHeight = (int) gc.getBounds().getHeight();
        int screenWidth = (int) gc.getBounds().getWidth();
        int windowHeight = (int) getHeight();
        int windowWidth = (int) getWidth();
        int x = (int) ((screenWidth - windowWidth)/2);
        int y = (int) ((screenHeight - windowHeight)/2);
        setLocation(x, y);
        getContentPane().add(pane);
        pack();
    }

    public void setPasswordFocus()   { passwordField.requestFocus(); }
    public void setUsernameFocus()   { usernameField.requestFocus(); }
    public void setName(String name) { usernameField.setText(name); }
    public void setPass(String pass) { passwordField.setText(pass); }
    public void setOKText(String ok) {
        okButton.setText(ok);
        pack();
    }

    public void setCancelText(String cancel) {
        cancelButton.setText(cancel);
        pack();
    }

    public void setNameLabel(String name) {
        nameLabel.setText(name);
        pack();
    }

    public void setPassLabel(String pass) {
        passLabel.setText(pass);
        pack();
    }

    public String getName() { return usernameField.getText(); }
    public String getPass() {
        return new String(passwordField.getPassword());
    }

    public boolean okPressed(){
        return pressed_OK;
    }

    public boolean showDialog(){
        setVisible(true);
        return okPressed();
    }
}
