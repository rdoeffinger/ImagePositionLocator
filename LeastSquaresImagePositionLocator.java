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
		int n_markers = markers.size();
		double[][] A = new double[n_markers][3];
		double[] bx = new double[n_markers];
		double[] by = new double[n_markers];
		double[] dist = new double[n_markers];
		double mindist = Double.POSITIVE_INFINITY;
		for (int i = 0; i < n_markers; i++) {
			Marker m = markers.get(i);
			A[i][0] = m.realpoint.longitude - cur_lon;
			A[i][1] = m.realpoint.latitude - cur_lat;
			A[i][2] = 1;
			dist[i] = A[i][0] * A[i][0] + A[i][1] * A[i][1];
			mindist = Math.min(mindist, dist[i]);
			if (mindist == 0) {
				return new Point2D(m.imgpoint.x, m.imgpoint.y);
			}
			bx[i] = m.imgpoint.x;
			by[i] = m.imgpoint.y;
		}
		// Multiple A and b by transpose(A)*weigths
		// TODO: review weigths, they are supposed to be
		// inversely proportional to datapoint reliability.
		// 1/distance^2 is cheap but just a wild guess, and
		// GPS signal quality when marker was set could
		// be used as input in addition...
		double bx30 = 0, bx31 = 0, bx32 = 0;
		double by30 = 0, by31 = 0, by32 = 0;
		double AtWA00 = 0, AtWA01 = 0, AtWA02 = 0, AtWA11 = 0, AtWA12 = 0, AtWA22 = 0;
		for (int i = 0; i < n_markers; i++) {
			double weight = mindist / dist[i];
			double a0 = A[i][0], a1 = A[i][1], a2 = A[i][2];
			double wa0 = a0 * weight, wa1 = a1 * weight, wa2 = a2 * weight;
			bx30 += bx[i] * wa0;
			bx31 += bx[i] * wa1;
			bx32 += bx[i] * wa2;
			by30 += by[i] * wa0;
			by31 += by[i] * wa1;
			by32 += by[i] * wa2;
			AtWA00 += a0 * wa0;
			AtWA01 += a0 * wa1;
			AtWA02 += a0 * wa2;
			AtWA11 += a1 * wa1;
			AtWA12 += a1 * wa2;
			AtWA22 += a2 * wa2;
			// Others not calculated as matrix is symmetric
		}
		// TODO: if det == 0 create extra point like for 2 markers case
		// Also warn if near 0 and thus unstable
		// Make use of the fact that matrix is symmetric
		double detAtWA = AtWA00 * AtWA11 * AtWA22 +
			2 * AtWA01 * AtWA12 * AtWA02 -
			AtWA02 * AtWA11 * AtWA02 -
			AtWA12 * AtWA12 * AtWA00 -
			AtWA22 * AtWA01 * AtWA01;
		// Inverse the matrix. Standard cross-product method.
		// We need only the last row though
		double inverse0 = AtWA01 * AtWA12 - AtWA11 * AtWA02;
		double inverse1 = AtWA02 * AtWA01 - AtWA12 * AtWA00;
		double inverse2 = AtWA00 * AtWA11 - AtWA01 * AtWA01;
		// Use inverse matrix to solve linear system
		// The last coefficient is the offset between the coordinate systems.
		// As we recentered GPS to our current position, the offset is our map position
		double posx = inverse0 * bx30 + inverse1 * bx31 + inverse2 * bx32;
		posx /= detAtWA;
		double posy = inverse0 * by30 + inverse1 * by31 + inverse2 * by32;
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
