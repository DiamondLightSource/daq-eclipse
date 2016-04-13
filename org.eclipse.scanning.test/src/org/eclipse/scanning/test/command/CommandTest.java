package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.device.DeviceResponse;
import org.eclipse.scanning.api.device.IDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.core.IResponseCreator;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.SinglePointModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.command.Interpreter;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.DeviceServiceImpl;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.scan.mock.MockScannable;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Ignore;
import org.junit.Test;
import org.python.core.PyException;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;


public class CommandTest {

	// TODO: Separate this into ScanRequestCreationTest and SubmissionTest.
	// TODO: Rename this to CommandPluginTest
	// Note that selecting "Run" rather than "Debug" in Eclipse
	// results in actually being able to see Python error messages.

	String brokerUri = "vm://localhost?broker.persistent=false";

	private ScanRequest<IROI> interpret(String command)
			throws PyException, InterruptedException, EventException,
			URISyntaxException, IOException, ScanningException {

		SynchronousQueue<ScanRequest<IROI>> queue = new SynchronousQueue<>();

		ScanServlet ss = new ScanServlet();
		ss.setBroker(brokerUri);
		ss.setSubmitQueue(IEventService.SUBMISSION_QUEUE);
		ss.setStatusSet(IEventService.STATUS_SET);
		ss.setStatusTopic(IEventService.STATUS_TOPIC);
		ss.connect();

		IEventService es = Services.getEventService();
		ISubscriber<IScanListener> subscriber = es.createSubscriber(
				new URI(brokerUri), IEventService.STATUS_TOPIC);

		subscriber.addListener(new IScanListener() {
			@Override
			public void scanStateChanged(ScanEvent evt) {
				try {
					queue.put((ScanRequest<IROI>) evt.getBean().getScanRequest());
				} catch (InterruptedException e) { }
			}

			@Override
			public void scanEventPerformed(ScanEvent evt) { }
		});

		// Block copied from RequesterTest.java.
		IDeviceService dservice = new DeviceServiceImpl(new MockScannableConnector());
		MandelbrotDetector mandy = new MandelbrotDetector();
		final DeviceInformation info = new DeviceInformation(); // This comes from extension point or spring in the real world.
		info.setName("mandelbrot");
		info.setLabel("Example Mandelbrot");
		info.setDescription("Example mandelbrot device");
		info.setId("org.eclipse.scanning.example.detector.mandelbrotDetector");
		info.setIcon("org.eclipse.scanning.example/icon/mandelbrot.png");
		mandy.setDeviceInformation(info);
		((DeviceServiceImpl)dservice)._register("mandelbrot", mandy);
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		IEventService eservice = new EventServiceImpl(new ActivemqConnectorService());
		final URI uri = new URI(brokerUri);
		IResponder<DeviceRequest> responder = eservice.createResponder(uri, IEventService.REQUEST_TOPIC, IEventService.RESPONSE_TOPIC);
		responder.setResponseCreator(new IResponseCreator<DeviceRequest>() {
			@Override
			public IResponseProcess<DeviceRequest> createResponder(DeviceRequest bean, IPublisher<DeviceRequest> statusNotifier) throws EventException {
				return new DeviceResponse(dservice, bean, statusNotifier);
			}
		});

		new Thread(new Interpreter(command) {
			{
				// Here we can put objects in the Python namespace for testing purposes.
				// The given command will be interpreted in the context of the objects created here.
				pi.set("my_scannable", new MockScannable("fred", 10));
				pi.set("another_scannable", new MockScannable("bill", 3));
				pi.exec("mandelbrot = lambda _: None");  // Blank detector function for now.
			}
		}).start();

		return queue.take();
	}

