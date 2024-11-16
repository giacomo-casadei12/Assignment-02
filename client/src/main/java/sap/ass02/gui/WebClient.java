package sap.ass02.gui;

import io.vertx.core.Future;
import sap.ass02.gui.GUI.EBikeApp;
import sap.ass02.gui.utils.Pair;
import sap.ass02.gui.utils.Triple;

import java.util.Map;

/**
 * The web Client that sends requests to the server
 * and processes its responses.
 */
public interface WebClient {
    /**
     * Request a user creation.
     *
     * @param username the username
     * @param password the password
     * @return a Future of a Boolean that will contain
     * true if the creation was successful
     */
    Future<Boolean> requestCreateUser(String username, String password);

    /**
     * Request a user deletion.
     *
     * @param userId the user id to be deleted
     * @return a Future of a Boolean that will contain
     * true if the deletion was successful
     */
    Future<Boolean> requestDeleteUser(int userId);

    /**
     * Request a user update.
     *
     * @param userId the user id to be updated
     * @param credit the new credit
     * @return a Future of a Boolean that will contain
     * true if the update was successful
     */
    Future<Boolean> requestUpdateUser(int userId, int credit);

    /**
     * Request a user role update.
     *
     * @param userId the user id to be updated
     * @param admin True for promoting the user to admin, False for demoting to user
     * @return a Future of a Boolean that will contain
     * true if the update was successful
     */
    Future<Boolean> requestUpdateUserRole(int userId, boolean admin);

    /**
     * Request a login.
     *
     * @param username the username
     * @param password the password
     * @return a Future of a Boolean that will contain
     * true if the login was successful
     */
    Future<Boolean> requestLogin(String username, String password);

    /**
     * Request a read for the users.
     *
     * @param userId   the user id
     * @param username the username
     * @return a Future of a Map containing representations of a user
     *      UserID -> Username, Credit, IsAdmin
     */
    Future<Map<Integer, Triple<String, Integer, Boolean>>> requestReadUser(int userId, String username);

    /**
     * Request an eBike creation.
     *
     * @param x the starting x coordinate
     * @param y the starting y coordinate
     * @return a Future of a Boolean that will contain
     * true if the creation was successful
     */
    Future<Boolean> requestCreateEBike(int x, int y);

    /**
     * Request an eBike deletion.
     *
     * @param eBikeId the e bike id to be deleted
     * @return a Future of a Boolean that will contain
     * true if the deletion was successful
     */
    Future<Boolean> requestDeleteEBike(int eBikeId);

    /**
     * Request an eBike update.
     *
     * @param eBikeId the e bike id to be updated
     * @param battery the new battery level
     * @param state   the new state
     * @param x       the new x coordinate
     * @param y       the new y coordinate
     * @return a Future of a Boolean that will contain
     * true if the update was successful
     */
    Future<Boolean> requestUpdateEBike(int eBikeId, int battery, String state, int x, int y);

    /**
     * Request a read for the bikes.
     *
     * @param eBikeId   the e bike id
     * @param x         the x coordinate for nearby matching
     * @param y         the y coordinate for nearby matching
     * @param available if true, return only the available bikes
     * @return a Future of a Map containing representations of a bike
     *      BikeID -> (X coord, Y coord), battery level, Status
     */
    Future<Map<Integer, Triple<Pair<Integer, Integer>, Integer, String>>> requestReadEBike(int eBikeId, int x, int y, boolean available);

    /**
     * Request the start of a ride.
     *
     * @param userId  the user id
     * @param eBikeId the e bike id
     * @return a Future of a Boolean that will contain
     * true if the creation was successful
     */
    Future<Boolean> requestStartRide(int userId, int eBikeId);

    /**
     * Request an update to a ride.
     *
     * @param userId  the user id
     * @param eBikeId the e bike id
     * @param x       the new x coordinate
     * @param y       the new y coordinate
     * @return a Future of a Pair of Integers containing
     * the credit left for the user and the battery level left for the bike
     */
    Future<Pair<Integer, Integer>> requestUpdateRide(int userId, int eBikeId, int x, int y);

    /**
     * Request end ride future.
     *
     * @param userId  the user id
     * @param eBikeId the e bike id
     * @return the future
     */
    Future<Boolean> requestEndRide(int userId, int eBikeId);

    /**
     * Request a ride deletion.
     *
     * @param rideId the ride id to be deleted
     * @return a Future of a Boolean that will contain
     * true if the deletion was successful
     */
    Future<Boolean> requestDeleteRide(int rideId);

    /**
     * Request a red of multiple rides.
     *
     * @param userId  the user id
     * @param eBikeId the e bike id
     * @param ongoing if true, returns only the ongoing rides
     * @return a Future of a Map that represent a Ride:
     *      RideId -> (X, Y), (UserId, EBikeId)
     */
    Future<Map<Integer, Pair<Pair<Integer, Integer>, Pair<String, String>>>> requestMultipleReadRide(int userId, int eBikeId, boolean ongoing);

    /**
     * Subscribe to the EventBus that propagate
     * each update done on bikes
     *
     * @param app the EBikeApp
     */
    void startMonitoringEBike(EBikeApp app);

    /**
     * Subscribe to the EventBus that propagate
     * each update done on users
     *
     * @param app the EBikeApp
     */
    void startMonitoringUsers(EBikeApp app);
}
