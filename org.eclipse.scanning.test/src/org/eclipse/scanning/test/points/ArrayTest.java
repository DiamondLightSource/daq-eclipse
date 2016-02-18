package org.eclipse.scanning.test.points;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.junit.Before;
import org.junit.Test;


public class ArrayTest {

	private static final String AXIS_NAME = "axis_name";
	private static final double[] POSITIONS = new double[] { -1000.0, -2.4, 0.0, 0.0, 0.1, 1.0, 200.0, 1.054e56 };

	private Iterable<IPosition> positionIterable;


	@Before
	public void before() throws Exception {

		ArrayModel model = new ArrayModel();
		model.setName(AXIS_NAME);
		model.setPositions(POSITIONS);

		positionIterable = new PointGeneratorFactory().createGenerator(model);
	}

	@Test
	public void testPositions() throws Exception {
		int index = 0;
		for (IPosition position : positionIterable) {
			assertThat(position, is(instanceOf(Scalar.class)));
			assertThat(((Scalar) position).getIndex(AXIS_NAME), is(equalTo(index)));
			assertThat(((Scalar) position).get(AXIS_NAME), is(equalTo(POSITIONS[index])));
			index++;
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void testEndOfIteration() throws Exception {
		Iterator<IPosition> iterator = positionIterable.iterator();
		while (iterator.hasNext()) {
			assertThat(iterator.hasNext(), is(true));
			iterator.next();
		}
		assertThat(iterator.hasNext(), is(false));
		iterator.next();
	}
}