	@Test
	public void testGridCommandWithROI() throws Exception {

		ScanRequest<IROI> request = interpret(
				"mscan(                           "
			+	"    grid(                        "
			+	"        axes=(my_scannable, 'y'),"  // Can use Scannable objects or strings.
			+	"        count=(5, 6),            "
			+	"        origin=(0, 2),           "
			+	"        size=(10, 9),            "
			+	"        roi=circ((4, 6), 5)      "
			+	"    ),                           "
			+	"    det=mandelbrot(0.1),         "
			+	"    broker_uri='"+brokerUri+"',  "
			+	")                                "
			);

		Collection<IScanPathModel> models = request.getModels();
		assertEquals(1, models.size());  // I.e. this is not a compound scan.

		IScanPathModel model = models.iterator().next();
		assertEquals(GridModel.class, model.getClass());

		GridModel gmodel = (GridModel) model;
		assertEquals("fred", gmodel.getFastAxisName());
		assertEquals("y", gmodel.getSlowAxisName());
		assertEquals(5, gmodel.getFastAxisPoints());
		assertEquals(6, gmodel.getSlowAxisPoints());
		assertEquals(true, gmodel.isSnake());

		BoundingBox bbox = gmodel.getBoundingBox();
		assertEquals(0, bbox.getFastAxisStart(), 1e-8);
		assertEquals(2, bbox.getSlowAxisStart(), 1e-8);
		assertEquals(10, bbox.getFastAxisLength(), 1e-8);
		assertEquals(9, bbox.getSlowAxisLength(), 1e-8);

		Collection<IROI> regions = request.getRegions(gmodel.getUniqueKey());
		assertEquals(1, regions.size());

		IROI region = regions.iterator().next();
		assertEquals(CircularROI.class, region.getClass());

		CircularROI cregion = (CircularROI) region;
		assertEquals(4, cregion.getCentre()[0], 1e-8);
		assertEquals(6, cregion.getCentre()[1], 1e-8);
		assertEquals(5, cregion.getRadius(), 1e-8);

		Map<String, IDetectorModel> detectors = request.getDetectors();
		assertTrue(detectors.keySet().contains("mandelbrot"));

		Object dmodel = detectors.get("mandelbrot");
		assertEquals(MandelbrotModel.class, dmodel.getClass());

		MandelbrotModel mmodel = (MandelbrotModel) dmodel;
		assertEquals(0.1, mmodel.getExposureTime(), 1e-8);
	}

	@Test
	public void testStepCommandWithMonitors() throws Exception {

		ScanRequest<IROI> request = interpret(
				// Note the absence of quotes about my_scannable.
				"mscan(step(my_scannable, -2, 5, 0.5),"
			+	"      mon=['x', another_scannable],  "  // Monitor two scannables.
			+	"      det=mandelbrot(0.1),           "
			+	"      broker_uri='"+brokerUri+"')    "
			);

		IScanPathModel model = ((List<IScanPathModel>) request.getModels()).get(0);
		assertEquals(StepModel.class, model.getClass());

		StepModel smodel = (StepModel) model;
		assertEquals("fred", smodel.getName());
		assertEquals(-2, smodel.getStart(), 1e-8);
		assertEquals(5, smodel.getStop(), 1e-8);
		assertEquals(0.5, smodel.getStep(), 1e-8);

		Collection<String> monitors = request.getMonitorNames();
		assertEquals(2, monitors.size());
		Iterator<String> monitorIterator = monitors.iterator();
		assertEquals("x", monitorIterator.next());
		assertEquals("bill", monitorIterator.next());
	}

	@Test
	public void testRasterCommandWithROIs() throws Exception {

		ScanRequest<IROI> request = interpret(
				"mscan(                               "
			+	"    grid(                            "
			+	"        axes=('x', 'y'),             "
			+	"        step=(0.5, 0.6),             "
			+	"        origin=(1, 2),               "
			+	"        size=(7, 8),                 "
			+	"        snake=True,                  "
			+	"        roi=[                        "
			+	"            circ((4, 4), 5),         "
			+	"            rect((3, 4), (3, 3), 0.1)"
			+	"        ]                            "
			+	"    ),                               "
			+	"    det=mandelbrot(0.1),             "
			+	"    broker_uri='"+brokerUri+"',      "
			+	")                                    "
			);

		IScanPathModel model = request.getModels().iterator().next();
		assertEquals(RasterModel.class, model.getClass());

		RasterModel rmodel = (RasterModel) model;
		assertEquals(0.5, rmodel.getFastAxisStep(), 1e-8);

		Collection<IROI> regions = request.getRegions(rmodel.getUniqueKey());
		assertEquals(2, regions.size());

		Iterator<IROI> regionIterator = regions.iterator();
		CircularROI cregion = (CircularROI) regionIterator.next();
		assertEquals(4, cregion.getCentre()[0], 1e-8);
		assertEquals(4, cregion.getCentre()[1], 1e-8);
		assertEquals(5, cregion.getRadius(), 1e-8);

		RectangularROI rregion = (RectangularROI) regionIterator.next();
		assertEquals(3, rregion.getPoint()[0], 1e-8);
		assertEquals(4, rregion.getPoint()[1], 1e-8);
		assertEquals(3, rregion.getLengths()[0], 1e-8);
		assertEquals(3, rregion.getLengths()[0], 1e-8);
		assertEquals(0.1, rregion.getAngle(), 0.1);  // Radians.
	}

