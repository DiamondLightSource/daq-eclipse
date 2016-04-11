package org.eclipse.scanning.command;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;


public class Interpreter implements Runnable {

	protected PythonInterpreter pi;
	private String command;  // This is temporarily here for testing purposes.
	// TODO: run() should listen for user input from... somewhere.

	public Interpreter(String command) throws PyException {

		pi = new PythonInterpreter();
		this.command = command;

		// FIXME: How to properly specify the path to the Python file?
		// At the moment we use a hack relying on the fact that the
		// JUnit working directory is org.eclipse.scanning.test/.
		pi.exec("import sys");
		pi.exec("sys.path.append('../org.eclipse.scanning.command/scripts/')");
		pi.exec("from mapping_scan_commands import *");
	}

	public void run() throws PyException {
		pi.exec(command);
	}

}
