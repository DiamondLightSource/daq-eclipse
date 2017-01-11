package org.eclipse.scanning.api.scan;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IDeviceDependentIterable;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;

/**
 * 
 * Most scans are static and therefore they can have their shape and size
 * discovered. Those scans which truely are iterators and on the fly decide
 * the next position, can only have their shapes and sizes estimated.
 * 
 * Shape is more expensive to estimate that size. For 10 million points size
 * is ~100ms depending on iterator type. Shape will take longer as more floating 
 * point operations are required.
 * 
 * This class is an estimator and not a data holder. Please use ScanInformation
 * to hold data to be sent around.
 * 
 * @author Matthew Gerring
 *
 */
public class ScanEstimator {

	/**
	 * Size, number of points in scan
	 */
	private final int   size;
	
	/**
	 * The rank of the scan
	 */
	private final int rank;
	
	/**
	 * Estimated time of scan
	 */
	private final long  scanTime;

	/**
	 * 
	 */
	private long  timePerPoint = -1;

	/**
	 * 
	 */
	private final IPointGenerator<?> generator;
	
	/**
	 * 
	 */
	private int[] shape;
	
	/**
	 * 
	 * @param pservice
	 * @param bean
	 */
	public ScanEstimator(IPointGeneratorService pservice, ScanRequest<?> request) throws GeneratorException{
		this(pservice, request, 0);
	}

	/**
	 * 
	 * @param pservice
	 * @param bean
	 * @param timePerPoint ms
	 * @throws GeneratorException 
	 */
	public ScanEstimator(IPointGeneratorService pservice, ScanRequest<?> request, long timePerPoint) throws GeneratorException {
		this(pservice.createCompoundGenerator(request.getCompoundModel()), request.getDetectors(), timePerPoint);
	}
	/**
	 * 
	 * @param pservice
	 * @param request
	 * @param timePerPoint
	 * @throws GeneratorException
	 */
	public ScanEstimator(IPointGenerator<?> gen, Map<String, Object> detectors, long timePerPoint) throws GeneratorException {

		
		this.generator = gen;
		
		// TODO FIXME If some detectors are malcolm, they may have a wait time.
		// If some are malcolm we may wish to ignore the input point time from the user
		// in favour of the malcolm time per point or maybe the device tells us how long it will take?
		if (detectors!=null) for (Object model : detectors.values()) {
			if (model instanceof IDetectorModel) {
				timePerPoint = Math.max(timePerPoint, Math.round(1000*((IDetectorModel)model).getExposureTime()));
			}
		}
		this.size          = getEstimatedSize(gen);
		this.rank          = gen.iterator().next().getScanRank(); // The rank of the scan is constant
		this.timePerPoint  = timePerPoint;
		this.scanTime      = size*timePerPoint;
	}

	private int getEstimatedSize(IPointGenerator<?> gen) throws GeneratorException {
		
		int size=0;
		if (gen instanceof IDeviceDependentIterable) {
			size = ((IDeviceDependentIterable)gen).size();
			
		} else {
			size = ((IPointGenerator<?>)gen).size();
			
		} 
		return size;   		
	}

	public int getSize() {
		return size;
	}

	public long getTimePerPoint() {
		return timePerPoint;
	}

	public void setTimePerPoint(long timePerPoint) {
		this.timePerPoint = timePerPoint;
	}

	public long getScanTime() {
		return scanTime;
	}

	public int getRank() {
		return rank;
	}
	
	/**
	 * The estimated scan shape. Clue is in the name, ScanEstimator
	 * @return
	 */
	public int[] getShape() {
		
		if (shape!=null) return shape;
	
		Iterator<IPosition> iterator = generator.iterator();
		IPosition last  = null;
		while(iterator.hasNext()) last = iterator.next(); // Could be large...
		
		this.shape = new int[getRank()];
		for (int i = 0; i < shape.length; i++) {
			shape[i] = last.getIndex(i)+1;
		}
		return shape;
	}
}
