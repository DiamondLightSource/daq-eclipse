package org.eclipse.scanning.test.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.Point;

public class TestGenerator extends AbstractGenerator<TestGeneratorModel, Point> {

	@Override
	public Iterator<Point> iterator() {
		throw new IllegalArgumentException("Not designed to be run, just to test extension point for when people want to load by extension!");
	}

}
