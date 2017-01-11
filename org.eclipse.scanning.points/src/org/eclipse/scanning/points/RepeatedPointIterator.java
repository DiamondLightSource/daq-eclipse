package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.RepeatedPointModel;

public class RepeatedPointIterator implements Iterator<IPosition> {

	private RepeatedPointModel   model;
	private int count = 0;
	
	public RepeatedPointIterator(RepeatedPointGenerator gen) {
		this.model= gen.getModel();
	}

	@Override
	public boolean hasNext() {
		return count<model.getCount();
	}
	
	private static boolean countSleeps;
	private static int     sleepCount;
	   /**
     * For testing we may count the sleeps of an interation
     * @param b
     */
	public static void _setCountSleeps(boolean count) {
		countSleeps = count;
		sleepCount  = 0;
	}

	public static int _getSleepCount() {
		return sleepCount;
	}

	@Override
	public IPosition next() {
		
		if (model.getSleep()>0) {
			try {
				Thread.sleep(model.getSleep());
				if (countSleeps) sleepCount++;
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		Scalar<Double> point = new Scalar<>(model.getName(), count, model.getValue());
		count++;
		return point;
	}

	public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
