package sap.ass02.userservice.domain.entities;

/**
 * The user representation
 */
public interface User {
    /**
     * Sets id.
     *
     * @param id the id
     */
    void setId(int id);

    /**
     * Sets the username.
     *
     * @param username the username
     */
    void setName(String username);

    /**
     * Set the credit.
     *
     * @param credit the credit
     */
    void setCredit(int credit);

    /**
     * Sets if the user is an admin.
     *
     * @param isAdmin true if the user is an admin
     */
    void setIsAdmin(boolean isAdmin);

    /**
     * get the id of the user.
     *
     * @return an int containing the id
     */
    int id();

    /**
     * get the username of the user.
     *
     * @return a String containing the username
     */
    String userName();

    /**
     * get the credit left for the user.
     *
     * @return an int containing the credit left
     */
    int credit();

    /**
     * get if the user is an admin.
     *
     * @return true if the user is an admin
     */
    boolean admin();
}
