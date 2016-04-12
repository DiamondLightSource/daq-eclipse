package org.eclipse.scanning.test.command;

import static org.junit.Assert.*;
import static org.eclipse.scanning.command.ScanRequestStringifier.stringify;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.command.Interpreter;
import org.eclipse.scanning.command.QueueSingleton;
import org.junit.Test;
import org.python.core.PyException;


public class RoundTripTest {

	private ScanRequest<IROI> interpret(String command) throws PyException, InterruptedException {
		new Thread(new Interpreter(command)).start();
		return QueueSingleton.INSTANCE.take();
	}

	@Test
	public void testGridModelConcise() throws PyException, InterruptedException {
		String command = "mscan(grid(('bob', 'alice'), (0.0, 1.0), (10.0, 11.0), count=(3, 4), snake=False))";

		assertEquals(command, stringify(interpret(command), false));
	}

	@Test
	public void testGridModelVerbose() throws PyException, InterruptedException {
		String command = "mscan(path=grid(axes=('bob', 'alice'), origin=(0.0, 1.0), size=(10.0, 11.0), count=(3, 4), snake=False))";

		assertEquals(command, stringify(interpret(command), true));
	}

	@Test
	public void testCompoundModel() throws PyException, InterruptedException {
		String command = "mscan([grid(('bob', 'alice'), (0.0, 1.0), (10.0, 11.0), count=(3, 4), snake=False), step('fred', 0.0, 10.0, 1.0)])";

		assertEquals(command, stringify(interpret(command), false));
	}

}
