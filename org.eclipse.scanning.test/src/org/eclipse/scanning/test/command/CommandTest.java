package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;

import org.eclipse.scanning.api.points.models.*;
import org.eclipse.scanning.command.CommandInterpreter;
import org.junit.Test;
import org.python.core.PyException;


public class CommandTest {

	@Test
	public void testGridCommand() throws PyException {

		CommandInterpreter ci = new CommandInterpreter();
		ci.exec("scan(grid(5, 5, bbox=(0, 0, 10, 10), snake=True), 'det', 0.1)");
		AbstractPointsModel pm = ci.retrieveModel();

		assertEquals(GridModel.class, pm.getClass());
		assertEquals(5, ((GridModel) pm).getRows());
		assertEquals("det", ci.retrieveDetector());
		assertEquals(0.1, ci.retrieveExposure(), 1e-8);
	}

}
