package org.eclipse.scanning.points;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPointContainer;
import org.eclipse.scanning.api.points.models.BoundingBoxModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.LinearModel;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.PointModel;
import org.eclipse.scanning.api.points.models.RasterModel;

public class GeneratorServiceImpl implements IGeneratorService {
	
	private static final Map<Class<?>, Class<? extends IGenerator<?>>> generators;
	
	// Use a factory pattern to register the types.
	// This pattern can always be replaced by extension points
	// to allow point generators to be dynamically registered. 
	static {
		Map<Class<?>, Class<? extends IGenerator<?>>> tmp = new HashMap<>(7);
		tmp.put(GridModel.class,             GridGenerator.class);
		tmp.put(LissajousModel.class,        LissajousGenerator.class);
		tmp.put(OneDEqualSpacingModel.class, OneDEqualSpacingGenerator.class);
		tmp.put(OneDStepModel.class,         OneDStepGenerator.class);
		tmp.put(RasterModel.class,           RasterGenerator.class);
		
		generators = Collections.unmodifiableMap(tmp);
	}

	@Override
	public <T,R> IGenerator<T> createGenerator(T model, R region) throws GeneratorException {
		try {
			IGenerator<T> gen = (IGenerator<T>)generators.get(model.getClass()).newInstance();
			 
			if (region != null && PointModel.class.isAssignableFrom(model.getClass())) {
			    synchModel((PointModel)model, (IROI)region);
			    gen.setContainer(wrap(region));
			}
			gen.setModel(model);
			return gen;
			
		} catch (GeneratorException g) {
			throw g;
		} catch (Exception ne) {
			throw new GeneratorException("Cannot make a new generator for "+model.getClass().getName(), ne);
		}
	}

	private IPointContainer<IROI> wrap(Object region) throws GeneratorException {
		if (!(region instanceof IROI)) throw new GeneratorException("Currently only type "+IROI.class.getName()+" can be wrapped!");
		
		final IROI roi = (IROI)region;
		return new IPointContainer<IROI>() {
			@Override
			public boolean containsPoint(double x, double y) {
				return roi.containsPoint(x, y);
			}

			@Override
			public IROI getROI() {
				return roi;
			}
		};
	}
	
	private <T extends PointModel> void synchModel(T model, IROI roi) throws GeneratorException {
		
		if (model.isLock()) return; // They locked the bounding rectangle.
		
		if (model instanceof BoundingBoxModel) {
			
			BoundingBoxModel box = (BoundingBoxModel)model;
			IRectangularROI rect = roi.getBounds();
			box.setX(rect.getPoint()[0]);
			box.setY(rect.getPoint()[1]);
			box.setxLength(rect.getLength(0));
			box.setyLength(rect.getLength(1));
	
			if (roi instanceof IRectangularROI) {
				box.setAngle(((IRectangularROI)roi).getAngle());
				box.setParentRectangle(true);
			}
			return;
			
		} else if (model instanceof LinearModel) {
			
			LinearModel line = (LinearModel)model;
            LinearROI   lroi = (LinearROI)roi;
            line.setX(lroi.getPoint()[0]);
            line.setY(lroi.getPoint()[1]);
            line.setAngle(lroi.getAngle());
			return;
		}
		
		throw new GeneratorException("Cannot deal with model "+model.getClass());
	}
}
