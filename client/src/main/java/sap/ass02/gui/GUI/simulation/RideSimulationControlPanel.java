package sap.ass02.gui.GUI.simulation;

import sap.ass02.gui.GUI.EBikeApp;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * The Panel showing that a ride is in progress.
 */
public class RideSimulationControlPanel extends JFrame {

    private final int userId;
    private final int bikeId;
    private final EBikeApp bikeApp;
    @Serial
    private static final long serialVersionUID = 10L;

    /**
     * Instantiates a new Ride simulation control panel.
     *
     * @param userId the user id of the current user
     * @param bikeId the bike id of the selected bike
     * @param app    the EBikeApp
     */
    public RideSimulationControlPanel(int userId, int bikeId, EBikeApp app) {
        super("Ongoing Ride: " + userId);
        this.userId = userId;
        this.bikeId = bikeId;
        bikeApp = app;
    }

    /**
     * Initialize the panel.
     */
    public void initialize(){
        setSize(400, 200);

        JButton stopButton = new JButton("Stop Riding");

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Rider name: " + userId));
        inputPanel.add(new JLabel("Riding e-bike: " + bikeId));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(stopButton);

        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        stopButton.addActionListener(e -> {
            bikeApp.endRide(userId, bikeId);
            dispose();
        });
    }

    /**
     * Shows the panel.
     */
    public void display() {
    	SwingUtilities.invokeLater(() -> this.setVisible(true));
    }

}
