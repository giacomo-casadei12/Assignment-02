package sap.ass02.rideservice.domain.ports.dataAccessPorts;

import sap.ass02.rideservice.domain.entities.User;

/**
 * The interface for accessing the database for users.
 */
public interface UserDA {

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
     * Update the credit for a single user
     *
     * @param id     the id of the user to be updated
     * @param credit the credit
     * @return true if the user was successfully updated
     */
    boolean updateUser(int id, int credit);

}
