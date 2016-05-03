package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AbstractBenchmarkScanTest {

	protected IRunnableDeviceService      dservice;
	protected IDeviceConnectorService     connector;
	protected IPointGeneratorService      gservice;
	protected IEventService               eservice;
	
	@BeforeClass
	public static void ensureLambdasLoaded() {
		// This is required so that we don't benchmark lambda loading.
		Arrays.asList(1,2,3).stream().map(x -> x+1).collect(Collectors.toList());
	}
	
	/**
	 * Required unless we use a benchmarking framework. However
	 * the test measures each increase in size and uses multiples
	 * plus the fudge. This avoids some of the benchmarking issues.
	 */
	private final static long fudge = 1200;

	@Test
	public void testStepScan() throws Exception {
	
		benchmarkStep(256, 2000, true); // set things up
		
		// Benchmark things. A good idea to do nothing much else on your machine for this...
		long point1     = benchmarkStep(1,     100); // should take not more than 2ms sleep + scan time
		long point64    = benchmarkStep(64,    (64*point1)+fudge);  // should take not more than 64*point1 + scan time
		long point256   = benchmarkStep(256,   (4*point64)+fudge);  // should take not more than 4*point64 sleep + scan time
		long point2560  = benchmarkStep(2560,  (10*point256)+fudge);  // should take not more than 4*point64 sleep + scan time
		long point10240 = benchmarkStep(10240, (4*point2560)+fudge);  // should take not more than 4*point64 sleep + scan time
	}

	private long benchmarkStep(int size, long reqTime) throws Exception {
		return benchmarkStep(size, reqTime, false);
	}

	private long benchmarkStep(int size, long reqTime, boolean silent) throws Exception {
				
		final StepModel smodel = new StepModel("benchmark1", 0, size, 1);
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setCreateImage(false);  // Would put our times off.
		dmodel.setExposureTime(0.001); // Sleep 1ms on the mock detector.
		dmodel.setName("detector");
		IRunnableDevice<MockDetectorModel> detector = dservice.createRunnableDevice(dmodel);
		if (!silent) System.out.println("\nChecking that "+size+" points take "+reqTime+"ms or less to run.");

		// Before, run, after, check time.
		IRunnableDevice<ScanModel> scanner = createTestScanner(smodel, null, null, null, detector);
		
		long before = System.currentTimeMillis();
		scanner.run(null);
		long after = System.currentTimeMillis();
		
		final long time = (after-before);
		if (!silent) System.out.println(size+" point(s) took "+time+"ms with detector exposure set to "+dmodel.getExposureTime()+"s");
		assertTrue("It should not take longer than "+reqTime+"ms to scan "+size+" points with mock devices set to 1 ms exposure.", 
				time<reqTime);
		
		// Attempt to make the VM roughtly do the same thing each run.
		System.gc();
		System.runFinalization();
		Thread.sleep(100); // Hopefully something happens, but probably not unless we intentionally fill the heap.
		                   // We just need to avoid a gc during the 
		
		return time;
	}

	private IRunnableDevice<ScanModel> createTestScanner(AbstractPointsModel pmodel,
														final ScanBean bean,
														final IPublisher<ScanBean> publisher,
														IScannable<?> monitor,
														IRunnableDevice<MockDetectorModel> detector) throws Exception {
		
		// Configure a detector with a collection time.
		if (detector == null) {
			MockDetectorModel dmodel = new MockDetectorModel();
			dmodel.setExposureTime(0.1);
			dmodel.setName("detector");
			detector = dservice.createRunnableDevice(dmodel);
		}
		
		// If none passed, create scan points for a grid.
		if (pmodel == null) {
			pmodel = new GridModel();
			((GridModel) pmodel).setSlowAxisPoints(5);
			((GridModel) pmodel).setFastAxisPoints(5);
			((GridModel) pmodel).setBoundingBox(new BoundingBox(0,0,3,3));
		}
		
		IPointGenerator<?,IPosition> gen = gservice.createGenerator(pmodel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
		smodel.setBean(bean);
		if (monitor!=null) smodel.setMonitors(monitor);
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, publisher);
		return scanner;
	}

}
