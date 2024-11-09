package sap.ass02.rideservice.infrastructure.DataAccessL;

import com.mysql.cj.jdbc.MysqlDataSource;
import sap.ass02.rideservice.domain.entities.EBike;
import sap.ass02.rideservice.domain.entities.EBikeImpl;
import sap.ass02.rideservice.domain.ports.dataAccessPorts.EBikeDA;
import sap.ass02.rideservice.utils.EBikeState;

import java.sql.*;

/**
 * The implementation of the EBikeDA interface.
 */
public class EBikeDB implements EBikeDA {

    private static final String PROBLEM_IN_THE_QUERY = "Problem in the query";
    private static final String BATTERY = "Battery";
    private static final String STATE = "State";
    private static final String POSITION_X = "PositionX";
    private static final String POSITION_Y = "PositionY";
    private final MysqlDataSource ds;

    /**
     * Instantiates a new EBikeDB.
     */
    public EBikeDB() {
        ds = new MysqlDataSource();
        ds.setUser("root");
        ds.setPassword("d3fR3@dy!");
        ds.setURL("jdbc:mysql://localhost:3307/ebcesena");
    }

    @Override
    public EBike getEBikeById(int id) {
        EBike bike = null;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM ebike WHERE ID = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                bike = new EBikeImpl();
                bike.setId(rs.getInt("id"));
                bike.setBattery(rs.getInt(BATTERY));
                bike.setState(EBikeState.values()[rs.getInt(STATE)].toString());
                bike.setPositionX(rs.getInt(POSITION_X));
                bike.setPositionY(rs.getInt(POSITION_Y));
            }
            rs.close();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return bike;
    }

    @Override
    public boolean updateEBike(EBike bike) {
        int rs;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("UPDATE ebike SET Battery = ?, State = ?, PositionX = ?, PositionY = ? WHERE ID = ?");
            stmt.setInt(1, bike.battery());
            stmt.setInt(2, EBikeState.valueOf(bike.state()).ordinal());
            stmt.setInt(3, bike.positionX());
            stmt.setInt(4, bike.positionY());
            stmt.setInt(5, bike.id());
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return rs > 0;
    }

}
