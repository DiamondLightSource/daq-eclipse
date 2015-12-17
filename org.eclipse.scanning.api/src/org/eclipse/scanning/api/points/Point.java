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

	private Double  x;
	private Double  y;
	private Integer xIndex;
	private Integer yIndex;

	public Point(int xIndex, double xPosition, int yIndex, double yPosition) {
		this.xIndex = xIndex;
		this.x      = xPosition;
		this.yIndex = yIndex;
		this.y      = yPosition;
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
		return Arrays.asList("x", "y");
	}

	@Override
	public Double get(String name) {
		if ("x".equalsIgnoreCase(name)) return getX();
		if ("y".equalsIgnoreCase(name)) return getY();
		return null;
	}
	
	@Override
	public int getIndex(String name) {
		if ("x".equalsIgnoreCase(name)) return xIndex;
		if ("y".equalsIgnoreCase(name)) return yIndex;
		return -1;
	}

}
