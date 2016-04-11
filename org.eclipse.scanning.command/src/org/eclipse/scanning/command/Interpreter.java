package org.eclipse.scanning.command;

import java.util.Properties;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;


/**
 * An Interpreter is an object capable of passing a string of Python syntax
 * to a freshly invoked Jython instance. TODO: More docs.
 */
public class Interpreter implements Runnable {

	protected PythonInterpreter pi;
	private String command;

	public Interpreter(String command) throws PyException {

		Properties postProperties = new Properties();

		// The following line fixes a Python import error seemingly arising
		// from using Jython in an OSGI environment.
		// See http://bugs.jython.org/issue2355 .
		postProperties.put("python.import.site", "false");

		PythonInterpreter.initialize(System.getProperties(), postProperties, new String[0]);

		pi = new PythonInterpreter();
		this.command = command;

		// FIXME: How to properly specify the path to the Python file?
		// At the moment we use a hack relying on the fact that the
		// JUnit working directory is org.eclipse.scanning.test/.
		pi.exec("import sys");
		pi.exec("sys.path.append('../org.eclipse.scanning.command/python/')");
		pi.exec("from scan_syntax import *");
		pi.exec("populate_detectors('vm://localhost?broker.persistent=false')");
	}

	public void run() throws PyException {
		pi.exec(command);
	}

}
