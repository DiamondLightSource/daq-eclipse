package org.eclipse.scanning.command;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.IScanPathModel;


public class ScanRequestStringifier {
	// Put this code here rather than in a method of ScanRequest
	// because ScanRequest objects are meant to be small things
	// sent across networks/stored in queues, etc..

	final public static String stringify(ScanRequest<IROI> request, Boolean verbose) {
		Collection<IScanPathModel> models = request.getModels();

		String fragment = "mscan(";
		Boolean argsPartiallyWritten = false;

		if (models.size() > 0) {
			// TODO: ROIs.
			if (argsPartiallyWritten) { fragment += ", "; }
			if (verbose) { fragment += "path="; }
			if (models.size() > 1) {
				fragment += "[";
				Boolean listPartiallyWritten = false;
				for (IScanPathModel model : models) {
					if (listPartiallyWritten) { fragment += ", "; }
					fragment += stringifyModelWithGracefulFailure(model, verbose);
					listPartiallyWritten = true;
				}
				fragment += "]";
			} else {
				fragment += stringifyModelWithGracefulFailure(models.iterator().next(), verbose);
			}
			argsPartiallyWritten = true;
		}

		// TODO: Detectors.

		fragment += ")";

		return fragment;
	}

	final private static String stringifyModelWithGracefulFailure(IScanPathModel model, Boolean verbose) {
		try {
			return ModelStringifier.stringify(model, verbose);
		} catch (StringificationNotImplementedException e) {
			return "???";
		}
	}
}
