package org.eclipse.scanning.sequencer.nexus;

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
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanDataModel;
import org.eclipse.scanning.api.scan.models.ScanDeviceModel;
import org.eclipse.scanning.api.scan.models.ScanDeviceModel.ScanFieldModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.sequencer.ServiceHolder;

/**
 * A wrapper around a nexus file exposing only the methods required for
 * perfoming a scan.
 */
public class NexusScanFileBuilder {
	
	private IDeviceConnectorService deviceService;
	private ScanModel model;
	private NexusScanInfo scanInfo;
	private List<NexusObjectProvider<?>> detectors;
	private List<NexusObjectProvider<?>> scannables;
	private List<NexusObjectProvider<?>> monitors;
	
	private Map<NexusObjectProvider<?>, DataDevice<?>> dataDevices = new HashMap<>();
	private NexusFileBuilder fileBuilder;
	
	public NexusScanFileBuilder(IDeviceConnectorService deviceService) {
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
	public NexusScanFile createNexusFile(ScanModel model, IRunnableDevice<?> scanDevice) throws NexusException, ScanningException {
		if (fileBuilder != null) {
			throw new IllegalStateException("The nexus file has already been created");
		}
		
		this.model = model;

		// Add and configures any devices we can get from the scan.
		final IPosition pos = model.getPositionIterable().iterator().next(); // The first position should have the same names as all positions.
		final List<String> scannableNames = pos.getNames();
		scanInfo = new NexusScanInfo(scannableNames);
		detectors = getNexusDevices(model.getDetectors(), scanInfo);
		if (scanDevice instanceof INexusDevice) detectors.add(((INexusDevice)scanDevice).getNexusProvider(scanInfo));
		
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
			// create a NXdata groups when there is no detector
			// (uses first monitor, or first scannable if there is no monitor either)
			createNXDataGroups(entryBuilder, null);
		} else {
			// create NXdata groups for each detector
			for (NexusObjectProvider<?> detector : detectors) {
				createNXDataGroups(entryBuilder, detector);
			}
		}
	}

	private void createNXDataGroups(NexusEntryBuilder entryBuilder, NexusObjectProvider<?> detector) throws NexusException {
		NexusObjectProvider<?> primaryDevice;
		List<NexusObjectProvider<?>> monitors = this.monitors;

		// determine the primary device
		if (detector != null) {
			// if there's a detector that it is the primary device
			primaryDevice = detector;
		} else if (!monitors.isEmpty()) {
			// otherwise it's the first monitor
			primaryDevice = monitors.get(0);
			// this monitor is removed from the list of monitors so as not to add it as a data device
			monitors = monitors.subList(1, monitors.size());
		} else if (!scannables.isEmpty()) {
			// unless there's no monitors either (a rare edge case), where we use the first scannable
			// note that this scannable is also added as data device
			primaryDevice = scannables.get(0);
		} else {
			// the scan has no devices at all (sanity check as this should already have been checked for) 
			throw new IllegalStateException("There must be at least one device to create a Nexus file.");
		}

		// create the NXdata group for the primary data field
		String primaryDeviceName = primaryDevice.getName();
		String primaryDataFieldName = primaryDevice.getPrimaryDataField();
		createNXDataGroup(entryBuilder, primaryDevice, monitors, scannables,
				primaryDeviceName, primaryDataFieldName);
		
		// create an NXdata group for each additional primary data field
		for (String dataFieldName : primaryDevice.getAdditionalPrimaryDataFields()) {
			String dataGroupName = primaryDeviceName + "_" + dataFieldName;
			createNXDataGroup(entryBuilder, primaryDevice, monitors, scannables,
					dataGroupName, dataFieldName);
		}
	}

	/**
	 * Create the {@link NXdata} groups for the given primary device.
	 * @param entryBuilder the entry builder to add to
	 * @param dataGroupName the name of the {@link NXdata} group within the parent {@link NXentry}
	 * @param primaryDataFieldName primary data field name
	 * @param detector the {@link NexusObjectProvider} for the detector, or <code>null</code>
	 * @throws NexusException
	 */
	private void createNXDataGroup(NexusEntryBuilder entryBuilder,
			NexusObjectProvider<?> primaryDevice,
			List<NexusObjectProvider<?>> monitors,
			List<NexusObjectProvider<?>> scannables,
			String dataGroupName,
			String primaryDataFieldName)
			throws NexusException {
		// create the data builder and add the primary device
		NexusDataBuilder dataBuilder = entryBuilder.newData(dataGroupName);
		DataDevice<?> primaryDataDevice = createPrimaryDataDevice(primaryDevice, primaryDataFieldName);
		dataBuilder.setPrimaryDevice(primaryDataDevice);
		
		// add the monitors (excludes the first monitor if the scan has no detectors)
		for (NexusObjectProvider<?> monitor : monitors) {
			dataBuilder.addDataDevice(getDataDevice(monitor, null));
		}
		
		// add the scannables
		int scannableIndex = 0;
		Iterator<NexusObjectProvider<?>> scannablesIter = scannables.iterator();
		while (scannablesIter.hasNext()) {
			dataBuilder.addDataDevice(getDataDevice(scannablesIter.next(), scannableIndex++));
		}
	}
	
	private DataDevice<?> createPrimaryDataDevice(NexusObjectProvider<?> nexusObjectProvider,
			String primaryDataSourceFieldName) {
		DataDevice<?> dataDevice = new DataDevice<>(nexusObjectProvider);
		dataDevice.setUseDeviceName(false);
		dataDevice.setPrimaryDataSourceFieldName(primaryDataSourceFieldName);

		if (detectors.isEmpty() && monitors.isEmpty()) {
			// using scannable as primary device as well as a scannable
			// only use main data field (e.g. value for an NXpositioner)
			dataDevice.setSourceFields(nexusObjectProvider.getPrimaryDataField());
		}

		return dataDevice;
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
			Integer scannableIndex) {
		DataDevice<?> dataDevice = dataDevices.get(nexusObjectProvider);
		if (dataDevice == null) {
			dataDevice = createDataDevice(nexusObjectProvider, scannableIndex);
			// cache the non-primary devices for any other NXdata groups
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
	 * @return
	 */
	private DataDevice<?> createDataDevice(NexusObjectProvider<?> nexusObjectProvider,
			Integer scannableIndex) {
		DataDevice<?> dataDevice = new DataDevice<>(nexusObjectProvider, scannableIndex);
		// all data fields are prefixed with the device name, except for
		// those from the primary device
		dataDevice.setUseDeviceName(true);
		if (model instanceof ScanDataModel) {
			// using a ScanDataModel allows for customization of how the data fields
			// of the device are added to the NXdata
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
