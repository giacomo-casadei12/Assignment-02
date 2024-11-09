package sap.ass02.gui.GUI;

import io.vertx.core.Future;
import sap.ass02.gui.GUI.dialogs.*;
import sap.ass02.gui.GUI.simulation.RideSimulation;
import sap.ass02.gui.GUI.simulation.RideSimulationControlPanel;
import sap.ass02.gui.WebClient;
import sap.ass02.gui.utils.Pair;
import sap.ass02.gui.utils.Triple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Main GUI of the EBike application.
 */
public class EBikeApp extends JFrame implements ActionListener {

    /**
     * The constant SINGLE_RESULT.
     */
    public static final int SINGLE_RESULT = 1;
    private final VisualiserPanel centralPanel;
    private final JPanel topPanel;
    private final JButton rechargeCreditButton;
    private final JButton nearbyBikeButton;
    private final JButton startRideButton;
    private final JButton myRidesButton;
    private final JButton allRidesButton;
    private final JButton allBikesButton;
    private final JButton allUsersButton;
    private Map<Integer, Triple<Pair<Integer, Integer>, Integer, String>> bikes = new ConcurrentHashMap<>();
    private final Map<Pair<Integer, Integer>, RideSimulation> rides = new ConcurrentHashMap<>();
    private Triple<String, Integer, Boolean> user;
    private int userId;
    private final String username;
    private final WebClient webClient;
    @Serial
    private static final long serialVersionUID = 11L;

    /**
     * Instantiates a new EBikeApp.
     *
     * @param webClient the web client that requests server
     * @param username  the username of the current user
     */
    public EBikeApp(WebClient webClient, String username) {
        this.webClient = webClient;
        rechargeCreditButton = new JButton("Recharge Credit");
        nearbyBikeButton = new JButton("Find nearby bikes");
        startRideButton = new JButton("Start Ride");
        myRidesButton = new JButton("My Rides");
        allRidesButton = new JButton("All Rides");
        allBikesButton = new JButton("All EBikes");
        allUsersButton = new JButton("All Users");
        centralPanel = new VisualiserPanel(800,500,this);
        topPanel = new JPanel();
        this.username = username;
    }

    /**
     * Initialize the view.
     */
    public void initialize() {
        retrieveData();
        setupView();
        webClient.startMonitoringEBike(this);
        webClient.startMonitoringUsers(this);
    }

    private void retrieveData() {
        this.webClient.requestReadUser(0,this.username).onComplete(res -> {
            if (res.result() != null) {
                var result = res.result();
                if (result.size() == SINGLE_RESULT) {
                    for (Integer key : result.keySet()) {
                        var element = result.get(key);
                        this.userId = key;
                        this.user = element;
                        if (this.user.third()) {
                            allRidesButton.setVisible(true);
                            allBikesButton.setVisible(true);
                            allUsersButton.setVisible(true);
                        }
                        refreshView();
                    }
                }
            }
        });
        this.webClient.requestReadEBike(0,0,0,false).onComplete(res -> {
            if (res.result() != null) {
                this.bikes = res.result();
                if (this.userId != 0) {
                    this.refreshView();
                }
            }
        });
    }

