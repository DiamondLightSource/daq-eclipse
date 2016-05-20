package org.eclipse.scanning.sequencer.nexus;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusScanFile;
import org.eclipse.dawnsci.nexus.builder.data.AxisDataDevice;
import org.eclipse.dawnsci.nexus.builder.data.DataDevice;
import org.eclipse.dawnsci.nexus.builder.data.DataDeviceBuilder;
import org.eclipse.dawnsci.nexus.builder.data.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.data.PrimaryDataDevice;
import org.eclipse.dawnsci.nexus.builder.impl.MapBasedMetadataProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanDataModel;
import org.eclipse.scanning.api.scan.models.ScanDeviceModel;
import org.eclipse.scanning.api.scan.models.ScanDeviceModel.ScanFieldModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.sequencer.ServiceHolder;

/**
 * Builds the NeXus file for a scan given a {@link ScanModel}.
 */
public class NexusScanFileBuilder {
	
	/**
	 * Enum of types of device that can be added to a Nexus File.
	 * <p>
	 * <em>The order of the elements in this enum defines the order in which the elements
	 * are added to the NeXus file. Do not change.</em>
	 */
	private static enum DeviceType {
		/**
		 * A metadata scannable. A scannables that creates all its datasets once,
		 * at the start of the scan,
		 */
		METADATA_SCANNABLE,
		
		/**
		 * A detectors. One or more {@link NXdata} groups are created for each detector. 
		 */
		DETECTOR,
		
		/**
		 * A scannable. The are scannables that are asked to move to a particular position
		 * at each point in the scan. The order of scannables is used to create the default
		 * axis for each {@link NXdata} group created.
		 */
		SCANNABLE,
		
		/**
		 * A monitor. A scannable which is not asked to move, but simply writes to its
		 * writable datasets at each point in the scan.
		 */
		MONITOR,
	}
	
	private IDeviceConnectorService deviceConnectorService;
	private ScanModel model;
	private NexusScanInfo scanInfo;
	private ScanPointsWriter scanPointsWriter;
	private NexusFileBuilder fileBuilder;
	
	// we need to cache various things as they are used more than once
	private Map<DeviceType, List<NexusObjectProvider<?>>> nexusObjectProviders = null;
	private Map<DeviceType, List<INexusDevice<?>>> nexusDevices = null;
	private Map<NexusObjectProvider<?>, AxisDataDevice<?>> dataDevices = new HashMap<>();
	
	public NexusScanFileBuilder(IDeviceConnectorService deviceConnectorService) {
		this.deviceConnectorService = deviceConnectorService;
		this.scanPointsWriter = new ScanPointsWriter();
	}
	
