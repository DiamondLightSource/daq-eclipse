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
	private String  xName = "x";
	private String  yName = "y";

	public Point(int xIndex, double xPosition, int yIndex, double yPosition) {
		this("x", xIndex, xPosition, "y", yIndex, yPosition);
	}

	public Point(String xName, int xIndex, double xPosition, String yName, int yIndex, double yPosition) {
		this.xName  = xName;
		this.xIndex = xIndex;
		this.x      = xPosition;
		this.yName  = yName;
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
		int result = super.hashCode();
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((xIndex == null) ? 0 : xIndex.hashCode());
		result = prime * result + ((xName == null) ? 0 : xName.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		result = prime * result + ((yIndex == null) ? 0 : yIndex.hashCode());
		result = prime * result + ((yName == null) ? 0 : yName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (xIndex == null) {
			if (other.xIndex != null)
				return false;
		} else if (!xIndex.equals(other.xIndex))
			return false;
		if (xName == null) {
			if (other.xName != null)
				return false;
		} else if (!xName.equals(other.xName))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		if (yIndex == null) {
			if (other.yIndex != null)
				return false;
		} else if (!yIndex.equals(other.yIndex))
			return false;
		if (yName == null) {
			if (other.yName != null)
				return false;
		} else if (!yName.equals(other.yName))
			return false;
		return true;
	}

	@Override
	public int size() {
		return 2;
	}

	@Override
	public List<String> getNames() {
		return Arrays.asList(xName, yName);
	}

	@Override
	public Double get(String name) {
		if (xName.equalsIgnoreCase(name)) return getX();
		if (yName.equalsIgnoreCase(name)) return getY();
		return null;
	}
	
	@Override
	public int getIndex(String name) {
		if (xName.equalsIgnoreCase(name)) return xIndex;
		if (yName.equalsIgnoreCase(name)) return yIndex;
		return -1;
	}

	public Integer getyIndex() {
		return yIndex;
	}

	public void setyIndex(Integer yIndex) {
		this.yIndex = yIndex;
	}

	public String getyName() {
		return yName;
	}

	public void setyName(String yName) {
		this.yName = yName;
	}

}
