package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.eclipse.scanning.api.points.models.*;
import org.eclipse.scanning.command.Interpreter;
import org.eclipse.scanning.command.InterpreterResult;
import org.junit.Test;
import org.python.core.PyException;


public class CommandTest {

	private InterpreterResult interpret(String command) throws PyException, InterruptedException {
		// The CommandInterpreter will send out models (and stuff) on this queue.
		BlockingQueue<InterpreterResult> iOutput = new SynchronousQueue<InterpreterResult>();
		new Thread(new Interpreter(iOutput, command)).start();
		return iOutput.take();
	}

	@Test
	public void testGridCommand() throws PyException, InterruptedException {

		InterpreterResult r = interpret(
				"scan(grid(axes=('my_x', 'y'), div=(5, 5), bbox=(0, 0, 10, 10), snake=True), 'det', 0.1)"
			);

		assertEquals(1, r.pmodels.size());  // I.e. this is not a compound scan.
		assertEquals(GridModel.class, r.pmodels.get(0).getClass());
		assertEquals(5, ((GridModel) r.pmodels.get(0)).getRows());
		assertEquals("my_x", ((GridModel) r.pmodels.get(0)).getxName());
		assertEquals(10.0, ((GridModel) r.pmodels.get(0)).getBoundingBox().getWidth(), 1e-8);
		assertEquals("det", r.detector);
		assertEquals(0.1, r.exposure, 1e-8);
	}

	@Test
	public void testStepCommand() throws PyException, InterruptedException {

		InterpreterResult r = interpret(
				"scan(step('my_scannable', -2, 5, 0.5), 'det', 0.1)"
			);

		assertEquals(StepModel.class, r.pmodels.get(0).getClass());
		assertEquals(-2.0, ((StepModel) r.pmodels.get(0)).getStart(), 1e-8);
		assertEquals(5.0, ((StepModel) r.pmodels.get(0)).getStop(), 1e-8);
		assertEquals(0.5, ((StepModel) r.pmodels.get(0)).getStep(), 1e-8);
	}

	@Test
	public void testRasterCommand() throws PyException, InterruptedException {

		InterpreterResult r = interpret(
				"scan(raster(axes=('x', 'y'), inc=(1, 1), bbox=(0, 0, 10, 10), snake=True), 'det', 0.1)"
			);

		assertEquals(RasterModel.class, r.pmodels.get(0).getClass());
		assertEquals(1.0, ((RasterModel) r.pmodels.get(0)).getxStep(), 1e-8);
		assertEquals("det", r.detector);
		assertEquals(0.1, r.exposure, 1e-8);
	}

	@Test
	public void testArrayCommand() throws PyException, InterruptedException {

		InterpreterResult r = interpret(
				"scan(array('qty', [0, 1, 1.5, 1e10]), 'det', 0.1)"
			);

		assertEquals(ArrayModel.class, r.pmodels.get(0).getClass());
		assertEquals(1.5, ((ArrayModel) r.pmodels.get(0)).getPositions()[2], 1e-8);
	}

	@Test
	public void testOneDEqualSpacingCommand() throws PyException, InterruptedException {

		InterpreterResult r = interpret(
				"scan(line(origin=(0, 4), length=10, angle=0.1, count=10), 'det', 0.1)"
			);

		assertEquals(OneDEqualSpacingModel.class, r.pmodels.get(0).getClass());
		assertEquals(0.0, ((OneDEqualSpacingModel) r.pmodels.get(0)).getBoundingLine().getxStart(), 1e-8);
		assertEquals(4.0, ((OneDEqualSpacingModel) r.pmodels.get(0)).getBoundingLine().getyStart(), 1e-8);
		assertEquals(0.1, ((OneDEqualSpacingModel) r.pmodels.get(0)).getBoundingLine().getAngle(), 1e-8);
		assertEquals(10, ((OneDEqualSpacingModel) r.pmodels.get(0)).getPoints());
	}

	@Test
	public void testOneDStepCommand() throws PyException, InterruptedException {

		InterpreterResult r = interpret(
				"scan(line(origin=(-2, 1.3), length=10, angle=0.1, step=0.5), 'det', 0.1)"
			);

		assertEquals(OneDStepModel.class, r.pmodels.get(0).getClass());
		assertEquals(-2.0, ((OneDStepModel) r.pmodels.get(0)).getBoundingLine().getxStart(), 1e-8);
		assertEquals(1.3, ((OneDStepModel) r.pmodels.get(0)).getBoundingLine().getyStart(), 1e-8);
		assertEquals(0.5, ((OneDStepModel) r.pmodels.get(0)).getStep(), 1e-8);
	}

	@Test
	public void testSinglePointCommand() throws PyException, InterruptedException {

		InterpreterResult r = interpret(
				"scan(point(4, 5), 'det', 0.1)"
			);

		assertEquals(SinglePointModel.class, r.pmodels.get(0).getClass());
		assertEquals(4.0, ((SinglePointModel) r.pmodels.get(0)).getX(), 1e-8);
	}

	@Test
	public void testCompoundCommand() throws PyException, InterruptedException {

		InterpreterResult r = interpret(
				"scan(                                                                      "
			+	"    compound(                                                              "
			+	"        grid(axes=('x', 'y'), div=(5, 5), bbox=(0, 0, 10, 10), snake=True),"
			+	"        step('qty', 0, 10, 1),                                             "
			+	"    ),                                                                     "
			+	"    'det', 0.1                                                             "
			+	")                                                                          "
			);

		assertEquals(2, r.pmodels.size());  // I.e. this is a compound scan with two components.
		// It is our job to interpret the list of points models as a compound scan.
		assertEquals(GridModel.class, r.pmodels.get(0).getClass());
		assertEquals(StepModel.class, r.pmodels.get(1).getClass());

		assertEquals(5, ((GridModel) r.pmodels.get(0)).getRows());
	}

}
