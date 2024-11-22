package sap.ass02.vertxrideservice.domain.entities;

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
     * Sets the start date.
     *
     * @param startDate a String containing the start date
     */
    void setStartDate(String startDate);

    /**
     * Sets the end date.
     *
     * @param endDate a String containing the end date
     */
    void setEndDate(String endDate);

    /**
     * Sets the user id.
     *
     * @param userID the user id
     */
    void setUserID(int userID);

    /**
     * Sets the bike id.
     *
     * @param bikeID the bike id
     */
    void setEBikeID(int bikeID);

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
