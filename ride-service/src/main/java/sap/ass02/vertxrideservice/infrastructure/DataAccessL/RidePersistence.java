package sap.ass02.vertxrideservice.infrastructure.DataAccessL;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sap.ass02.rideservice.utils.JsonFieldsConstants;
import sap.ass02.vertxrideservice.utils.VertxSingleton;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import static sap.ass02.vertxrideservice.utils.JsonFieldsConstants.*;

/**
 * The implementation of the RideDA interface.
 */
public class RidePersistence extends AbstractVerticle {

    private static final Logger LOGGER = Logger.getLogger("[EBikeCesena RideService Persistence]");

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";
    private static final String USER_ID = "userID";
    private static final String E_BIKE_ID = "eBikeID";
    private static final String PROBLEM_IN_THE_QUERY = "Problem in the query";
    private final MysqlDataSource ds;
    private final SimpleDateFormat format;
    private EventBus eventBus;

    /**
     * Instantiates a new RideDB.
     */
    public RidePersistence() {
        ds = new MysqlDataSource();
        ds.setUser("root");
        ds.setPassword("d3fR3@dy!");
        ds.setURL("jdbc:mysql://localhost:3307/ebcesena2rides");
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
    }

    @Override
    public void start() {
        LOGGER.log(Level.INFO, "Persistence initializing...");

        this.eventBus = VertxSingleton.getInstance().getVertx().eventBus();
        eventBus.consumer("RidePersistenceGetAllRides", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.getAllRides(Integer.parseInt(json.getString("RequestId")));
        });