	/**
	 * Creates the nexus file for the given {@link ScanModel}. 
	 * The structure of the nexus file is determined by model and the
	 * devices that the model references - these are retrieved from the
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
		this.scanInfo = createScanInfo(model);
		
		nexusDevices = extractNexusDevices(model);
		
		// convert this to a map of nexus object providers for each type
		nexusObjectProviders = new EnumMap<>(DeviceType.class);
		nexusDevices.forEach((k, v) -> nexusObjectProviders.put(k, v.stream().map(
				d -> d.getNexusProvider(scanInfo)).filter(n -> n != null).collect(Collectors.toList())));
		
		// create the scan points writer and add it as a monitor
		scanPointsWriter = createScanPointsWriter();
		nexusObjectProviders.get(DeviceType.MONITOR).add(scanPointsWriter.getNexusProvider(scanInfo));
		
		// We use the new nexus framework to join everything up into the scan
		// Create a builder
		fileBuilder = ServiceHolder.getFactory().newNexusFileBuilder(model.getFilePath());
		createEntry(fileBuilder);
		
		return fileBuilder.createFile();
	}
	
	private NexusScanInfo createScanInfo(ScanModel scanModel) {
		final List<String> scannableNames = 
				scanModel.getPositionIterable().iterator().next().getNames();
		
		NexusScanInfo scanInfo = new NexusScanInfo(scannableNames);
		scanInfo.setMonitorNames(getDeviceNames(scanModel.getMonitors()));
		scanInfo.setMetadataScannableNames(getDeviceNames(scanModel.getMetadataScannables()));
		
		return scanInfo;
	}
	
	private Set<String> getDeviceNames(Collection<IScannable<?>> devices) {
		return devices.stream().map(d -> d.getName()).collect(Collectors.toSet());
	}
	
	private Map<DeviceType, List<INexusDevice<?>>> extractNexusDevices(ScanModel model) throws ScanningException {
		final IPosition firstPosition = model.getPositionIterable().iterator().next();
		final List<String> scannableNames = firstPosition.getNames();
		
		Map<DeviceType, List<INexusDevice<?>>> nexusDevices = new EnumMap<>(DeviceType.class);
		nexusDevices.put(DeviceType.DETECTOR, getNexusDevices(model.getDetectors()));
		nexusDevices.put(DeviceType.SCANNABLE, getNexusScannables(scannableNames));
		nexusDevices.put(DeviceType.MONITOR, getNexusDevices(model.getMonitors()));
		nexusDevices.put(DeviceType.METADATA_SCANNABLE, getNexusDevices(model.getMetadataScannables()));
		
		return nexusDevices;
	}
	
	private ScanPointsWriter createScanPointsWriter() {
		ScanPointsWriter scanPointsWriter = new ScanPointsWriter();
		
		// get the nexus object providers for all device types excluding metadata scannables
		EnumSet<DeviceType> deviceTypes = EnumSet.complementOf(EnumSet.of(DeviceType.METADATA_SCANNABLE));
		List<NexusObjectProvider<?>> nexusObjectProviders = this.nexusObjectProviders.entrySet().stream()
			.filter(e -> deviceTypes.contains(e.getKey())) // filter where key is in deviceType set
			.flatMap(e -> e.getValue().stream())  // concatenate value lists into a single stream
			.collect(Collectors.toList());  // collect in a list
		
		scanPointsWriter.setNexusObjectProviders(nexusObjectProviders);
		
		return scanPointsWriter;
	}
	
	/**
	 * Creates and populates the {@link NXentry} for the NeXus file.
	 * @param fileBuilder a {@link NexusFileBuilder}
	 * @throws NexusException
	 */
	private void createEntry(NexusFileBuilder fileBuilder) throws NexusException {
		final NexusEntryBuilder entryBuilder  = fileBuilder.newEntry();
		entryBuilder.addDefaultGroups();
		
		addScanMetadata(entryBuilder, model.getScanMetadata());
		
		// add all the devices to the entry. Metadata scannables are added first.
		for (DeviceType deviceType : EnumSet.allOf(DeviceType.class)) {
			addDevicesToEntry(entryBuilder, deviceType);
		}
		
		// create the NXdata groups
		createNexusDataGroups(entryBuilder);
	}
	
	private void addDevicesToEntry(NexusEntryBuilder entryBuilder, DeviceType deviceType) throws NexusException {
		entryBuilder.addAll(nexusObjectProviders.get(deviceType));
		
		List<CustomNexusEntryModification> customModifications =
				nexusDevices.get(deviceType).stream().
				map(d -> d.getCustomNexusModification()).
				filter(Objects::nonNull).
				collect(Collectors.toList());
		for (CustomNexusEntryModification customModification : customModifications) {
			entryBuilder.modifyEntry(customModification);
		}
	}
	
	private void addScanMetadata(NexusEntryBuilder entryBuilder, List<ScanMetadata> scanMetadataList) throws NexusException {
		if (scanMetadataList != null) {
			for (ScanMetadata scanMetadata : scanMetadataList) {
				// convert the ScanMetadata into a MapBasedMetadataProvider and add to the entry builder
				NexusBaseClass category = getBaseClassForMetadataType(scanMetadata.getType());
				MapBasedMetadataProvider metadataProvider = new MapBasedMetadataProvider(category);
				for (String metadataFieldName : scanMetadata.getMetadataFieldNames()) {
					Object value = scanMetadata.getMetadataFieldValue(metadataFieldName);
					metadataProvider.addMetadataEntry(metadataFieldName, value); 
				}
				
				entryBuilder.addMetadata(metadataProvider);
			}
		}
	}
	
