package org.eclipse.scanning.example.preprocess;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AbstractBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.process.AbstractPreprocessor;
import org.eclipse.scanning.api.scan.process.ProcessingException;
import org.eclipse.scanning.example.detector.MandelbrotModel;

/**
 * A preprocessor which overwrites the stage names with 'xfred' and 'yfred' in various models.
 * 
 * We are just doing various example things here to the request without purpose. Your implementation
 * should ensure that the correct models for scan path and for detector are defined. For instance a 
 * GridModel might be replaced with a malcolm detector model.
 * 
 * @author Matthew Gerring
 *
 */
public class ExamplePreprocessor extends AbstractPreprocessor {

	@Override
	public <T> ScanRequest<T> preprocess(ScanRequest<T> req) throws ProcessingException {
	
		for (IScanPathModel model : req.getModels()) {
			if (model instanceof StepModel) {
				((StepModel)model).setName("xfred");
			} if (model instanceof AbstractBoundingBoxModel) {
				((AbstractBoundingBoxModel)model).setxName("xfred");
				((AbstractBoundingBoxModel)model).setyName("yfred");
			}
		}
		
		for (String name : req.getDetectors().keySet()) {
			Object dmodel = req.getDetectors().get(name);
			if (dmodel instanceof MandelbrotModel) {
				MandelbrotModel mmodel = (MandelbrotModel)dmodel;
				mmodel.setxName("xfred");
				mmodel.setxName("yfred");
			}
		}
		
		return req;
	}

}
