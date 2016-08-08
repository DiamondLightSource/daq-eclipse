package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
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
			throw new PointsValidationException("The model must be a RandomOffsetGridModel", model, "offset"); // TODO Not really an offset problem.
		}
	}

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		GridIterator gridIterator = (GridIterator) super.iteratorFromValidModel();
		double percentageOffset = ((RandomOffsetGridModel) model).getOffset();
		double standardDeviation = gridIterator.getXStep() * percentageOffset / 100.0;
		return new RandomOffsetDecorator(gridIterator, standardDeviation);
	}

}