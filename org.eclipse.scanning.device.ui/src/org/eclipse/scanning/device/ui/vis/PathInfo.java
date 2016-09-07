/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device.ui.vis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;

/**
 * Simple class to hold information about a scan path
 * The information is held in image coordinates not axis coordinates.
 * Therefore the datasets produces by getX and getY may be
 * directly plotted.
 */
class PathInfo {
	
	// TODO Encapsulate these fields properly...
	protected int    pointCount      = 0;
	protected double smallestXStep   = Double.MAX_VALUE;
	protected double smallestYStep   = Double.MAX_VALUE;
	protected double smallestAbsStep = Double.MAX_VALUE;
	
	// These fields have been encapsulated.
	private List<Double> xCoordinates = new ArrayList<>();
	private List<Double> yCoordinates = new ArrayList<>();

	private String pointCountFormat = "%,d";
	private String doubleFormat = "%.4g";

	public String getFormattedPointCount() {
		return String.format(pointCountFormat, pointCount);
	}
	public String getFormattedSmallestXStep() {
		return formatDouble(smallestXStep);
	}
	public String getFormattedSmallestYStep() {
		return formatDouble(smallestYStep);
	}
	public String getFormattedSmallestAbsStep() {
		return formatDouble(smallestAbsStep);
	}
	private double[] getXCoordinates() {
		double[] xCoords = new double[xCoordinates.size()];
		for (int index = 0; index < xCoordinates.size(); index++) {
			xCoords[index] = xCoordinates.get(index).doubleValue();
		}
		return xCoords;
	}
	private double[] getYCoordinates() {
		double[] yCoords = new double[yCoordinates.size()];
		for (int index = 0; index < yCoordinates.size(); index++) {
			yCoords[index] = yCoordinates.get(index).doubleValue();
		}
		return yCoords;
	}
	
	public IDataset getX() {
		return DatasetFactory.createFromObject(getXCoordinates());
	}
	public IDataset getY() {
		return DatasetFactory.createFromObject(getYCoordinates());
	}

	private String formatDouble(double value) {
		if (value == Double.MAX_VALUE) {
			return "N/A";
		}
		return String.format(doubleFormat, value);
	}
	
	public Map<String, Object> toDict() {
		Map<String, Object> ret = new HashMap<>(1);
		ret.put(PathInfo.class.getSimpleName(), this);
		return ret;
	}
	
	public static PathInfo fromDict(Map<String, Object> map) {
		return (PathInfo) map.get(PathInfo.class.getSimpleName());
	}
	
	public int size() {
		return xCoordinates.size();
	}
	public void add(Double x, Double y) {
		xCoordinates.add(x);
		yCoordinates.add(y);
	}

}