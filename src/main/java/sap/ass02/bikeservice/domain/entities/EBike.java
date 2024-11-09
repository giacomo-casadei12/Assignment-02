package sap.ass02.bikeservice.domain.entities;

/**
 * The Electric Bike representation
 */
public interface EBike {

    /**
     * Id int.
     *
     * @return the id of the bike
     */
    int id();

    /**
     * Battery int.
     *
     * @return the battery percentage of the bike
     */
    int battery();

    /**
     * State string.
     *
     * @return the actual state of the bike
     */
    String state();

    /**
     * Position x int.
     *
     * @return the actual X coordinate of the bike
     */
    int positionX();

    /**
     * Position y int.
     *
     * @return the actual Y coordinate of the bike
     */
    int positionY();

    /**
     * Sets id.
     *
     * @param id the id
     */
    void setId(int id);

    /**
     * Sets the battery left for the bike.
     *
     * @param battery the battery left
     */
    void setBattery(int battery);

    /**
     * Sets the state of the bike.
     *
     * @param state the state
     */
    void setState(String state);

    /**
     * Sets the position in the x coordinate of the bike.
     *
     * @param x the x coordinate
     */
    void setPositionX(int x);

    /**
     * Sets the position in the y coordinate of the bike.
     *
     * @param y the y coordinate
     */
    void setPositionY(int y);
}
