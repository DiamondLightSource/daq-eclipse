package org.eclipse.scanning.command.factory;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;

class PolygonalROIExpresser extends PyModelExpresser<PolygonalROI> {

	public String pyExpress(PolygonalROI proi, boolean verbose) {
		// TODO Use StringBuilder
		String fragment = "poly(";

		boolean pointListPartiallyWritten = false;
		for (IROI p : proi.getPoints()) {
			if (pointListPartiallyWritten) fragment += ", ";
			fragment += "("+((PointROI) p).getPointX()+", "+((PointROI) p).getPointY()+")";
		}

		fragment += ")";
		return fragment;
	}

}