    /**
     * Sets the dimensions and buttons of the view.
     */
    protected void setupView() {
        setTitle("EBike App");
        setSize(800,600);
        setResizable(false);

        setLayout(new BorderLayout());

        rechargeCreditButton.addActionListener(this);

        nearbyBikeButton.addActionListener(this);

        startRideButton.addActionListener(this);

        myRidesButton.addActionListener(this);
        allRidesButton.addActionListener(this);
        allBikesButton.addActionListener(this);
        allUsersButton.addActionListener(this);

        topPanel.add(rechargeCreditButton);
        topPanel.add(nearbyBikeButton);
        topPanel.add(startRideButton);
        topPanel.add(myRidesButton);
        topPanel.add(allRidesButton);
        topPanel.add(allUsersButton);
        topPanel.add(allBikesButton);
        allRidesButton.setVisible(false);
        allBikesButton.setVisible(false);
        allUsersButton.setVisible(false);

        if (this.user != null && this.user.third()) {
            allRidesButton.setVisible(true);
            allBikesButton.setVisible(true);
            allUsersButton.setVisible(true);
        }


        add(topPanel,BorderLayout.NORTH);

        add(centralPanel,BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                System.exit(-SINGLE_RESULT);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.nearbyBikeButton)) {
            SwingUtilities.invokeLater(() -> {
                var d = new NearbyEBikeDialog(this);
                d.initializeDialog();
                d.setVisible(true);
            });
        } else if (e.getSource().equals(this.rechargeCreditButton)) {
            SwingUtilities.invokeLater(() -> {
                var d = new RechargeCreditDialog(this.userId,this.user.second(),this);
                d.initializeDialog();
                d.setVisible(true);
            });
        } else if (e.getSource().equals(this.startRideButton)) {
            SwingUtilities.invokeLater(() -> {
                var d = new RideDialog(this, this.userId);
                d.initializeDialog();
                d.setVisible(true);
            });
        } else if (e.getSource().equals(this.myRidesButton)) {
            SwingUtilities.invokeLater(() -> new AllRideDialog(this, this.userId));
        } else if (e.getSource().equals(this.allRidesButton)) {
            SwingUtilities.invokeLater(() -> new AllRideDialog(this, 0));
        } else if (e.getSource().equals(this.allUsersButton)) {
            SwingUtilities.invokeLater(() -> new AllUsersDialog(this));
        } else if (e.getSource().equals(this.allBikesButton)) {
            SwingUtilities.invokeLater(() -> new AllEBikesDialog(this, new HashMap<>()));
        }
    }

    /**
     * Update EBikes when a message from the EventBus is received.
     *
     * @param eBikeId the e bike id that has been modified
     * @param x       the x coordinate
     * @param y       the y coordinate
     * @param battery the battery level
     * @param status  the status
     */
    public void updateEBikeFromEventbus(int eBikeId, int x, int y, int battery, String status) {
        this.bikes.put(eBikeId, new Triple<>(new Pair<>(x, y), battery, status));
        this.refreshView();
    }

    /**
     * Update User when a message from the EventBus is received.
     *
     * @param credit the new actual credit for the user
     */
    public void updateUserFromEventbus(int credit) {
        this.user = new Triple<>(this.user.first(), credit, this.user.third());
        this.refreshView();
    }

    /**
     * Update the current user from the server and refresh the view.
     */
    public void updateUser() {
        this.webClient.requestReadUser(0,this.user.first()).onComplete(res -> {
            if (res.result() != null) {
                var result = res.result();
                if (result.size() == SINGLE_RESULT) {
                    for (Integer key : result.keySet()) {
                        this.user = result.get(key);
                    }
                    this.refreshView();
                }
            }
        });
    }

    private void refreshView() {
        centralPanel.refresh();
    }

    /**
     * Start a new ride.
     *
     * @param userId the user id
     * @param bikeId the bike id
     */
    public void startNewRide(int userId, int bikeId) {
        var bikeLocation = this.bikes.get(bikeId);
        var rideSimulation = new RideSimulation(bikeId, bikeLocation.first().first(), bikeLocation.first().second(),this);
        var ridingWindow = new RideSimulationControlPanel(userId, bikeId, this);
        ridingWindow.initialize();
        ridingWindow.display();
        rideSimulation.start();
        this.rides.put(new Pair<>(userId, bikeId), rideSimulation);
    }

    /**
     * End a ride.
     *
     * @param userId the user id
     * @param bikeId the bike id
     */
    public void endRide(int userId, int bikeId) {
        var key = new Pair<>(userId, bikeId);
        var r = rides.get(key);
        r.stopSimulation();
        this.rides.remove(key);
    }

    /**
     * Request an update for a user.
     *
     * @param userId the user id
     * @param credit the new total credit for that user
     * @return a Future of Boolean that will contain true if the update was successful
     */
    public Future<Boolean> requestUpdateUser(int userId, int credit){
        return webClient.requestUpdateUser(userId, credit);
    }

    /**
     * Request the deletion of a user.
     *
     * @param userId the user id to be deleted
     * @return a Future of Boolean that will contain
     *      true if the deletion was successful
     */
    public Future<Boolean> requestDeleteUser(int userId){
        return webClient.requestDeleteUser(userId);
    }

    /**
     * Request a read for users.
     *
     * @param userId   the user id
     * @param username the username
     * @return a Future of a Map containing representations of a user
     *      UserID -> Username, Credit, IsAdmin
     */
    public Future<Map<Integer, Triple<String, Integer, Boolean>>> requestReadUser(int userId, String username){
        return webClient.requestReadUser(userId, username);
    }

    /**
     * Request an update for a bike.
     *
     * @param ebikeId the ebike id
     * @param battery the battery
     * @param state   the state
     * @param x       the x coordinate
     * @param y       the y coordinate
     * @return a Future of Boolean that will contain true if the update was successful
     */
    public Future<Boolean> requestUpdateEBike(int ebikeId, int battery, String state, int x, int y){
        return webClient.requestUpdateEBike(ebikeId,battery,state,x,y);
    }

    /**
     * Request the deletion of a bike.
     *
     * @param ebikeId the ebike id to be deleted
     * @return a Future of Boolean that will contain
     *      true if the deletion was successful
     */
    public Future<Boolean> requestDeleteEBike(int ebikeId){
        return webClient.requestDeleteEBike(ebikeId);
    }

    /**
     * Request a creation of a bike.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return a Future of Boolean that will contain
     *      true if the creation was successful
     */
    public Future<Boolean> requestCreateEBike(int x, int y){
        return webClient.requestCreateEBike(x, y);
    }

    /**
     * Request multiple bikes.
     *
     * @param ebikeId   the e bike id
     * @param x         the x coordinate for nearby matching
     * @param y         the y coordinate for nearby matching
     * @param available if true, return only the available bikes
     * @return a Future of a Map containing representations of a bike
     *      BikeID -> (X coord, Y coord), battery level, Status
     */
    public Future<Map<Integer, Triple<Pair<Integer, Integer>, Integer, String>>> requestReadEBike(int ebikeId, int x, int y, boolean available){
        return webClient.requestReadEBike(ebikeId, x, y, available);
    }

    /**
     * Request multiple rides.
     *
     * @param userId  the user id
     * @param eBikeId the e bike id
     * @param ongoing if true, returns only the ongoing rides
     * @return a Future of a Map that represent a Ride:
     *      RideId -> (X, Y), (UserId, EBikeId)
     */
    public Future<Map<Integer,Pair<Pair<Integer, Integer>,Pair<String, String>>>> requestMultipleReadRide(int userId, int eBikeId, boolean ongoing){
        return webClient.requestMultipleReadRide(userId, eBikeId, ongoing);
    }

    /**
     * Request the deletion of a ride.
     *
     * @param rideId the ride id
     * @return a Future of Boolean that will contain
     *      true if the deletion was successful
     */
    public Future<Boolean> requestDeleteRide(int rideId){
        return webClient.requestDeleteRide(rideId);
    }

    /**
     * Request the start of a ride.
     *
     * @param eBikeId the e bike id
     * @return a Future of Boolean that will contain true if the
     *      ride was started successfully
     */
    public Future<Boolean> requestStartRide(int eBikeId){
        return webClient.requestStartRide(this.userId, eBikeId);
    }

    /**
     * Request the end of a ride.
     *
     * @param eBikeId the e bike id
     * @return a Future of Boolean that will contain true if the
     *      ride was ended successfully
     */
    public Future<Boolean> requestEndRide(int eBikeId){
        return webClient.requestEndRide(this.userId, eBikeId);
    }

    /**
     * Request an update for a ride
     *
     * @param eBikeId the e bike id of the bike used in that ride
     * @param x       the x coordinate
     * @param y       the y coordinate
     * @return a Future of a Pair that will contain the new values for
     *      user's credit and bike's battery level
     */
    public Future<Pair<Integer, Integer>> requestUpdateRide(int eBikeId, int x, int y){
        return webClient.requestUpdateRide(this.userId, eBikeId, x, y);
    }

    /**
     * Get the bikes shown in the Visualizer Panel.
     *
     * @return a Map containing a representation of bikes
     *      BikeID -> (X coord, Y coord), battery level, Status
     */
    public Map<Integer, Triple<Pair<Integer, Integer>, Integer, String>> getEBikes(){
        return this.bikes;
    }

    /**
     * Get the current user info.
     *
     * @return a Triple containing the username, the
     *      credit and if it is an admin
     */
    public Triple<String, Integer, Boolean> getUser(){
        return this.user;
    }

    /**
     * Get the user id.
     *
     * @return the userID of the current user
     */
    public int getUserId(){
        return this.userId;
    }



}
