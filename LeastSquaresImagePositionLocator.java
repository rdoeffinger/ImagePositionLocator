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

import java.util.List;

public class LeastSquaresImagePositionLocator implements ImagePositionLocator {
	private List<Marker> markers;

	public LeastSquaresImagePositionLocator() {
	}

	public Point2D getPointPosition(GpsPoint currentPosition) {
		// TODO: for markers == 2 create extra point
		if (markers == null || markers.size() <= 2 || currentPosition == null)
			return null;
		// Build linear system to solve to get coordinate
		// transform - separately for x and y
		// Need a 3rd constant 1 input to represent translations
		// (compare: w in OpenGL).
		double[][] A = new double[markers.size()][3];
		double[] bx = new double[markers.size()];
		double[] by = new double[markers.size()];
		for (int i = 0; i < markers.size(); i++) {
			A[i][0] = markers.get(i).realpoint.longitude;
			A[i][1] = markers.get(i).realpoint.latitude;
			A[i][2] = 1;
			bx[i] = markers.get(i).imgpoint.x;
			by[i] = markers.get(i).imgpoint.y;
		}
		// Multiple A and b by transpose(A)*weigths
		// TODO: add weigths, they are supposed to be
		// inversely proportional to datapoint reliability.
		// 1/distance^2 might just and is cheap, though
		// GPS signal quality when marker was set could
		// be used as input in addition...
		double[] bx3 = new double[3];
		double[] by3 = new double[3];
		double[][] AtWA = new double[3][3];
		for (int i = 0; i < markers.size(); i++) {
			bx3[0] += bx[i] * A[i][0];
			bx3[1] += bx[i] * A[i][1];
			bx3[2] += bx[i] * A[i][2];
			by3[0] += by[i] * A[i][0];
			by3[1] += by[i] * A[i][1];
			by3[2] += by[i] * A[i][2];
			AtWA[0][0] += A[i][0] * A[i][0];
			AtWA[0][1] += A[i][0] * A[i][1];
			AtWA[0][2] += A[i][0] * A[i][2];
			AtWA[1][1] += A[i][1] * A[i][1];
			AtWA[1][2] += A[i][1] * A[i][2];
			AtWA[2][2] += A[i][2] * A[i][2];
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
		double[][] inverse = new double [3][3];
		inverse[0][0] = AtWA[1][1] * AtWA[2][2] - AtWA[1][2] * AtWA[2][1];
		inverse[0][1] = AtWA[2][1] * AtWA[0][2] - AtWA[2][2] * AtWA[0][1];
		inverse[0][2] = AtWA[0][1] * AtWA[1][2] - AtWA[0][2] * AtWA[1][1];
		inverse[1][0] = AtWA[1][2] * AtWA[2][0] - AtWA[1][0] * AtWA[2][2];
		inverse[1][1] = AtWA[2][2] * AtWA[0][0] - AtWA[2][0] * AtWA[0][2];
		inverse[1][2] = AtWA[0][2] * AtWA[1][0] - AtWA[0][0] * AtWA[1][2];
		inverse[2][0] = AtWA[1][0] * AtWA[2][1] - AtWA[1][1] * AtWA[2][0];
		inverse[2][1] = AtWA[2][0] * AtWA[0][1] - AtWA[2][1] * AtWA[0][0];
		inverse[2][2] = AtWA[0][0] * AtWA[1][1] - AtWA[0][1] * AtWA[1][0];
		// Use inverse matrix to solve linear system
		double[] coeffsx = new double[3];
		double[] coeffsy = new double[3];
		for (int i = 0; i < 3; i++) {
			coeffsx[i] = inverse[i][0] * bx3[0] + inverse[i][1] * bx3[1] + inverse[i][2] * bx3[2];
			coeffsy[i] = inverse[i][0] * by3[0] + inverse[i][1] * by3[1] + inverse[i][2] * by3[2];
			coeffsx[i] /= detAtWA;
			coeffsy[i] /= detAtWA;
		}
		// Apply found coordinate transformation
		return new Point2D(coeffsx[0] * currentPosition.longitude + coeffsx[1] * currentPosition.latitude + coeffsx[2],
				coeffsy[0] * currentPosition.longitude + coeffsy[1] * currentPosition.latitude + coeffsy[2]);
	}

	public void newMarkerAdded(List<Marker> markers) {
		// TODO: Run a trial run of the solver to check quality of points.
		// If bad, display a warning. Possibly try to find outliers
		// and suggest for correction
		this.markers = markers;
	}
}
