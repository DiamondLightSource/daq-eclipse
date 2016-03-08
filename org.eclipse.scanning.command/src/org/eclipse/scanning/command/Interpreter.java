package org.eclipse.scanning.command;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;
import org.eclipse.scanning.api.points.models.*;


public class Interpreter implements Runnable {

	private PythonInterpreter pi;
	private BlockingQueue<InterpreterResult> output;
	private String command;  // This is temporarily here for testing purposes.
	// TODO: run() should listen for user input from... somewhere.

	public Interpreter(
			BlockingQueue<InterpreterResult> output,
			String command
		) throws PyException {

		pi = new PythonInterpreter();
		this.command = command;
		this.output = output;

		// Put some classes in the Python module scope.
		pi.set("_ArrayList", ArrayList.class);
		pi.set("_InterpreterResult", InterpreterResult.class);
		pi.set("_StepModel", StepModel.class);
		pi.set("_GridModel", GridModel.class);
		pi.set("_BoundingBox", BoundingBox.class);
		pi.set("_output", this.output);

		// FIXME: How to properly specify the path to the Python file?
		// At the moment we use a hack relying on the fact that the
		// JUnit working directory is org.eclipse.scanning.test/.
		pi.exec("execfile('../org.eclipse.scanning.command/python/scan_syntax.py')");
	}

	public void run() throws PyException {
		pi.exec(command);
	}

}
