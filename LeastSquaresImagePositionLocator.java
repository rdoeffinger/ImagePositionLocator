/* Copyright (C) 2015  Reimar Döffinger
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

import java.util.ArrayList;
import java.util.List;

public class LeastSquaresImagePositionLocator implements ImagePositionLocator {
	private List<Marker> markers;

	public LeastSquaresImagePositionLocator() {
	}

	public Point2D getPointPosition(GpsPoint currentPosition) {
		if (markers == null || markers.size() <= 2 || currentPosition == null)
			return null;
		// Recenter for better numerical stability
		double cur_lon = currentPosition.longitude;
		double cur_lat = currentPosition.latitude;
		// Build linear system to solve to get coordinate
		// transform - separately for x and y
		// Need a 3rd constant 1 input to represent translations
		// (compare: w in OpenGL).
		// While at it calculate the minimum distance as well.
		double[][] A = new double[markers.size()][3];
		double[] bx = new double[markers.size()];
		double[] by = new double[markers.size()];
		double[] dist = new double[markers.size()];
		double mindist = Double.POSITIVE_INFINITY;
		for (int i = 0; i < markers.size(); i++) {
			A[i][0] = markers.get(i).realpoint.longitude - cur_lon;
			A[i][1] = markers.get(i).realpoint.latitude - cur_lat;
			A[i][2] = 1;
			dist[i] = A[i][0] * A[i][0] + A[i][1] * A[i][1];
			mindist = Math.min(mindist, dist[i]);
			if (mindist == 0) {
				return new Point2D(markers.get(i).imgpoint.x, markers.get(i).imgpoint.y);
			}
			bx[i] = markers.get(i).imgpoint.x;
			by[i] = markers.get(i).imgpoint.y;
		}
		// Multiple A and b by transpose(A)*weigths
		// TODO: review weigths, they are supposed to be
		// inversely proportional to datapoint reliability.
		// 1/distance^2 is cheap but just a wild guess, and
		// GPS signal quality when marker was set could
		// be used as input in addition...
		double[] bx3 = new double[3];
		double[] by3 = new double[3];
		double[][] AtWA = new double[3][3];
		for (int i = 0; i < markers.size(); i++) {
			double weight = mindist / dist[i];
			bx3[0] += bx[i] * A[i][0] * weight;
			bx3[1] += bx[i] * A[i][1] * weight;
			bx3[2] += bx[i] * A[i][2] * weight;
			by3[0] += by[i] * A[i][0] * weight;
			by3[1] += by[i] * A[i][1] * weight;
			by3[2] += by[i] * A[i][2] * weight;
			AtWA[0][0] += A[i][0] * A[i][0] * weight;
			AtWA[0][1] += A[i][0] * A[i][1] * weight;
			AtWA[0][2] += A[i][0] * A[i][2] * weight;
			AtWA[1][1] += A[i][1] * A[i][1] * weight;
			AtWA[1][2] += A[i][1] * A[i][2] * weight;
			AtWA[2][2] += A[i][2] * A[i][2] * weight;
		}
		AtWA[1][0] = AtWA[0][1];
		AtWA[2][0] = AtWA[0][2];
		AtWA[2][1] = AtWA[1][2];
		// TODO: if det == 0 create extra point like for 2 markers case
		// Also warn if near 0 and thus unstable
		double detAtWA = AtWA[0][0] * AtWA[1][1] * AtWA[2][2] +
			AtWA[0][1] * AtWA[1][2] * AtWA[2][0] +
			AtWA[0][2] * AtWA[1][0] * AtWA[2][1] -
			AtWA[0][2] * AtWA[1][1] * AtWA[2][0] -
			AtWA[1][2] * AtWA[2][1] * AtWA[0][0] -
			AtWA[2][2] * AtWA[0][1] * AtWA[1][0];
		// Inverse the matrix. Standard cross-product method.
		// We need only the last row though
		double[] inverse = new double[3];
		inverse[0] = AtWA[1][0] * AtWA[2][1] - AtWA[1][1] * AtWA[2][0];
		inverse[1] = AtWA[2][0] * AtWA[0][1] - AtWA[2][1] * AtWA[0][0];
		inverse[2] = AtWA[0][0] * AtWA[1][1] - AtWA[0][1] * AtWA[1][0];
		// Use inverse matrix to solve linear system
		// The last coefficient is the offset between the coordinate systems.
		// As we recentered GPS to our current position, the offset is our map position
		double posx = inverse[0] * bx3[0] + inverse[1] * bx3[1] + inverse[2] * bx3[2];
		posx /= detAtWA;
		double posy = inverse[0] * by3[0] + inverse[1] * by3[1] + inverse[2] * by3[2];
		posy /= detAtWA;
		// Apply found coordinate transformation
		return new Point2D(posx, posy);
	}

	public void newMarkerAdded(List<Marker> markers) {
		// TODO: Run a trial run of the solver to check quality of points.
		// If bad, display a warning. Possibly try to find outliers
		// and suggest for correction
		this.markers = new ArrayList<Marker>(markers);
		if (this.markers.size() == 2) {
			// Note: this can only work if both map and
			// GPS have identical scales in X and Y direction.
			this.markers.add(this.markers.get(0).getOrthogonal(this.markers.get(1)));
		}
	}
}
