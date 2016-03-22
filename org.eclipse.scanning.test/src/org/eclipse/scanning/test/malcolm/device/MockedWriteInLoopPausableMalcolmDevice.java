package org.eclipse.scanning.test.malcolm.device;

import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.dawnsci.hdf.object.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf.object.IHierarchicalDataFile;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.malcolm.models.MalcolmDetectorModelWithMap;
import org.eclipse.scanning.api.scan.ScanningException;


/**
 * Device which pretends to write a 1024x1024 file a number of times
 * @author fri44821
 *
 */
public class MockedWriteInLoopPausableMalcolmDevice extends LoopingMockedMalcolmDevice {

	public MockedWriteInLoopPausableMalcolmDevice(final String name, final LatchDelegate latcher) throws ScanningException {		
		super(name, latcher);
		/**
		 * The task to be executed repeatably
		 */
		callableTask = new Callable<Long>() {
			
			@Override
			public Long call() throws Exception {

				int[] shape = (int[])model.getParameterMap().get("shape");
				if (shape==null) shape = new int[]{1024,1024};
				IDataset       rimage   = Random.rand(shape);
				rimage.setName("image");
				
				
				// TODO FIXME Have to remove this is order for device to work @see MockedMalcolmDevice
 				IHierarchicalDataFile file=null;
 				try {
        			file = HierarchicalDataFactory.getWriter((String)model.getParameterMap().get("file"));
 					
					file.group("/entry");
					file.group("/entry/data");
					file.appendDataset(rimage.getName(), rimage, "/entry/data");
					
					count++;

					// We mimic and event coming in from Malcolm
					// In reality these will come in from ZeroMQ but
					// will call sendEvent(...) in the same way.
					final MalcolmEventBean bean = new MalcolmEventBean(getState());
					bean.setPercentComplete((count/amount)*100d);	
					bean.setFilePath((String)model.getParameterMap().get("file"));
					bean.setDatasetPath("/entry/data");
					
					// Hardcoded shape change of dataset, in reality it will not be so simple.
					bean.setOldShape(new int[]{count-1, shape[0], shape[1]});
					bean.setNewShape(new int[]{count, shape[0], shape[1]});
					sendEvent(bean);
					
					return null;

				} finally {
					if (file!=null) file.close();
        		}
			}
		};
	}

	@Override
	public MalcolmDetectorModelWithMap validate(MalcolmDetectorModelWithMap model) throws MalcolmDeviceException {
		Map<String, Object> params = model.getParameterMap();
		if (!params.containsKey("shape")) throw new MalcolmDeviceException(this, "shape must be set!");
		if (!params.containsKey("nframes")) throw new MalcolmDeviceException(this, "nframes must be set!");
		if (!params.containsKey("file")) throw new MalcolmDeviceException(this, "file must be set!");
		if (!params.containsKey("exposure")) throw new MalcolmDeviceException(this, "exposure must be set!");
		return null;
	}

	@Override
	public void configure(MalcolmDetectorModelWithMap params) throws ScanningException {
		
		validate(params);
		setState(DeviceState.CONFIGURING);
		this.model = params;
		if (params.getParameterMap().containsKey("configureSleep")) {
			try {
				long sleepTime = Math.round(((double)params.getParameterMap().get("configureSleep"))*1000d);
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				throw new MalcolmDeviceException(this, "Cannot sleep during configure!", e);
			}
		}
		setState(DeviceState.READY);
		
		// We configure a bean with all the scan specific things
		final MalcolmEventBean bean = new MalcolmEventBean();
		bean.setFilePath(params.getParameterMap().get("file").toString());
		bean.setDatasetPath("/entry/data");
		bean.setDeviceName(getName());
		bean.setBeamline("Testing");
		bean.setPercentComplete(0d);
        setTemplateBean(bean);
	}
}