	private NexusBaseClass getBaseClassForMetadataType(MetadataType metadataType) {
		if (metadataType == null) {
			return null;
		}
		switch (metadataType) {
			case ENTRY:
				return NexusBaseClass.NX_ENTRY;
			case INSTRUMENT:
				return NexusBaseClass.NX_INSTRUMENT;
			case SAMPLE:
				return NexusBaseClass.NX_SAMPLE;
			case USER:
				return NexusBaseClass.NX_USER;
			default:
				throw new IllegalArgumentException("Unknown metadata type : " + metadataType);
		}
	}
	
	private List<INexusDevice<?>> getNexusDevices(Collection<?> devices) {
		return devices.stream().filter(d -> d instanceof INexusDevice<?>).map(
				d -> (INexusDevice<?>) d).collect(Collectors.toList());
	}
	
	private List<INexusDevice<?>> getNexusScannables(List<String> scannableNames) throws ScanningException {
		try {
			return scannableNames.stream().map(name -> getNexusScannable(name)).
					filter(s -> s != null).
					collect(Collectors.toList());
		} catch (Exception e) {
			if (e.getCause() instanceof ScanningException) {
				throw (ScanningException) e.getCause();
			} else {
				throw e;
			}
		}
	}
	
	private INexusDevice<?> getNexusScannable(String scannableName) {
		try {
			IScannable<?> scannable = deviceConnectorService.getScannable(scannableName);
			if (scannable == null) {
				throw new IllegalArgumentException("No such scannable: " + scannableName);
			}
			if (scannable instanceof INexusDevice<?>) {
				return (INexusDevice<?>) scannable;
			}
			
			return null;
		} catch (ScanningException e) {
			throw new RuntimeException("Error getting scannable with name: " + scannableName, e);
		}
	}
	
	/**
	 * Create the {@link NXdata} groups for the scan
	 * @param entryBuilder
	 * @throws NexusException
	 */
	private void createNexusDataGroups(final NexusEntryBuilder entryBuilder) throws NexusException {
		Set<DeviceType> deviceTypes = EnumSet.of(DeviceType.DETECTOR, DeviceType.SCANNABLE, DeviceType.MONITOR);
		if (deviceTypes.stream().allMatch(t -> nexusObjectProviders.get(t).isEmpty())) {
			throw new NexusException("The scan must include at least one device in order to write a NeXus file.");
		}
		
		List<NexusObjectProvider<?>> detectors = nexusObjectProviders.get(DeviceType.DETECTOR);
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
		List<NexusObjectProvider<?>> scannables = nexusObjectProviders.get(DeviceType.SCANNABLE);
		List<NexusObjectProvider<?>> monitors = new LinkedList<>(nexusObjectProviders.get(DeviceType.MONITOR));

		// determine the primary device
		final NexusObjectProvider<?> primaryDevice;
		final DeviceType primaryDeviceType;
		if (detector != null) {
			// if there's a detector then it is the primary device
			primaryDevice = detector;
			primaryDeviceType = DeviceType.DETECTOR;
			monitors.remove(scanPointsWriter.getNexusProvider(scanInfo));
		} else if (!monitors.isEmpty()) {
			// otherwise the first monitor is the primary device (and therefore is not a data device)
			primaryDevice = monitors.remove(0);
			primaryDeviceType = DeviceType.MONITOR;
		} else if (!scannables.isEmpty()) {
			// if there are no monitors either (a rare edge case), where we use the first scannable
			// note that this scannable is also added as data device
			primaryDevice = scannables.get(0);
			primaryDeviceType = DeviceType.SCANNABLE;
		} else {
			// the scan has no devices at all (sanity check as this should already have been checked for) 
			throw new IllegalStateException("There must be at least one device to create a Nexus file.");
		}
		
		// create the NXdata group for the primary data field
		String primaryDeviceName = primaryDevice.getName();
		String primaryDataFieldName = primaryDevice.getPrimaryDataFieldName();		
		createNXDataGroup(entryBuilder, primaryDevice, primaryDeviceType, monitors,
				scannables, primaryDeviceName, primaryDataFieldName);
		
		// create an NXdata group for each additional primary data field (if any)
		for (String dataFieldName : primaryDevice.getAdditionalPrimaryDataFieldNames()) {
			String dataGroupName = primaryDeviceName + "_" + dataFieldName;
			createNXDataGroup(entryBuilder, primaryDevice, primaryDeviceType, monitors,
					scannables, dataGroupName, dataFieldName);
		}
	}

