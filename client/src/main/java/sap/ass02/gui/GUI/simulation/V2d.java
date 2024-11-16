/*
 *   V2d.java
 *
 * Copyright 2000-2001-2002  aliCE team at deis.unibo.it
 *
 * This software is the proprietary information of deis.unibo.it
 * Use is subject to license terms.
 *
 */
package sap.ass02.gui.GUI.simulation;

/**
 * 2-dimensional vector
 * objects are completely state-less
 */
public record V2d(double x, double y) implements java.io.Serializable {

    /**
     * Rotate v 2 d.
     *
     * @param degree the degree
     * @return the v 2 d
     */
    public V2d rotate(double degree) {
        var rad = degree * Math.PI / 180;
        var cs = Math.cos(rad);
        var sn = Math.sin(rad);
        var x1 = x * cs - y * sn;
        var y1 = x * sn + y * cs;
        return new V2d(x1, y1).getNormalized();
    }

    /**
     * Gets normalized.
     *
     * @return the normalized
     */
    public V2d getNormalized() {
        double module = Math.sqrt(x * x + y * y);
        return new V2d(x / module, y / module);
    }

    /**
     * Mul v 2 d.
     *
     * @param fact the fact
     * @return the v 2 d
     */
    public V2d mul(double fact) {
        return new V2d(x * fact, y * fact);
    }

    @Override
    public String toString() {
        return "V2d(" + x + "," + y + ")";
    }

}
