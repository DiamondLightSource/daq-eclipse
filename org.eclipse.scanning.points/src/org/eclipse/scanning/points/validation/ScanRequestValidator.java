package org.eclipse.scanning.points.validation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.ScanningException;

class ScanRequestValidator implements IValidator<ScanRequest<?>> {
	
	
	private IValidatorService vservice;

	@Override
	public void setService(IValidatorService vservice) {
		this.vservice = vservice;
	}
	
	@Override
	public void validate(ScanRequest<?> req) throws ValidationException, InstantiationException, IllegalAccessException {

	    final CompoundModel<?> cm = req.getCompoundModel();
		if (cm!=null && cm.getModels()!=null && !cm.getModels().isEmpty()) {
			vservice.validate(cm);
		} else {
			throw new ModelValidationException("There is no compound model available", req, "compoundModel");
		}
		try {
			Map<String, Object> dmodels = req.getDetectors();
			if (dmodels!=null) {
				if (dmodels.isEmpty()) throw new ModelValidationException("The detector models are empty!", req, "detectors");
			    validateMalcolmRules(dmodels);
		        validateDetectors(dmodels);
			}
		} catch (ScanningException ne) {
            throw new ValidationException(ne);
		}
	}

	private void validateMalcolmRules(Map<String, Object> dmodels)  throws ValidationException, ScanningException {
		
		IRunnableDeviceService dservice = ValidatorService.getRunnableDeviceService();
		if (dservice!=null) {
			Map<DeviceRole, Integer> count = new HashMap<>();
			for (DeviceRole role : DeviceRole.values()) count.put(role, 0);
			for (String name : dmodels.keySet()) {
				DeviceRole role = null;
				DeviceInformation<?> info = dservice.getDeviceInformation(name);
				if (info==null) {
					try {
						final IRunnableDevice<?> device = dservice.createRunnableDevice(dmodels.get(name));
						if (device.getRole()==DeviceRole.PROCESSING) {
							role = device.getRole();
						} else {
							// Only processing may be created on the fly, the others must have names.
							throw new ValidationException("Detector '"+name+"' cannot be found!");
						}
					} catch (ScanningException ne) {
						throw ne; // If we cannot make a device with this model, the scan request is not valid.
					}
				} else{
					role = info.getDeviceRole();
				}
				if (role==null) throw new ValidationException("Detector '"+name+"' cannot be found!");
				Integer c = count.get(role);
				count.put(role, ++c);
			}
			if (count.get(DeviceRole.MALCOLM)>1) {
				throw new ValidationException("Only one malcolm device may be used per scan.");
			}
			if (count.get(DeviceRole.MALCOLM)>0 && count.get(DeviceRole.HARDWARE)>0) {
				throw new ValidationException("Malcolm devices may not currently be mixed with other types of hardware devices.\n"
						             + "You may use processing devices with a malcolm device.");
			}
		}
	}

	private void validateDetectors(Map<String, Object> dmodels) throws ValidationException, InstantiationException, IllegalAccessException {
	
		// All the models must validate too
		for (Object model : dmodels.values()) {
			IValidator<Object> validator = vservice.getValidator(model);
			if (validator!=null) validator.validate(model); // We just ignore those without validators.
		}
		
	}

}
