package org.eclipse.scanning.example.malcolm.devices;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXmonitor;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;

/**
 * The panda devices controls the motors and writes nexus for them.
 */
public final class DummyMalcolmControlledPanda extends DummyMalcolmControlledDevice {

	private final List<String> monitorNames;
	private final List<String> axesToMove;

	public DummyMalcolmControlledPanda(List<String> monitorNames, List<String> axesToMove) {
		super();
		this.monitorNames = monitorNames;
		this.axesToMove = axesToMove;
	}

	@Override
	public void createNexusFile(String dirPath, int scanRank) throws NexusException {
		final String filePath = dirPath + "panda" + DummyMalcolmDevice.FILE_EXTENSION_HDF5;
		System.out.println("Dummy malcolm device creating nexus file " + filePath);
		TreeFile treeFile = NexusNodeFactory.createTreeFile(filePath);
		NXroot root = NexusNodeFactory.createNXroot();
		treeFile.setGroupNode(root);
		NXentry entry = NexusNodeFactory.createNXentry();
		root.setEntry(entry);

		// add the positioners to the entry
		for (String axis : axesToMove) {
			// The path to positioner datasets written by malcolm is e.g. /entry/x/x
			NXpositioner positioner = NexusNodeFactory.createNXpositioner();
			entry.addGroupNode(axis, positioner);
			addDataset(axis, positioner.initializeLazyDataset(
					axis, scanRank, Double.class), scanRank);
		}
		
		// add the monitors to the entry
		for (String monitorName : monitorNames) {
			NXmonitor monitor = NexusNodeFactory.createNXmonitor();
			entry.addGroupNode(monitorName, monitor);
			// TODO: if we want non-scalar monitors we'll have to change the model
			addDataset(monitorName, monitor.initializeLazyDataset(
					monitorName, scanRank, Double.class), scanRank);
		}
		
		// add an entry to the unique keys collection
		String[] uniqueKeysDatasetPathSegments = DummyMalcolmDevice.UNIQUE_KEYS_DATASET_PATH.split("/");
		NXcollection ndAttributesCollection = NexusNodeFactory.createNXcollection();
		entry.setCollection(uniqueKeysDatasetPathSegments[2], ndAttributesCollection);
		addDataset(DummyMalcolmDevice.DATASET_NAME_UNIQUE_KEYS, ndAttributesCollection.initializeLazyDataset(
				uniqueKeysDatasetPathSegments[3], scanRank, String.class), scanRank);
		
		nexusFile = saveNexusFile(treeFile);
	}

	@Override
	public void writePosition(IPosition position) throws Exception {
		for (String axis : axesToMove) {
			Object posValue = position.get(axis);
			if (posValue == null) { // a malcolm controlled positioner which is not a axis (maybe aggregated, e.g. one of a group of jacks)
				posValue = Random.rand();
			}
			IDataset data = DatasetFactory.createFromObject(posValue);
			writeData(axis, position, data);
		}
		for (String monitorName : monitorNames) {
			writeData(monitorName, position, Random.rand());
		}
		
		// write unique key
		final int uniqueKey = position.getStepIndex() + 1;
		final IDataset newPositionData = DatasetFactory.createFromObject(uniqueKey);
		writeData(DummyMalcolmDevice.DATASET_NAME_UNIQUE_KEYS, position, newPositionData);
		nexusFile.flush();
	}

	@Override
	public String getName() {
		return "panda";
	}
	
}