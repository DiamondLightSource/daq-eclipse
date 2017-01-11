package org.eclipse.scanning.command.factory;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.command.PyExpressionNotImplementedException;

class ROICollectionExpresser extends PyModelExpresser<Collection<IROI>> {

	final String pyExpress(Collection<IROI> rois, boolean verbose) throws Exception {

		if (rois.size() == 0)
			throw new PyExpressionNotImplementedException();

		else if (rois.size() == 1 && !verbose)
			return factory.pyExpress(rois.iterator().next(), verbose);

		else {
			String fragment = "[";
			boolean listPartiallyWritten = false;

			for (IROI roi : rois) {
				if (listPartiallyWritten) fragment += ", ";
				fragment += factory.pyExpress(roi, verbose);
				listPartiallyWritten |= true;
			}

			fragment += "]";
			return fragment;
		}
	}
}
