package org.eclipse.scanning.points;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPointContainer;
import org.eclipse.scanning.api.points.models.BoundingBoxModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.LissajousModel;

public class GeneratorServiceImpl implements IGeneratorService {
	
	private static final Map<Class<?>, Class<? extends IGenerator>> generators;
	
	// Use a factory pattern to register the types.
	// This pattern can always be replaced by extension points
	// to allow point generators to be dynamically registered. 
	static {
		Map<Class<?>, Class<? extends IGenerator>> tmp = new HashMap<>(7);
		tmp.put(GridModel.class, GridGenerator.class);
		tmp.put(LissajousModel.class, LissajousGenerator.class);
		
		generators = Collections.unmodifiableMap(tmp);
	}

	@Override
	public <T> IGenerator<T> createGenerator(T model, Object region) throws GeneratorException {
		try {
			IGenerator<T> gen = generators.get(model.getClass()).newInstance();
			 
			if (region != null && BoundingBoxModel.class.isAssignableFrom(model.getClass())) {
			    synchModel((BoundingBoxModel)model, (IROI)region);
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

	private IPointContainer wrap(Object region) throws GeneratorException {
		if (!(region instanceof IROI)) throw new GeneratorException("Currently only type "+IROI.class.getName()+" can be wrapped!");
		
		final IROI roi = (IROI)region;
		return new IPointContainer() {
			@Override
			public boolean containsPoint(double x, double y) {
				return roi.containsPoint(x, y);
			}
		};
	}
	
	private <T extends BoundingBoxModel> void synchModel(T model, IROI roi) throws GeneratorException {
		
		if (model.isLock()) return; // They locked the bounding rectangle.
		IRectangularROI rect = roi.getBounds();
		model.setMinX(rect.getPoint()[0]);
		model.setMinY(rect.getPoint()[1]);
		model.setxLength(rect.getLength(0));
		model.setyLength(rect.getLength(1));

		if (roi instanceof IRectangularROI) {
			model.setAngle(((IRectangularROI)roi).getAngle());
			model.setParentRectangle(true);
		}
	}
}
