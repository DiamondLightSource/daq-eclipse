package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * 
 * This class takes a position iterator and it is a compound generator,
 * attempts to remove the inner scans which subscan devices such as Malcolm will take care of
 * from the compound generator and return only the outer scans.
 * 
 * @author Matthew Gerring
 *
 */
public class SubscanModerator {
	
	private Iterable<IPosition>    positionIterable;
	private IPointGeneratorService gservice;

	public SubscanModerator(Iterable<IPosition> generator, List<IRunnableDevice<?>> detectors, IPointGeneratorService gservice) throws ScanningException {
		this.gservice = gservice;
		try {
			moderate(generator, detectors);
		} catch (MalcolmDeviceException | GeneratorException e) {
			throw new ScanningException("Unable to moderate scan for malcolm devices!", e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void moderate(Iterable<IPosition> generator, List<IRunnableDevice<?>> detectors) throws GeneratorException, ScanningException {
		
		if (detectors==null || detectors.isEmpty()) {
			positionIterable = generator;
			return;
		}
		if (!(generator instanceof IPointGenerator<?>)) {
			positionIterable = generator;
			return;
		}
		IPointGenerator<?> gen = (IPointGenerator<?>)generator;
		if (!(gen.getModel() instanceof CompoundModel)) {
			positionIterable = gen;
			return;
		}
		
		List<String> axes = getAxes(detectors);
		if (axes==null || axes.isEmpty()) {
			positionIterable = gen;
			return;
		}
		
		// We need a compound model to moderate this stuff
		CompoundModel cmodel = ((CompoundModel)gen.getModel()).clone();
		List<Object> orig   = cmodel.getModels();
		if (orig.isEmpty()) throw new ScanningException("No models are provided in the compound model!");
		
		List<Object> models = new ArrayList<>();
		
		boolean reachedOuterScan = false;
		for (int i = orig.size()-1; i > -1; i--) {
			Object model = orig.get(i);
			if (!reachedOuterScan) {
				IPointGenerator<?> g = gservice.createGenerator(model);
				IPosition first = g.iterator().next();
				List<String> names = first.getNames();
				if (axes.containsAll(names)) {// These will be deal with by malcolm
					continue; // The device will deal with it.
				}
			}
			reachedOuterScan = true;
			models.add(0, model);
		}
		if (models.isEmpty()) {
			this.positionIterable = gservice.createGenerator(new StaticModel(1));
			return;
		}
		
		cmodel.setModels(models);
		this.positionIterable = gservice.createCompoundGenerator(cmodel);
	}

	private List<String> getAxes(List<IRunnableDevice<?>> detectors) throws MalcolmDeviceException {
		List<String> ret = new ArrayList<>();
		for (IRunnableDevice<?> device : detectors) {
			// TODO Deal with the axes of other subscan devices as they arise.
			if (device instanceof IMalcolmDevice) {
				IMalcolmDevice<?> mdevice = (IMalcolmDevice<?>)device;
				String[] axes = mdevice.getAttributeValue("axesToMove");
				if (axes!=null) ret.addAll(Arrays.asList(axes));
			}
		}
		if (ret.isEmpty()) return null;
		return ret;
	}

	public Iterable<IPosition> getPositionIterable() {
		return positionIterable;
	}

	public void setPositionIterable(Iterable<IPosition> positionIterable) {
		this.positionIterable = positionIterable;
	}


}
