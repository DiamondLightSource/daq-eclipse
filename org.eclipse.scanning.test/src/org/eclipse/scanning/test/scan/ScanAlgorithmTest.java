package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.remotedataset.test.mock.LoaderServiceMock;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.LevelEnd;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CollatedStepModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.scan.mock.AnnotatedMockDetectorModel;
import org.eclipse.scanning.test.scan.mock.AnnotatedMockScannable;
import org.eclipse.scanning.test.scan.mock.AnnotatedMockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannable;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class ScanAlgorithmTest extends BrokerTest {

	private IRunnableDeviceService      dservice;
	private IDeviceConnectorService     connector;
	private IPointGeneratorService      gservice;
	private IEventService               eservice;
	private ILoaderService              lservice;
	
	@Before
	public void start() throws Exception {
		
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		connector = new MockScannableConnector();
		dservice  = new RunnableDeviceServiceImpl(connector);
		RunnableDeviceServiceImpl impl = (RunnableDeviceServiceImpl)dservice;
		impl._register(MockDetectorModel.class, MockWritableDetector.class);
		impl._register(AnnotatedMockDetectorModel.class, AnnotatedMockWritableDetector.class);
		impl._register(MockWritingMandlebrotModel.class, MockWritingMandelbrotDetector.class);
		impl._register(MandelbrotModel.class, MandelbrotDetector.class);

		gservice  = new PointGeneratorFactory();

		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		eservice  = new EventServiceImpl(new ActivemqConnectorService());
		
		lservice = new LoaderServiceMock();
		
		// Provide lots of services that OSGi would normally.
		Services.setEventService(eservice);
		Services.setRunnableDeviceService(dservice);
		Services.setGeneratorService(gservice);
		Services.setConnector(connector);
		ServiceHolder.setTestServices(lservice, new DefaultNexusBuilderFactory(), null);
		org.eclipse.dawnsci.nexus.ServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
	}
	
	/**
	 * 1000 scannables, 1000 detectors each divided by into 10 levels.
	 * 
	 * @throws Exception
	 */
	@Test
	public void checkTimes100PointsNoAnnotations() throws Exception {
		
		int pointCount     = 100;
		int scannableCount = 1000;
		int detectorCount  = 1000;
		
		final List<IScannable<?>> scannables = new ArrayList<>();
		MockScannableConnector mc = (MockScannableConnector)connector;
		for (int i = 0; i < scannableCount; i++) {
			MockScannable ms = new MockScannable("specialScannable"+i, 0d);
			ms.setRequireSleep(false);
			ms.setLevel(i%10);
			mc.register(ms);
			scannables.add(ms);
 		}
		
		final List<IRunnableDevice<?>> detectors = new ArrayList<>(detectorCount);
		for (int i = 0; i < detectorCount; i++) {
			MockDetectorModel mod = new MockDetectorModel();
			mod.setName("detector"+i);
			mod.setCreateImage(false);  // Would put our times off.
			mod.setExposureTime(0);
			
			IRunnableDevice<?> dev = dservice.createRunnableDevice(mod);
			dev.setLevel(i%10);
			detectors.add(dev);
		}
		
		long time = checkTimes(pointCount, scannables, detectors);
		assertTrue(time<18);
		
	}
	
	@Test
	public void checkTimes100PointsWithAnnotations() throws Exception {
		
		int pointCount     = 99; // Gives 100 points because it's a step model
		int scannableCount = 1000;
		int detectorCount  = 1000;
		
		final List<IScannable<?>> scannables = new ArrayList<>();
		MockScannableConnector mc = (MockScannableConnector)connector;
		for (int i = 0; i < scannableCount; i++) {
			MockScannable ms = new AnnotatedMockScannable("annotatedSpecialScannable"+i, 0d);
			ms.setRequireSleep(false);
			ms.setLevel(i%10);
			mc.register(ms);
			scannables.add(ms);
 		}
		
		final List<IRunnableDevice<?>> detectors = new ArrayList<>(detectorCount);
		for (int i = 0; i < detectorCount; i++) {
			MockDetectorModel mod = new AnnotatedMockDetectorModel();
			mod.setName("annotatedDetector"+i);
			mod.setCreateImage(false);  // Would put our times off.
			mod.setExposureTime(0);
			
			IRunnableDevice<?> dev = dservice.createRunnableDevice(mod);
			dev.setLevel(i%10);
			detectors.add(dev);
		}
		
		long time = checkTimes(pointCount, scannables, detectors);
		assertTrue(time<18);
		
		for (IScannable<?> s : scannables) {
			AnnotatedMockScannable ams = (AnnotatedMockScannable)s;
			assertEquals(1,    ams.getCount(ScanStart.class));
			assertEquals(100,  ams.getCount(PointStart.class));
			assertEquals(100,  ams.getCount(PointEnd.class));
			assertEquals(100,  ams.getCount(LevelStart.class));
			assertEquals(100,  ams.getCount(LevelEnd.class));
			assertEquals(1,    ams.getCount(ScanEnd.class));
		}
		
		for (IRunnableDevice<?> d : detectors) {
			AnnotatedMockWritableDetector ams = (AnnotatedMockWritableDetector)d;
			assertEquals(1,    ams.getCount(ScanStart.class));
			assertEquals(100,  ams.getCount(PointStart.class));
			assertEquals(100,  ams.getCount(PointEnd.class));
			assertEquals(200,  ams.getCount(LevelStart.class)); // Detectors are called once run, once write
			assertEquals(200,  ams.getCount(LevelEnd.class));   // Detectors are called once run, once write
			assertEquals(1,    ams.getCount(ScanEnd.class));
		}

	}

	
	private long checkTimes(int pointCount, List<IScannable<?>> scannables, List<IRunnableDevice<?>> detectors) throws Exception {
				
		final String[] names = new String[scannables.size()];
		for (int i = 0; i < scannables.size(); i++) {
			names[i] = scannables.get(i).getName();
 		}
		
		final StepModel onek = new CollatedStepModel(0,pointCount,1,names);
		Iterable<IPosition> gen = gservice.createGenerator(onek);
		
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);

		// Create a file to scan into.
		smodel.setFilePath(null); // Intentionally no nexus writing
		
    	smodel.setDetectors(detectors);

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, null);

		long before = System.currentTimeMillis();
		scanner.run(null);
		long after = System.currentTimeMillis();
		double single = (after-before)/1000d;
		System.out.println("Time for one point was: "+Math.round(single)+"ms");
		
		return Math.round(single);

	}
}
