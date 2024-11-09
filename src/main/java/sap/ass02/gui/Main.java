package sap.ass02.gui;

import sap.ass02.gui.GUI.dialogs.LoginDialog;

import javax.swing.*;

public class Main {
    /**
     * The main method for launching the GUI.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        WebClient client = new WebClientImpl();
        SwingUtilities.invokeLater(() -> {
            var dialog = new LoginDialog(null, client);
            dialog.initializeDialog();
            dialog.setVisible(true);
        });
    }
}
