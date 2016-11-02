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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.ModelValidationException;
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
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.Services;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;

/**
 * A dummy Malcolm device for use in dummy mode or tests.
 */
public class DummyMalcolmDevice extends AbstractMalcolmDevice<DummyMalcolmModel>
		implements INexusDevice<NXdetector>, IMalcolmDevice<DummyMalcolmModel> {

	public static final String TABLE_COLUMN_NAME = "name";
	public static final String TABLE_COLUMN_FILENAME = "filename";
	public static final String TABLE_COLUMN_TYPE = "type";
	public static final String TABLE_COLUMN_PATH = "path";
	public static final String TABLE_COLUMN_UNIQUEID = "uniqueid";
	public static final String TABLE_COLUMN_RANK = "rank";

	private static final String UNIQUE_KEYS_COLLECTION_NAME = "NDAttributes";
	private static final String UNIQUE_KEYS_DATASET_NAME = "NDArrayUniqueId";
	
	private ChoiceAttribute state;
	private StringAttribute status;
	private BooleanAttribute busy;
	private NumberAttribute completedSteps;
	private NumberAttribute configuredSteps;
	private NumberAttribute totalSteps;
	private StringArrayAttribute axesToMove;
	private TableAttribute datasets;

	private Map<String, MalcolmAttribute> allAttributes;

	private IPointGenerator<?> generator;
	private boolean firstRunCompleted = false;
	
	private ScanInformation scanInformation = null; 
	
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
		state.setChoices(new String[] { "Resetting", "Idle", "Editing", "Editable", "Saving", "Reverting", "Ready",
				"Configuring", "PreRun", "Running", "PostRun", "Paused", "Seeking", "Aborting", "Aborted",
				"Fault,Disabling,Disabled", "", "" });
		state.setValue("Idle");
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
		axesToMove.setName("axesToMove");
		axesToMove.setLabel("axesToMove");
		axesToMove.setDescription("Default axis names to scan for configure()");
		axesToMove.setWriteable(false);
		allAttributes.put(axesToMove.getName(), axesToMove);
	}

	@Override
	public void validate(DummyMalcolmModel model) throws Exception {
		super.validate(model);
		if (model.getFilePath()==null || model.getFilePath().length()<1) {
			throw new ModelValidationException("A directory must provided in which to write the test files.", model, "filePath");
		}
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
//		throw new UnsupportedOperationException("Nexus writing not yet implemented");
		return null;
	}

	@Override
	public void configure(DummyMalcolmModel model) throws ScanningException {
		setDeviceState(DeviceState.CONFIGURING);

		generator = model.getGenerator();
		try {
			datasets = createDatasetsAttribute(model);
			allAttributes.put("datasets", datasets);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		totalSteps.setValue(64);
		configuredSteps.setValue(64);
		// super.configure sets device state to ready
		super.configure(model);
	}
	
	private TableAttribute createDatasetsAttribute(DummyMalcolmModel model) {
		Map<String, Class<?>> types = new LinkedHashMap<>();
		types.put(TABLE_COLUMN_NAME, String.class);
		types.put(TABLE_COLUMN_FILENAME, String.class);
		types.put(TABLE_COLUMN_TYPE, String.class);
		types.put(TABLE_COLUMN_PATH, String.class);
		types.put(TABLE_COLUMN_UNIQUEID, String.class);
		types.put(TABLE_COLUMN_RANK, String.class);
		
		MalcolmTable table = new MalcolmTable(types);
		for (DummyMalcolmControlledDeviceModel dummyDeviceModel : model.getDummyDeviceModels()) {
			String deviceName = dummyDeviceModel.getName();
			for (DummyMalcolmDatasetModel datasetModel : dummyDeviceModel.getDatasets()) {
				Map<String, Object> datasetRow = new HashMap<>();
				datasetRow.put(TABLE_COLUMN_NAME, deviceName + "." + datasetModel.getName());
				datasetRow.put(TABLE_COLUMN_FILENAME, dummyDeviceModel.getFileName());
				datasetRow.put(TABLE_COLUMN_TYPE, datasetModel.getMalcolmType().name().toLowerCase());
				datasetRow.put(TABLE_COLUMN_PATH, datasetModel.getPath());
				datasetRow.put(TABLE_COLUMN_UNIQUEID, dummyDeviceModel.getUniqueId());
				datasetRow.put(TABLE_COLUMN_RANK, datasetModel.getRank());
				table.addRow(datasetRow);
			}
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
	
	@Override
	public void run(IPosition pos) throws ScanningException, InterruptedException {
		setDeviceState(DeviceState.RUNNING);
		status.setValue("Running");
		completedSteps.setValue(totalSteps.getValue());
		
		if (!firstRunCompleted) {
			createNexusFiles();
			firstRunCompleted = true;
		}
		
		status.setValue("Finished writing");
		setDeviceState(DeviceState.READY);
	}

	private void createNexusFiles() throws ScanningException {
		DummyMalcolmModel model = getModel();
		if (model.getDummyDeviceModels().isEmpty()) return;
		
		String dirPath = model.getFilePath();
		if (!dirPath.endsWith("/")) {
			dirPath += "/";
		}
		
		INexusFileFactory nexusFileFactory = ServiceHolder.getNexusFileFactory();
		for (DummyMalcolmControlledDeviceModel dummyDeviceModel : model.getDummyDeviceModels()) {
			try {
				createNexusFile(dirPath, nexusFileFactory, dummyDeviceModel);
			} catch (NexusException e) {
				throw new ScanningException("Unable to create nexus file for device " +
						dummyDeviceModel.getName());
			}
		}
	}
	
	@ScanStart
	public void setScanInformation(ScanInformation scanInformation) {
		// TODO, move to AbstractMalcolmDevice if necessary
		this.scanInformation = scanInformation;
	}

	private void createNexusFile(String dirPath, final INexusFileFactory nexusFileFactory,
			DummyMalcolmControlledDeviceModel dummyDeviceModel) throws NexusException {
		final String filePath = dirPath + dummyDeviceModel.getFileName();
		TreeFile treeFile = NexusNodeFactory.createTreeFile(filePath);
		NXroot root = NexusNodeFactory.createNXroot();
		treeFile.setGroupNode(root);
		NXentry entry = NexusNodeFactory.createNXentry();
		root.setEntry(entry);
		NXinstrument instrument = NexusNodeFactory.createNXinstrument();
		entry.setInstrument(instrument);
		
		NXcollection uniqueIdsCollection = NexusNodeFactory.createNXcollection();
		entry.setCollection(UNIQUE_KEYS_COLLECTION_NAME, uniqueIdsCollection);
		uniqueIdsCollection.initializeLazyDataset(UNIQUE_KEYS_DATASET_NAME,
				scanInformation.getRank(), String.class);
		
		final String dummyDeviceName = dummyDeviceModel.getName();
		NXobject nexusObject = null;
		switch (dummyDeviceModel.getRole()) {
			case DETECTOR:
				break;
			case MONITOR:
				nexusObject = NexusNodeFactory.createNXmonitor();
				instrument.addGroupNode(dummyDeviceName, nexusObject);
				break;
			case SCANNABLE:
				nexusObject = NexusNodeFactory.createNXpositioner();
				instrument.addGroupNode(dummyDeviceName, nexusObject);
				break;
			default:
				throw new RuntimeException("Unknown device role " + dummyDeviceModel.getRole());
		}
		
		int scanRank = scanInformation.getRank();
		for (DummyMalcolmDatasetModel datasetModel : dummyDeviceModel.getDatasets()) {
			String datasetName = datasetModel.getName();
			
			if (dummyDeviceModel.getRole() == ScanRole.DETECTOR) {
				// create a new NXdata group for each detector
				nexusObject = NexusNodeFactory.createNXdata();
				String dataGroupName;
				switch (datasetModel.getMalcolmType()) {
					case PRIMARY:
						dataGroupName = dummyDeviceName;
						break;
					case SECONDARY:
						dataGroupName = dummyDeviceName + "_" + datasetName;
						break;
					default:
						throw new RuntimeException("Invalid dataset type for detector "
								+ datasetModel.getMalcolmType());
				}
				entry.addGroupNode(dataGroupName, nexusObject);
			}
			
			nexusObject.initializeLazyDataset(datasetModel.getName(), 
					scanRank + datasetModel.getRank(), datasetModel.getDtype());
		}
		
		NexusFile file = nexusFileFactory.newNexusFile(filePath, true);
		file.createAndOpenToWrite();
		file.addNode("/", treeFile.getGroupNode());
		file.flush();
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
			updateAttributeswithLatestValues();
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
		updateAttributeswithLatestValues();
		
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
		System.out.println("getAllAttributes called");
		updateAttributeswithLatestValues();
		
		return new ArrayList<>(allAttributes.values());
	}

	private void updateAttributeswithLatestValues() throws ScanningException {
		state.setValue(getDeviceState().toString());
		status.setValue(getDeviceStatus());
		busy.setValue(isDeviceBusy());
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
