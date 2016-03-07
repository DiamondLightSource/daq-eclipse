package org.eclipse.scanning.command;

import java.util.concurrent.BlockingQueue;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;
import org.eclipse.scanning.api.points.models.*;
import org.eclipse.scanning.api.scan.models.ScanModel;


public class CommandInterpreter implements Runnable {

	private PythonInterpreter pi;
	private BlockingQueue<AbstractPointsModel> output;
	private String command;  // This is temporarily here for testing purposes.
	// TODO: run() should listen for user input from... somewhere.

	public CommandInterpreter(
			BlockingQueue<AbstractPointsModel> listener,
			String command
		) throws PyException {

		pi = new PythonInterpreter();
		this.command = command;
		output = listener;

		// Put some classes in the Python module scope.
		pi.set("_StepModel", StepModel.class);
		pi.set("_GridModel", GridModel.class);
		pi.set("_BoundingBox", BoundingBox.class);
		pi.set("_output", output);

		// FIXME: How to properly specify the path to the Python file?
		// At the moment we use a hack relying on the fact that the
		// JUnit working directory is org.eclipse.scanning.test/.
		pi.exec("execfile('../org.eclipse.scanning.command/python/scan_syntax.py')");
	}

	public void run() throws PyException {
		pi.exec(command);
	}

	public String retrieveDetector() {
		return pi.get("_detector", String.class);
	}

	public double retrieveExposure() {
		return pi.get("_exposure", Double.class);
	}

}