	@Test
	public void testArrayCommand() throws Exception {

		ScanRequest<IROI> request = interpret(
				"mscan(array('qty', [-3, 1, 1.5, 1e10]),"
			+	"      det=mandelbrot(0.1),             "
			+	"      broker_uri='"+brokerUri+"')     "
			);

		IScanPathModel model = request.getModels().iterator().next();
		assertEquals(ArrayModel.class, model.getClass());

		ArrayModel amodel = (ArrayModel) model;
		assertEquals("qty", amodel.getName());
		assertEquals(-3, amodel.getPositions()[0], 1e-8);
		assertEquals(1, amodel.getPositions()[1], 1e-8);
		assertEquals(1.5, amodel.getPositions()[2], 1e-8);
		assertEquals(1e10, amodel.getPositions()[3], 1);
	}

	@Test
	public void testOneDEqualSpacingCommand() throws Exception {

		ScanRequest<IROI> request = interpret(
				"mscan(line(origin=(0, 4), length=10, angle=0.1, count=10),"
			+	"      det=[mandelbrot(0.1)],                              "
			+	"      broker_uri='"+brokerUri+"')                         "
			);

		IScanPathModel model = request.getModels().iterator().next();
		assertEquals(OneDEqualSpacingModel.class, model.getClass());

		OneDEqualSpacingModel omodel = (OneDEqualSpacingModel) model;
		assertEquals(0, omodel.getBoundingLine().getxStart(), 1e-8);
		assertEquals(4, omodel.getBoundingLine().getyStart(), 1e-8);
		assertEquals(0.1, omodel.getBoundingLine().getAngle(), 1e-8);
		assertEquals(10, omodel.getBoundingLine().getLength(), 1e-8);
		assertEquals(10, omodel.getPoints());
	}

	@Test
	public void testOneDStepCommand() throws Exception {

		ScanRequest<IROI> request = interpret(
				"mscan(line(origin=(-2, 1.3), length=10, angle=0.1, step=0.5),"
			+	"      det=mandelbrot(0.1),                                   "
			+	"      broker_uri='"+brokerUri+"')                            "
			);

		IScanPathModel model = request.getModels().iterator().next();
		assertEquals(OneDStepModel.class, model.getClass());

		OneDStepModel omodel = (OneDStepModel) model;
		assertEquals(-2, omodel.getBoundingLine().getxStart(), 1e-8);
		assertEquals(1.3, omodel.getBoundingLine().getyStart(), 1e-8);
		assertEquals(0.1, omodel.getBoundingLine().getAngle(), 1e-8);
		assertEquals(10, omodel.getBoundingLine().getLength(), 1e-8);
		assertEquals(0.5, omodel.getStep(), 1e-8);
	}

	@Test
	public void testSinglePointCommand() throws Exception {

		ScanRequest<IROI> request = interpret(
				"mscan(point(4, 5), mandelbrot(0.1), broker_uri='"+brokerUri+"')"
			);

		IScanPathModel model = request.getModels().iterator().next();
		assertEquals(SinglePointModel.class, model.getClass());

		SinglePointModel spmodel = (SinglePointModel) model;
		assertEquals(4, spmodel.getX(), 1e-8);
		assertEquals(5, spmodel.getY(), 1e-8);

		Map<String, IDetectorModel> detectors = request.getDetectors();
		assertTrue(detectors.keySet().contains("mandelbrot"));

		Object dmodel = detectors.get("mandelbrot");
		assertEquals(MandelbrotModel.class, dmodel.getClass());

		MandelbrotModel mmodel = (MandelbrotModel) dmodel;
		assertEquals(0.1, mmodel.getExposureTime(), 1e-8);
	}

