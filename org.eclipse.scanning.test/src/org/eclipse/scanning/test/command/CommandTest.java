package org.eclipse.scanning.test.command;

import static org.junit.Assert.*;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.*;
import org.eclipse.scanning.command.Interpreter;
import org.eclipse.scanning.command.QueueSingleton;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.junit.Test;
import org.python.core.PyException;


public class CommandTest {

	private ScanRequest<IROI> interpret(String command) throws PyException, InterruptedException {
		new Thread(new Interpreter(command)).start();
		return QueueSingleton.INSTANCE.take();
	}

	@Test
	public void testGridCommand() throws PyException, InterruptedException {

		ScanRequest<IROI> r = interpret(
				"scan(grid(axes=('my_x', 'y'), div=(5, 5), bbox=(0, 0, 10, 10), roi=circ(4, 4, 5)),"
			+	"     ('det', 0.1))                                                                "
			);

		assertEquals(1, r.getModels().length);  // I.e. this is not a compound scan.
		assertEquals(GridModel.class, r.getModels()[0].getClass());
		assertEquals(5, ((GridModel) r.getModels()[0]).getRows());
		assertEquals("my_x", ((GridModel) r.getModels()[0]).getxName());
		assertEquals(10.0, ((GridModel) r.getModels()[0]).getBoundingBox().getWidth(), 1e-8);

		assertEquals(CircularROI.class, r.getRegions(r.getModels()[0].getUniqueKey())[0].getClass());

		assertTrue(r.getDetectors().keySet().contains("det"));
		assertEquals(MandelbrotModel.class, r.getDetectors().get("det").getClass());
		assertEquals(0.1, ((MandelbrotModel) r.getDetectors().get("det")).getExposure(), 1e-8);
	}

	@Test
	public void testStepCommand() throws PyException, InterruptedException {

		ScanRequest<IROI> r = interpret(
				"scan(step('my_scannable', -2, 5, 0.5), ('det', 0.1))"
			);

		assertEquals(StepModel.class, r.getModels()[0].getClass());
		assertEquals(-2.0, ((StepModel) r.getModels()[0]).getStart(), 1e-8);
		assertEquals(5.0, ((StepModel) r.getModels()[0]).getStop(), 1e-8);
		assertEquals(0.5, ((StepModel) r.getModels()[0]).getStep(), 1e-8);
	}

	@Test
	public void testRasterCommand() throws PyException, InterruptedException {

		ScanRequest<IROI> r = interpret(
				"scan(raster(axes=('x', 'y'), inc=(1, 1), bbox=(0, 0, 10, 10), snake=True),"
			+	"     ('det', 0.1))                                                        "
			);

		assertEquals(RasterModel.class, r.getModels()[0].getClass());
		assertEquals(1.0, ((RasterModel) r.getModels()[0]).getxStep(), 1e-8);
	}

	@Test
	public void testArrayCommand() throws PyException, InterruptedException {

		ScanRequest<IROI> r = interpret(
				"scan(array('qty', [0, 1, 1.5, 1e10]), ('det', 0.1))"
			);

		assertEquals(ArrayModel.class, r.getModels()[0].getClass());
		assertEquals(1.5, ((ArrayModel) r.getModels()[0]).getPositions()[2], 1e-8);
	}

	@Test
	public void testOneDEqualSpacingCommand() throws PyException, InterruptedException {

		ScanRequest<IROI> r = interpret(
				"scan(line(origin=(0, 4), length=10, angle=0.1, count=10), ('det', 0.1))"
			);

		assertEquals(OneDEqualSpacingModel.class, r.getModels()[0].getClass());
		assertEquals(0.0, ((OneDEqualSpacingModel) r.getModels()[0]).getBoundingLine().getxStart(), 1e-8);
		assertEquals(4.0, ((OneDEqualSpacingModel) r.getModels()[0]).getBoundingLine().getyStart(), 1e-8);
		assertEquals(0.1, ((OneDEqualSpacingModel) r.getModels()[0]).getBoundingLine().getAngle(), 1e-8);
		assertEquals(10, ((OneDEqualSpacingModel) r.getModels()[0]).getPoints());
	}

	@Test
	public void testOneDStepCommand() throws PyException, InterruptedException {

		ScanRequest<IROI> r = interpret(
				"scan(line(origin=(-2, 1.3), length=10, angle=0.1, step=0.5), ('det', 0.1))"
			);

		assertEquals(OneDStepModel.class, r.getModels()[0].getClass());
		assertEquals(-2.0, ((OneDStepModel) r.getModels()[0]).getBoundingLine().getxStart(), 1e-8);
		assertEquals(1.3, ((OneDStepModel) r.getModels()[0]).getBoundingLine().getyStart(), 1e-8);
		assertEquals(0.5, ((OneDStepModel) r.getModels()[0]).getStep(), 1e-8);
	}

	@Test
	public void testSinglePointCommand() throws PyException, InterruptedException {

		ScanRequest<IROI> r = interpret(
				"scan(point(4, 5), ('det', 0.1))"
			);

		assertEquals(SinglePointModel.class, r.getModels()[0].getClass());
		assertEquals(4.0, ((SinglePointModel) r.getModels()[0]).getX(), 1e-8);
	}

	@Test
	public void testCompoundCommand() throws PyException, InterruptedException {

		ScanRequest<IROI> r = interpret(
				"scan(                                                                      "
			+	"    [                                                                      "
			+	"        grid(axes=('x', 'y'), div=(5, 5), bbox=(0, 0, 10, 10), snake=True),"
			+	"        step('qty', 0, 10, 1),                                             "
			+	"    ],                                                                     "
			+	"    ('det', 0.1)                                                           "
			+	")                                                                          "
			);

		assertEquals(2, r.getModels().length);  // I.e. this is a compound scan with two components.
		// It is our job to interpret the list of points models as a compound scan.
		assertEquals(GridModel.class, r.getModels()[0].getClass());
		assertEquals(StepModel.class, r.getModels()[1].getClass());

		assertEquals(5, ((GridModel) r.getModels()[0]).getRows());
	}

}
