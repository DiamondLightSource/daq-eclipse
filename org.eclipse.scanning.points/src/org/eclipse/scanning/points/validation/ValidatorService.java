package org.eclipse.scanning.points.validation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.points.PointGeneratorFactory;

public class ValidatorService implements IValidatorService {
	
	static {
		System.out.println("Starting ValidatorService");
	}
	
	private final PointGeneratorFactory factory = new PointGeneratorFactory();

	private static final Map<Class<?>, Class<? extends IValidator>> validators;
	static {
		Map<Class<?>, Class<? extends IValidator>> tmp = new HashMap<>();
		tmp.put(BoundingBox.class,   BoundingBoxValidator.class);
		tmp.put(CompoundModel.class, CompoundValidator.class);
		
		validators = Collections.unmodifiableMap(tmp);
	}

	@Override
	public <T> void validate(T model) throws Exception {
		IValidator<T> validator = getValidator(model);
		validator.validate(model);
	}

	@Override
	public <T> IValidator<T> getValidator(T model) throws InstantiationException, IllegalAccessException {
		
		if (model==null) throw new NullPointerException("The model is null!");
		if (validators.containsKey(model.getClass())) {
			return validators.get(model.getClass()).newInstance();
		}
	
		if (model instanceof IScanPathModel) { // Ask a generator
			try {
				IScanPathModel     pmodel = (IScanPathModel)model;
				IPointGenerator<?> gen    = factory.createGenerator(pmodel);
				return (IValidator<T>)gen;
				
			} catch (GeneratorException e) {
				throw new IllegalAccessException(e.getMessage());
			}
		}
		throw new IllegalAccessException("There is no validator for "+model.getClass().getSimpleName());
	}

}
