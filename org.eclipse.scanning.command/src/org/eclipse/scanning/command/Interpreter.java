package org.eclipse.scanning.command;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import org.python.core.PyException;
import org.python.util.PythonInterpreter;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.*;
import org.eclipse.scanning.example.detector.MandelbrotModel;


public class Interpreter implements Runnable {

	private PythonInterpreter pi;
	private BlockingQueue<ScanRequest<IROI>> output;
	private String command;  // This is temporarily here for testing purposes.
	// TODO: run() should listen for user input from... somewhere.

	public Interpreter(
			BlockingQueue<ScanRequest<IROI>> output,
			String command
		) throws PyException {

		pi = new PythonInterpreter();
		this.command = command;
		this.output = output;

		// Put some classes in the Python module scope.
		pi.set("_ArrayList", ArrayList.class);
		pi.set("_ScanRequest", new ScanRequest<IROI>().getClass());
		pi.set("_output", this.output);

		/* Points models */
		pi.set("_StepModel", StepModel.class);
		pi.set("_GridModel", GridModel.class);
		pi.set("_RasterModel", RasterModel.class);
		pi.set("_SinglePointModel", SinglePointModel.class);
		pi.set("_ArrayModel", ArrayModel.class);
		pi.set("_OneDEqualSpacingModel", OneDEqualSpacingModel.class);
		pi.set("_OneDStepModel", OneDStepModel.class);

		/* Bounding shapes */
		pi.set("_BoundingBox", BoundingBox.class);
		pi.set("_BoundingLine", BoundingLine.class);

		/* ROIs */
		pi.set("_IROI", IROI.class);
		pi.set("_CircularROI", CircularROI.class);
		pi.set("_PolygonalROI", PolygonalROI.class);
		pi.set("_RectangularROI", RectangularROI.class);
		// ROIBeans seem to exist in dawnsci, but ScanRequest does not want them.

		/* Detector models */
		pi.set("_MandelbrotModel", MandelbrotModel.class);

		// FIXME: How to properly specify the path to the Python file?
		// At the moment we use a hack relying on the fact that the
		// JUnit working directory is org.eclipse.scanning.test/.
		pi.exec("execfile('../org.eclipse.scanning.command/python/scan_syntax.py')");
	}

	public void run() throws PyException {
		pi.exec(command);
	}

}
