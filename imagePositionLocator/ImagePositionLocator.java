package de.hu_berlin.informatik.spws2014.imagePositionLocator;

import java.util.List;

public interface ImagePositionLocator {
	
	/**
	 * Computes the image point to currentPosition
	 * @param currentPosition
	 * @return new image point
	 */
	public Point2D getPointPosition(GpsPoint currentPosition);

	/**
	 * Add new list of markers to algorithm knowledge 
	 */
	public void newMarkerAdded(List<Marker> markers);
	
}
