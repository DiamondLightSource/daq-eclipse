package org.eclipse.scanning.command;

import org.eclipse.scanning.api.points.models.AbstractPointsModel;


public class InterpreterResult {

	public AbstractPointsModel pmodel;
	public String detector;
	public Double exposure;

	public InterpreterResult(AbstractPointsModel pmodel, String detector, Double exposure) {
		this.pmodel = pmodel;
		this.detector = detector;
		this.exposure = exposure;
	}

}
