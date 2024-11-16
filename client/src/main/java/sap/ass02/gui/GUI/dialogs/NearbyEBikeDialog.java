package sap.ass02.gui.GUI.dialogs;

import sap.ass02.gui.GUI.EBikeApp;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * The Dialog that permits the user to specify
 * its position to find nearby bikes.
 */
public class NearbyEBikeDialog extends JDialog {

    private JTextField xCoordField;
    private JTextField yCoordField;
    private JButton okButton;
    private JButton cancelButton;
    private final EBikeApp app;
    @Serial
    private static final long serialVersionUID = 7L;

    /**
     * Instantiates a new Nearby e bike dialog.
     *
     * @param app the EBikeApp
     */
    public NearbyEBikeDialog(EBikeApp app) {
        super(app, "Insert actual position", true);
        this.app = app;
    }

    /**
     * Initialize the dialog.
     */
    public void initializeDialog() {
        xCoordField = new JTextField(15);
        yCoordField = new JTextField(15);
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        setupLayout();
        addEventHandlers();
        pack();
        setLocationRelativeTo(app);
    }

    private void setupLayout() {
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Your location - X coord:"));
        inputPanel.add(xCoordField);
        inputPanel.add(new JLabel("Your location - Y coord:"));
        inputPanel.add(yCoordField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addEventHandlers() {
        okButton.addActionListener(e -> {

            int xCoord = Integer.parseInt(xCoordField.getText());
            int yCoord = Integer.parseInt(yCoordField.getText());
            this.app.requestReadEBike(0, xCoord, yCoord, false).onComplete(x -> {
                if (!x.result().isEmpty()) {
                    new AllEBikesDialog(app, x.result());
                    dispose();
                } else {
                    this.showNonBlockingMessage();
                }
            });
        });

        cancelButton.addActionListener(e -> dispose());
    }

    private void showNonBlockingMessage() {

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(NearbyEBikeDialog.this, "No bikes nearby", "No bikes", JOptionPane.INFORMATION_MESSAGE);
            }
        }.execute();
    }

}
