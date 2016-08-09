package org.eclipse.scanning.points.validation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.points.PointGeneratorFactory;

public class ValidatorService implements IValidatorService {
	
	private static final Map<Class<?>, Class<? extends IValidator>> validators;
	static {
		Map<Class<?>, Class<? extends IValidator>> tmp = new HashMap<>();
		
		final PointGeneratorFactory factory = new PointGeneratorFactory();
		Map<Class<? extends IScanPathModel>, Class<? extends IPointGenerator>> gens = factory.getGenerators();
		for (Class<? extends IScanPathModel> modelClass : gens.keySet()) {
			tmp.put(modelClass, (Class<IValidator>)gens.get(modelClass));
		}
		tmp.put(BoundingBox.class, BoundingBoxValidator.class);
		
		validators = Collections.unmodifiableMap(tmp);
	}

	@Override
	public <T> void validate(T model) throws Exception {
		IValidator<T> validator = getValidator(model);
		validator.validate(model);
	}

	@Override
	public <T> IValidator<T> getValidator(T model) throws InstantiationException, IllegalAccessException {
		return validators.get(model.getClass()).newInstance();
	}

}
