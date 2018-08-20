/* Copyright (C) 2014,2015  Maximilian Diedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package de.hu_berlin.informatik.spws2014.ImagePositionLocator;

import java.io.Serializable;

/**
 * Representing a pixel position relative to an image origin.
 */
public class Point2D implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int x;
    public final int y;

    public Point2D (int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point2D(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public Point2D(FPoint2D a) {
        this.x = (int) a.x;
        this.y = (int) a.y;
    }

    public Point2D getOrthogonal(Point2D origin) {
        // Turn opposite to GPS as y axis goes down, not up
        double xnew = origin.x + (y - origin.y);
        double ynew = origin.y - (x - origin.x);
        return new Point2D(xnew, ynew);
    }

    public double getDistance(Point2D a) {
        double tmp = Math.sqrt(Math.pow(a.x - this.x, 2) + Math.pow(a.y - this.y, 2));
        if (tmp != 0) return tmp;
        else return Double.MIN_NORMAL;
    }

    public double getDistance(FPoint2D inp) {
        return this.getDistance(new Point2D(inp));
    }

    public boolean smallerThan(Point2D inp) {
        return (inp.x > this.x) || (inp.y > this.y);
    }

    public String toString() {
        return "P2D<" + x + "," + y + ">";
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == Point2D.class && this.equals((Point2D) o);
    }

    public boolean equals(Point2D p) {
        return p != null && this.x == p.x && this.y == p.y;
    }

    @Override
    public int hashCode() {
        return ((y & 65536) << 16) | (x & 65536);
    }
}
