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

import java.io.File;
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

import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
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
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.Services;
import org.eclipse.scanning.example.malcolm.devices.DummyMalcolmControlledDetector;
import org.eclipse.scanning.example.malcolm.devices.DummyMalcolmControlledPanda;
import org.eclipse.scanning.example.malcolm.devices.IDummyMalcolmControlledDevice;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.eclipse.scanning.malcolm.core.MalcolmDatasetType;
import org.eclipse.scanning.sequencer.SubscanModerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dummy Malcolm device for use in dummy mode or tests.
 */
public class DummyMalcolmDevice extends AbstractMalcolmDevice<DummyMalcolmModel>
		implements IMalcolmDevice<DummyMalcolmModel> {
	
	public static final String DATASET_NAME_UNIQUE_KEYS = "uniqueKeys";

	public static final String UNIQUE_KEYS_DATASET_PATH = "/entry/NDAttributes/NDArrayUniqueId";
	
	public static final String FILE_EXTENSION_HDF5 = ".h5";
	
	private static Logger logger = LoggerFactory.getLogger(DummyMalcolmDevice.class);
	
	private ChoiceAttribute state;
	private StringAttribute status;
	private BooleanAttribute busy;
	private NumberAttribute completedSteps;
	private NumberAttribute configuredSteps;
	private NumberAttribute totalSteps;
	private StringArrayAttribute axesToMove;
	private TableAttribute datasets;
	private TableAttribute layout;

	private Map<String, MalcolmAttribute> allAttributes;

	private boolean firstRunCompleted = false;
	
	private int stepIndex = 0;
	
	private boolean paused = false;
	
	private int scanRank;
	
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
		axesToMove.setValue(model.getAxesToMove().toArray(new String[model.getAxesToMove().size()]));
		axesToMove.setName(ATTRIBUTE_NAME_AXES_TO_MOVE);
		axesToMove.setLabel(ATTRIBUTE_NAME_AXES_TO_MOVE);
		axesToMove.setDescription("Default axis names to scan for configure()");
		axesToMove.setWriteable(false);
		allAttributes.put(axesToMove.getName(), axesToMove);
		
		// set scanRank to the size of axesToMove initially. this will be overwritten before a scan starts
		scanRank = axesToMove.getValue().length;
	}
	
	public void setModel(DummyMalcolmModel model) {
		super.setModel(model);
		axesToMove.setValue(model.getAxesToMove().toArray(new String[model.getAxesToMove().size()]));
	}

	@Override
	public void validate(DummyMalcolmModel model) throws ValidationException {
		super.validate(model);
		// validate field: axesToMove
		if (model.getAxesToMove() != null) {
			final List<String> axesToMove = Arrays.asList(this.axesToMove.getValue());
			for (String axisToMove : model.getAxesToMove()) {
				if (!axesToMove.contains(axisToMove)) {
					throw new ModelValidationException("Invalid axis name: " + axisToMove, model, "axesToMove");
				}
			}
		}
		
		// validate field: fileDir
		if (model.getFileDir() == null) {
			throw new ModelValidationException("Output dir for malcolm not set", model, "fileDir");
		}
		final File fileDir = new File(model.getFileDir());
		if (!fileDir.exists()) {
			throw new ModelValidationException("The output dir for malcolm does not exist: " + model.getFileDir(),
					model, "fileDir");
		}
		if (!fileDir.isDirectory()) {
			throw new ModelValidationException("The output dir for malcolm is not a directory: " + model.getFileDir(),
					model, "fileDir");
		}
		
		// validate the point generator has been set (not part of the model, but it does need
		// to be set. In the real MalcolmDevice class this is part of the model sent over the
		// connection to the actual malcolm device for validation
		if (pointGenerator == null) {
			throw new ModelValidationException("The point generator was not set on the malcolm device",
					this, "pointGenerator");
		}
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
				d -> d.getName(), d -> new DummyMalcolmControlledDetector(d, axesToMoveList)));
		devices.put("panda", new DummyMalcolmControlledPanda(axesToMoveList, model.getMonitorNames()));
	}
	
	@ScanFinally
	public void scanFinally() {
		// close all the nexus file
		if (devices!=null) for (Map.Entry<String, IDummyMalcolmControlledDevice> entry : devices.entrySet()) {
			try {
				entry.getValue().closeNexusFile();
			} catch (NexusException e) {
				throw new RuntimeException("Unable to create nexus file for device " + entry.getKey());
			}
		}
		
		// reset device state for next scan.
		devices = null;
		firstRunCompleted = false;
	}
	
	@Override
	protected void setDeviceState(DeviceState nstate) throws ScanningException {
		super.setDeviceState(nstate);
		if (state != null) {
			state.setValue(nstate.toString());
		}
	}
	
	@PreConfigure
	public void setPointGenerator(IPointGenerator<?> pointGenerator) {
		super.setPointGenerator(pointGenerator);
		
		if (pointGenerator!=null) { // Some tests end up using the configure call of 
			                        // RunnableDeviceService which does not have a pointGenerator
			scanRank = pointGenerator.iterator().next().getScanRank();
			if (scanRank < 0) {
				scanRank = 1;
			}
		}
	}
	
	private int getScanRank() {
		return scanRank;
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
		for (String axis: model.getAxesToMove()) {
			final String path = String.format("/entry/%s/%s", axis, axis); // e.g. /entry/j1/j1
			table.addRow(createDatasetRow(axis, "value",
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

	private TableAttribute createLayoutAttribute() throws MalcolmDeviceException {
		Map<String, Class<?>> types = new LinkedHashMap<>();
		types.put("name", String.class);
		types.put("mri", String.class);
		types.put("x", Double.class);
		types.put("y", Double.class);
		types.put("visible", Boolean.class);
		
		// add rows for each DummyMalcolmDatasetModel
		MalcolmTable table = new MalcolmTable(types);
		
		Map<String, Object> datasetRow1 = new HashMap<>();
		datasetRow1.put("name", "BRICK");
		datasetRow1.put("mri", "P45-BRICK01");
		datasetRow1.put("x", 0);
		datasetRow1.put("y", 0);
		datasetRow1.put("visible", false);
		
		Map<String, Object> datasetRow2 = new HashMap<>();
		datasetRow2.put("name", "MIC");
		datasetRow2.put("mri", "P45-MIC");
		datasetRow2.put("x", 0);
		datasetRow2.put("y", 0);
		datasetRow2.put("visible", false);
		
		Map<String, Object> datasetRow3 = new HashMap<>();
		datasetRow3.put("name", "ZEBRA");
		datasetRow3.put("mri", "ZEBRA");
		datasetRow3.put("x", 0);
		datasetRow3.put("y", 0);
		datasetRow3.put("visible", false);
				
		table.addRow(datasetRow1);
		table.addRow(datasetRow2);
		table.addRow(datasetRow3);
		
		TableAttribute layout = new TableAttribute();
		layout.setValue(table);
		layout.setHeadings(table.getHeadings().toArray(new String[table.getHeadings().size()]));
		layout.setName("layout");
		layout.setLabel("");
		layout.setDescription("Layout of child blocks");
		layout.setWriteable(false);
		
		return layout;
	}
	
	@Override
	public void run(IPosition outerScanPosition) throws ScanningException, InterruptedException {
		paused = false;
		setDeviceState(DeviceState.RUNNING);
		status.setValue("Running");
		completedSteps.setValue(totalSteps.getValue());
		
		if (!firstRunCompleted) {
			createNexusFiles();
			firstRunCompleted = true;
		}

		// get an iterator over the inner scan positions
		final SubscanModerator moderator = new SubscanModerator(pointGenerator,
				Arrays.asList(this), Services.getPointGeneratorService());
		Iterable<IPosition> innerScanPositions = moderator.getInnerIterable();
		
		// get each dummy device to write its position at each inner scan position
		stepIndex = 0;
		for (IPosition innerScanPosition : innerScanPositions) {
			final long pointStartTime = System.nanoTime();
			final long targetDuration = (long) (model.getExposureTime() * 1000000000.0); // nanoseconds
			
			while (paused) {
				Thread.sleep(100);
			}
			final IPosition overallScanPosition = innerScanPosition.compound(outerScanPosition);
			for (IDummyMalcolmControlledDevice device : devices.values()) {
				try {
					device.writePosition(overallScanPosition);
				} catch (Exception e) {
					logger.error("Couldn't write data for device " + device.getName(), e);
				}
			}
			
			// If required, sleep until the requested exposure time is over
			long currentTime = System.nanoTime();
			long duration = currentTime - pointStartTime;
			if (duration < targetDuration) {
				long millisToWait = (targetDuration - duration) / 1000000;
				Thread.sleep(millisToWait);
			}
			
			innerScanPosition.setStepIndex(stepIndex++);
			firePositionComplete(innerScanPosition);
		}
		stepIndex = 0;
		
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
				entry.getValue().createNexusFile(dirPath, scanRank);
			} catch (NexusException e) {
				throw new ScanningException("Unable to create nexus file for device " + entry.getKey());
			}
		}
	}

	@Override
	public void addMalcolmListener(IMalcolmListener l) {
		// nothing to do  (TODO: do we need malcolm listeners?)
	}

	@Override
	public void removeMalcolmListener(IMalcolmListener l) {
		// nothing to do  (TODO: do we need malcolm listeners?)
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
	public void pause() throws ScanningException {
		paused = true;
	}
	
	@Override
	public void resume() throws ScanningException {
		paused = false;
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
		
		layout = createLayoutAttribute();
		allAttributes.put("layout", layout);
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
