package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointContainer;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.EmptyModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IBoundingLineModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.StepModel;

public class PointGeneratorFactory implements IPointGeneratorService {
	
	// TODO FIXME All generators must also specify the indices of the location
	// of each value as well as the abolute motor position for correct NeXus writing.
	
	private static final Map<Class<?>, Class<? extends IPointGenerator>> generators;
	private static final Map<String,   Class<? extends IPointGenerator>> ids;
	
	// Use a factory pattern to register the types.
	// This pattern can always be replaced by extension points
	// to allow point generators to be dynamically registered. 
	static {
		System.out.println("Starting generator service");
		Map<Class<?>, Class<? extends IPointGenerator>> gens = new HashMap<>(7);
		gens.put(StepModel.class,             StepGenerator.class);
		gens.put(GridModel.class,             GridGenerator.class);
		gens.put(OneDEqualSpacingModel.class, OneDEqualSpacingGenerator.class);
		gens.put(OneDStepModel.class,         OneDStepGenerator.class);
		gens.put(RasterModel.class,           RasterGenerator.class);
		gens.put(EmptyModel.class,            EmptyGenerator.class);
		
		Map<String,   Class<? extends IPointGenerator>> tids = createIds(gens);

		try { // Extensions must provide an id, it is a compulsory field.
			readExtensions(gens, tids);
		} catch (CoreException e) {
			e.printStackTrace(); // Static block, intentionally do not use logging.
		}
		
		generators = Collections.unmodifiableMap(gens);
		ids        = Collections.unmodifiableMap(tids);
	}

	@Override
	public <T,R,P> IPointGenerator<T,P> createGenerator(T model, R... regions) throws GeneratorException {
		try {
			IPointGenerator<T,P> gen = (IPointGenerator<T,P>)generators.get(model.getClass()).newInstance();
			
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

	private static Map<String, Class<? extends IPointGenerator>> createIds(Map<Class<?>, Class<? extends IPointGenerator>> gens) {
		TreeMap<String, Class<? extends IPointGenerator>> ids = new TreeMap<>();
		for (Class<? extends IPointGenerator> clazz : gens.values()) {
			try {
				ids.put(clazz.newInstance().getId(), clazz);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} 
		}
		return ids;
	}

	private static void readExtensions(Map<Class<?>, Class<? extends IPointGenerator>> gens,
			                           Map<String,   Class<? extends IPointGenerator>> tids) throws CoreException {
		
		if (Platform.getExtensionRegistry()!=null) {
			final IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.scanning.api.generator");
			for (IConfigurationElement e : eles) {
				final IPointGenerator gen = (IPointGenerator)e.createExecutableExtension("class");
				final Object     mod = e.createExecutableExtension("model");
				gens.put(mod.getClass(), gen.getClass());
				tids.put(e.getAttribute("id"), gen.getClass());
			}
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

		if (model instanceof IBoundingBoxModel) {

			BoundingBox box = new BoundingBox();
			IRectangularROI rect = roi.getBounds();
			box.setxStart(rect.getPoint()[0]);
			box.setyStart(rect.getPoint()[1]);
			box.setWidth(rect.getLength(0));
			box.setHeight(rect.getLength(1));
			((IBoundingBoxModel) model).setBoundingBox(box);
//			return;

		} else if (model instanceof IBoundingLineModel) {

			BoundingLine line = new BoundingLine();
			LinearROI lroi = (LinearROI) roi;
			line.setxStart(lroi.getPoint()[0]);
			line.setyStart(lroi.getPoint()[1]);
			line.setLength(lroi.getLength());
			line.setAngle(lroi.getAngle());
			((IBoundingLineModel) model).setBoundingLine(line);
//			return;
		}

		//throw new GeneratorException("Cannot deal with model "+model.getClass());
	}

	@Override
	public IPointGenerator<?, IPosition> createCompoundGenerator(IPointGenerator<?,? extends IPosition>... generators) throws GeneratorException {
		return new CompoundGenerator(generators);
	}

	@Override
	public Collection<String> getRegisteredGenerators() {
		return ids.keySet();
	}

	@Override
	public <T, P> IPointGenerator<T, P> createGenerator(String id) throws GeneratorException {
		try {
		    return ids.get(id).newInstance();
		} catch (IllegalAccessException | InstantiationException ne) {
			throw new GeneratorException(ne);
		}
	}
}
