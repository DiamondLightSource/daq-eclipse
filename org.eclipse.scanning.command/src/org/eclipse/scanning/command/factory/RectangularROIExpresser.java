package org.eclipse.scanning.command.factory;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

class RectangularROIExpresser extends PyModelExpresser<RectangularROI> {

	public String pyExpress(RectangularROI rroi, boolean verbose) {
		// TODO Use StringBuilder
		return "rect("
				+(verbose?"origin=":"")+"("
					+rroi.getPointX()+", "+rroi.getPointY()
				+"), "
				+(verbose?"size=":"")+"("
					+rroi.getLengths()[0]+", "+rroi.getLengths()[1]
				+")"
			+")";
	}

}
