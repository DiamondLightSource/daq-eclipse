package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.RandomOffsetGridModel;

public class RandomOffsetGridGenerator extends GridGenerator {
	
	RandomOffsetGridGenerator() {
		setLabel("Random Offset Grid");
		setDescription("Creates a grid scan (a scan of x and y) with random offsets applied to each point.\nThe scan supports bidirectional or 'snake' mode.");
		setIconPath("icons/scanner--grid.png"); // This icon exists in the rendering bundle 
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (!(model instanceof RandomOffsetGridModel)) {
			throw new ModelValidationException("The model must be a RandomOffsetGridModel", model, "offset"); // TODO Not really an offset problem.
		}
	}

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new GridIterator(this);
	}

}