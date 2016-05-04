package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
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
	
		MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setCreateImage(false);  // Would put our times off.
		dmodel.setExposureTime(0.001); // Sleep 1ms on the mock detector.
		dmodel.setName("detector");
		IRunnableDevice<MockDetectorModel> detector = dservice.createRunnableDevice(dmodel);

		benchmarkStep(new BenchmarkBean(256, 2000l, 1, true, detector)); // set things up
		
		// Benchmark things. A good idea to do nothing much else on your machine for this...
		long point1     = benchmarkStep(new BenchmarkBean(1,     100, 1, detector)); // should take not more than 2ms sleep + scan time
		
		// should take not more than 64*point1 + scan time
		long point64    = benchmarkStep(new BenchmarkBean(64,    (64*point1)+fudge,   10, detector));  
		
		// should take not more than 4*point64 sleep + scan time
		long point256   = benchmarkStep(new BenchmarkBean(256,   (4*point64)+fudge,   10, detector));  
		
		// should take not more than 4*point64 sleep + scan time
		long point2560  = benchmarkStep(new BenchmarkBean(2560,  (10*point256)+fudge, 10, detector));  
		
		// should take not more than 4*point64 sleep + scan time
		long point10240 = benchmarkStep(new BenchmarkBean(10240, (4*point2560)+fudge, 10, detector));  
	}


	/**
	 * 
	 * @param size
	 * @param reqTime
	 * @param tries - we try several times to get the time because sometimes the gc will run.
	 * @param silent
	 * @return
	 * @throws Exception
	 */
	private long benchmarkStep(final BenchmarkBean bean) throws Exception {
				
		if (!bean.isSilent()) System.out.println("\nChecking that "+bean.getSize()+" points take "+bean.getReqTime()+"ms or less to run. Using "+bean.getTries()+" tries.");

		// Before, run, after, check time.
		final StepModel smodel = new StepModel(bean.getScannableName(), 0, bean.getSize(), 1);
		IRunnableDevice<ScanModel> scanner = createTestScanner(smodel, bean.getDetector());
		
		long time = 0l;
		for (int i = 0; i < bean.getTries(); i++) {
			long before = System.currentTimeMillis();
			scanner.run(null);
			long after = System.currentTimeMillis();
			
			time = (after-before);
			
			if (time>bean.getReqTime()) continue;
			break;
		}
		
		final IDetectorModel dmodel = bean.getDetector().getModel();
		if (!bean.isSilent()) System.out.println(bean.getSize()+" point(s) took "+time+"ms with detector exposure set to "+dmodel.getExposureTime()+"s");
		assertTrue("It should not take longer than "+bean.getReqTime()+"ms to scan "+bean.getSize()+" points with mock devices set to 1 ms exposure.", 
				    time<bean.getReqTime());
		
		// Attempt to make the VM roughly do the same thing each run.
		System.gc();
		System.runFinalization();
		Thread.sleep(100); // Hopefully something happens, but probably not unless we intentionally fill the heap.
		                   // We just need to avoid a gc during the benchmarking phase.
		
		return time;
	}

	private IRunnableDevice<ScanModel> createTestScanner(AbstractPointsModel pmodel,
														IRunnableDevice<?> detector) throws Exception {
		
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
		
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel);
		return scanner;
	}

}
