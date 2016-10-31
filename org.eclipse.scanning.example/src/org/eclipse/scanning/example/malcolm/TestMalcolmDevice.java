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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.tree.impl.TreeFileImpl;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
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
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.Services;

/**
 * A dummy Malcolm device which simple writes two Nexus files with random image data in each
 */
public class TestMalcolmDevice extends AbstractRunnableDevice<TestMalcolmModel>
		implements IWritableDetector<TestMalcolmModel>, INexusDevice<NXdetector>, IMalcolmDevice<TestMalcolmModel> {

	ChoiceAttribute state;
	StringAttribute status;
	BooleanAttribute busy;
	NumberAttribute completedSteps;
	NumberAttribute configuredSteps;
	NumberAttribute totalSteps;
	StringArrayAttribute axesToMove;
	TableAttribute datasets;

	List<MalcolmAttribute> allAttributes;

	IPointGenerator<?> generator;
	String filePath;

	// Field names to be used in the NeXus file
	private static final String FIELD_NAME_VALUE = "value";
	private static final String FIELD_NAME_SPECTRUM = "spectrum";
	private static final String FIELD_NAME_SPECTRUM_AXIS = "spectrum_axis";
	private static final String FIELD_NAME_IMAGINARY_AXIS = "imaginary";
	private static final String FIELD_NAME_REAL_AXIS = "real";

	public TestMalcolmDevice() throws IOException, ScanningException {
		super(Services.getRunnableDeviceService()); // Necessary if you are
													// going to spring it
		this.model = new TestMalcolmModel();
		setupAttributes();
		setDeviceState(DeviceState.IDLE);
	}

	private void setupAttributes() {
		allAttributes = new LinkedList<>();

		state = new ChoiceAttribute();
		state.setChoices(new String[] { "Resetting", "Idle", "Editing", "Editable", "Saving", "Reverting", "Ready",
				"Configuring", "PreRun", "Running", "PostRun", "Paused", "Seeking", "Aborting", "Aborted",
				"Fault,Disabling,Disabled", "", "" });
		state.setValue("Idle");
		state.setName("state");
		state.setLabel("state");
		state.setDescription("State of Block");
		state.setWriteable(false);
		allAttributes.add(state);

		status = new StringAttribute();
		status.setValue("Waiting");
		status.setName("status");
		status.setLabel("status");
		status.setDescription("Status of Block");
		status.setWriteable(false);
		allAttributes.add(status);

		busy = new BooleanAttribute();
		busy.setValue(false);
		busy.setName("busy");
		busy.setLabel("busy");
		busy.setDescription("Whether Block busy or not");
		busy.setWriteable(false);
		allAttributes.add(busy);

		completedSteps = new NumberAttribute();
		completedSteps.setDtype("int32");
		completedSteps.setValue(0);
		completedSteps.setName("completedSteps");
		completedSteps.setLabel("completedSteps");
		completedSteps.setDescription("Readback of number of scan steps");
		completedSteps.setWriteable(false);
		allAttributes.add(completedSteps);

		configuredSteps = new NumberAttribute();
		configuredSteps.setDtype("int32");
		configuredSteps.setValue(0);
		configuredSteps.setName("configuredSteps");
		configuredSteps.setLabel("configuredSteps");
		configuredSteps.setDescription("Number of steps currently configured");
		allAttributes.add(configuredSteps);

		totalSteps = new NumberAttribute();
		totalSteps.setDtype("int32");
		totalSteps.setValue(0);
		totalSteps.setName("totalSteps");
		totalSteps.setLabel("totalSteps");
		totalSteps.setDescription("Readback of number of scan steps");
		totalSteps.setWriteable(false);
		allAttributes.add(totalSteps);

		axesToMove = new StringArrayAttribute();
		axesToMove.setValue(new String[]{"stage_x", "stage_y"});
		axesToMove.setName("axesToMove");
		axesToMove.setLabel("axesToMove");
		axesToMove.setDescription("Default axis names to scan for configure()");
		axesToMove.setWriteable(false);
		allAttributes.add(axesToMove);

		datasets = new TableAttribute();
		Map<String, LinkedList<Object>> columns = new LinkedHashMap<>();
		LinkedList<Object> nameColumn = new LinkedList<>();
		nameColumn.add("mic.StatsTotal");
		nameColumn.add("mic.detector");
		nameColumn.add("panda.x");
		nameColumn.add("panda.y");
		columns.put("name", nameColumn);
		LinkedList<Object> filenameColumn = new LinkedList<>();
		filenameColumn.add("test_snake_002.h5");
		filenameColumn.add("test_snake_002.h5");
		filenameColumn.add("test_panda_001.h5");
		filenameColumn.add("test_panda_001.h5");
		columns.put("filename", filenameColumn);
		LinkedList<Object> typeColumn = new LinkedList<>();
		typeColumn.add("additional");
		typeColumn.add("primary");
		typeColumn.add("additional");
		typeColumn.add("additional");
		columns.put("type", typeColumn);
		LinkedList<Object> pathColumn = new LinkedList<>();
		pathColumn.add("/entry/StatsTotal/StatsTotal");
		pathColumn.add("/entry/detector/detector");
		pathColumn.add("/entry/x/x");
		pathColumn.add("/entry/y/y");
		columns.put("path", pathColumn);
		LinkedList<Object> uniqueidColumn = new LinkedList<>();
		uniqueidColumn.add("/entry/NDAttributes/NDArrayUniqueId");
		uniqueidColumn.add("/entry/NDAttributes/NDArrayUniqueId");
		uniqueidColumn.add("/entry/NDAttributes/NDArrayUniqueId");
		uniqueidColumn.add("/entry/NDAttributes/NDArrayUniqueId");
		columns.put("uniqueid", uniqueidColumn);
		Map<String, Class<?>> types = new LinkedHashMap<>();
		types.put("name", String.class);
		types.put("filename", String.class);
		types.put("type", String.class);
		types.put("path", String.class);
		types.put("uniqueid", String.class);
		MalcolmTable table = new MalcolmTable(columns, types);
		datasets.setValue(table);
		datasets.setHeadings(new String[] { "name", "filename", "type", "path", "uniqueid" });
		datasets.setName("datasets");
		datasets.setLabel("datasets");
		datasets.setDescription("Datasets produced in HDF file");
		datasets.setWriteable(false);
		allAttributes.add(datasets);
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
		NXdetector detector = createNexusObject(info);
		NexusObjectWrapper<NXdetector> nexusProvider = new NexusObjectWrapper<>(getName(), detector);

		// "data" is the name of the primary data field (i.e. the 'signal' field
		// of the default NXdata)
		nexusProvider.setPrimaryDataFieldName(NXdetector.NX_DATA);
		// An additional NXdata group with "spectrum" as the signal to hold the
		// 1D spectrum data
		nexusProvider.addAdditionalPrimaryDataFieldName(FIELD_NAME_SPECTRUM);
		// An additional NXdata group with "value" as the signal to hold the
		// Mandelbrot value
		nexusProvider.addAdditionalPrimaryDataFieldName(FIELD_NAME_VALUE);

		// Add the axes to the image and spectrum data. scanRank here
		// corresponds to the position
		// in the axes attribute written in the NeXus file (0 based)
		int scanRank = info.getRank();
		nexusProvider.addAxisDataFieldForPrimaryDataField(FIELD_NAME_REAL_AXIS, NXdetector.NX_DATA, scanRank);
		nexusProvider.addAxisDataFieldForPrimaryDataField(FIELD_NAME_IMAGINARY_AXIS, NXdetector.NX_DATA, scanRank + 1);
		nexusProvider.addAxisDataFieldForPrimaryDataField(FIELD_NAME_SPECTRUM_AXIS, FIELD_NAME_SPECTRUM, scanRank);

		return nexusProvider;
	}

	private NXdetector createNexusObject(NexusScanInfo info) throws NexusException {
		final NXdetector detector = NexusNodeFactory.createNXdetector();

		return detector;
	}

	@Override
	public void configure(TestMalcolmModel model) throws ScanningException {
		setDeviceState(DeviceState.CONFIGURING);

		generator = model.getGenerator();
		filePath = model.getFilePath();

		try {
			if (!filePath.endsWith("/")) {
				filePath += "/";
			}
			datasets.getValue().getColumn("filename").set(0,
					filePath + datasets.getValue().getColumn("filename").get(0));
			datasets.getValue().getColumn("filename").set(1,
					filePath + datasets.getValue().getColumn("filename").get(1));
			datasets.getValue().getColumn("filename").set(2,
					filePath + datasets.getValue().getColumn("filename").get(2));
			datasets.getValue().getColumn("filename").set(3,
					filePath + datasets.getValue().getColumn("filename").get(3));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		totalSteps.setValue(64);
		configuredSteps.setValue(64);
		// super.configure sets device state to ready
		super.configure(model);
	}

	@Override
	public void run(IPosition pos) throws ScanningException, InterruptedException {
		setDeviceState(DeviceState.RUNNING);
		status.setValue("Running");
		completedSteps.setValue(totalSteps.getValue());
		write(pos);
		status.setValue("Finished writing");
	}

	private void createNexusFiles() throws Exception {
		INexusFileFactory nexusFileFactory = new NexusFileFactoryHDF5();

		String filename0 = datasets.getValue().getColumn("filename").get(0).toString();

		final TreeFileImpl treeFile0 = NexusNodeFactory.createTreeFile(filename0);

		NXroot root0 = NexusNodeFactory.createNXroot();
		root0.setAttributeFile_name(filename0);
		root0.setAttributeFile_time("2016-10-31T08:45:10.123456");
		root0.setAttributeNeXus_version("4.3.0");
		root0.setAttributeHDF5_Version("1.8.9");
		root0.setAttribute(null, "h5py_version", "2.3.0");

		// create the single entry object of the nexus file
		NXentry entry0 = NexusNodeFactory.createNXentry();
		root0.setEntry(entry0);

		NXdata detectorDataGroup = NexusNodeFactory.createNXdata();
		NXdata statsTotalDataGroup = NexusNodeFactory.createNXdata();
		entry0.setData("detector", detectorDataGroup);
		entry0.setData("StatsTotal", statsTotalDataGroup);

		IDataset image = org.eclipse.january.dataset.Random.rand(new int[] { 900, 1000 });

		detectorDataGroup.setDataset("detector", image);

		IDataset image2 = org.eclipse.january.dataset.Random.rand(new int[] { 960, 1280 });

		statsTotalDataGroup.setDataset("StatsTotal", image2);

		treeFile0.setGroupNode(root0);

		NexusFile file = nexusFileFactory.newNexusFile(treeFile0.getFilename(), false);
		file.createAndOpenToWrite();

		file.addNode("/", treeFile0.getGroupNode());
		file.flush();

		// Second file

		String filename1 = datasets.getValue().getColumn("filename").get(2).toString();

		final TreeFileImpl treeFile1 = NexusNodeFactory.createTreeFile(filename1);

		NXroot root1 = NexusNodeFactory.createNXroot();
		root1.setAttributeFile_name(filename1);
		root1.setAttributeFile_time("2016-10-31T08:45:11.939912");
		root1.setAttributeNeXus_version("4.3.0");
		root1.setAttributeHDF5_Version("1.8.9");
		root1.setAttribute(null, "h5py_version", "2.3.0");

		// create the single entry object of the nexus file
		NXentry entry1 = NexusNodeFactory.createNXentry();
		root1.setEntry(entry1);

		NXdata xDataGroup = NexusNodeFactory.createNXdata();
		NXdata yDataGroup = NexusNodeFactory.createNXdata();
		entry1.setData("x", xDataGroup);
		entry1.setData("y", yDataGroup);

		IDataset image3 = org.eclipse.january.dataset.Random.rand(new int[] { 460, 980 });

		xDataGroup.setDataset("x", image3);

		IDataset image4 = org.eclipse.january.dataset.Random.rand(new int[] { 1000, 1000 });

		yDataGroup.setDataset("y", image4);

		treeFile1.setGroupNode(root1);

		NexusFile file1 = nexusFileFactory.newNexusFile(treeFile1.getFilename(), false);
		file1.createAndOpenToWrite();

		file1.addNode("/", treeFile1.getGroupNode());
		file1.flush();
	}

	@Override
	public boolean write(IPosition pos) throws ScanningException {
		try {
			createNexusFiles();

		} catch (Exception e) {
			// Change state to fault if exception is caught
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Failed to write the data to the NeXus file", e);
		}

		// Finished writing set state back to ready
		setDeviceState(DeviceState.READY);
		return true;
	}

	@Override
	public void addMalcolmListener(IMalcolmListener<?> l) {
		// TODO Auto-generated method stub
		System.out.println("add ML called");
	}

	@Override
	public void removeMalcolmListener(IMalcolmListener<?> l) {
		// TODO Auto-generated method stub
		System.out.println("rem ML called");

	}

	@Override
	public void dispose() throws MalcolmDeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public DeviceState latch(long time, TimeUnit unit, DeviceState... ignoredStates) throws MalcolmDeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLocked() throws MalcolmDeviceException {
		// TODO Auto-generated method stub
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
		for (MalcolmAttribute malcolmAttribute : allAttributes) {
			if (malcolmAttribute.getName().equals(attribute)) {
				return malcolmAttribute.getValue();
			}
		}
		return null;
	}

	@Override
	public Object getAttribute(String attribute) throws ScanningException {
		System.out.println("getAttribute called");
		updateAttributeswithLatestValues();
		for (MalcolmAttribute malcolmAttribute : allAttributes) {
			if (malcolmAttribute.getName().equals(attribute)) {
				return malcolmAttribute;
			}
		}
		return null;
	}

	@Override
	public List<MalcolmAttribute> getAllAttributes() throws ScanningException {
		System.out.println("getAllAttributes called");
		updateAttributeswithLatestValues();
		return allAttributes;
	}

	private void updateAttributeswithLatestValues() throws ScanningException {
		state.setValue(getDeviceState().toString());
		status.setValue(getDeviceStatus());
		busy.setValue(isDeviceBusy());
	}
}
