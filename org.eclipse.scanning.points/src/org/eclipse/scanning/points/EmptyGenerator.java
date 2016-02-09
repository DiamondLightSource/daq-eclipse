package org.eclipse.scanning.points;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.EmptyPosition;
import org.eclipse.scanning.api.points.models.EmptyModel;

public class EmptyGenerator extends AbstractGenerator<EmptyModel,EmptyPosition> {

	@Override
	public Iterator<EmptyPosition> iterator() {
		return Arrays.asList(new EmptyPosition()).iterator();
	}

}