	@Test
	public void testSquareBracketCombinations() throws Exception {

		ScanRequest<IROI> request = interpret(
				"mscan([point(4, 5)], mandelbrot(0.1), broker_uri='"+brokerUri+"')"
			);
		assertEquals(4, ((SinglePointModel) request.getModels().iterator().next()).getX(), 1e-8);
		assertEquals(0.1, ((MandelbrotModel) request.getDetectors().get("mandelbrot")).getExposureTime(), 1e-8);

		request = interpret(
				"mscan(point(4, 5), [mandelbrot(0.1)], broker_uri='"+brokerUri+"')"
			);
		assertEquals(4, ((SinglePointModel) request.getModels().iterator().next()).getX(), 1e-8);
		assertEquals(0.1, ((MandelbrotModel) request.getDetectors().get("mandelbrot")).getExposureTime(), 1e-8);

		request = interpret(
				"mscan([point(4, 5)], [mandelbrot(0.1)], broker_uri='"+brokerUri+"')"
			);
		assertEquals(4, ((SinglePointModel) request.getModels().iterator().next()).getX(), 1e-8);
		assertEquals(0.1, ((MandelbrotModel) request.getDetectors().get("mandelbrot")).getExposureTime(), 1e-8);
	}

	@Test
	public void testCompoundCommand() throws Exception {

		ScanRequest<IROI> request = interpret(
				"mscan(                                                                                "
			+	"    path=[                                                                            "
			+	"        grid(axes=('x', 'y'), count=(5, 5), origin=(0, 0), size=(10, 10), snake=True),"
			+	"        step('qty', 0, 10, 1),                                                        "
			+	"    ],                                                                                "
			+	"    det=mandelbrot(0.1),                                                              "
			+	"    broker_uri='"+brokerUri+"',                                                       "
			+	")                                                                                     "
			);

		Collection<IScanPathModel> models = request.getModels();
		assertEquals(2, models.size());  // I.e. this is a compound scan with two components.

		Iterator<IScanPathModel> modelIterator = models.iterator();
		GridModel gmodel = (GridModel) modelIterator.next();
		assertEquals(5, gmodel.getSlowAxisPoints());

		StepModel smodel = (StepModel) modelIterator.next();
		assertEquals(10, smodel.getStop(), 1e-8);
	}

	@Test
	public void testMoveToKeepStillCommand() throws Exception {

		ScanRequest<IROI> request = interpret(
				// Note the absence of quotes about my_scannable.
				"mscan([step(my_scannable, -2, 5, 0.5), val('y', 5)],"
			+	"      det=mandelbrot(0.1),                          "
			+	"      broker_uri='"+brokerUri+"')                   "
			);

		Collection<IScanPathModel> models = request.getModels();
		assertEquals(2, models.size());

		Iterator<IScanPathModel> modelIterator = models.iterator();
		modelIterator.next();  // Throw away the step model.

		ArrayModel amodel = (ArrayModel) modelIterator.next();
		assertEquals(1, amodel.getPositions().length);
		assertEquals(5, amodel.getPositions()[0], 1e-8);
	}

	@Ignore("ScanRequest<?>.equals() doesn't allow this test to work.")
	@Test
	public void testArgStyleInvariance() throws Exception {

		ScanRequest<IROI> requestFullKeywords = interpret(
				"mscan(                          "
			+	"    path=grid(                  "
			+	"        axes=('x', 'y'),        "
			+	"        origin=(1, 2),          "
			+	"        size=(7, 8),            "
			+	"        step=(0.5, 0.6),        "
			+	"        roi=[                   "
			+	"            circ(               "
			+	"                origin=(4, 4),  "
			+	"                radius=5        "
			+	"            ),                  "
			+	"            rect(               "
			+	"                origin=(3, 4),  "
			+	"                size=(3, 3),    "
			+	"                angle=0.1       "
			+	"            ),                  "
			+	"        ]                       "
			+	"    ),                          "
			+	"    det=mandelbrot(0.1),        "
			+	"    broker_uri='"+brokerUri+"', "
			+	")                               "
			);

		ScanRequest<IROI> requestMinimalKeywords = interpret(
				"mscan(                                  "
			+	"    grid(                               "
			+	"        ('x', 'y'), (1, 2), (7, 8),     "
			+	"        step=(0.5, 0.6),                "
			+	"        roi=[                           "
			+	"            circ((4, 4), 5),            "
			+	"            rect((3, 4), (3, 3), 0.1),  "
			+	"        ]                               "
			+	"    ),                                  "
			+	"    mandelbrot(0.1),                    "
			+	"    broker_uri='"+brokerUri+"',         "
			+	")                                       "
			);

		assertTrue(requestMinimalKeywords.equals(requestFullKeywords));
	}

}
