package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
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
import org.eclipse.scanning.command.Interpreter;
import org.eclipse.scanning.command.QueueSingleton;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.server.servlet.ScanServlet;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.scan.mock.MockScannable;
import org.junit.Ignore;
import org.junit.Test;
import org.python.core.PyException;


public class CommandTest {

	private ScanRequest<IROI> interpret(String command) throws PyException, InterruptedException, EventException, URISyntaxException {

		SynchronousQueue<ScanRequest<IROI>> queue = new SynchronousQueue<>();

		ScanServlet ss = new ScanServlet();
		ss.setBroker("vm://localhost?persistent=false");
		ss.setSubmitQueue("commandTestQueue");
		ss.setStatusSet("commandTestStatusSet");
		ss.setStatusTopic("commandTestStatusTopic");
		ss.connect();
		System.out.println("ss.statusTopic is " + ss.getStatusTopic());

		IEventService es = Services.getEventService();
		ISubscriber<IScanListener> subscriber = es.createSubscriber(
				new URI("vm://localhost?persistent=false"), ss.getStatusTopic());

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

		new Thread(new Interpreter(command) {
			{
				// Here we can put objects in the Python namespace for testing purposes.
				// The given command will be interpreted in the context of the objects created here.
				pi.set("my_scannable", new MockScannable("fred", 10));
			}
		}).start();

		return queue.take();
	}

	@Test
	public void testGridCommandWithROI() throws PyException, InterruptedException, EventException, URISyntaxException {

		ScanRequest<IROI> request = interpret(
				"mscan(                           "
			+	"    grid(                        "
			+	"        axes=(my_scannable, 'y'),"  // Can use Scannable objects or strings.
			+	"        count=(5, 6),            "
			+	"        origin=(0, 2),           "
			+	"        size=(10, 9),            "
			+	"        roi=circ((4, 6), 5)      "
			+	"    ),                           "
			+	"    det=mandelbrot(0.1)          "
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
	public void testStepCommand() throws PyException, InterruptedException, EventException, URISyntaxException {

		ScanRequest<IROI> request = interpret(
				// Note the absence of quotes about my_scannable.
				"mscan(step(my_scannable, -2, 5, 0.5), det=mandelbrot(0.1))"
			);

		IScanPathModel model = ((List<IScanPathModel>) request.getModels()).get(0);
		assertEquals(StepModel.class, model.getClass());

		StepModel smodel = (StepModel) model;
		assertEquals("fred", smodel.getName());
		assertEquals(-2, smodel.getStart(), 1e-8);
		assertEquals(5, smodel.getStop(), 1e-8);
		assertEquals(0.5, smodel.getStep(), 1e-8);
	}

	@Test
	public void testRasterCommandWithROIs() throws PyException, InterruptedException, EventException, URISyntaxException {

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
			+	"    det=mandelbrot(0.1)              "
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
	public void testArrayCommand() throws PyException, InterruptedException, EventException, URISyntaxException {

		ScanRequest<IROI> request = interpret(
				"mscan(array('qty', [-3, 1, 1.5, 1e10]), det=mandelbrot(0.1))"
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
	public void testOneDEqualSpacingCommand() throws PyException, InterruptedException, EventException, URISyntaxException {

		ScanRequest<IROI> request = interpret(
				"mscan(line(origin=(0, 4), length=10, angle=0.1, count=10), det=[mandelbrot(0.1)])"
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
	public void testOneDStepCommand() throws PyException, InterruptedException, EventException, URISyntaxException {

		ScanRequest<IROI> request = interpret(
				"mscan(line(origin=(-2, 1.3), length=10, angle=0.1, step=0.5), det=mandelbrot(0.1))"
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
	public void testSinglePointCommand() throws PyException, InterruptedException, EventException, URISyntaxException {

		ScanRequest<IROI> request = interpret(
				"mscan(point(4, 5), mandelbrot(0.1))"
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
	public void testSquareBracketCombinations() throws PyException, InterruptedException, EventException, URISyntaxException {

		ScanRequest<IROI> request = interpret(
				"mscan([point(4, 5)], mandelbrot(0.1))"
			);
		assertEquals(4, ((SinglePointModel) request.getModels().iterator().next()).getX(), 1e-8);
		assertEquals(0.1, ((MandelbrotModel) request.getDetectors().get("mandelbrot")).getExposureTime(), 1e-8);

		request = interpret(
				"mscan(point(4, 5), [mandelbrot(0.1)])"
			);
		assertEquals(4, ((SinglePointModel) request.getModels().iterator().next()).getX(), 1e-8);
		assertEquals(0.1, ((MandelbrotModel) request.getDetectors().get("mandelbrot")).getExposureTime(), 1e-8);

		request = interpret(
				"mscan([point(4, 5)], [mandelbrot(0.1)])"
			);
		assertEquals(4, ((SinglePointModel) request.getModels().iterator().next()).getX(), 1e-8);
		assertEquals(0.1, ((MandelbrotModel) request.getDetectors().get("mandelbrot")).getExposureTime(), 1e-8);
	}

	@Test
	public void testCompoundCommand() throws PyException, InterruptedException, EventException, URISyntaxException {

		ScanRequest<IROI> request = interpret(
				"mscan(                                                                                "
			+	"    path=[                                                                            "
			+	"        grid(axes=('x', 'y'), count=(5, 5), origin=(0, 0), size=(10, 10), snake=True),"
			+	"        step('qty', 0, 10, 1),                                                        "
			+	"    ],                                                                                "
			+	"    det=mandelbrot(0.1)                                                               "
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

	@Ignore("ScanRequest<?>.equals() doesn't allow this test to work.")
	@Test
	public void testArgStyleInvariance() throws PyException, InterruptedException, EventException, URISyntaxException {

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
			+	"    det=mandelbrot(0.1)         "
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
			+	"    mandelbrot(0.1)                     "
			+	")                                       "
			);

		assertTrue(requestMinimalKeywords.equals(requestFullKeywords));
	}

}
