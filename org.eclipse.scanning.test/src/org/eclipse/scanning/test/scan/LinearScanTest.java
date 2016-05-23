package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.json.MarshallerService;
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
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class LinearScanTest extends BrokerTest{
	
	protected IRunnableDeviceService      dservice;
	protected IDeviceConnectorService     connector;
	protected IPointGeneratorService      gservice;
	protected IEventService               eservice;
	private IPublisher<ScanBean>          publisher;
	private ISubscriber<EventListener>    subscriber;
	
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
		
		this.publisher = eservice.createPublisher(uri, IEventService.STATUS_TOPIC);
		this.subscriber = eservice.createSubscriber(uri, IEventService.STATUS_TOPIC);

	}
	
	@After
	public void clean() throws Exception {
        this.publisher.disconnect();
        this.subscriber.disconnect();
	}

	@Test
	public void testSimpleLineScan() throws Exception {
			
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(10);

		IRunnableDevice<ScanModel> scanner = createTestScanner(model, roi);
		
		final List<IPosition> positions = new ArrayList<>();
		subscriber.addListener(new IScanListener() {
			public void scanEventPerformed(ScanEvent evt) {
				final IPosition pos = evt.getBean().getPosition();
				positions.add(pos);
			}
		});

		scanner.run(null);
		
		assertEquals(10, positions.size());
		
		
		checkRun(scanner);
		
	}

	
	private void checkRun(IRunnableDevice<ScanModel> scanner) throws Exception {
		// Bit of a hack to get the generator from the model - should this be easier?
		// Do not copy this code
		ScanModel smodel = (ScanModel)((AbstractRunnableDevice)scanner).getModel();
		IPointGenerator<?> gen = (IPointGenerator<?>)smodel.getPositionIterable();
		MockDetectorModel dmodel = (MockDetectorModel)((AbstractRunnableDevice)smodel.getDetectors().get(0)).getModel();
		assertEquals(gen.size(), dmodel.getRan());
		assertEquals(gen.size(), dmodel.getWritten());
	}

	private IRunnableDevice<ScanModel> createTestScanner(IScanPathModel pmodel, IROI roi) throws Exception {
		
		// Configure a detector with a collection time.
		MandelbrotModel dmodel = new MandelbrotModel();
		dmodel.setExposureTime(0.01);
		dmodel.setName("detector");
		dmodel.setColumns(64);
		dmodel.setRows(64);
		dmodel.setRealAxisName("x");
		dmodel.setImaginaryAxisName("y");
		IRunnableDevice<?>	detector = dservice.createRunnableDevice(dmodel);
		
		IPointGenerator<?> gen = gservice.createGenerator(pmodel, roi);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPositionIterable(gen);
		smodel.setDetectors(detector);
				
		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(smodel, publisher);

		return scanner;
	}


}
