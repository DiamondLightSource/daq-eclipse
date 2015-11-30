/*-
 * Copyright © 2015 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.api.points;

import java.util.Arrays;
import java.util.List;

/**
 * An immutable class used to represent a point for use in a mapping scan.
 * 
 * This class represents an x,y position for a mapping scan.
 *
 * @author James Mudd
 */
public class Point extends AbstractPosition {

	private Double x;
	private Double y;

	public Point(double xPosition, double yPosition) {
		this.x = xPosition;
		this.y = yPosition;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", x, y);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	@Override
	public int size() {
		return 2;
	}

	@Override
	public List<String> getNames() {
		return Arrays.asList("X", "Y");
	}

	@Override
	public Double get(String name) {
		if ("X".equals(name)) return getX();
		if ("Y".equals(name)) return getY();
		return null;
	}
}
