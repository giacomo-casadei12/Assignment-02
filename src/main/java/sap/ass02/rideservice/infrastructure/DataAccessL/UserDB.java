package sap.ass02.rideservice.infrastructure.DataAccessL;

import com.mysql.cj.jdbc.MysqlDataSource;
import sap.ass02.rideservice.domain.entities.User;
import sap.ass02.rideservice.domain.entities.UserImpl;
import sap.ass02.rideservice.domain.ports.dataAccessPorts.UserDA;

import java.sql.*;

/**
 * The implementation of the UserDA interface.
 */
public class UserDB implements UserDA {
    private static final String USER_NAME = "UserName";
    private static final String CREDIT = "Credit";
    private static final String IS_ADMIN = "IsAdmin";
    private static final String PROBLEM_IN_THE_QUERY = "Problem in the query";
    private final MysqlDataSource ds;

    /**
     * Instantiates a new UserDB.
     */
    public UserDB() {
        ds = new MysqlDataSource();
        ds.setUser("root");
        ds.setPassword("d3fR3@dy!");
        ds.setURL("jdbc:mysql://localhost:3307/ebcesena");
    }

    @Override
    public User getUserByName(String userName) {
        User user = null;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            stmt.setString(1, userName);
            ResultSet rs = stmt.executeQuery();
            user = getMutableUser(user, stmt, rs);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return user;
    }

    @Override
    public User getUserById(int id) {
        User user = null;
        try (Connection connection = ds.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE ID = " + id);
            user = getMutableUser(user, stmt, rs);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return user;
    }

    @Override
    public boolean updateUser(int id, int credit) {
        int rs;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET credit = ? WHERE ID = ?");
            stmt.setInt(2, id);
            stmt.setInt(1, credit);
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return rs > 0;
    }

    private User getMutableUser(User user, Statement stmt, ResultSet rs) throws SQLException {
        if (rs.next()) {
            user = new UserImpl();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString(USER_NAME));
            user.setCredit(rs.getInt(CREDIT));
            user.setIsAdmin(rs.getBoolean(IS_ADMIN));
        }
        rs.close();
        stmt.close();
        return user;
    }

}
