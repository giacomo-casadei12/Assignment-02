package sap.ass02.gui.GUI.dialogs;

import sap.ass02.gui.GUI.EBikeApp;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * The Dialog that permits the user to
 * recharge its credit.
 */
public class RechargeCreditDialog extends JDialog {

    private final JTextField creditField;
    private final int userId;
    private final int actualCredit;
    private final EBikeApp app;
    @Serial
    private static final long serialVersionUID = 8L;

    /**
     * Instantiates a new Recharge credit dialog.
     *
     * @param userId       the user id of the requesting user
     * @param actualCredit the actual credit of the user
     * @param parent       the EBikeApp
     */
    public RechargeCreditDialog(int userId, int actualCredit, EBikeApp parent) {
        super(parent, "Login", true);
        this.app = parent;
        creditField = new JTextField();
        this.userId = userId;
        this.actualCredit = actualCredit;
    }

    /**
     * Initialize the dialog.
     */
    public void initializeDialog() {

        setLayout(new BorderLayout());
        setSize(300, 200);
        setLocationRelativeTo(app);


        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel creditLabel = new JLabel("Credit to add:");

        inputPanel.add(creditLabel);
        inputPanel.add(creditField);

        JButton rechargeButton = new JButton("Recharge");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(rechargeButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        rechargeButton.addActionListener(e -> {
            int credit = 0;
            boolean valid = true;

            try {
                credit = Integer.parseInt(creditField.getText());
                if (credit <= 0) {
                    valid = false;
                }
            } catch (NumberFormatException ex) {
                valid = false;
            }

            if (valid) {
                credit = credit + actualCredit;
                app.requestUpdateUser(this.userId, credit).onComplete(x -> {
                    if (x.result()) {
                        showNonBlockingMessage("Successfully recharged credit", "Success", JOptionPane.INFORMATION_MESSAGE);
                        app.updateUser();
                    } else {
                        showNonBlockingMessage("Something went wrong", "Fail", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } else {
                showNonBlockingMessage("Please enter a valid positive number", "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(RechargeCreditDialog.this, message, title, messageType);
                if (title.contains("Success")) {
                    dispose();
                }
            }
        }.execute();
    }
}
