package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.DataDevice;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanDataModel;
import org.eclipse.scanning.api.scan.models.ScanDeviceModel;
import org.eclipse.scanning.api.scan.models.ScanDeviceModel.ScanFieldModel;
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
	
	private Map<NexusObjectProvider<?>, DataDevice<?>> dataDevices = new HashMap<>();
	private NexusFileBuilder fileBuilder;
	
	NexusFileScanBuilder(IDeviceConnectorService deviceService) {
		this.deviceService = deviceService; 
	}
	
	/**
	 * Creates a nexus file for the given {@link ScanModel}. 
	 * @param model
	 * @return
	 * @throws NexusException
	 * @throws ScanningException
	 */
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
		fileBuilder = ServiceHolder.getFactory().newNexusFileBuilder(model.getFilePath());
		createEntry(fileBuilder);
		fileBuilder.saveFile();
		
		return true; // successfully created file
	}
	
	private void createEntry(NexusFileBuilder fileBuilder) throws NexusException {
		final NexusEntryBuilder entryBuilder  = fileBuilder.newEntry();
		entryBuilder.addDefaultGroups();
		
		// add all the instruments to the entry
		entryBuilder.addAll(detectors);
		entryBuilder.addAll(scannables);
		entryBuilder.addAll(monitors);

		createNexusDataGroups(entryBuilder);
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
	
	/**
	 * Create the {@link NXdata} groups for the scan
	 * @param entryBuilder
	 * @throws NexusException
	 */
	private void createNexusDataGroups(final NexusEntryBuilder entryBuilder) throws NexusException {
		if (detectors.isEmpty() && monitors.isEmpty()) {
			throw new NexusException("At least one detector or monitor is required to create an NXdata group.");
		}
		
		if (detectors.isEmpty()) {
			createNXData(entryBuilder, null);
		} else {
			// create a new NXdata group for each detector
			for (NexusObjectProvider<?> detector : detectors) {
				createNXData(entryBuilder, detector);
			}
		}
	}

	/**
	 * Create the {@link NXdata} group for the given detector, which
	 * may be <code>null</code> in the case that the scan has no detectors
	 * @param entryBuilder the entry builder to add to
	 * @param detector the {@link NexusObjectProvider} for the detector, or <code>null</code>
	 * @throws NexusException
	 */
	private void createNXData(NexusEntryBuilder entryBuilder, NexusObjectProvider<?> detector) throws NexusException {
		NexusObjectProvider<?> primaryDevice;
		Iterator<NexusObjectProvider<?>> monitorsIter = monitors.iterator();

		// the detector is the primary device if present, otherwise it's the first monitor
		if (detector == null) {
			primaryDevice = monitorsIter.next();
		} else {
			primaryDevice = detector;
		}
		
		// create the data builder and add the primary device
		NexusDataBuilder dataBuilder = entryBuilder.newData(primaryDevice.getName());
		dataBuilder.setPrimaryDevice(getDataDevice(primaryDevice, null, true));
		
		// add the (remaining) monitors
		while (monitorsIter.hasNext()) {
			dataBuilder.addDataDevice(getDataDevice(monitorsIter.next(), null, false));
		}
		
		// add the scannables
		int scannableIndex = 0;
		for (NexusObjectProvider<?> scannable : scannables) {
			dataBuilder.addDataDevice(getDataDevice(scannable, scannableIndex++, false));
		}
	}
	
	private DataDevice<?> getDataDevice(NexusObjectProvider<?> nexusObjectProvider,
			Integer scannableIndex, boolean isPrimaryDevice) {
		DataDevice<?> dataDevice = dataDevices.get(nexusObjectProvider);
		if (dataDevice == null) {
			dataDevice = createDataDevice(nexusObjectProvider, scannableIndex, isPrimaryDevice);
			dataDevices.put(nexusObjectProvider, dataDevice);
		}
		
		return dataDevice;
	}
	
	/**
	 * Creates the {@link DataDevice} for the given {@link NexusObjectProvider},
	 * @param nexusObjectProvider
	 * @param scannableIndex if the {@link NexusObjectProvider} represents an
	 *    {@link IScannable} then the index of that scannable in the list of scannables,
	 *    otherwise <code>null</code>
	 * @param isPrimaryDevice <code>true</code> if this device is the primary device
	 *    for the {@link NXdata} group, <code>false</code> otherwise
	 * @return
	 */
	private DataDevice<?> createDataDevice(NexusObjectProvider<?> nexusObjectProvider,
			Integer scannableIndex, boolean isPrimaryDevice) {
		DataDevice<?> dataDevice = new DataDevice<>(nexusObjectProvider, scannableIndex);
		// all data fields are prefixed with the device name, except for
		// those from the primary device
		dataDevice.setUseDeviceName(!isPrimaryDevice);
		if (model instanceof ScanDataModel) {
			ScanDeviceModel scanDeviceModel = ((ScanDataModel) model).getScanDevice(
					nexusObjectProvider.getName());
			if (scanDeviceModel != null) {
				configureDataDevice(dataDevice, scanDeviceModel);
			}
		}
		
		return dataDevice;
	}
	
	/**
	 * Configures the {@link DataDevice} according to the given {@link ScanDeviceModel}.
	 * @param dataDevice data device, wrapping a {@link NexusObjectProvider}
	 * @param scanDeviceModel scan device model
	 */
	private void configureDataDevice(DataDevice<?> dataDevice, ScanDeviceModel scanDeviceModel) {
		Boolean useDeviceName = scanDeviceModel.getUseDeviceName();
		
		// set whether the device name should be used in the destination field names
		if (useDeviceName != null) {
			dataDevice.setUseDeviceName(useDeviceName);
		}
		
		// add named fields only means only add fields
		if (scanDeviceModel.getAddNamedFieldsOnly()) {
			dataDevice.clearSourceFields();
		}
		
		// set the default dimension mappings
		dataDevice.setDefaultDimensionMappings(scanDeviceModel.getDefaultDimensionMappings());
		dataDevice.setDefaultAxisDimension(scanDeviceModel.getDefaultAxisDimension());

		// configure the information for any fields
		Map<String, ScanFieldModel> fieldDimensionModels = scanDeviceModel.getFieldDimensionModels();
		for (String sourceFieldName : fieldDimensionModels.keySet()) {
			ScanFieldModel fieldDimensionModel = fieldDimensionModels.get(sourceFieldName);
			if (fieldDimensionModel != null) {
				// add the field info from the ScanModel to the DataDevice 
				Integer fieldDefaultAxisDimension = fieldDimensionModel.getDefaultAxisDimension();
				int[] dimensionMappings = fieldDimensionModel.getDimensionMappings();
				dataDevice.addSourceField(sourceFieldName, fieldDefaultAxisDimension, dimensionMappings);
			} else {
				dataDevice.addSourceField(sourceFieldName);
			}
			
			String destinationFieldName = scanDeviceModel.getDestinationFieldName(sourceFieldName);
			if (destinationFieldName != null) {
				dataDevice.setDestinationFieldName(sourceFieldName, destinationFieldName);
			}
		}
		
	}
	
}
