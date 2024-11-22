package sap.ass02.vertxrideservice.domain.entities;

import java.util.Objects;

/**
 * The implementation of the MutableEBike interface.
 */
public class EBikeImpl implements EBike {

    private int id;
    private int battery;
    private String state;
    private int positionX;
    private int positionY;

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void setBattery(int battery) {
        this.battery = battery;
    }

    @Override
    public void setState(String state) {
        this.state = state;
    }

    @Override
    public void setPositionX(int x) {
        this.positionX = x;
    }

    @Override
    public void setPositionY(int y) {
        this.positionY = y;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public int battery() {
        return this.battery;
    }

    @Override
    public String state() {
        return this.state;
    }

    @Override
    public int positionX() {
        return this.positionX;
    }

    @Override
    public int positionY() {
        return this.positionY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EBikeImpl that = (EBikeImpl) o;
        return id == that.id && battery == that.battery && positionX == that.positionX && positionY == that.positionY && Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, battery, state, positionX, positionY);
    }
}
