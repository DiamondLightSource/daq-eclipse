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

import java.io.IOException;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.DelegateNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
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
public class MandelbrotDetector extends AbstractRunnableDevice<MandelbrotModel> implements IWritableDetector<MandelbrotModel>, INexusDevice<NXdetector> {

	private static final String FIELD_NAME_VALUE = "value";
	private static final String FIELD_NAME_SPECTRUM = "spectrum";

	// Data to be passed from run() to write()
	private IDataset image;
	private IDataset spectrum;
	private double value;

	// Writable datasets
	private ILazyWriteableDataset imageData;
	private ILazyWriteableDataset spectrumData;
	private ILazyWriteableDataset valueData;

	public MandelbrotDetector() throws IOException {
		super();
		this.model = new MandelbrotModel();
	}

	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) {
		DelegateNexusProvider<NXdetector> nexusProvider = new DelegateNexusProvider<NXdetector>(
				getName(), NexusBaseClass.NX_DETECTOR, info, this);

		// Add all fields for any NXdata groups that this device creates
		nexusProvider.setDataFields(NXdetector.NX_DATA, FIELD_NAME_SPECTRUM, FIELD_NAME_VALUE);
		
		// "data" is the name of the primary data field (i.e. the 'signal' field of the default NXdata)
		nexusProvider.setPrimaryDataField(NXdetector.NX_DATA);
		// An additional NXdata group with "spectrum" as the signal to hold the 1D spectrum data
		nexusProvider.addAdditionalPrimaryDataField(FIELD_NAME_SPECTRUM);
		// An additional NXdata group with "value" as the signal to hold the Mandelbrot value
		nexusProvider.addAdditionalPrimaryDataField(FIELD_NAME_VALUE);

		return nexusProvider;
	}

	@Override
	public NXdetector createNexusObject(NexusNodeFactory nodeFactory, NexusScanInfo info) {
		
		final NXdetector detector = nodeFactory.createNXdetector();

		int scanRank = info.getRank();
		// We add 2 to the scan rank to include the image
		imageData = detector.initializeLazyDataset(NXdetector.NX_DATA, scanRank + 2, Dataset.FLOAT64);
		// We add 1 to the scan rank to include the spectrum
		spectrumData = detector.initializeLazyDataset(FIELD_NAME_SPECTRUM, scanRank + 1, Dataset.FLOAT64);
		// Total is a single scalar value (i.e. zero-dimensional) for each point in the scan
		// Dimensions match that of the scan
		valueData = detector.initializeLazyDataset(FIELD_NAME_VALUE, scanRank, Dataset.FLOAT64);

		// Setting chunking is a very good idea if speed is required.
		imageData.setChunking(info.createChunk(model.getRows(), model.getColumns()));
		// FIXME This should work but causes a HDF5 Error: #000: H5Pdcpl.c line 2034 in H5Pset_chunk(): all chunk dimensions must be positive.
		// Leave commented out for now
		//spectrumData.setChunking(info.createChunk(model.getPoints()));

		// Write detector metadata
		detector.setField("exposure_time", model.getExposure());
		detector.setAttribute("exposure_time", "units", "seconds");
		detector.setField("escape_radius", model.getEscapeRadius());
		detector.setField("max_iterations", model.getMaxIterations());

		// The axis datasets
		// FIXME These are not linked using an axis tag to the 4D block (Don't think thats possible yet)
		detector.setDataset("image_x_axis", DatasetFactory.createLinearSpace(-model.getMaxX(), model.getMaxX(), model.getRows(), Dataset.FLOAT64));
		detector.setDataset("image_y_axis", DatasetFactory.createLinearSpace(-model.getMaxY(), model.getMaxY(), model.getColumns(), Dataset.FLOAT64));
		detector.setDataset("spectrum_axis", DatasetFactory.createLinearSpace(0.0, model.getMaxX(), model.getPoints(), Dataset.FLOAT64));

		return detector;
	}

	@Override
	public void configure(MandelbrotModel model) throws ScanningException {
		super.configure(model);
		setName(model.getName());
	}

	@Override
	public void run(IPosition pos) throws ScanningException, InterruptedException {

		// Find out where we are in the scan. This is unique to the Mandelbrot
		// detector as it's a dummy in general a detector shouldn't need to get
		// the position in the scan
		final double a = (Double) pos.get(model.getxName());
		final double b = (Double) pos.get(model.getyName());

		// Calculate the data for the image spectrum and total
		image = calculateJuliaSet(a, b, model.getColumns(), model.getRows());
		spectrum = calculateJuliaSetLine(a, b, 0.0, 0.0, model.getMaxX(), model.getPoints());
		value = mandelbrot(a, b);

		// Pause for a bit to make exposure time work
		if (model.getExposure() > 0) {
			Thread.sleep(Math.round(1000 * model.getExposure()));
		}
	}

	@Override
	public boolean write(IPosition pos) throws ScanningException {

		try {
			SliceND sliceND = NexusScanInfo.createLocation(imageData, pos.getNames(), pos.getIndices(), model.getRows(), model.getColumns());
			imageData.setSlice(null, image, sliceND);
			
			sliceND = NexusScanInfo.createLocation(spectrumData, pos.getNames(), pos.getIndices(), model.getPoints());
			spectrumData.setSlice(null, spectrum, sliceND);

			sliceND = NexusScanInfo.createLocation(valueData, pos.getNames(), pos.getIndices());
			valueData.setSlice(null, DoubleDataset.createFromObject(value), sliceND);

		} catch (Exception e) {
			throw new ScanningException("Failed to write the data to the NeXus file", e);
		}

		return true;
	}

	/**
	 * Fill a Julia set around the origin for the value C = a + bi
	 */
	private IDataset calculateJuliaSet(final double a, final double b, int columns, int rows) {
		final double xStart = -model.getMaxX();
		final double xStop = model.getMaxX();
		final double yStart = -model.getMaxY();
		final double yStop = model.getMaxY();
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
	
}
