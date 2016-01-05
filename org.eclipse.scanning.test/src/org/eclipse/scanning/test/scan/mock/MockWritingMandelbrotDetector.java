/*-
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

package org.eclipse.scanning.test.scan.mock;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.metadata.Metadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyWriteableDataset;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.ScanningException;


/**
 * A mock detector used for testing ONLY
 * 
 * This detector bypasses the NeXus API for writing things in the correct place
 * and hard codes a particular file path.
 * 
 * DO NOT COPY!
 * 
 */
public class MockWritingMandelbrotDetector extends AbstractRunnableDevice<MockWritingMandlebrotModel> implements IWritableDetector<MockWritingMandlebrotModel> {

	public enum OutputDimensions { ONE_D, TWO_D }

	public static final String VALUE_NAME = "mandelbrot_value";
	

	private MockWritingMandlebrotModel      model;
	private IDataset              toWrite;
	private ILazyWriteableDataset writer;
	
	public MockWritingMandelbrotDetector() throws IOException {
		super();
		this.model = new MockWritingMandlebrotModel();
	}

	public int[] getDataDimensions() throws Exception {
		if (model.getOutputDimensions() == OutputDimensions.ONE_D) {
			return new int[] { model.getxSize()*model.getySize() };
		} else if (model.getOutputDimensions() == OutputDimensions.TWO_D) {
			return new int[] { model.getColumns(), model.getRows() };
		} else {
			throw new IllegalStateException("Unknown number of dimensions!");
		}
	}


	@Override
	public void configure(MockWritingMandlebrotModel model) throws ScanningException {
		
		this.model      = model;
		setState(DeviceState.READY);
		
		// We make a lazy writeable dataset to write out the mandels.
		final int[] shape = new int[]{model.getxSize(), model.getySize(), model.getRows(), model.getColumns()};
		
		try {
			/**
			 * @see org.eclipse.dawnsci.nexus.NexusFileTest.testLazyWriteStringArray()
			 
			  TODO FIXME Hack warning! This is not the way to write to NeXus.
			  We are just doing this for the test!
			  
			  DO NOT COPY!
			*/
			NexusFile file = model.getFile();			
			GroupNode par = file.getGroup("/entry1/instrument/detector", true); // DO NOT COPY!
			writer = new LazyWriteableDataset(model.getName(), Dataset.FLOAT, shape, shape, shape, null); // DO NOT COPY!
			
			file.createData(par, writer); // DO NOT COPY!
			
		} catch (NexusException ne) {
			throw new ScanningException("Cannot open file for writing!", ne);
		}
        
	}

	@Override
	public void run(IPosition pos) throws ScanningException {
		
		final double a = (Double)pos.get(model.getxName());
		final double b = (Double)pos.get(model.getyName());

		double value = mandelbrot(a, b);
		
		if (model.getOutputDimensions() == OutputDimensions.ONE_D) {
			toWrite = calculateJuliaSetLine(a, b, 0.0, 0.0, model.getMaxx(), model.getxSize()*model.getySize());
		} else if (model.getOutputDimensions() == OutputDimensions.TWO_D) {
			toWrite = calculateJuliaSet(a, b, model.getColumns(), model.getRows());
		}
		final Map<String, Serializable> mp = new HashMap<>(1);
		mp.put("value", value);
		Metadata meta = new Metadata(mp);
		toWrite.addMetadata(meta);	
  	}

	@Override
	public boolean write(IPosition pos) throws ScanningException {
		
		/**
		  TODO FIXME Hack warning! This is not the way to write to NeXus.
		  We are just doing this for the test!
		  
		  DO NOT COPY!
		*/
		final int[] start = new int[]{pos.getIndex("x"), pos.getIndex("y"), 0, 0}; // DO NOT COPY!
		final int[] stop  = new int[] {pos.getIndex("x")+1, pos.getIndex("y")+1, model.getRows(), model.getColumns()};
		
		SliceND slice = SliceND.createSlice(writer, start, stop); // DO NOT COPY!
		try {
			writer.setSlice(new IMonitor.Stub(), toWrite, slice); // DO NOT COPY!
		} catch (Exception e) {
			throw new ScanningException("Slice unable to write!", e); // DO NOT COPY!
		}
        
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
		IDataset juliaSet = new DoubleDataset(1, 1, rows, columns);
		for (int yIndex = 0; yIndex < rows; yIndex++) {
			y = yStart + yIndex * yStep;
			IDataset line = calculateJuliaSetLine(a, b, y, xStart, xStop, columns);
			for (int x = 0; x < line.getSize(); x++) {
				juliaSet.set(line.getObject(x), 0, 0, yIndex, x);	
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
