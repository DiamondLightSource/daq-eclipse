package org.eclipse.scanning.points.validation;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.BoundingBox;

public class BoundingBoxValidator implements IValidator<BoundingBox> {
	
	public void validate(BoundingBox model) throws Exception {
        if (model.getFastAxisLength()==0)  throw new PointsValidationException("The length must not be 0!", model, "fastAxisLength");
        if (model.getSlowAxisLength()==0)  throw new PointsValidationException("The length must not be 0!", model, "slowAxisLength");
	}

}
