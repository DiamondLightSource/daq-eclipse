package org.eclipse.scanning.command;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.IScanPathModel;


public class ScanRequestStringifier {
	// Put this code here rather than in a method of ScanRequest
	// because ScanRequest objects are meant to be small things
	// sent across networks/stored in queues, etc..

	final public static String stringify(ScanRequest<IROI> request, Boolean verbose) {
		IScanPathModel[] models = request.getModels();

		String fragment = "mscan(";
		Boolean argsPartiallyWritten = false;

		if (models.length > 0) {
			// TODO: ROIs.
			if (argsPartiallyWritten) { fragment += ", "; }
			if (verbose) { fragment += "path="; }
			if (models.length > 1) {
				fragment += "[";
				Boolean listPartiallyWritten = false;
				for (IScanPathModel model : models) {
					if (listPartiallyWritten) { fragment += ", "; }
					fragment += ModelStringifier.stringify(model, verbose);
					listPartiallyWritten = true;
				}
				fragment += "]";
			} else {
				fragment += ModelStringifier.stringify(models[0], verbose);
			}
			argsPartiallyWritten = true;
		}

		// TODO: Detectors.

		fragment += ")";

		return fragment;
	}
}
