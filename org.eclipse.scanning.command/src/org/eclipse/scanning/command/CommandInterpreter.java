package org.eclipse.scanning.command;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;
import org.eclipse.scanning.api.points.models.*;
import org.eclipse.scanning.api.scan.models.ScanModel;


public class CommandInterpreter {

	private PythonInterpreter pi;

	public CommandInterpreter() throws PyException {
		pi = new PythonInterpreter();

		// Put some classes in the Python module scope.
		pi.set("_StepModel", StepModel.class);
		pi.set("_GridModel", GridModel.class);
		pi.set("_BoundingBox", BoundingBox.class);

		// FIXME: How to properly specify the path to the Python file?
		// At the moment we use a hack relying on the fact that the
		// JUnit working directory is org.eclipse.scanning.test/.
		pi.exec("execfile('../org.eclipse.scanning.command/python/scan_syntax.py')");
	}

	public void exec(String command) throws PyException {
		pi.exec(command);
	}

	public AbstractPointsModel retrieveModel() {
		return pi.get("_pmodel", AbstractPointsModel.class);
	}

	public String retrieveDetector() {
		return pi.get("_detector", String.class);
	}

	public double retrieveExposure() {
		return pi.get("_exposure", Double.class);
	}

}
