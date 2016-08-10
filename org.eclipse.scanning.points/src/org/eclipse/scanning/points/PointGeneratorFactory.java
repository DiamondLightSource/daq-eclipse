package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CollatedStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.EmptyModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IBoundingLineModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.RandomOffsetGridModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.points.models.StepModel;

public class PointGeneratorFactory implements IPointGeneratorService {
		
	@SuppressWarnings("rawtypes")
	private static final Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator>> generators;
	private static final Map<String,   GeneratorInfo>                                           info;
	
	// Use a factory pattern to register the types.
	// This pattern can always be replaced by extension points
	// to allow point generators to be dynamically registered. 
	static {
		System.out.println("Starting generator service");
		@SuppressWarnings("rawtypes")
		Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator>> gens = new HashMap<>(7);
		// NOTE Repeated generators are currently not allowed. Will not break the service
		// (models class keys are different) but causes ambiguity in the GUI when it creates a
		// generator for a model.
		gens.put(StepModel.class,             StepGenerator.class);
		gens.put(CollatedStepModel.class,     CollatedStepGenerator.class);
		gens.put(ArrayModel.class,            ArrayGenerator.class);
		gens.put(GridModel.class,             GridGenerator.class);
		gens.put(OneDEqualSpacingModel.class, OneDEqualSpacingGenerator.class);
		gens.put(OneDStepModel.class,         OneDStepGenerator.class);
		gens.put(RasterModel.class,           RasterGenerator.class);
		gens.put(EmptyModel.class,            EmptyGenerator.class);
		gens.put(RandomOffsetGridModel.class, RandomOffsetGridGenerator.class);
		gens.put(SpiralModel.class,           SpiralGenerator.class);
		
		Map<String,   GeneratorInfo> tinfo = new TreeMap<>();
		fillStaticGeneratorInfo(gens, tinfo);

		try { // Extensions must provide an id, it is a compulsory field.
			readExtensions(gens, tinfo);
		} catch (CoreException e) {
			e.printStackTrace(); // Static block, intentionally do not use logging.
		}
		
		generators = Collections.unmodifiableMap(gens);
		info       = Collections.unmodifiableMap(tinfo);
	}
	
	public Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator>> getGenerators() {
		return generators;
	}

	@Override
	public <T extends IScanPathModel, R> IPointGenerator<T> createGenerator(T model, Collection<R> regions) throws GeneratorException {
		try {
			IPointGenerator<T> gen = (IPointGenerator<T>)generators.get(model.getClass()).newInstance();
			if (regions != null) {
				for (R region : regions) {
					synchModel(model, (IROI) region);
					break; // to preserve old behaviour of only using first region
					// TODO fix this by removing break statement and correctly handling multiple regions
				}
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

	private static void fillStaticGeneratorInfo(Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator>> gens, Map<String,   GeneratorInfo> ids) {

		for (Class<? extends IScanPathModel> modelClass : gens.keySet()) {
			try {
				final GeneratorInfo info = new GeneratorInfo();
				info.setModelClass(modelClass);
				info.setGeneratorClass(gens.get(modelClass));
				ids.put(info.getGeneratorClass().newInstance().getId(), info);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			} 
		}
	}

	private static void readExtensions(Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator>> gens,
			                           Map<String,   GeneratorInfo> tids) throws CoreException {
		
		if (Platform.getExtensionRegistry()!=null) {
			final IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.scanning.api.generator");
			for (IConfigurationElement e : eles) {
				final IPointGenerator    gen = (IPointGenerator)e.createExecutableExtension("class");
				final IScanPathModel     mod = (IScanPathModel)e.createExecutableExtension("model");
				
				gens.put(mod.getClass(), gen.getClass());
				
				final GeneratorInfo info = new GeneratorInfo();
				info.setModelClass(mod.getClass());
				info.setGeneratorClass(gen.getClass());
				info.setLabel(e.getAttribute("label"));
				info.setDescription(e.getAttribute("description"));
				
				String id = e.getAttribute("id");
				tids.put(id, info);
			}
		}
	}

	private List<IPointContainer<?>> wrap(Collection<?> regions) throws GeneratorException {
		
		if (regions==null || regions.isEmpty()) return null;
		
		List<IPointContainer<?>> ret = new ArrayList<>();
		for (Object region : regions) {
			if (!(region instanceof IROI)) throw new GeneratorException("Currently only type "+IROI.class.getName()+" can be wrapped!");
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
			box.setFastAxisStart(rect.getPoint()[0]);
			box.setSlowAxisStart(rect.getPoint()[1]);
			box.setFastAxisLength(rect.getLength(0));
			box.setSlowAxisLength(rect.getLength(1));
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
	public IPointGenerator<?> createCompoundGenerator(IPointGenerator<?>... generators) throws GeneratorException {
		if (generators.length==1) return generators[0];
		return new CompoundGenerator(generators);
	}

	@Override
	public Collection<String> getRegisteredGenerators() {
		return info.keySet();
	}

	@Override
	public <T extends IScanPathModel> IPointGenerator<T> createGenerator(String id) throws GeneratorException {
		try {
			GeneratorInfo ginfo = info.get(id);
			IPointGenerator<T> gen = ginfo.getGeneratorClass().newInstance();
			T                  mod = (T)ginfo.getModelClass().newInstance();
			gen.setModel(mod);
			if (ginfo.getLabel()!=null) gen.setLabel(ginfo.getLabel());
			if (ginfo.getDescription()!=null) gen.setDescription(ginfo.getDescription());
			return gen;
			
		} catch (IllegalAccessException | InstantiationException ne) {
			throw new GeneratorException(ne);
		}
	}

	@Override
	public IPointGenerator<?> createCompoundGenerator(CompoundModel cmodel) throws GeneratorException {
		
		IPointGenerator<?>[] gens = new IPointGenerator<?>[cmodel.getModels().size()];
		int index = 0;
		for (IScanPathModel model : cmodel.getModels()) {
			Collection<?> regions = findRegions(cmodel, model);
			gens[index] = createGenerator(model, regions);
			index++;
		}
		return createCompoundGenerator(gens);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> Collection<R> findRegions(CompoundModel cmodel, IScanPathModel model) throws GeneratorException {
		
		if (cmodel.getRegions()==null) return null;
		
        final Collection<R> regions = new LinkedHashSet<R>(); // Order should not be important but some tests assume it
		final Collection<String> names = model.getScannableNames();
		for (ScanRegion<?> region : cmodel.getRegions()) {
			if (region.getScannables().containsAll(names)) {
				regions.add((R)region.getRoi());
			}
		}
		return regions;
	}
}
