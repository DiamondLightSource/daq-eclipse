package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.scanning.api.points.models.*;
import org.eclipse.scanning.command.Interpreter;
import org.eclipse.scanning.command.InterpreterResult;
import org.junit.Test;
import org.python.core.PyException;


public class CommandTest {

	@Test
	public void testGridCommand() throws PyException, InterruptedException {

		// The CommandInterpreter will send out models (and stuff) on this queue.
		BlockingQueue<InterpreterResult> ciOutput = new ArrayBlockingQueue<InterpreterResult>(1);
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
	public void testCompoundCommand() throws PyException, InterruptedException {

		BlockingQueue<InterpreterResult> ciOutput = new ArrayBlockingQueue<InterpreterResult>(1);

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
