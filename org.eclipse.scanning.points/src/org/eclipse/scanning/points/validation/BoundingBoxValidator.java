package org.eclipse.scanning.points.validation;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.BoundingBox;

class BoundingBoxValidator implements IValidator<BoundingBox> {
	
	public void validate(BoundingBox model) throws Exception {
        if (model.getFastAxisLength()==0)  throw new ModelValidationException("The length must not be 0!", model, "fastAxisLength");
        if (model.getSlowAxisLength()==0)  throw new ModelValidationException("The length must not be 0!", model, "slowAxisLength");
	}

}
