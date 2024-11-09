package sap.ass02.rideservice.domain.ports;

/**
 * The interface exposing method for retrieve
 * info from the Data Access Layer regarding the users.
 */
public interface UserPersistence {

    /**
     * Update the current credit for a user.
     *
     * @param id     the id of the user to be updated
     * @param credit the int containing the new value for credit left for the user
     * @return true if the user was updated successfully
     */
    boolean updateUser(int id, int credit);


}
