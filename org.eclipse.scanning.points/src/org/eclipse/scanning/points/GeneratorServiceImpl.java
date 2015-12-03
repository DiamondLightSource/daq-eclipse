package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPointContainer;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IModelWithBoundingLine;
import org.eclipse.scanning.api.points.models.IPathModelWithBoundingBox;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.StepModel;

public class GeneratorServiceImpl implements IGeneratorService {
	
	private static final Map<Class<?>, Class<? extends IGenerator<?,?>>> generators;
	
	// Use a factory pattern to register the types.
	// This pattern can always be replaced by extension points
	// to allow point generators to be dynamically registered. 
	static {
		System.out.println("Starting generator service");
		Map<Class<?>, Class<? extends IGenerator<?,?>>> tmp = new HashMap<>(7);
		tmp.put(StepModel.class,             StepGenerator.class);
		tmp.put(GridModel.class,             GridGenerator.class);
		tmp.put(LissajousModel.class,        LissajousGenerator.class);
		tmp.put(OneDEqualSpacingModel.class, OneDEqualSpacingGenerator.class);
		tmp.put(OneDStepModel.class,         OneDStepGenerator.class);
		tmp.put(RasterModel.class,           RasterGenerator.class);
		
		// TODO We should support beamline specific generators here one day.
		// This step is very similar to how loaders were added to DAWN.
		// Lots of default loaders added and then allowing loaders from
		// extension point as well. For mapping we should add all the 
		// point generators here so extendable generators should be done
		// at a later step.
		
		generators = Collections.unmodifiableMap(tmp);
	}

	@Override
	public <T,R,P> IGenerator<T,P> createGenerator(T model, R... regions) throws GeneratorException {
		try {
			IGenerator<T,P> gen = (IGenerator<T,P>)generators.get(model.getClass()).newInstance();
			
			// FIXME need to generate a bounding box covering all regions, not just the first
			if (regions != null && regions.length > 0) {
				synchModel(model, (IROI) regions[0]);
				gen.setContainers(wrap(regions));
			}
			gen.setModel(model);
			return gen;
			
		} catch (GeneratorException g) {
			throw g;
		} catch (Exception ne) {
			throw new GeneratorException("Cannot make a new generator for "+model.getClass().getName(), ne);
		}
	}

	private List<IPointContainer<?>> wrap(Object... regions) throws GeneratorException {
		
		if (regions==null || regions.length<1) return null;
		if (!(regions[0] instanceof IROI)) throw new GeneratorException("Currently only type "+IROI.class.getName()+" can be wrapped!");
		
		List<IPointContainer<?>> ret = new ArrayList<>();
		for (Object region : regions) {
			final IROI roi = (IROI)region;
			IPointContainer<IROI> container = new IPointContainer<IROI>() {
				@Override
				public boolean containsPoint(double x, double y) {
					return roi.containsPoint(x, y);
				}

				@Override
				public IROI getROI() {
					return roi;
				}
			};
			ret.add(container);
		}
		return ret;
	}
	
	private <T> void synchModel(T model, IROI roi) throws GeneratorException {

		if (model instanceof IPathModelWithBoundingBox) {

			BoundingBox box = new BoundingBox();
			IRectangularROI rect = roi.getBounds();
			box.setxStart(rect.getPoint()[0]);
			box.setyStart(rect.getPoint()[1]);
			box.setWidth(rect.getLength(0));
			box.setHeight(rect.getLength(1));
			((IPathModelWithBoundingBox) model).setBoundingBox(box);
//			return;

		} else if (model instanceof IModelWithBoundingLine) {

			BoundingLine line = new BoundingLine();
			LinearROI lroi = (LinearROI) roi;
			line.setxStart(lroi.getPoint()[0]);
			line.setyStart(lroi.getPoint()[1]);
			line.setAngle(lroi.getAngle());
			((IModelWithBoundingLine) model).setBoundingLine(line);
//			return;
		}

		//throw new GeneratorException("Cannot deal with model "+model.getClass());
	}

	@Override
	public IGenerator<?, IPosition> createCompoundGenerator(IGenerator<?,? extends IPosition>... generators) throws GeneratorException {
		return new CompoundGenerator(generators);
	}
}
