package org.eclipse.scanning.command.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.RepeatedPointModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.command.PyExpressionNotImplementedException;

/**
 * 
 * This is a factory for expressing various Java objects in mscan command syntax.
 * 
 * To add a new point generation model to mscan:
 * 1. Add a new expresser for your model here.
 * 2. Edit the test PyExpresserTest to have a test for expressing your model.
 * 3. Edit mapping_scan_commands.py to have a Jython method which matches your expresser string.
 * 4. Edit the test ScanRequestCreationTest to run a test with your model
 * 
 * @author Matthew Gerring
 *
 */
public class PyExpressionFactory {

	private static Map<Class<?>, PyModelExpresser<?>> expressers;
	static {
		Map<Class<?>, PyModelExpresser<?>> exp = new HashMap<Class<?>, PyModelExpresser<?>>(7);
		exp.put(StepModel.class,           new StepModelExpresser());
		exp.put(GridModel.class,           new GridModelExpresser());
		exp.put(RasterModel.class,         new RasterModelExpresser());
		exp.put(ArrayModel.class,          new ArrayModelExpresser());
		exp.put(RepeatedPointModel.class,  new RepeatedPointExpresser());
		
		exp.put(Collection.class,     new ROICollectionExpresser());
		exp.put(List.class,           new ROICollectionExpresser());
		exp.put(CircularROI.class,    new CircularROIExpresser());
		exp.put(RectangularROI.class, new RectangularROIExpresser());
		exp.put(PolygonalROI.class,   new PolygonalROIExpresser());
		
		exp.put(ScanRequest.class,    new ScanRequestExpresser());
		
		expressers = exp;
	}
	
	public <T> String pyExpress(T model, boolean verbose) throws Exception {
		final PyModelExpresser<T> expresser = getExpresser(model);
		return expresser.pyExpress(model, verbose);
	}

	
	@SuppressWarnings("unchecked")
	private <T> PyModelExpresser<T> getExpresser(T model) throws PyExpressionNotImplementedException {
		
		PyModelExpresser<T> expresser=null;
		
		if (expressers.containsKey(model.getClass())) {
			expresser = (PyModelExpresser<T>)expressers.get(model.getClass());
		} else {
			Class[] classes = model.getClass().getInterfaces();
			for (Class class1 : classes) {
				if (expressers.containsKey(class1)) {
					expresser = (PyModelExpresser<T>)expressers.get(class1);
					break;
				}
			}
		}
		
		if (expresser==null) throw new PyExpressionNotImplementedException("The model '"+model.getClass()+"' does not have a python expresser!");
        
		expresser.setFactory(this);
        return expresser;
	}


	public <T> String pyExpress(T model, Collection<IROI> rois, boolean verbose) throws Exception {
		final PyModelExpresser<T> expresser = getExpresser(model);
		return expresser.pyExpress(model, rois, verbose);
	}
}
