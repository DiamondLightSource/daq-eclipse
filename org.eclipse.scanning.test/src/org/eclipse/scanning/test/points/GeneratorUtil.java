/**
 * 
 */
package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.scan.ScanEstimator;

/**
 * @author Matthew Gerring
 *
 */
class GeneratorUtil {

	/**
	 * Checks the points list vs the iterator
	 * @param gen
	 * @throws Exception 
	 */
	public static void testGeneratorPoints(IPointGenerator<?,?> gen, int... expectedShape) throws Exception {
		
		final List ponts = gen.createPoints();
		final List its   = new ArrayList<>(gen.size());
		final Iterator it = gen.iterator();
		while(it.hasNext()) its.add(it.next());
		
		IPosition[] pnts1 = array(ponts);
		IPosition[] pnts2 = array(its);
		
		if (pnts2.length!=pnts1.length) throw new Exception("Not the same size! Iterator size is "+its.size()+" full list size is "+ponts.size());
        for (int i = 0; i < pnts1.length; i++) {
			if (!pnts1[i].equals(pnts2[i])) {
				throw new Exception(pnts1[i]+" does not equal "+pnts2[i]);
			}
		}
		
		// Check the estimator. In this case it is not doing anything
		// that we don't already know, so we can test it.
		Iterable<Point> iterable = (Iterable<Point>)gen;
		final ScanEstimator estimator = new ScanEstimator(iterable, expectedShape!=null&&expectedShape.length>0);
		
		if (ponts.size()!=estimator.getSize()) throw new Exception("Different size from shape estimator!");

		if (expectedShape!=null && expectedShape.length>0) {
		   if (!Arrays.equals(expectedShape, estimator.getShape())) throw new Exception("Different shape from shape estimator! Expected "+Arrays.toString(expectedShape)+" but was "+Arrays.toString(estimator.getShape()));
		}
 	}

	private static IPosition[] array(List<IPosition> p) {
		return p.toArray(new IPosition[p.size()]);
	}
}
