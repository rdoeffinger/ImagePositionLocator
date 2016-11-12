package de.hu_berlin.informatik.spws2014.ImagePositionLocator;

import java.util.List;

public interface ImagePositionLocator {

    /**
     * Computes the image point to currentPosition
     * @param currentPosition
     * @return new image point
     */
    public Point2D getPointPosition(GpsPoint currentPosition);

    /**
     * Reverse operation of getPointPosition.
     *
     * Note that this does not necessarily imply that
     * getPointPosition(getGpsPosition(a)) == a or
     * getGpsPosition(getPointPosition(a)) == a.
     */
    public GpsPoint getGpsPosition(Point2D imagePosition);

    /**
     * Add new list of markers to algorithm knowledge
     */
    public void newMarkerAdded(List<Marker> markers);

}
