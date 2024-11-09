package sap.ass02.userservice.domain.ports.dataAccessPorts;

import sap.ass02.userservice.domain.entities.User;

import java.util.List;

/**
 * The interface for accessing the database for users.
 */
public interface UserDA {
    /**
     * Gets all users.
     *
     * @return a List of MutableUser containing all the users
     */
    List<User> getAllUsers();

    /**
     * Gets user by its name.
     *
     * @param userName the userName of the user
     * @return a MutableUser containing th user requested
     */
    User getUserByName(String userName);

    /**
     * Gets user by its id.
     *
     * @param id the id of the user
     * @return a MutableUser containing th user requested
     */
    User getUserById(int id);

    /**
     * Login boolean.
     *
     * @param userName the username
     * @param password the password
     * @return the boolean
     */
    boolean login(String userName, String password);

    /**
     * Create a new user.
     *
     * @param userName the userName for the new user (must be unique in the db)
     * @param password the password for the new user
     * @return true if the user was successfully created
     */
    boolean createUser(String userName, String password);

    /**
     * Update the credit for a single user
     *
     * @param id     the id of the user to be updated
     * @param credit the credit
     * @return true if the user was successfully updated
     */
    boolean updateUser(int id, int credit);

    /**
     * Delete a single user.
     *
     * @param id the id of the user to be deleted
     * @return true if the user was successfully deleted
     */
    boolean deleteUser(int id);
}
