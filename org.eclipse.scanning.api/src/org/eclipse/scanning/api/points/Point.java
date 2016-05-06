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

import org.eclipse.scanning.api.annotation.UiHidden;

/**
 * An immutable class used to represent a point for use in a mapping scan.
 * 
 * This class represents an x,y position for a mapping scan.
 *
 * @author James Mudd
 */
public class Point extends AbstractPosition {

	private final Double  x;
	private final Double  y;
	private final Integer xIndex;
	private final Integer yIndex;
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
	public int size() {
		return 2;
	}

	@UiHidden
	@Override
	public List<String> getNames() {
		return Arrays.asList(yName, xName);
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
}
