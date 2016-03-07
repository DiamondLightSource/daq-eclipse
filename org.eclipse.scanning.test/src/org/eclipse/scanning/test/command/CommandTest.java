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
		ci.exec("grid(5, 5, bbox=(0, 0, 10, 10,), snake=True)");
		AbstractPointsModel pm = ci.retrieveModel();

		assertEquals(GridModel.class, pm.getClass());
		assertEquals(5, ((GridModel) pm).getRows());
	}

}
