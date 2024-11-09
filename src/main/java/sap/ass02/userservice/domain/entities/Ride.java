package sap.ass02.userservice.domain.entities;

/**
 * The Ride representation
 */
public interface Ride {
    /**
     * Sets the id.
     *
     * @param id the id
     */
    void setId(int id);

    /**
     * get the ride id.
     *
     * @return an int containing the id
     */
    int id();

    /**
     * get the Start date string.
     *
     * @return the string containing the start date
     */
    String startDate();

    /**
     * get the End date string.
     *
     * @return the string containing the end date
     */
    String endDate();

    /**
     * get the User id.
     *
     * @return an int containing the User id
     */
    int userID();

    /**
     * get the bike id.
     *
     * @return an int containing the Bike id
     */
    int eBikeID();
}
