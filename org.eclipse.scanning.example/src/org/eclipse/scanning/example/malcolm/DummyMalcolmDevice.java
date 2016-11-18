/*-
 * Copyright © 2016 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.example.malcolm;

import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.MONITOR;
import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.POSITION_SET;
import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.POSITION_VALUE;
import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.PRIMARY;
import static org.eclipse.scanning.malcolm.core.MalcolmDatasetType.SECONDARY;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXmonitor;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.BooleanAttribute;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.eclipse.scanning.api.malcolm.attributes.TableAttribute;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.connector.MessageGenerator;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.Services;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.eclipse.scanning.malcolm.core.MalcolmDatasetType;

/**
 * A dummy Malcolm device for use in dummy mode or tests.
 */
public class DummyMalcolmDevice extends AbstractMalcolmDevice<DummyMalcolmModel>
		implements IMalcolmDevice<DummyMalcolmModel> {
	
	private static interface IDummyMalcolmControlledDevice {
		
		public void createNexusFile(String dirPath) throws NexusException;

		public void writePosition(IPosition position);
		
	}
	
	private final class DummyMalcolmControlledDetector implements IDummyMalcolmControlledDevice {
		
		private Map<String, ILazyWriteableDataset> datasets = new HashMap<>();
		
		private final DummyMalcolmControlledDetectorModel model;
		
		public DummyMalcolmControlledDetector(DummyMalcolmControlledDetectorModel model) {
			this.model = model;
		}

		@Override
		public void createNexusFile(String dirPath) throws NexusException {
			int scanRank = scanInformation.getRank();
			
			final String filePath = dirPath + model.getName() + FILE_EXTENSION_HDF5;
			System.out.println("Dummy malcolm device creating nexus file " + filePath);
			TreeFile treeFile = NexusNodeFactory.createTreeFile(filePath);
			NXroot root = NexusNodeFactory.createNXroot();
			treeFile.setGroupNode(root);
			NXentry entry = NexusNodeFactory.createNXentry();
			root.setEntry(entry);
			
			// add an entry to the unique keys collection
			String[] uniqueKeysDatasetPathSegments = UNIQUE_KEYS_DATASET_PATH.split("/");
			NXcollection ndAttributesCollection = NexusNodeFactory.createNXcollection();
			entry.setCollection(uniqueKeysDatasetPathSegments[2], ndAttributesCollection);
			datasets.put("uniqueKeys", ndAttributesCollection.initializeLazyDataset(
					uniqueKeysDatasetPathSegments[3], scanRank, String.class)); 
			
			// create an NXdata 
			Map<String, DataNode> axesDemandDataNodes = new HashMap<>();
			for (DummyMalcolmDatasetModel datasetModel : model.getDatasets()) {
				final String datasetName = datasetModel.getName();
				NXdata dataGroup = NexusNodeFactory.createNXdata();
				entry.setData(datasetName, dataGroup);
				// initialize the dataset. The scan rank is added to the dataset rank 
				datasets.put(datasetName,  dataGroup.initializeLazyDataset(datasetName, 
						scanRank + datasetModel.getRank(), datasetModel.getDtype()));
				// add the demand values for the axes
				for (String axis : getModel().getAxesToMove()) {
					DataNode axisDemandDataNode = axesDemandDataNodes.get(axis);
					String dataNodeName = axis + "_set";
					if (axisDemandDataNode == null) {
						// demand value has rank 1
						dataGroup.initializeLazyDataset(dataNodeName, 1, Double.class);
						axisDemandDataNode = dataGroup.getDataNode(dataNodeName);
						axesDemandDataNodes.put(axis, axisDemandDataNode);
					} else {
						dataGroup.addDataNode(dataNodeName, axisDemandDataNode);
					}
				}
			}
			
			// save the nexus tree to disk
			saveNexusFile(treeFile);
		}

		@Override
		public void writePosition(IPosition position) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private final class PandaDevice implements IDummyMalcolmControlledDevice {
		
		@Override
		public void createNexusFile(String dirPath) throws NexusException {
			final String filePath = dirPath + "panda" + FILE_EXTENSION_HDF5;
			System.out.println("Dummy malcolm device creating nexus file " + filePath);
			TreeFile treeFile = NexusNodeFactory.createTreeFile(filePath);
			NXroot root = NexusNodeFactory.createNXroot();
			treeFile.setGroupNode(root);
			NXentry entry = NexusNodeFactory.createNXentry();
			root.setEntry(entry);

			// add the positioners to the entry
			for (String positionerName : getModel().getPositionerNames()) {
				// The path to positioner datasets written by malcolm is e.g. /entry/x/x
				NXpositioner positioner = NexusNodeFactory.createNXpositioner(); // nexus class doesn't matter really
				entry.addGroupNode(positionerName, positioner);
				positioner.initializeLazyDataset(positionerName, scanInformation.getRank(), Double.class);
			}
			
			// add the monitors to the entry
			for (String monitorName : getModel().getMonitorNames()) {
				NXmonitor monitor = NexusNodeFactory.createNXmonitor();
				entry.addGroupNode(monitorName, monitor);
				// TODO: if we want non-scalar monitors we'll have to change the model
				monitor.initializeLazyDataset(monitorName, scanInformation.getRank(), Double.class);
			}
			
			// add an entry to the unique keys collection
			String[] uniqueKeysDatasetPathSegments = UNIQUE_KEYS_DATASET_PATH.split("/");
			NXcollection ndAttributesCollection = NexusNodeFactory.createNXcollection();
			entry.setCollection(uniqueKeysDatasetPathSegments[2], ndAttributesCollection);
			ndAttributesCollection.initializeLazyDataset(uniqueKeysDatasetPathSegments[3],
					scanInformation.getRank(), String.class);
			
			saveNexusFile(treeFile);
		}

		@Override
		public void writePosition(IPosition position) {
			// TODO Auto-generated method stub
			
		}
		
	}

	public static final String UNIQUE_KEYS_DATASET_PATH = "/entry/NDAttributes/NDArrayUniqueId";
	
	public static final String FILE_EXTENSION_HDF5 = ".h5";
	
	private ChoiceAttribute state;
	private StringAttribute status;
	private BooleanAttribute busy;
	private NumberAttribute completedSteps;
	private NumberAttribute configuredSteps;
	private NumberAttribute totalSteps;
	private StringArrayAttribute axesToMove;
	private TableAttribute datasets;

	private Map<String, MalcolmAttribute> allAttributes;

	private boolean firstRunCompleted = false;
	
	private ScanInformation scanInformation = null; 
	
	// the dummy devices are responsible for writing the nexus files 
	private Map<String, IDummyMalcolmControlledDevice> devices = null;
	
	public DummyMalcolmDevice() throws IOException, ScanningException {
		super(new DummyMalcolmConnectorService(),
				Services.getRunnableDeviceService()); // Necessary if you are going to spring it
		this.model = new DummyMalcolmModel();
		setupAttributes();
		setDeviceState(DeviceState.IDLE);
	}
	
	private void setupAttributes() {
		allAttributes = new LinkedHashMap<>();

		state = new ChoiceAttribute();
		state.setChoices(Arrays.stream(DeviceState.values()).map(
				state -> state.toString()).toArray(String[]::new));
		state.setValue(DeviceState.IDLE.toString());
		state.setName("state");
		state.setLabel("state");
		state.setDescription("State of Block");
		state.setWriteable(false);
		allAttributes.put(state.getName(), state);

		status = new StringAttribute();
		status.setValue("Waiting");
		status.setName("status");
		status.setLabel("status");
		status.setDescription("Status of Block");
		status.setWriteable(false);
		allAttributes.put(status.getName(), status);

		busy = new BooleanAttribute();
		busy.setValue(false);
		busy.setName("busy");
		busy.setLabel("busy");
		busy.setDescription("Whether Block busy or not");
		busy.setWriteable(false);
		allAttributes.put(busy.getName(), busy);

		completedSteps = new NumberAttribute();
		completedSteps.setDtype("int32");
		completedSteps.setValue(0);
		completedSteps.setName("completedSteps");
		completedSteps.setLabel("completedSteps");
		completedSteps.setDescription("Readback of number of scan steps");
		completedSteps.setWriteable(false);
		allAttributes.put(completedSteps.getName(), completedSteps);

		configuredSteps = new NumberAttribute();
		configuredSteps.setDtype("int32");
		configuredSteps.setValue(0);
		configuredSteps.setName("configuredSteps");
		configuredSteps.setLabel("configuredSteps");
		configuredSteps.setDescription("Number of steps currently configured");
		allAttributes.put(configuredSteps.getName(), configuredSteps);

		totalSteps = new NumberAttribute();
		totalSteps.setDtype("int32");
		totalSteps.setValue(0);
		totalSteps.setName("totalSteps");
		totalSteps.setLabel("totalSteps");
		totalSteps.setDescription("Readback of number of scan steps");
		totalSteps.setWriteable(false);
		allAttributes.put(totalSteps.getName(), totalSteps);

		axesToMove = new StringArrayAttribute();
		axesToMove.setValue(new String[]{"stage_x", "stage_y"});
		axesToMove.setName(ATTRIBUTE_NAME_AXES_TO_MOVE);
		axesToMove.setLabel(ATTRIBUTE_NAME_AXES_TO_MOVE);
		axesToMove.setDescription("Default axis names to scan for configure()");
		axesToMove.setWriteable(false);
		allAttributes.put(axesToMove.getName(), axesToMove);
	}

	@Override
	public void validate(DummyMalcolmModel model) throws Exception {
		super.validate(model);
		// Note: fileDir doesn't need to be set as ScanProcess sets it if not set already
	}

	@Override
	public void configure(DummyMalcolmModel model) throws ScanningException {
		setDeviceState(DeviceState.CONFIGURING);

		// Note: cannot create dataset attr at this point as we don't know the scan rank,
		// which is required for the datasets for the scannables
		totalSteps.setValue(64);
		configuredSteps.setValue(64);
		List<String> axesToMoveList = model.getAxesToMove();
		if (axesToMoveList != null) {
			this.axesToMove.setValue((axesToMoveList.toArray(new String[axesToMoveList.size()])));
		}

		// super.configure sets device state to ready
		super.configure(model);
		
		devices = model.getDummyDetectorModels().stream().collect(Collectors.toMap(
				d -> d.getName(), d -> new DummyMalcolmControlledDetector(d)));
		devices.put("panda", new PandaDevice());
	}
	
	@Override
	protected void setDeviceState(DeviceState nstate) throws ScanningException {
		super.setDeviceState(nstate);
		if (state != null) {
			state.setValue(nstate.toString());
		}
	}
	
	private int getScanRank() {
		if (scanInformation == null) {
			return getModel().getAxesToMove().size();
		}
		return scanInformation.getRank();
	}

	private TableAttribute createDatasetsAttribute(DummyMalcolmModel model) throws MalcolmDeviceException {
		Map<String, Class<?>> types = new LinkedHashMap<>();
		types.put(DATASETS_TABLE_COLUMN_NAME, String.class);
		types.put(DATASETS_TABLE_COLUMN_FILENAME, String.class);
		types.put(DATASETS_TABLE_COLUMN_TYPE, String.class);
		types.put(DATASETS_TABLE_COLUMN_PATH, String.class);
		types.put(DATASETS_TABLE_COLUMN_RANK, Integer.class);
		types.put(DATASETS_TABLE_COLUMN_UNIQUEID, String.class);
		
		// add rows for each DummyMalcolmDatasetModel
		MalcolmTable table = new MalcolmTable(types);

		int scanRank = getScanRank();
		for (DummyMalcolmControlledDetectorModel detectorModel : model.getDummyDetectorModels()) {
			String deviceName = detectorModel.getName();
			MalcolmDatasetType datasetType = PRIMARY; // the first dataset is the primary dataset
			for (DummyMalcolmDatasetModel datasetModel : detectorModel.getDatasets()) {
				final String datasetName = datasetModel.getName();
				final String path = String.format("/entry/%s/%s", datasetName, datasetName);
				// The primary dataset is called det.data, whatever its actual name
				final String linkName = datasetType == PRIMARY ? NXdata.NX_DATA : datasetName;
				final int datasetRank = scanRank + datasetModel.getRank();
				table.addRow(createDatasetRow(deviceName, linkName,
						deviceName + FILE_EXTENSION_HDF5, datasetType, path, datasetRank));
				datasetType = SECONDARY;
			}
		}
		
		// Add rows for the demand values for the axes controlled by malcolm. Malcolm adds these
		// to the NXdata for each primary and secondary dataset of each detector. As they
		// are all the same, the datasets attribute only returns the first one
		if (!model.getDummyDetectorModels().isEmpty()) {
			final String firstDetectorName = model.getDummyDetectorModels().get(0).getName();
			for (String axisToMove : model.getAxesToMove()) {
				final String datasetName = "value_set";
				final String path = String.format("/entry/%s/%s_set", firstDetectorName, axisToMove); // e.g. /entry/detector/x_set
				table.addRow(createDatasetRow(axisToMove, datasetName,
						firstDetectorName + FILE_EXTENSION_HDF5, POSITION_SET, path, 1)); 
			}
		}
		
		// Add rows for the value datasets of each positioner (i.e. read-back-value)
		for (String positionerName: model.getPositionerNames()) {
			final String path = String.format("/entry/%s/%s", positionerName, positionerName); // e.g. /entry/j1/j1
			table.addRow(createDatasetRow(positionerName, "value",
					"panda" + FILE_EXTENSION_HDF5, POSITION_VALUE, path, scanRank));
		}
		
		// Add rows for the value datasets of each monitor
		for (String monitorName : model.getMonitorNames()) {
			final String path = String.format("/entry/%s/%s", monitorName, monitorName); // e.g. /entry/i0/i0
			table.addRow(createDatasetRow(monitorName, "value", "panda" + FILE_EXTENSION_HDF5,
					MONITOR, path, scanRank)); // TODO can currently only handle scalar monitors
		}
		
		TableAttribute datasets = new TableAttribute();
		datasets.setValue(table);
		datasets.setHeadings(table.getHeadings().toArray(new String[table.getHeadings().size()]));
		datasets.setName("datasets");
		datasets.setLabel("datasets");
		datasets.setDescription("Datasets produced in HDF file");
		datasets.setWriteable(true);
		
		return datasets;
	}
	
	private Map<String, Object> createDatasetRow(String deviceName, String datasetName,
			String fileName, MalcolmDatasetType type, String path, int rank) {
		Map<String, Object> datasetRow = new HashMap<>();
		datasetRow.put(DATASETS_TABLE_COLUMN_NAME, deviceName + "." + datasetName);
		datasetRow.put(DATASETS_TABLE_COLUMN_FILENAME, fileName);
		datasetRow.put(DATASETS_TABLE_COLUMN_TYPE, type.name().toLowerCase());
		datasetRow.put(DATASETS_TABLE_COLUMN_PATH, path);
		datasetRow.put(DATASETS_TABLE_COLUMN_RANK, rank);
		datasetRow.put(DATASETS_TABLE_COLUMN_UNIQUEID, UNIQUE_KEYS_DATASET_PATH);
		return datasetRow;
	}
	
	@Override
	public void run(IPosition pos) throws ScanningException, InterruptedException {
		setDeviceState(DeviceState.RUNNING);
		status.setValue("Running");
		completedSteps.setValue(totalSteps.getValue());
		
		if (!firstRunCompleted) {
			createNexusFiles();
			firstRunCompleted = true;
		}
		
//		SubscanModerator moderator = new SubscanModerator(getPointGenerator(),
//				Arrays.asList(this), 
//		for (IPosition position : moderator.getInnerIterable()) {
//			for (IDummyMalcolmControlledDevice device : devices.values()) {
//				device.writePosition(position);
//			}
//		}
		
		status.setValue("Finished writing");
		setDeviceState(DeviceState.READY);
	}

	private void createNexusFiles() throws ScanningException {
		DummyMalcolmModel model = getModel();
		if (model.getDummyDetectorModels().isEmpty()) return;
		
		String dirPath = model.getFileDir();
		if (!dirPath.endsWith("/")) {
			dirPath += "/";
		}

		for (Map.Entry<String, IDummyMalcolmControlledDevice> entry : devices.entrySet()) {
			try {
				entry.getValue().createNexusFile(dirPath);
			} catch (NexusException e) {
				throw new ScanningException("Unable to create nexus file for device " + entry.getKey());
			}
		}
	}
	
	@ScanStart
	public void setScanInformation(ScanInformation scanInformation) {
		// TODO, move to AbstractMalcolmDevice if necessary
		this.scanInformation = scanInformation;
	}

	private void saveNexusFile(TreeFile nexusTree) throws NexusException {
		NexusFile file = ServiceHolder.getNexusFileFactory().newNexusFile(nexusTree.getFilename(), true);
		file.createAndOpenToWrite();
		file.addNode("/", nexusTree.getGroupNode());
		file.flush();
		file.close();
	}

	@Override
	public void addMalcolmListener(IMalcolmListener l) {
		System.out.println("addMalcolmListener called");
	}

	@Override
	public void removeMalcolmListener(IMalcolmListener l) {
		System.out.println("removeMalcomListener called");
	}

	@Override
	public void dispose() throws MalcolmDeviceException {
		// nothing to do

	}

	@Override
	public DeviceState latch(long time, TimeUnit unit, DeviceState... ignoredStates) throws MalcolmDeviceException {
		// nothing to do
		return null;
	}

	@Override
	public boolean isLocked() throws MalcolmDeviceException {
		// never locked
		return false;
	}

	@Override
	public Object getAttributeValue(String attribute) throws MalcolmDeviceException {
		System.out.println("getAttributeValue called");
		try {
			updateAttributesWithLatestValues();
		} catch (ScanningException e) {
			throw new MalcolmDeviceException(e.getMessage());
		}
		
		MalcolmAttribute malcolmAttribute = allAttributes.get(attribute);
		if (malcolmAttribute != null) {
			return malcolmAttribute.getValue();
		}
		return null;
	}

	@Override
	public Object getAttribute(String attribute) throws ScanningException {
		System.out.println("getAttribute called");
		updateAttributesWithLatestValues();
		
		return allAttributes.get(attribute);
	}
	
	public void setAttributeValue(String attributeName, Object value) throws ScanningException {
		Object attr = getAttribute(attributeName);
		if (attr==null) throw new ScanningException("There is no attribute called "+attributeName);
		try {
			Method setValue = attr.getClass().getMethod("setValue", value.getClass());
			setValue.invoke(attr, value);
		} catch (NoSuchMethodError | Exception ne) {
			throw new ScanningException(ne);
		}
	}
	
	@Override
	public List<MalcolmAttribute> getAllAttributes() throws ScanningException {
		updateAttributesWithLatestValues();
		
		return new ArrayList<>(allAttributes.values());
	}

	private void updateAttributesWithLatestValues() throws ScanningException {
		DeviceState deviceState = getDeviceState();
		if (deviceState == null) deviceState = DeviceState.IDLE;
		state.setValue(deviceState.toString());
		status.setValue(getDeviceStatus());
		busy.setValue(isDeviceBusy());
		
		datasets = createDatasetsAttribute(model);
		allAttributes.put("datasets", datasets);
	}
	
	private static class DummyMalcolmConnectorService implements IMalcolmConnectorService<MalcolmMessage> {

		@Override
		public void connect(URI malcolmUri) throws MalcolmDeviceException {
			// do nothing
		}

		@Override
		public void disconnect() throws MalcolmDeviceException {
			// do nothing
		}

		@Override
		public MalcolmMessage send(IMalcolmDevice device, MalcolmMessage message)
				throws MalcolmDeviceException {
			// do nothing
			return null;
		}

		@Override
		public void subscribe(IMalcolmDevice device, MalcolmMessage msg,
				IMalcolmListener<MalcolmMessage> listener) throws MalcolmDeviceException {
			// do nothing
		}

		@Override
		public MalcolmMessage unsubscribe(IMalcolmDevice device, MalcolmMessage msg,
				IMalcolmListener<MalcolmMessage>... listeners) throws MalcolmDeviceException {
			// do nothing
			return null;
		}

		@Override
		public MessageGenerator<MalcolmMessage> createConnection() {
			// do nothing
			return null;
		}

		@Override
		public MessageGenerator<MalcolmMessage> createDeviceConnection(IMalcolmDevice device)
				throws MalcolmDeviceException {
			// do nothing
			return null;
		}
		
	}
	
	
}
