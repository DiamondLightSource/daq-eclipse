package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.remotedataset.test.mock.LoaderServiceMock;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class LinearScanTest extends BrokerTest{
	
	protected IRunnableDeviceService      dservice;
	protected IDeviceConnectorService     connector;
	protected IPointGeneratorService      gservice;
	protected IEventService               eservice;
	private IPublisher<ScanBean>          publisher;
	private ISubscriber<EventListener>    subscriber;
	private File tmp;
	
	@Before
	public void setup() throws Exception {
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
		
		// TODO Perhaps put service setting in super class or utility
		Services.setEventService(eservice);
		Services.setRunnableDeviceService(dservice);
		Services.setGeneratorService(gservice);
		Services.setConnector(connector);
		org.eclipse.dawnsci.nexus.ServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		org.eclipse.scanning.sequencer.ServiceHolder.setTestServices(new LoaderServiceMock(), new DefaultNexusBuilderFactory(), null);
		
		this.publisher = eservice.createPublisher(uri, IEventService.STATUS_TOPIC);
		this.subscriber = eservice.createSubscriber(uri, IEventService.STATUS_TOPIC);

		tmp = File.createTempFile("testAScan_", ".nxs");
		tmp.deleteOnExit();

	}
	
	@After
	public void clean() throws Exception {
        this.publisher.disconnect();
        this.subscriber.disconnect();
        tmp.delete();
	}

	@Ignore("This MUST be fixed and working...")
	@Test
	public void testSimpleLineScan() throws Exception {
			
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(10);
        model.setxName("xNex");
        model.setyName("yNex");

		doScan(model, roi);
		
	}
	
	@Ignore("This MUST be fixed and working...")
	@Test
	public void testSimpleGridScan() throws Exception {
			
		
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(0);
		box.setSlowAxisStart(0);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(3);
	
		GridModel model = new GridModel();
		model.setSlowAxisPoints(5);
		model.setFastAxisPoints(5);
		model.setBoundingBox(box);
		model.setFastAxisName("xNex");
		model.setSlowAxisName("yNex");

		doScan(model, null);
	}
	
	private void doScan(IScanPathModel model, LinearROI roi) throws Exception {
		
		IRunnableDevice<ScanModel> scanner = createTestScanner(model, roi);
		
		final List<IPosition> positions = new ArrayList<>();
		subscriber.addListener(new IScanListener() {
			public void scanEventPerformed(ScanEvent evt) {
				final IPosition pos = evt.getBean().getPosition();
				System.out.println(pos);
				positions.add(pos);
			}
		});

		scanner.run(null);
		
		int size = ((IPointGenerator)scanner.getModel().getPositionIterable()).size();
		assertEquals(size, positions.size());
		
	}

	private IRunnableDevice<ScanModel> createTestScanner(IScanPathModel pmodel, IROI roi) throws Exception {
		
		// Configure a detector with a collection time.
		MandelbrotModel dmodel = new MandelbrotModel();
		dmodel.setExposureTime(0.01);
		dmodel.setName("detector");
		dmodel.setColumns(64);
		dmodel.setRows(64);
		dmodel.setRealAxisName("xNex");
		dmodel.setImaginaryAxisName("yNex");
		IRunnableDevice<?>	detector = dservice.createRunnableDevice(dmodel);
		
		IPointGenerator<?> gen = roi!=null ? gservice.createGenerator(pmodel, roi) : gservice.createGenerator(pmodel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
		
		smodel.setFilePath(tmp.getAbsolutePath());
				
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, publisher);

		return scanner;
	}


}
