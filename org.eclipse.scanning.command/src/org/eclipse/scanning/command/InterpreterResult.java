package org.eclipse.scanning.command;

import java.util.List;

import org.eclipse.scanning.api.points.models.AbstractPointsModel;


public class InterpreterResult {

	public List<AbstractPointsModel> pmodels;
	public String detector;
	public Double exposure;

	public InterpreterResult(List<AbstractPointsModel> pmodels, String detector, Double exposure) {
		this.pmodels = pmodels;  // pmodels.size() > 1 indicates a compound scan.
		this.detector = detector;
		this.exposure = exposure;
	}

}
