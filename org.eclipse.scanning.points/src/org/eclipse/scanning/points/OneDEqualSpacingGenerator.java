package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;

public class OneDEqualSpacingGenerator extends AbstractGenerator<OneDEqualSpacingModel> {

	OneDEqualSpacingGenerator() {
		setLabel("Line Equal Spacing");
		setDescription("Creates a line scan along a line defined in two dimensions.");
		setIconPath("icons/scanner--line.png"); // This icon exists in the rendering bundle 
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (model.getPoints() < 1) throw new ModelValidationException("Must have one or more points in model!", model, "points");
		if (model.getxName()==null) throw new ModelValidationException("The model must have a x axis!\nIt is the motor name used for this axis.", model, "xName");
		if (model.getyName()==null) throw new ModelValidationException("The model must have a y axis!\nIt is the motor name used for this axis.", model, "yName");
	}

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new LineIterator(this);
	}
}
