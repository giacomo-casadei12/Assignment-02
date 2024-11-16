package sap.ass02.gui.GUI.simulation;

/**
 * 2-dimensional point
 * objects are completely state-less
 */
public record P2d(double x, double y) implements java.io.Serializable {

    /**
     * Sum p 2 d.
     *
     * @param v the v
     * @return the p 2 d
     */
    public P2d sum(V2d v) {
        return new P2d(x + v.x(), y + v.y());
    }

    @Override
    public String toString() {
        return "P2d(" + x + "," + y + ")";
    }

}
