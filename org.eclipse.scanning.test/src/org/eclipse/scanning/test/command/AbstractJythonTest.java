package org.eclipse.scanning.test.command;

import java.util.Properties;

import org.eclipse.scanning.test.scan.mock.MockScannable;
import org.python.util.PythonInterpreter;


public abstract class AbstractJythonTest {
	protected static PythonInterpreter pi;

	static {
		Properties postProperties = new Properties();

		// The following line fixes a Python import error seemingly arising
		// from using Jython in an OSGI environment.
		// See http://bugs.jython.org/issue2355 .
		postProperties.put("python.import.site", "false");

		PythonInterpreter.initialize(System.getProperties(), postProperties, new String[0]);
		pi = new PythonInterpreter();

		// FIXME: How to properly specify the path to the Python file?
		// At the moment we use a hack relying on the fact that the
		// JUnit working directory is org.eclipse.scanning.test/.
		pi.exec("import sys");
		pi.exec("sys.path.append('../org.eclipse.scanning.command/scripts/')");
		pi.exec("from mapping_scan_commands import *");
		pi.exec("from mapping_scan_commands import _instantiate");
		pi.exec("from org.eclipse.scanning.example.detector import MandelbrotModel");
		pi.set("my_scannable", new MockScannable("fred", 10));
		pi.set("another_scannable", new MockScannable("bill", 3));
		pi.exec("mandelbrot = lambda t: ('mandelbrot',"
			+	"_instantiate(MandelbrotModel, {'exposureTime': t}))");
	}
}
