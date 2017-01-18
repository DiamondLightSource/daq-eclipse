package org.eclipse.scanning.command.factory;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;

class CircularROIExpresser extends PyModelExpresser<CircularROI> {

	public String pyExpress(CircularROI croi, boolean verbose) {
		// TODO Use StringBuilder
		return "circ("
				+(verbose?"origin=":"")
					+"("+croi.getCentre()[0]+", "+croi.getCentre()[1]+"), "
				+(verbose?"radius=":"")+croi.getRadius()
			+")";
	}
}
