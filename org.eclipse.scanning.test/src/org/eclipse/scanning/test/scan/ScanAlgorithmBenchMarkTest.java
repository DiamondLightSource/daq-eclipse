package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.remotedataset.test.mock.LoaderServiceMock;
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
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannable;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class ScanAlgorithmBenchMarkTest extends BrokerTest {

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
	public void checkTimes100Points() throws Exception {
		
		int pointCount     = 100;
		int scannableCount = 1000;
		int detectorCount  = 1000;
		
		final String[] names = new String[scannableCount];
		MockScannableConnector mc = (MockScannableConnector)connector;
		for (int i = 0; i < names.length; i++) {
			MockScannable ms = new MockScannable("specialScannable"+i, 0d);
			ms.setRequireSleep(false);
			ms.setLevel(i%10);
			mc.register(ms);
			names[i] = ms.getName();
 		}
		final StepModel onek = new CollatedStepModel(0,pointCount,1,names);
		Iterable<IPosition> gen = gservice.createGenerator(onek);
		
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);

		// Create a file to scan into.
		smodel.setFilePath(null); // Intentionally no nexus writing
		
		final List<IRunnableDevice<?>> ds = new ArrayList<>(detectorCount);
		for (int i = 0; i < detectorCount; i++) {
			MockDetectorModel mod = new MockDetectorModel();
			mod.setName("detector"+i);
			mod.setCreateImage(false);  // Would put our times off.
			mod.setExposureTime(0);
			
			IRunnableDevice<?> dev = dservice.createRunnableDevice(mod);
			dev.setLevel(i%10);
			ds.add(dev);
			
		}
		smodel.setDetectors(ds);

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, null);

		long before = System.currentTimeMillis();
		scanner.run(null);
		long after = System.currentTimeMillis();
		double single = (after-before)/1000d;
		System.out.println("Time for one point was: "+Math.round(single)+"ms");
		
		assertTrue(Math.round(single)<18);
	}
}
