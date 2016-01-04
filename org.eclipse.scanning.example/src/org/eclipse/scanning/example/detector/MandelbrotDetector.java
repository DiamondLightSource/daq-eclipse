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
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.ScanningException;


/**
 * A dummy detector which must be set up with references to two Scannables representing X and Y positions. When used in a step scan, this detector generates a
 * value of 0 if the point (x, y) is in the Mandelbrot set, and greater than zero otherwise.
 * <p>
 * Note: values will always be high if used at (x, y) positions more than 2 units away from the origin.
 */
public class MandelbrotDetector extends AbstractRunnableDevice<MandelbrotModel> implements IWritableDetector<MandelbrotModel> {

	public enum OutputDimensions { ONE_D, TWO_D }

	public static final String VALUE_NAME = "mandelbrot_value";

	// Configurable fields
	private IPosition pos;	

	public MandelbrotDetector() {
		super();
		this.model = new MandelbrotModel();
	}

	public void setPosition(IPosition pos) {
		this.pos = pos;
	}

	public int[] getDataDimensions() throws Exception {
		if (model.getOutputDimensions() == OutputDimensions.ONE_D) {
			return new int[] { model.getPoints() };
		} else if (model.getOutputDimensions() == OutputDimensions.TWO_D) {
			return new int[] { model.getColumns(), model.getRows() };
		} else {
			throw new IllegalStateException("Unknown number of dimensions!");
		}
	}


	private MandelbrotModel model;

	@Override
	public void configure(MandelbrotModel model) throws ScanningException {
		this.model = model;
		setState(DeviceState.READY);
	}
	
	@Override
	public void run() throws ScanningException {
		
		final double a = (Double)pos.get("X");
		final double b = (Double)pos.get("Y");

		double value = mandelbrot(a, b);
		// TODO FIXME store value
		
		IDataset ret = null;
		if (model.getOutputDimensions() == OutputDimensions.ONE_D) {
			ret = calculateJuliaSetLine(a, b, 0.0, 0.0, model.getMaxx(), model.getPoints());
		} else if (model.getOutputDimensions() == OutputDimensions.TWO_D) {
			ret = calculateJuliaSet(a, b, model.getColumns(), model.getRows());
		}
		final Map<String, Serializable> mp = new HashMap<>(1);
		mp.put("value", value);
		Metadata meta = new Metadata(mp);
        ret.addMetadata(meta);
		
  	}

	@Override
	public boolean write(IPosition position) throws ScanningException {
		
        // TODO Read the IDataset to an HDF5 file?
        
		return true;
	}

	/**
	 * Fill a Julia set around the origin for the value C = a + bi
	 */
	private IDataset calculateJuliaSet(final double a, final double b, int columns, int rows) {
		final double xStart = -model.getMaxx();
		final double xStop = model.getMaxx();
		final double yStart = -model.getMaxy();
		final double yStop = model.getMaxy();
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
		double escapeRadiusSquared = model.getEscapeRadius() * model.getEscapeRadius();
		do {
			xSquared = x * x;
			ySquared = y * y;
			tempX = xSquared - ySquared + a;
			y = 2 * x * y + b;
			x = tempX;
			iteration++;
		} while (iteration < model.getMaxIterations() && xSquared + ySquared < escapeRadiusSquared);

		double modulus = Math.sqrt(x * x + y * y);

		// If modulus > 1.0, normalise the result
		// (Theoretically, I think this should make the value roughly independent of MAX_ITERATIONS and ESCAPE_RADIUS)
		if (modulus > 1.0) {
			return iteration - (Math.log(Math.log(modulus)) / Math.log(2.0));
		}
		// Otherwise just return the iteration count
		return iteration;
	}


	@Override
	public void abort() throws ScanningException {
		throw new ScanningException("Operation not supported!");
	}

	@Override
	public void pause() throws ScanningException {
		throw new ScanningException("Operation not supported!");
	}

	@Override
	public void resume() throws ScanningException {
		throw new ScanningException("Operation not supported!");
	}
}
