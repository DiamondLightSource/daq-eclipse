/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanNotFinished;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertSolsticeScanGroup;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertSignal;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertTarget;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.junit.Before;
import org.junit.Test;

public class MandelbrotExampleTest extends NexusTest {
	
	private static IWritableDetector<MandelbrotModel> detector;

	@Before
	public void before() throws Exception {
		
		MandelbrotModel model = createMandelbrotModel();
		detector = (IWritableDetector<MandelbrotModel>)dservice.createRunnableDevice(model);
		assertNotNull(detector);
		detector.addRunListener(new IRunListener() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException{
                //System.out.println("Ran mandelbrot detector @ "+evt.getPosition());
			}
		});
	}
	
	@Test
	public void test2ConsecutiveSmallScans() throws Exception {	
		
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, 2, 2);
		scanner.run(null);

		scanner = createGridScan(detector, output, 2, 2);
		scanner.run(null);
	}
	
	/**
	 * This test fails if the chunking is not done by the detector.
	 *  
	 * @throws Exception
	 */
	@Test
	public void testWriteTime2Dvs3D() throws Exception {

		// Tell configure detector to write 1 image into a 2D scan
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, 8, 5);
		ScanModel mod = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		IPosition first = mod.getPositionIterable().iterator().next();
		detector.run(first);
		
		long before = System.currentTimeMillis();
		detector.write(first);
		long after = System.currentTimeMillis();
		long diff2 = (after-before);
		System.out.println("Writing 1 image in 3D stack took: "+diff2+" ms");
		
		File soutput = File.createTempFile("test_mandel_nexus", ".nxs");
		soutput.deleteOnExit();
		scanner = createGridScan(detector, soutput, 10, 8, 5);
		mod = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		first = mod.getPositionIterable().iterator().next();
		detector.run(first);
		
		before = System.currentTimeMillis();
		detector.write(first);
		after = System.currentTimeMillis();
		long diff3 = (after-before);
		System.out.println("Writing 1 image in 4D stack took: "+diff3+" ms");

		assertTrue(diff3<Math.max(20, diff2*1.5));
	}

	@Test
	public void test2DNexusScan() throws Exception {
		testScan(8,5);
	}
	
	@Test
	public void test3DNexusSpiralScan() throws Exception {
		IRunnableDevice<ScanModel> scanner = createSpiralScan(detector, output); // Outer scan of another scannable, for instance temp.
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);
		NXroot rootNode = getNexusRoot(scanner);
		NXentry entry = rootNode.getEntry();
		Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		
		NXdata nXdata = nxDataGroups.get(nxDataGroups.keySet().iterator().next());
		//3d spiral, outer should be 0, inner should both be 1
		Attribute att = nXdata.getAttribute("neXusScannable1_value_set_indices");
		String e = att.getFirstElement();
		assertEquals(0, Integer.parseInt(e));
		
		att = nXdata.getAttribute("xNex" + "_value_set_indices");
		e = att.getFirstElement();
		assertEquals(1, Integer.parseInt(e));
		
		att = nXdata.getAttribute("yNex" + "_value_set_indices");
		e = att.getFirstElement();
		assertEquals(1, Integer.parseInt(e));
	}
	
	@Test
	public void test2DNexusNoImage() throws Exception {
		detector.getModel().setSaveImage(false);
		try {
			
			IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, new int[]{8,5}); // Outer scan of another scannable, for instance temp.
			assertScanNotFinished(getNexusRoot(scanner).getEntry());
			scanner.run(null);
			
			NXroot rootNode = getNexusRoot(scanner);
			NXentry entry = rootNode.getEntry();
			Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
			
			boolean found = false;
			
			Iterator<NXdata> it = nxDataGroups.values().iterator();
			//check no NXdata of rank 4
			while (it.hasNext()) {
				
				NXdata next = it.next();
				String signal = next.getAttributeSignal();
				if (next.getDataNode(signal).getDataset().getRank()==4) {
					found = true;
					break;
				}
				
			}
			assertFalse(found);
			
		} finally {
			detector.getModel().setSaveImage(true);
		}
		
	}
	
	@Test
	public void test3DNexusScan() throws Exception {
		testScan(3,2,5);
	}
	
	// TODO Why does this not pass?
	//@Test
	public void test3DNexusScanLarge() throws Exception {
		long before = System.currentTimeMillis();
		testScan(300,2,5);
		long after = System.currentTimeMillis();
		long diff  = after-before;
		assertTrue(diff<20000);
	}

	@Test
	public void test4DNexusScan() throws Exception {
		testScan(3,3,2,2);
	}
	
	@Test
	public void test5DNexusScan() throws Exception {
		testScan(1,1,1,2,2);
	}
	
	@Test
	public void test8DNexusScan() throws Exception {
		testScan(1,1,1,1,1,1,2,2);
	}
	
	private NXroot getNexusRoot(IRunnableDevice<ScanModel> scanner) throws Exception {
		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();

		INexusFileFactory fileFactory = org.eclipse.dawnsci.nexus.ServiceHolder.getNexusFileFactory();
		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();
		
		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		return (NXroot) nexusTree.getGroupNode();
	}

	private void testScan(int... shape) throws Exception {
		
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, shape); // Outer scan of another scannable, for instance temp.
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);
	
		// Check we reached ready (it will normally throw an exception on error)
		checkNexusFile(scanner, shape); // Step model is +1 on the size
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertEquals(DeviceState.READY, scanner.getDeviceState());

		NXroot rootNode = getNexusRoot(scanner);
		NXentry entry = rootNode.getEntry();
		NXinstrument instrument = entry.getInstrument();
		
		// check that the scan points have been written correctly
		assertSolsticeScanGroup(entry, sizes);
		
		LinkedHashMap<String, List<String>> signalFieldAxes = new LinkedHashMap<>();
		// axis for additional dimensions of a datafield, e.g. image
		signalFieldAxes.put(NXdetector.NX_DATA, Arrays.asList("real", "imaginary"));
		signalFieldAxes.put("spectrum", Arrays.asList("spectrum_axis"));
		signalFieldAxes.put("value", Collections.emptyList());
		
		String detectorName = scanModel.getDetectors().get(0).getName();
		NXdetector nxDetector = instrument.getDetector(detectorName);
		assertEquals(detector.getModel().getExposureTime(), nxDetector.getCount_timeScalar().doubleValue(), 1e-15);
		
		// map of detector data field to name of nxData group where that field is the @signal field
		Map<String, String> expectedDataGroupNames =
				signalFieldAxes.keySet().stream().collect(Collectors.toMap(Function.identity(),
				x -> detectorName + (x.equals(NXdetector.NX_DATA) ? "" : "_" + x)));

		// validate the main NXdata generated by the NexusDataBuilder
		Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		assertEquals(signalFieldAxes.size(), nxDataGroups.size());
		assertTrue(nxDataGroups.keySet().containsAll(
				expectedDataGroupNames.values()));
		for (String nxDataGroupName : nxDataGroups.keySet()) {
			NXdata nxData = entry.getData(nxDataGroupName);

			String sourceFieldName = nxDataGroupName.equals(detectorName) ? NXdetector.NX_DATA :
				nxDataGroupName.substring(nxDataGroupName.indexOf('_') + 1);
			assertSignal(nxData, sourceFieldName);
			// check the nxData's signal field is a link to the appropriate source data node of the detector
			DataNode dataNode = nxDetector.getDataNode(sourceFieldName);
			IDataset dataset = dataNode.getDataset().getSlice();
			assertSame(dataNode, nxData.getDataNode(sourceFieldName));
			assertTarget(nxData, sourceFieldName, rootNode, "/entry/instrument/" + detectorName
					+ "/" + sourceFieldName);

			// check that the other primary data fields of the detector haven't been added to this NXdata
			for (String primaryDataFieldName : signalFieldAxes.keySet()) {
				if (!primaryDataFieldName.equals(sourceFieldName)) {
					assertNull(nxData.getDataNode(primaryDataFieldName));
				}
			}

			int[] shape = dataset.getShape();
			for (int i = 0; i < sizes.length; i++)
				assertEquals(sizes[i], shape[i]);

			// Make sure none of the numbers are NaNs. The detector
			// is expected to fill this scan with non-nulls.
			final PositionIterator it = new PositionIterator(shape);
			while (it.hasNext()) {
				int[] next = it.getPos();
				assertFalse(Double.isNaN(dataset.getDouble(next)));
			}

			// Check axes
			final IPosition pos = scanModel.getPositionIterable().iterator().next();
			final Collection<String> scannableNames = pos.getNames();

			// Append _value_demand to each name in list, then add detector axis fields to result
			List<String> expectedAxesNames = Stream.concat(
					scannableNames.stream().map(x -> x + "_value_set"),
					signalFieldAxes.get(sourceFieldName).stream()).collect(Collectors.toList());
			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

			int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
			int i = -1;
			for (String  scannableName : scannableNames) {
				
			    i++;
				NXpositioner positioner = instrument.getPositioner(scannableName);
				assertNotNull(positioner);

				dataNode = positioner.getDataNode("value_set");
				dataset = dataNode.getDataset().getSlice();
				shape = dataset.getShape();
				assertEquals(1, shape.length);
				assertEquals(sizes[i], shape[0]);

				String nxDataFieldName = scannableName + "_value_set";
				assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
				assertIndices(nxData, nxDataFieldName, i);
				assertTarget(nxData, nxDataFieldName, rootNode,
						"/entry/instrument/" + scannableName + "/value_set");

				// Actual values should be scanD
				dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
				dataset = dataNode.getDataset().getSlice();
				shape = dataset.getShape();
				assertArrayEquals(sizes, shape);

				nxDataFieldName = scannableName + "_" + NXpositioner.NX_VALUE;
				assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
				assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
				assertTarget(nxData, nxDataFieldName, rootNode,
						"/entry/instrument/" + scannableName + "/"
								+ NXpositioner.NX_VALUE);
			}
		}
	}
	
	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<?> detector, File file, int... size) throws Exception {
		
		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel();
		gmodel.setFastAxisName("xNex");
		gmodel.setFastAxisPoints(size[size.length-1]);
		gmodel.setSlowAxisName("yNex");
		gmodel.setSlowAxisPoints(size[size.length-2]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		
		IPointGenerator<?> gen = gservice.createGenerator(gmodel);
		
		IPointGenerator<?>[] gens = new IPointGenerator<?>[size.length - 1];
		// We add the outer scans, if any
		if (size.length > 2) { 
			for (int dim = size.length-3; dim>-1; dim--) {
				final StepModel model;
				if (size[dim]-1>0) {
				    model = new StepModel("neXusScannable"+(dim+1), 10,20,9.99d/(size[dim]-1));
				} else {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,30); // Will generate one value at 10
				}
				final IPointGenerator<?> step = gservice.createGenerator(model);
				gens[dim] = step;
			}
		}
		gens[size.length - 2] = gen;

		gen = gservice.createCompoundGenerator(gens);
	
		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
		
		// Create a file to scan into.
		smodel.setFilePath(file.getAbsolutePath());
		System.out.println("File writing to "+smodel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, null);
		
		final IPointGenerator<?> fgen = gen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				try {
					System.out.println("Running acquisition scan of size "+fgen.size());
				} catch (GeneratorException e) {
					throw new ScanningException(e);
				}
			}
		});

		return scanner;
	}
	
	private IRunnableDevice<ScanModel> createSpiralScan(final IRunnableDevice<?> detector, File file) throws Exception {
		
		SpiralModel spmodel = new SpiralModel("xNex","yNex");
		spmodel.setScale(0.1);
		spmodel.setBoundingBox(new BoundingBox(0,0,1,1));
	
		IPointGenerator<?> gen = gservice.createGenerator(spmodel);

		final StepModel  model = new StepModel("neXusScannable1", 0,3,1);
		final IPointGenerator<?> step = gservice.createGenerator(model);

		gen = gservice.createCompoundGenerator(new IPointGenerator<?>[]{step,gen});
		
		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
		
		// Create a file to scan into.
		smodel.setFilePath(file.getAbsolutePath());
		System.out.println("File writing to "+smodel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, null);
		
		final IPointGenerator<?> fgen = gen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				try {
					System.out.println("Running acquisition scan of size "+fgen.size());
				} catch (GeneratorException e) {
					throw new ScanningException(e);
				}
			}
		});

		return scanner;
	}

}
