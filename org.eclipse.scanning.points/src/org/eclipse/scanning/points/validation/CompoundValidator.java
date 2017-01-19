package org.eclipse.scanning.points.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;

class CompoundValidator implements IValidator<CompoundModel<?>> {

	public void validate(CompoundModel<?> model) throws ValidationException, InstantiationException, IllegalAccessException {
		
		if (model.getModels()==null || model.getModels().isEmpty()) {
			throw new ModelValidationException("There are no models defined.", model, "models");
		}
		
		// Each model is ok
		final IValidatorService vservice = new ValidatorService();
		for (Object mod : model.getModels()) vservice.validate(mod);
		
		// Models are separate axes
		validateAxes(model.getModels());

	}
	

	private void validateAxes(List<Object> models) throws ValidationException {
		
        List<String> usedAxes = new ArrayList<String>();
        for (Object model : models) {
			if (model instanceof IScanPathModel) {
				List<String> axes = ((IScanPathModel)model).getScannableNames();
				if (axes.stream().anyMatch(usedAxes::contains)) throw new ModelValidationException("The axes "+axes+" are used in a previous model!", model);
				usedAxes.addAll(axes);
			}
		}
	}

}