	/**
	 * Create the {@link NXdata} groups for the given primary device.
	 * @param entryBuilder the entry builder to add to
	 * @param primaryDevice the primary device (e.g. a detector or monitor)
	 * @param primaryDeviceType the type of the primary device
	 * @param monitors the monitors
	 * @param scannable the scannables 
	 * @param dataGroupName the name of the {@link NXdata} group within the parent {@link NXentry}
	 * @param primaryDataFieldName the name that the primary data field name
	 *   (i.e. the <code>@signal</code> field) should have within the NXdata group
	 * @throws NexusException
	 */
	private void createNXDataGroup(NexusEntryBuilder entryBuilder,
			NexusObjectProvider<?> primaryDevice,
			DeviceType primaryDeviceType,
			List<NexusObjectProvider<?>> monitors,
			List<NexusObjectProvider<?>> scannables,
			String dataGroupName,
			String primaryDataFieldName)
			throws NexusException {
		if (entryBuilder.getNXentry().containsNode(dataGroupName)) {
			dataGroupName += "_data"; // append _data if the node already exists
		}
		
		// create the data builder and add the primary device
		final NexusDataBuilder dataBuilder = entryBuilder.newData(dataGroupName);
		
		PrimaryDataDevice<?> primaryDataDevice = createPrimaryDataDevice(
				primaryDevice, primaryDeviceType, primaryDataFieldName);
		dataBuilder.setPrimaryDevice(primaryDataDevice);
		
		// add the monitors (excludes the first monitor if the scan has no detectors)
		for (NexusObjectProvider<?> monitor : monitors) {
			dataBuilder.addAxisDevice(getAxisDataDevice(monitor, null));
		}
		
		// add the scannables
		int scannableIndex = 0;
		Iterator<NexusObjectProvider<?>> scannablesIter = scannables.iterator();
		while (scannablesIter.hasNext()) {
			dataBuilder.addAxisDevice(getAxisDataDevice(scannablesIter.next(), scannableIndex++));
		}
	}
	
	private <N extends NXobject> PrimaryDataDevice<N> createPrimaryDataDevice(
			NexusObjectProvider<N> nexusObjectProvider,
			DeviceType primaryDeviceType, String signalDataFieldName) throws NexusException {
		
		if (primaryDeviceType == DeviceType.SCANNABLE) {
			// using scannable as primary device as well as a scannable
			// only use main data field (e.g. value for an NXpositioner)
			DataDeviceBuilder<N> dataDeviceBuilder = DataDeviceBuilder.newPrimaryDataDeviceBuilder(
					nexusObjectProvider);
			dataDeviceBuilder.setAxisFields();
			return (PrimaryDataDevice<N>) dataDeviceBuilder.build();
		}

		return DataDeviceBuilder.newPrimaryDataDevice(nexusObjectProvider, signalDataFieldName);
	}
	
