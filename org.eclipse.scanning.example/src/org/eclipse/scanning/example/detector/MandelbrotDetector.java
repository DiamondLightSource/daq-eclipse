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

package org.eclipse.scanning.example.detector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.Metadata;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.scanning.api.IDetector;
import org.eclipse.scanning.api.points.IPosition;


/**
 * A dummy detector which must be set up with references to two Scannables representing X and Y positions. When used in a step scan, this detector generates a
 * value of 0 if the point (x, y) is in the Mandelbrot set, and greater than zero otherwise.
 * <p>
 * Note: values will always be high if used at (x, y) positions more than 2 units away from the origin.
 */
public class MandelbrotDetector implements IDetector<IDataset> {

	public enum OutputDimensions { ONE_D, TWO_D }

	public static final String VALUE_NAME = "mandelbrot_value";

	// Constants
	private static final int    MAX_ITERATIONS = 500;
	private static final double ESCAPE_RADIUS = 10.0;
	private static final int    COLUMNS = 301;
	private static final int    ROWS = 241;
	private static final int    POINTS = 1000;
	private static final double MAX_X = 1.5;
	private static final double MAX_Y = 1.2;


	// Configurable fields
	private IPosition pos;
	private OutputDimensions outputDimensions = OutputDimensions.TWO_D;

	private String name;

	public MandelbrotDetector() {
		super();
	}

	public void setPosition(IPosition pos) {
		this.pos = pos;
	}

	public void setOutputDimensions(OutputDimensions outputDimensions) {
		this.outputDimensions = outputDimensions;
	}

	public int[] getDataDimensions() throws Exception {
		if (outputDimensions == OutputDimensions.ONE_D) {
			return new int[] { POINTS };
		} else if (outputDimensions == OutputDimensions.TWO_D) {
			return new int[] { COLUMNS, ROWS };
		} else {
			throw new IllegalStateException("Unknown number of dimensions!");
		}
	}


	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}


	@Override
	public void collectData() throws Exception {
		// Not required
	}

	@Override
	public IDataset readout() throws Exception {
		
		final double a = (Double)pos.get("X");
		final double b = (Double)pos.get("Y");

		double value = mandelbrot(a, b);
		// TODO FIXME store value
		
		IDataset ret = null;
		if (outputDimensions == OutputDimensions.ONE_D) {
			ret = calculateJuliaSetLine(a, b, 0.0, 0.0, MAX_X, POINTS);
		} else if (outputDimensions == OutputDimensions.TWO_D) {
			ret = calculateJuliaSet(a, b, COLUMNS, ROWS);
		}
		final Map<String, Serializable> mp = new HashMap<>(1);
		mp.put("value", value);
		Metadata meta = new Metadata(mp);
        ret.addMetadata(meta);
        
		return ret;
	}

	/**
	 * Fill a Julia set around the origin for the value C = a + bi
	 */
	private IDataset calculateJuliaSet(final double a, final double b, int columns, int rows) {
		final double xStart = -MAX_X;
		final double xStop = MAX_X;
		final double yStart = -MAX_Y;
		final double yStop = MAX_Y;
		final double yStep = (yStop - yStart) / (rows - 1);
		double y;
		IDataset juliaSet = new DoubleDataset(rows,columns);
		for (int yIndex = 0; yIndex < rows; yIndex++) {
			y = yStart + yIndex * yStep;
			IDataset line = calculateJuliaSetLine(a, b, y, xStart, xStop, columns);
			for (int x = 0; x < line.getSize(); x++) {
				juliaSet.set(line.getObject(x), yIndex, x);	
			}
		}
		return juliaSet;
	}

	/**
	 * Fill a Julia set line between xStart and xStop at the given y value, for the value C = a + bi
	 */
	private IDataset calculateJuliaSetLine(final double a, final double b, final double y, final double xStart, final double xStop, final int numPoints) {
		final double xStep = (xStop - xStart) / (numPoints - 1);
		double x;
		IDataset juliaSetLine = new DoubleDataset(numPoints);
		for (int xIndex = 0; xIndex < numPoints; xIndex++) {
			x = xStart + xIndex * xStep;
			juliaSetLine.set(julia(x, y, a, b), xIndex);
		}
		return juliaSetLine;
	}

	/**
	 * Iterations of f(z) = z^2 + C, where z = x + yi, C = a + bi, and initial z = 0
	 */
	private double mandelbrot(final double a, final double b) {
		return julia(0.0, 0.0, a, b);
	}

	/**
	 * Iterations of f(z) = z^2 + C, where z = x + yi and C = a + bi
	 */
	private double julia(double x, double y, final double a, final double b) {
		int iteration = 0;
		double xSquared, ySquared, tempX;
		double escapeRadiusSquared = ESCAPE_RADIUS * ESCAPE_RADIUS;
		do {
			xSquared = x * x;
			ySquared = y * y;
			tempX = xSquared - ySquared + a;
			y = 2 * x * y + b;
			x = tempX;
			iteration++;
		} while (iteration < MAX_ITERATIONS && xSquared + ySquared < escapeRadiusSquared);

		double modulus = Math.sqrt(x * x + y * y);

		// If modulus > 1.0, normalise the result
		// (Theoretically, I think this should make the value roughly independent of MAX_ITERATIONS and ESCAPE_RADIUS)
		if (modulus > 1.0) {
			return iteration - (Math.log(Math.log(modulus)) / Math.log(2.0));
		}
		// Otherwise just return the iteration count
		return iteration;
	}

	private int level = 1000;
	
	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public int getLevel() {
		return level;
	}
}
