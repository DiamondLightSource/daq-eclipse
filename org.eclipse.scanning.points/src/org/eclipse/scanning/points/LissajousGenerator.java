package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.LissajousModel;

public class LissajousGenerator extends AbstractGenerator<LissajousModel> {

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new LissajousIterator(this);
	}

	@Override
	protected void validateModel() {
		if (model.getPoints() < 1) throw new ModelValidationException("Must have one or more points in model!", model, "points");
	}

}
