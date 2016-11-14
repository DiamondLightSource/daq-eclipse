package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.LissajousModel;

public class LissajousGenerator extends AbstractGenerator<LissajousModel> {

	public LissajousGenerator() {
		setLabel("Lissajous Curve");
		setDescription("Creates a lissajous curve inside a bounding box.");
		setIconPath("icons/scanner--lissajous.png"); // This icon exists in the rendering bundle 
	}

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new LissajousIterator(this);
	}

	@Override
	protected void validateModel() {
		if (model.getPoints() < 1) throw new ModelValidationException("Must have one or more points in model!", model, "points");
	}

}