        eventBus.consumer("RidePersistenceGetOngoingRides", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.getAllOngoingRides(Integer.parseInt(json.getString("RequestId")));
        });

        eventBus.consumer("RidePersistenceGetAllRidesByUser", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.getAllRidesByUser(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(JsonFieldsConstants.USER_ID)));
        });

        eventBus.consumer("RidePersistenceGetAllRidesByBike", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.getAllRidesByEBike(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(JsonFieldsConstants.E_BIKE_ID)));
        });

        eventBus.consumer("RidePersistenceGetRideByRideId", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.getRideById(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(JsonFieldsConstants.RIDE_ID)));
        });

        eventBus.consumer("RidePersistenceGetRideByUser", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.getOngoingRideByUserId(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(JsonFieldsConstants.USER_ID)));
        });

        eventBus.consumer("RidePersistenceEndRide", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());
            if (Integer.parseInt(json.getString("RequestId")) == 0) {
                this.endRideByUserId(Integer.parseInt(json.getString(JsonFieldsConstants.USER_ID)));
            } else {
                this.endRide(Integer.parseInt(json.getString("RequestId")),
                        Integer.parseInt(json.getString(JsonFieldsConstants.RIDE_ID)));
            }
        });

        eventBus.consumer("RidePersistenceDeleteRide", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.deleteRide(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(JsonFieldsConstants.RIDE_ID)));
        });

        eventBus.consumer("RidePersistenceCreateRide", msg -> {
            JsonObject json = new JsonObject(msg.body().toString());

            this.createRide(Integer.parseInt(json.getString("RequestId")),
                    Integer.parseInt(json.getString(JsonFieldsConstants.USER_ID)), Integer.parseInt(json.getString(JsonFieldsConstants.E_BIKE_ID)));
        });

        LOGGER.log(Level.INFO, "Persistence ready to process messages");
    }

    private void getAllRides(int requestId) {
        JsonObject jsonRes = new JsonObject();
        jsonRes.put("RequestId", requestId);
        try (Connection connection = ds.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM rides");
            fillJsonAndCloseSQL(jsonRes, stmt, rs);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        eventBus.publish("RideWebControllerSendResponse", jsonRes.toString());
    }

    private void getAllOngoingRides(int requestId) {
        JsonObject jsonRes = new JsonObject();
        jsonRes.put("RequestId", requestId);
        try (Connection connection = ds.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM rides WHERE EndDate IS NULL");
            fillJsonAndCloseSQL(jsonRes, stmt, rs);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        eventBus.publish("RideWebControllerSendResponse", jsonRes.toString());
    }

    private void getAllRidesByUser(int requestId, int userId) {
        JsonObject jsonRes = new JsonObject();
        jsonRes.put("RequestId", requestId);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM rides WHERE UserID = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            fillJsonAndCloseSQL(jsonRes, stmt, rs);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        eventBus.publish("RideWebControllerSendResponse", jsonRes.toString());
    }

    private void getAllRidesByEBike(int requestId, int eBikeId) {
        JsonObject jsonRes = new JsonObject();
        jsonRes.put("RequestId", requestId);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM rides WHERE EBikeID = ?");
            stmt.setInt(1, eBikeId);
            ResultSet rs = stmt.executeQuery();
            fillJsonAndCloseSQL(jsonRes, stmt, rs);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        eventBus.publish("RideWebControllerSendResponse", jsonRes.toString());
    }

    private void getRideById(int requestId, int id) {
        JsonObject jsonRes = new JsonObject();
        jsonRes.put("RequestId", requestId);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM rides WHERE ID = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            getSingleRideAndCloseSQL(rs, jsonRes, stmt);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        eventBus.publish("RideWebControllerSendResponse", jsonRes.toString());
    }

    private void getOngoingRideByUserId(int requestId, int userId) {
        JsonObject jsonRes = new JsonObject();
        jsonRes.put("RequestId", requestId);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM rides WHERE UserID = ? AND EndDate IS NULL");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            getSingleRideAndCloseSQL(rs, jsonRes, stmt);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        eventBus.publish("RideWebControllerSendResponse", jsonRes.toString());
    }

    private void createRide(int requestId, int userId, int eBikeId) {
        int rs;
        int lastID = getLastID();
        JsonObject jsonRes = new JsonObject();
        jsonRes.put("RequestId", requestId);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO rides VALUES(?,?,null,?,?)");
            stmt.setInt(1, lastID+1);
            stmt.setString(2, format.format(new Date(System.currentTimeMillis())));
            stmt.setInt(3, userId);
            stmt.setInt(4, eBikeId);
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        if (rs > 0) {
            jsonRes.put(RESULT, "ok");
        } else {
            jsonRes.put(RESULT, "error");
        }
        eventBus.publish("RideWebControllerSendResponse", jsonRes.toString());
    }

    private void endRide(int requestId, int id) {
        int rs;
        JsonObject jsonRes = new JsonObject();
        jsonRes.put("RequestId", requestId);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("UPDATE rides SET EndDate = ? WHERE ID = ?");
            stmt.setString(1, format.format(new Date(System.currentTimeMillis())));
            stmt.setInt(2, id);
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        if (rs > 0) {
            jsonRes.put(RESULT, "ok");
        } else {
            jsonRes.put(RESULT, "error");
        }
        eventBus.publish("RideWebControllerSendResponse", jsonRes.toString());
    }

    private void endRideByUserId(int userId) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("UPDATE rides SET EndDate = ? WHERE UserID = ? AND EndDate IS NULL");
            stmt.setString(1, format.format(new Date(System.currentTimeMillis())));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
    }

    private void deleteRide(int requestId, int id) {
        int rs;
        JsonObject jsonRes = new JsonObject();
        jsonRes.put("RequestId", requestId);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM rides WHERE ID = ?");
            stmt.setInt(1, id);
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        if (rs > 0) {
            jsonRes.put(RESULT, "ok");
        } else {
            jsonRes.put(RESULT, "error");
        }
        eventBus.publish("RideWebControllerSendResponse", jsonRes.toString());
    }

    private void getSingleRideAndCloseSQL(ResultSet rs, JsonObject json, PreparedStatement stmt) throws SQLException {
        if (rs.next()) {
            buildRideJson(rs, json);
        }
        rs.close();
        stmt.close();
    }

    private void fillJsonAndCloseSQL(JsonObject jsonRes, Statement stmt, ResultSet rs) throws SQLException {
        JsonArray resArray = new JsonArray();
        while (rs.next()) {
            JsonObject jsonSubRes = new JsonObject();
            buildRideJson(rs, jsonSubRes);
            resArray.add(jsonSubRes);
        }
        jsonRes.put(RESULT, resArray);
        rs.close();
        stmt.close();
    }

    private void buildRideJson(ResultSet rs, JsonObject jsonSubRes) throws SQLException {
        jsonSubRes.put(JsonFieldsConstants.RIDE_ID, rs.getInt("id"));
        jsonSubRes.put(JsonFieldsConstants.USER_ID, rs.getInt(USER_ID));
        jsonSubRes.put(JsonFieldsConstants.E_BIKE_ID, rs.getInt(E_BIKE_ID));
        jsonSubRes.put(START_DATE, rs.getString(START_DATE));
        jsonSubRes.put(END_DATE, rs.getString(END_DATE));
    }

    private int getLastID() {
        ResultSet rs;
        int lastID = 0;
        try (Connection connection = ds.getConnection()) {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM rides ORDER BY ID DESC LIMIT 1");
            if(rs.next()){
                lastID = rs.getInt("id");
            }
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }

        return lastID;
    }
}
