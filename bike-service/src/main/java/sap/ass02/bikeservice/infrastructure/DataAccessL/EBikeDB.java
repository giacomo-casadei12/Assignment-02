package sap.ass02.bikeservice.infrastructure.DataAccessL;

import com.mysql.cj.jdbc.MysqlDataSource;
import sap.ass02.bikeservice.domain.entities.EBike;
import sap.ass02.bikeservice.domain.entities.EBikeImpl;
import sap.ass02.bikeservice.domain.ports.dataAccessPorts.EBikeDA;
import sap.ass02.bikeservice.utils.EBikeState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    static final private int NEARBY_RANGE = 10;

    /**
     * Instantiates a new EBikeDB.
     */
    public EBikeDB() {
        ds = new MysqlDataSource();
        ds.setUser("root");
        ds.setPassword("d3fR3@dy!");
        ds.setURL("jdbc:mysql://host.docker.internal:3307/ebcesena2bikes");
    }

    @Override
    public List<EBike> getAllEBikes() {
        List<EBike> bikes = new ArrayList<>();
        try (Connection connection = ds.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM bikes");
            fillBikeListAndCloseSQL(bikes, stmt, rs);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return bikes;
    }

    @Override
    public List<EBike> getAllAvailableEBikes() {
        List<EBike> bikes = new ArrayList<>();
        try (Connection connection = ds.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM bikes WHERE State = 0");
            fillBikeListAndCloseSQL(bikes, stmt, rs);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return bikes;
    }

    @Override
    public List<EBike> getAllEBikesNearby(int positionX, int positionY ) {
        List<EBike> bikes = new ArrayList<>();
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM bikes WHERE PositionX BETWEEN ? AND ? AND PositionY BETWEEN ? AND ?");
            stmt.setInt(1, positionX - NEARBY_RANGE);
            stmt.setInt(2, positionX + NEARBY_RANGE);
            stmt.setInt(3, positionY - NEARBY_RANGE);
            stmt.setInt(4, positionY + NEARBY_RANGE);
            ResultSet rs = stmt.executeQuery();
            fillBikeListAndCloseSQL(bikes, stmt, rs);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return bikes;
    }

    @Override
    public EBike getEBikeById(int id) {
        EBike bike = null;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM bikes WHERE ID = ?");
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
    public boolean createEBike(int positionX, int positionY) {
        int rs;
        int lastID = getLastID();
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO bikes VALUES(?,100,0,?,?)");
            stmt.setInt(1, lastID+1);
            stmt.setInt(2, positionX);
            stmt.setInt(3, positionY);
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return rs > 0;
    }

    @Override
    public boolean updateEBike(EBike bike) {
        int rs;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("UPDATE bikes SET Battery = ?, State = ?, PositionX = ?, PositionY = ? WHERE ID = ?");
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

    @Override
    public boolean deleteEBike(int id) {
        int rs;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM bikes WHERE ID = ?");
            stmt.setInt(1, id);
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return rs > 0;
    }

    private void fillBikeListAndCloseSQL(List<EBike> bikes, Statement stmt, ResultSet rs) throws SQLException {
        while (rs.next()) {
            EBike bike = new EBikeImpl();
            bike.setId(rs.getInt("id"));
            bike.setBattery(rs.getInt(BATTERY));
            bike.setState(EBikeState.values()[rs.getInt(STATE)].toString());
            bike.setPositionX(rs.getInt(POSITION_X));
            bike.setPositionY(rs.getInt(POSITION_Y));
            bikes.add(bike);
        }
        rs.close();
        stmt.close();
    }

    private int getLastID() {
        ResultSet rs;
        int lastID = 0;
        try (Connection connection = ds.getConnection()) {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM bikes ORDER BY ID DESC LIMIT 1");
            if(rs.next()){
                lastID = rs.getInt("id");
            }
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }

        return lastID;
    }
}
