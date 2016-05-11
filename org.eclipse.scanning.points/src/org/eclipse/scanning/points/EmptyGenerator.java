package org.eclipse.scanning.points;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.EmptyPosition;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.EmptyModel;

public class EmptyGenerator extends AbstractGenerator<EmptyModel> {

	EmptyGenerator() {
		setLabel("Empty");
		setDescription("Empty generator used when wrapping malcolm scans with no CPU steps.");
		setVisible(false);
	}

	@Override
	protected void validateModel() {
		// Nothing to validate!
	}

	@Override
	protected Iterator<IPosition> iteratorFromValidModel() {
		return Arrays.asList((IPosition)new EmptyPosition()).iterator();
	}
	
	// Users to not edit the EmptyGenerator
	public boolean isVisible() {
		return false;
	}

}
