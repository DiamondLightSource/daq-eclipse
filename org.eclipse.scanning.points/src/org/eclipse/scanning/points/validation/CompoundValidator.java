package org.eclipse.scanning.points.validation;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.CompoundModel;

class CompoundValidator implements IValidator<CompoundModel> {

	public void validate(CompoundModel model) throws Exception {
		
		if (model.getModels()==null || model.getModels().isEmpty()) {
			throw new ModelValidationException("There are no models defined.", model, "models");
		}
		final IValidatorService vservice = new ValidatorService();
		for (Object mod : model.getModels()) vservice.validate(mod);
	}
}
