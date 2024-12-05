package sap.ass02.userservice.infrastructure.DataAccessL;

import com.mysql.cj.jdbc.MysqlDataSource;
import sap.ass02.userservice.domain.entities.User;
import sap.ass02.userservice.domain.entities.UserImpl;
import sap.ass02.userservice.domain.ports.dataAccessPorts.UserDA;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the UserDA interface.
 */
public class UserDB implements UserDA {
    private static final String USER_NAME = "UserName";
    private static final String CREDIT = "Credit";
    private static final String IS_ADMIN = "Admin";
    private static final String PROBLEM_IN_THE_QUERY = "Problem in the query";
    private final MysqlDataSource ds;

    /**
     * Instantiates a new UserDB.
     */
    public UserDB() {
        ds = new MysqlDataSource();
        ds.setUser(System.getenv("DB_USER"));
        ds.setPassword(System.getenv("DB_PASSWORD"));
        ds.setURL(System.getenv("DB_URL"));
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection connection = ds.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                User user = new UserImpl();
                user.setId(rs.getInt("UserId"));
                user.setName(rs.getString(USER_NAME));
                user.setCredit(rs.getInt(CREDIT));
                user.setIsAdmin(rs.getBoolean(IS_ADMIN));
                users.add(user);
            }
            rs.close();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return users;
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
            ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE UserId = " + id);
            user = getMutableUser(user, stmt, rs);
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return user;
    }

    @Override
    public boolean login(String userName, String password) {
        User user = null;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, userName);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            user = getMutableUser(user, stmt, rs);
            rs.close();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return user != null;
    }

    @Override
    public boolean createUser(String userName, String password) {
        int rs;
        int lastID = getLastID();
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO users VALUES(?,?,?,0,0)");
            stmt.setInt(1, lastID+1);
            stmt.setString(2, userName);
            stmt.setString(3, password);
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return rs > 0;
    }

    @Override
    public boolean updateUser(int id, int credit) {
        int rs;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET credit = ? WHERE UserId = ?");
            stmt.setInt(2, id);
            stmt.setInt(1, credit);
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return rs > 0;
    }

    @Override
    public boolean updateUserRole(int id, boolean admin) {
        int rs;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET Admin = ? WHERE UserId = ?");
            stmt.setInt(2, id);
            stmt.setInt(1, admin ? 1 : 0);
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return rs > 0;
    }

    @Override
    public boolean deleteUser(int id) {
        int rs;
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM users WHERE UserId = ?");
            stmt.setInt(1, id);
            rs = stmt.executeUpdate();
            stmt.close();
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }
        return rs > 0;
    }

    private int getLastID() {
        ResultSet rs;
        int lastID = 0;
        try (Connection connection = ds.getConnection()) {
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM users ORDER BY UserId DESC LIMIT 1");
            if(rs.next()){
                lastID = rs.getInt("UserId");
            }
        } catch( SQLException e) {
            throw new IllegalStateException(PROBLEM_IN_THE_QUERY, e);
        }

        return lastID;
    }

    private User getMutableUser(User user, Statement stmt, ResultSet rs) throws SQLException {
        if (rs.next()) {
            user = new UserImpl();
            user.setId(rs.getInt("UserId"));
            user.setName(rs.getString(USER_NAME));
            user.setCredit(rs.getInt(CREDIT));
            user.setIsAdmin(rs.getBoolean(IS_ADMIN));
        }
        rs.close();
        stmt.close();
        return user;
    }

}
