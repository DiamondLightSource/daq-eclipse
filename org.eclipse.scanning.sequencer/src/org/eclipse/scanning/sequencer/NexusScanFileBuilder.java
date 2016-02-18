package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.DataDevice;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusScanFile;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanDataModel;
import org.eclipse.scanning.api.scan.models.ScanDeviceModel;
import org.eclipse.scanning.api.scan.models.ScanDeviceModel.ScanFieldModel;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 * A wrapper around a nexus file exposing only the methods required for
 * perfoming a scan.
 */
class NexusScanFileBuilder {
	
	private IDeviceConnectorService deviceService;
	private ScanModel model;
	private NexusScanInfo scanInfo;
	private List<NexusObjectProvider<?>> detectors;
	private List<NexusObjectProvider<?>> scannables;
	private List<NexusObjectProvider<?>> monitors;
	
	private Map<NexusObjectProvider<?>, DataDevice<?>> dataDevices = new HashMap<>();
	private NexusFileBuilder fileBuilder;
	
	NexusScanFileBuilder(IDeviceConnectorService deviceService) {
		this.deviceService = deviceService; 
	}
	
	/**
	 * Creates the nexus file for the given {@link ScanModel}. 
	 * The structure of the nexus file is determined by model and the
	 * devices that the model references - these are retreived from the
	 * {@link IDeviceConnectorService}.
	 * 
	 * @param model model of scan
	 * @return the nexus scan file  
	 * @throws NexusException
	 * @throws ScanningException
	 */
	public NexusScanFile createNexusFile(ScanModel model) throws NexusException, ScanningException {
		if (fileBuilder != null) {
			throw new IllegalStateException("The nexus file has already been created");
		}
		
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
		
		return fileBuilder.createFile();
	}
	
	/**
	 * Creates and populates the {@link NXentry} for the NeXus file.
	 * @param fileBuilder a {@link NexusFileBuilder}
	 * @throws NexusException
	 */
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
		if (detectors.isEmpty() && monitors.isEmpty() && scannables.isEmpty()) {
			throw new NexusException("The scan must include at least one device in order to write a NeXus file.");
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

		// the detector is the primary device if present
		if (detector != null) {
			primaryDevice = detector;
		} else if (monitorsIter.hasNext()) {
			// otherwise it's the first monitor
			primaryDevice = monitorsIter.next(); 
		} else if (!scannables.isEmpty()) {
			// unless there's no monitors either (an rare edge case)
			// where we have to use the first scannable
			// TODO FIXME - we've tried to do something sensible in this case
			primaryDevice = scannables.get(0);
		} else {
			// sanity check, this should already have been checked for by this point
			throw new IllegalStateException();
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
		Iterator<NexusObjectProvider<?>> scannablesIter = scannables.iterator();
		while (scannablesIter.hasNext()) {
			dataBuilder.addDataDevice(getDataDevice(scannablesIter.next(), scannableIndex++, false));
		}
	}
	
	/**
	 * Gets the data device for the given {@link NexusObjectProvider},
	 * creating it if it doesn't exist.
	 * 
	 * @param nexusObjectProvider nexus object provider
	 * @param scannableIndex index in scan for {@link IScannable}s
	 * @param isPrimaryDevice <code>true</code> if this is the primary device for
	 *    the scan, <code>false</code> otherwise
	 * @return the data device
	 */
	private DataDevice<?> getDataDevice(NexusObjectProvider<?> nexusObjectProvider,
			Integer scannableIndex, boolean isPrimaryDevice) {
		DataDevice<?> dataDevice = null;
		if (!isPrimaryDevice) {
			dataDevice = dataDevices.get(nexusObjectProvider);
		}
		
		if (dataDevice == null) {
			dataDevice = createDataDevice(nexusObjectProvider, scannableIndex, isPrimaryDevice);
			if (isPrimaryDevice) {
				if (detectors.isEmpty() && monitors.isEmpty()) {
					// using scannable as primary device as well as a scannable
					// only use main data field (e.g. value for an NXpositioner)
					dataDevice.setSourceFields(nexusObjectProvider.getDefaultWritableDataFieldName());
				}
			} else {
				// cache the non-primary devices for the next NXdata section - there's one for each detector
				dataDevices.put(nexusObjectProvider, dataDevice);
			}
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
