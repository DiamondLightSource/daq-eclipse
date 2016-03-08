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

	@Test
	public void testGridCommand() throws PyException, InterruptedException {

		// The CommandInterpreter will send out models (and stuff) on this queue.
		BlockingQueue<InterpreterResult> ciOutput = new SynchronousQueue<InterpreterResult>();
		// TODO: Use a different BlockingQueue implementation?

		Interpreter ci = new Interpreter(
				ciOutput,
				"scan(grid(axes=('x', 'y'), div=(5, 5), bbox=(0, 0, 10, 10), snake=True), 'det', 0.1)"
			);

		new Thread(ci).start();

		InterpreterResult cr = ciOutput.take();

		assertEquals(1, cr.pmodels.size());  // I.e. this is not a compound scan.
		assertEquals(GridModel.class, cr.pmodels.get(0).getClass());
		assertEquals(5, ((GridModel) cr.pmodels.get(0)).getRows());
		assertEquals("det", cr.detector);
		assertEquals(0.1, cr.exposure, 1e-8);
	}

	@Test
	public void testRasterCommand() throws PyException, InterruptedException {

		BlockingQueue<InterpreterResult> ciOutput = new SynchronousQueue<InterpreterResult>();

		Interpreter ci = new Interpreter(
				ciOutput,
				"scan(raster(axes=('x', 'y'), inc=(1, 1), bbox=(0, 0, 10, 10), snake=True), 'det', 0.1)"
			);

		new Thread(ci).start();

		InterpreterResult cr = ciOutput.take();

		assertEquals(RasterModel.class, cr.pmodels.get(0).getClass());
		assertEquals(1.0, ((RasterModel) cr.pmodels.get(0)).getxStep(), 1e-8);
		assertEquals("det", cr.detector);
		assertEquals(0.1, cr.exposure, 1e-8);
	}

	@Test
	public void testArrayCommand() throws PyException, InterruptedException {

		BlockingQueue<InterpreterResult> ciOutput = new SynchronousQueue<InterpreterResult>();

		Interpreter ci = new Interpreter(
				ciOutput,
				"scan(array('qty', [0, 1, 1.5, 1e10]), 'det', 0.1)"
			);

		new Thread(ci).start();

		InterpreterResult cr = ciOutput.take();

		assertEquals(ArrayModel.class, cr.pmodels.get(0).getClass());
		assertEquals(1.5, ((ArrayModel) cr.pmodels.get(0)).getPositions()[2], 1e-8);
	}

	@Test
	public void testOneDEqualSpacingCommand() throws PyException, InterruptedException {

		BlockingQueue<InterpreterResult> ciOutput = new SynchronousQueue<InterpreterResult>();

		Interpreter ci = new Interpreter(
				ciOutput,
				"scan(line(origin=(0, 0), length=10, angle=0.1, count=10), 'det', 0.1)"
			);

		new Thread(ci).start();

		InterpreterResult cr = ciOutput.take();

		assertEquals(OneDEqualSpacingModel.class, cr.pmodels.get(0).getClass());
	}

	@Test
	public void testOneDStepCommand() throws PyException, InterruptedException {

		BlockingQueue<InterpreterResult> ciOutput = new SynchronousQueue<InterpreterResult>();

		Interpreter ci = new Interpreter(
				ciOutput,
				"scan(line(origin=(0, 0), length=10, angle=0.1, step=1), 'det', 0.1)"
			);

		new Thread(ci).start();

		InterpreterResult cr = ciOutput.take();

		assertEquals(OneDStepModel.class, cr.pmodels.get(0).getClass());
	}

	@Test
	public void testSinglePointCommand() throws PyException, InterruptedException {

		BlockingQueue<InterpreterResult> ciOutput = new SynchronousQueue<InterpreterResult>();

		Interpreter ci = new Interpreter(
				ciOutput,
				"scan(point(4, 5), 'det', 0.1)"
			);

		new Thread(ci).start();

		InterpreterResult cr = ciOutput.take();

		assertEquals(SinglePointModel.class, cr.pmodels.get(0).getClass());
	}

	@Test
	public void testCompoundCommand() throws PyException, InterruptedException {

		BlockingQueue<InterpreterResult> ciOutput = new SynchronousQueue<InterpreterResult>();

		Interpreter ci = new Interpreter(
				ciOutput,
				"scan(                                                                      "
			+	"    compound(                                                              "
			+	"        grid(axes=('x', 'y'), div=(5, 5), bbox=(0, 0, 10, 10), snake=True),"
			+	"        step('qty', 0, 10, 1),                                             "
			+	"    ),                                                                     "
			+	"    'det', 0.1                                                             "
			+	")                                                                          "
			);

		new Thread(ci).start();

		InterpreterResult cr = ciOutput.take();

		assertEquals(2, cr.pmodels.size());  // I.e. this is a compound scan with two components.
		// It is our job to interpret the list of points models as a compound scan.
		assertEquals(GridModel.class, cr.pmodels.get(0).getClass());
		assertEquals(StepModel.class, cr.pmodels.get(1).getClass());
	}

}
