package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.scanning.api.points.models.*;
import org.eclipse.scanning.command.CommandInterpreter;
import org.junit.Test;
import org.python.core.PyException;


public class CommandTest {

	@Test
	public void testGridCommand() throws PyException, InterruptedException {

		// The CommandInterpreter will send out models on this queue.
		BlockingQueue<AbstractPointsModel> ciOutput = new ArrayBlockingQueue<AbstractPointsModel>(1);
		// TODO: Use a different BlockingQueue implementation?

		CommandInterpreter ci = new CommandInterpreter(
				ciOutput, "scan(grid(5, 5, bbox=(0, 0, 10, 10), snake=True), 'det', 0.1)");

		new Thread(ci).start();

		AbstractPointsModel pm = ciOutput.take();

		assertEquals(GridModel.class, pm.getClass());
		assertEquals(5, ((GridModel) pm).getRows());
		assertEquals("det", ci.retrieveDetector());
		assertEquals(0.1, ci.retrieveExposure(), 1e-8);
	}

}
