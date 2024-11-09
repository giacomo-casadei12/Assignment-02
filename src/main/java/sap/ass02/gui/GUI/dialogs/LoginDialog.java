package sap.ass02.gui.GUI.dialogs;

import sap.ass02.gui.GUI.EBikeApp;
import sap.ass02.gui.WebClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;

/**
 * The Dialog that permits the user
 * to sign in in the system or create a new user.
 */
public class LoginDialog extends JDialog {
    private final Frame parent;
    private final WebClient webClient;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    @Serial
    private static final long serialVersionUID = 6L;

    /**
     * Instantiates a new Login dialog.
     *
     * @param parent    the parent Frame
     * @param webClient the web client connected to the server
     */
    public LoginDialog(Frame parent, WebClient webClient) {
        super(parent, "Login", true);
        this.parent = parent;
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        this.webClient = webClient;

    }

    /**
     * Initialize the dialog.
     */
    public void initializeDialog() {

        setLayout(new BorderLayout());
        setSize(300, 200);
        setLocationRelativeTo(parent);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");

        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton createUserButton = new JButton("Register");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(createUserButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                showNonBlockingMessage("Please enter both username and password.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                webClient.requestLogin(username, password).onComplete(x -> {
                    if (x.result()) {
                        showNonBlockingMessage("Login successful for user: " + username, "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        showNonBlockingMessage("Username and/or password wrong", "Fail", JOptionPane.ERROR_MESSAGE);
                    }
            });
            }
        });

        createUserButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            webClient.requestCreateUser(username, password).onComplete(x -> {
                if (x.result()) {
                    showNonBlockingMessage("Successfully created user: " + username, "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showNonBlockingMessage("Error in creating user", "Fail", JOptionPane.ERROR_MESSAGE);
                }
            });
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.exit(-1);
            }
        });

    }


    private void showNonBlockingMessage(String message, String title, int messageType) {

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                return null;
            }

            @Override
            protected void done() {

                JOptionPane.showMessageDialog(LoginDialog.this, message, title, messageType);
                if (title.contains("Success")) {
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        var app = new EBikeApp(webClient, usernameField.getText());
                        app.initialize();
                        app.setVisible(true);
                    });
                }
            }
        }.execute();
    }

}