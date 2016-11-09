package org.eclipse.scanning.points.validation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.CompoundModel;

class ScanRequestValidator implements IValidator<ScanRequest<?>> {
	
	
	private IValidatorService vservice;

	@Override
	public void setService(IValidatorService vservice) {
		this.vservice = vservice;
	}
	
	@Override
	public void validate(ScanRequest<?> req) throws Exception {

	    final CompoundModel<?> cm = req.getCompoundModel();
		if (cm!=null && cm.getModels()!=null && !cm.getModels().isEmpty()) vservice.validate(cm);
		
		Map<String,Object> dmodels = req.getDetectors();
		
		// Currently if there is one malcolm device, there must not be any other devices
		IRunnableDeviceService dservice = ValidatorService.getRunnableDeviceService();
		if (dservice!=null) {
			Map<DeviceRole, Integer> count = new HashMap<>();
			for (DeviceRole role : DeviceRole.values()) count.put(role, 0);
			for (String name : dmodels.keySet()) {
				DeviceInformation<?> info = dservice.getDeviceInformation(name);
				Integer c = count.get(info.getDeviceRole());
				count.put(info.getDeviceRole(), ++c);
			}
			if (count.get(DeviceRole.MALCOLM)>1) {
				throw new Exception("Only one malcolm device may be used per scan.");
			}
			if (count.get(DeviceRole.MALCOLM)>0 && count.get(DeviceRole.HARDWARE)>0) {
				throw new Exception("Malcolm devices may not currently be mixed with other types of hardware devices.\n"
						             + "You may use processing devices with a malcolm device.");
			}
		}
		
		// All the models must validate too
		for (Object model : dmodels.values()) {
			IValidator<Object> validator = vservice.getValidator(model);
			if (validator!=null) validator.validate(model); // We just ignore those without validators.
		}
		
	}

}
