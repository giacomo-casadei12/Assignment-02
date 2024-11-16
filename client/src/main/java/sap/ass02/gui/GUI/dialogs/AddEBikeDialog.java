package sap.ass02.gui.GUI.dialogs;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * 
 * Courteously implemented by ChatGPT
 * prompt:
 * "Hello ChatGPT. Could you write me a Java class 
 *  implementing a JDialog with title "Adding E-Bike", 
 *  including "OK" and "Cancel" buttons, and some input fields, 
 *  namely: an id input field (with label "E-Bike id"),
 *  an x input field (with label "E-Bike location - X coord:") 
 *  and a y input field (with label "E-Bike location - Y coord:").
 *  Thanks a lot!"
 * 
 */
public class AddEBikeDialog extends JDialog {

    private JTextField xCoordField;
    private JTextField yCoordField;
    private JButton okButton;
    private JButton cancelButton;
    private final AllEBikesDialog app;
    @Serial
    private static final long serialVersionUID = 2L;

    /**
     * Instantiates a new Add EBike dialog.
     *
     * @param owner the AllEBikesDialog from which this has been called
     */
    public AddEBikeDialog(AllEBikesDialog owner) {
        super(owner, "Adding E-Bike", true);
        this.app = owner;
    }

    /**
     * Initialize the dialog.
     */
    public void initializeDialog() {
        initializeComponents();
        setupLayout();
        addEventHandlers();
        pack();
        setLocationRelativeTo(app);
    }

    private void initializeComponents() {
        xCoordField = new JTextField(15);
        yCoordField = new JTextField(15);
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
    }

    private void setupLayout() {
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("E-Bike location - X coord:"));
        inputPanel.add(xCoordField);
        inputPanel.add(new JLabel("E-Bike location - Y coord:"));
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
            String xCoord = xCoordField.getText();
            String yCoord = yCoordField.getText();
            app.addEBike(Integer.parseInt(xCoord), Integer.parseInt(yCoord));
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }

}
