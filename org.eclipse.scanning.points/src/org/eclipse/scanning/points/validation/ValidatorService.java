package org.eclipse.scanning.points.validation;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorService implements IValidatorService {
	
	private static Logger logger = LoggerFactory.getLogger(ValidatorService.class);
	
	static {
		System.out.println("Starting ValidatorService");
	}
	
	private static IPointGeneratorService factory;
	
	public void setPointGeneratorService(IPointGeneratorService pservice) {
		factory = pservice;
	}
	
	private static IRunnableDeviceService dservice;
	
	public void setEventService(IEventService eservice) {
		if (dservice!=null) return;
		try {
			dservice = eservice.createRemoteService(new URI(CommandConstants.getScanningBrokerUri()), IRunnableDeviceService.class);
		} catch (Exception ne) {
			logger.error("Cannot get a device service to validate detector models!");
		}
	}
	
	public static IPointGeneratorService getPointGeneratorService() {
		return factory;
	}
	public static IRunnableDeviceService getRunnableDeviceService() {
		return dservice;
	}


	@SuppressWarnings("rawtypes")
	private static final Map<Class<?>, Class<? extends IValidator>> validators;
	static {
		@SuppressWarnings("rawtypes")
		Map<Class<?>, Class<? extends IValidator>> tmp = new HashMap<>();
		tmp.put(BoundingBox.class,   BoundingBoxValidator.class);
		tmp.put(CompoundModel.class, CompoundValidator.class);
		tmp.put(ScanRequest.class,   ScanRequestValidator.class);
		
		validators = Collections.unmodifiableMap(tmp);
	}

	@Override
	public <T> void validate(T model) throws Exception {
		IValidator<T> validator = getValidator(model);
		if (validator==null) throw new IllegalAccessException("There is no validator for "+model.getClass().getSimpleName());
		validator.setService(this);
		validator.validate(model);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IValidator<T> getValidator(T model) throws InstantiationException, IllegalAccessException {
		
		if (model==null) throw new NullPointerException("The model is null!");
		
		if (model instanceof IValidator) throw new IllegalArgumentException("Models should be vanilla and not contain logic for validating themselves!");
		
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
		
		if (model instanceof IDetectorModel && dservice!=null) {
			IRunnableDevice<Object> device=null;
			try {
				device = dservice.createRunnableDevice(model, false);
			} catch (ScanningException e) {
				logger.trace("No device found for "+model, e);
			}
			if (device!=null) return (IValidator<T>)device;
		}
		
		return null;
	}

}
