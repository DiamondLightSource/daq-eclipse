package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.AxisDevice;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanDeviceDimensionModel;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 * Creates a nexus file for a given {@link ScanModel}.
 */
class NexusFileScanBuilder {
	
	private IDeviceConnectorService deviceService;
	private ScanModel model;
	private NexusScanInfo scanInfo;
	private List<NexusObjectProvider<?>> detectors;
	private List<NexusObjectProvider<?>> scannables;
	private List<NexusObjectProvider<?>> monitors;
	
	NexusFileScanBuilder(IDeviceConnectorService deviceService) {
		this.deviceService = deviceService; 
	}
	
	public boolean createNexusFile(ScanModel model) throws NexusException, ScanningException {
		this.model = model;

		// Add and configures any devices we can get from the scan.
		final IPosition pos = model.getPositionIterable().iterator().next(); // The first position should have the same names as all positions.
		final List<String> scannableNames = pos.getNames();
		scanInfo = new NexusScanInfo(scannableNames);
		detectors = getNexusDevices(model.getDetectors(), scanInfo);
		scannables = getNexusScannables(scannableNames, scanInfo);
		monitors = getNexusDevices(model.getMonitors(), scanInfo);
		
		// We use the new nexus framework to join everything up into the scan
		// Create a builder
		final NexusFileBuilder fileBuilder = ServiceHolder.getFactory().newNexusFileBuilder(model.getFilePath());
		createEntry(fileBuilder);
		fileBuilder.saveFile();
		
		return true; // successfully created file
	}
	
	private void createEntry(NexusFileBuilder fileBuilder) throws NexusException {
		final NexusEntryBuilder entryBuilder  = fileBuilder.newEntry();
		entryBuilder.addDefaultGroups();
		entryBuilder.addAll(detectors);
		entryBuilder.addAll(scannables);
		entryBuilder.addAll(monitors);
		
		// configure the NXdata group - only add a group for the first detector
		if (!detectors.isEmpty()) {
			createNXData(entryBuilder);
		}
	}
	
	
	private <T> List<NexusObjectProvider<?>> getNexusDevices(Iterable<T> devices, NexusScanInfo scanInfo) {
		List<NexusObjectProvider<?>> nexusDevices = new ArrayList<>();
		if (devices != null) {
			for (T device : devices) {
				if (device instanceof INexusDevice<?>) {
					nexusDevices.add(((INexusDevice<?>) device).getNexusProvider(scanInfo));
				}
			}
		}
		
		return nexusDevices;
	}
	
	private List<NexusObjectProvider<?>> getNexusScannables(List<String> scannableNames, NexusScanInfo scanInfo) throws ScanningException {
		final List<IScannable<?>> scannables = new ArrayList<>(scannableNames.isEmpty() ? 0 : scannableNames.size());
		if (scannableNames != null) for (String scannableName : scannableNames) {
			IScannable<?> scannable = deviceService.getScannable(scannableName);
			if (scannable == null) {
				throw new IllegalArgumentException("No such scannable: " + scannableName);
			}
			scannables.add(scannable);
		}
		
		return getNexusDevices(scannables, scanInfo);
	}
	
	
	private void createNXData(NexusEntryBuilder entryBuilder) throws NexusException {
		NexusObjectProvider<?> detector = detectors.get(0);
		
		NexusDataBuilder dataBuilder = entryBuilder.newData(detector.getName());
		dataBuilder.setDataDevice(detector);
		
		int scannableIndex = 0;
		for (NexusObjectProvider<?> scannable : scannables) {
			AxisDevice<?> axisDevice = createAxisDevice(scannable, scannableIndex++);
			dataBuilder.addAxisDevice(axisDevice);
		}
		for (NexusObjectProvider<?> monitor : monitors) {
			AxisDevice<?> axisDevice = createAxisDevice(monitor, null);
			dataBuilder.addAxisDevice(axisDevice);
		}
	}
	
	private <N extends NXobject> AxisDevice<N> createAxisDevice(
			NexusObjectProvider<N> device, Integer scannableIndex) throws NexusException {
		final String deviceName = device.getName();
		ScanDeviceDimensionModel deviceDimensionModel = model.getScanDeviceDimensionModel(deviceName);
		AxisDevice<N> axisDevice = new AxisDevice<>(device);
		
		// get the default axis dimension if specified
		Integer defaultAxisDimension = null;
		int[] dimensionMappings = null;
		if (deviceDimensionModel != null) {
			defaultAxisDimension = deviceDimensionModel.getPrimaryAxisForDimension();
			dimensionMappings = deviceDimensionModel.getDimensionMappings();
		} else if (scannableIndex != null) {
			defaultAxisDimension = scannableIndex;
		}
		
		if (defaultAxisDimension != null) {
			axisDevice.setDefaultAxisDimension(defaultAxisDimension);
		}
		if (dimensionMappings != null) {
			axisDevice.setDimensionMappings(dimensionMappings);
		}
		
		return axisDevice;
	}
	
}
