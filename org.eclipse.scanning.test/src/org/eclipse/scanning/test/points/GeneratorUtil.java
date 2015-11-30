/**
 * 
 */
package org.eclipse.scanning.test.points;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;

/**
 * @author fcp94556
 *
 */
class GeneratorUtil {

	/**
	 * Checks the points list vs the iterator
	 * @param gen
	 * @throws Exception 
	 */
	public static void testGeneratorPoints(IGenerator<?,?> gen) throws Exception {
		
		final List ponts = gen.createPoints();
		final List its   = new ArrayList<>(gen.size());
		final Iterator it = gen.iterator();
		while(it.hasNext()) its.add(it.next());
		
		IPosition[] pnts1 = array(ponts);
		IPosition[] pnts2 = array(ponts);
		
		if (pnts2.length!=pnts1.length) throw new Exception("Not the same size!");
        for (int i = 0; i < pnts1.length; i++) {
			if (!pnts1[i].equals(pnts2[i])) {
				throw new Exception(pnts1[i]+" does not equal "+pnts2[i]);
			}
		}
 	}

	private static IPosition[] array(List<IPosition> p) {
		return p.toArray(new IPosition[p.size()]);
	}
}
