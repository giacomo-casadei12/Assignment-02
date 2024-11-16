package sap.ass02.userservice.domain.ports;

import sap.ass02.userservice.domain.entities.User;

import java.util.List;

/**
 * The interface exposing method for retrieve
 * info from the Data Access Layer regarding the users.
 */
public interface UserPersistence {

    /**
     * Gets all users.
     *
     * @return a List of User containing all the users
     */
    List<User> getAllUsers();

    /**
     * Gets a user given its id or username.
     *
     * @param id       the id of the user
     * @param userName the username of the user
     * @return a User containing the requested user
     */
    User getUser(int id, String userName);

    /**
     * A user request the Login.
     *
     * @param userName the username provided by the user
     * @param password the password provided by the user
     * @return true if the credentials provided by the user were correct
     */
    boolean login(String userName, String password);

    /**
     * Create a new user.
     *
     * @param userName the username of the user
     * @param password the password of the user
     * @return true if the user was created successfully
     */
    boolean createUser(String userName, String password);

    /**
     * Update the current credit for a user.
     *
     * @param id     the id of the user to be updated
     * @param credit the int containing the new value for credit left for the user
     * @return true if the user was updated successfully
     */
    boolean updateUser(int id, int credit);

    boolean updateUserRole(int id, boolean admin);

    /**
     * Delete a user.
     *
     * @param id the id of the user to be deleted
     * @return true if the user was deleted successfully
     */
    boolean deleteUser(int id);

}