	/**
	 * Gets the data device for the given {@link NexusObjectProvider},
	 * creating it if it doesn't exist.
	 * 
	 * @param nexusObjectProvider nexus object provider
	 * @param scannableIndex index in scan for {@link IScannable}s, or <code>null</code>
	 *    if the scannable is being scanned (i.e. is a monitor or metadata scannable).
	 * @param isPrimaryDevice <code>true</code> if this is the primary device for
	 *    the scan, <code>false</code> otherwise
	 * @return the data device
	 * @throws NexusException 
	 */
	private AxisDataDevice<?> getAxisDataDevice(NexusObjectProvider<?> nexusObjectProvider,
			Integer scannableIndex) throws NexusException {
		AxisDataDevice<?> dataDevice = dataDevices.get(nexusObjectProvider);
		if (dataDevice == null) {
			dataDevice = createAxisDataDevice(nexusObjectProvider, scannableIndex);
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
	 * @throws NexusException 
	 */
	private <N extends NXobject> AxisDataDevice<N> createAxisDataDevice(
			NexusObjectProvider<N> nexusObjectProvider, Integer scannableIndex) throws NexusException {
		if (model instanceof ScanDataModel) {
			// using a ScanDataModel allows for customization of how the data fields
			// of the device are added to the NXdata
			ScanDeviceModel scanDeviceModel = ((ScanDataModel) model).getScanDevice(
					nexusObjectProvider.getName());
			if (scanDeviceModel != null) {
				createCustomAxisDataDevice(nexusObjectProvider, scanDeviceModel, scannableIndex);
			}
		}
		
		return DataDeviceBuilder.newAxisDataDevice(nexusObjectProvider, scannableIndex); 
	}
	
	/**
	 * Configures the {@link DataDevice} according to the given {@link ScanDeviceModel}.
	 * @param nexusObjectProvider
	 * @param scanDeviceModel scan device model
	 */
	private <N extends NXobject> void createCustomAxisDataDevice(
			NexusObjectProvider<N> nexusObjectProvider, ScanDeviceModel scanDeviceModel,
			Integer scannableIndex) {
		DataDeviceBuilder<N> builder = DataDeviceBuilder.newAxisDataDeviceBuilder(
				nexusObjectProvider, scannableIndex);
		
		// add named fields only means only add fields
		if (scanDeviceModel.getAddNamedFieldsOnly()) {
			builder.clearAxisFields();
		}
		
		// set the default dimension mappings
		builder.setDefaultDimensionMappings(scanDeviceModel.getDefaultDimensionMappings());
		builder.setDefaultAxisDimension(scanDeviceModel.getDefaultAxisDimension());

		// configure the information for any fields
		Map<String, ScanFieldModel> fieldDimensionModels = scanDeviceModel.getFieldDimensionModels();
		for (String sourceFieldName : fieldDimensionModels.keySet()) {
			ScanFieldModel fieldDimensionModel = fieldDimensionModels.get(sourceFieldName);
			builder.addAxisField(sourceFieldName);
			if (fieldDimensionModel != null) {
				// add the field info from the ScanModel to the DataDevice
				
				// the name of the field in the NXdata
				String destinationFieldName = scanDeviceModel.getDestinationFieldName(sourceFieldName);
				if (destinationFieldName != null) {
					builder.setDestinationFieldName(sourceFieldName, destinationFieldName);
				}
				
				// the index of the dimension of the signal field that this field is a default axis for
				Integer fieldDefaultAxisDimension = fieldDimensionModel.getDefaultAxisDimension();
				if (fieldDefaultAxisDimension != null) {
					builder.setDefaultAxisDimension(fieldDefaultAxisDimension);
				}
				
				// the dimension mappings between this field and the signal field 
				int[] dimensionMappings = fieldDimensionModel.getDimensionMappings();
				if (dimensionMappings != null) {
					builder.setDimensionMappings(sourceFieldName, dimensionMappings);
				}
				
			}
		}
	}

	public ScanPointsWriter getScanPointsWriter() {
		return scanPointsWriter;
	}
	
}
