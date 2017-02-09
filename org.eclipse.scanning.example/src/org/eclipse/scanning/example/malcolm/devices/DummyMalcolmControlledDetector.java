package org.eclipse.scanning.example.malcolm.devices;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.example.malcolm.DummyMalcolmControlledDetectorModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDatasetModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dummy malcolm controlled detector which writes nexus. 
 */
public final class DummyMalcolmControlledDetector extends DummyMalcolmControlledDevice {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyMalcolmControlledDetector.class);
	
	private final DummyMalcolmControlledDetectorModel model;
	private final List<String> axesToMove;
	
	public DummyMalcolmControlledDetector(String name, DummyMalcolmControlledDetectorModel model, List<String> axesToMove) {
		super(name);
		this.model = model;
		this.axesToMove = axesToMove;
	}

	@Override
	public void createNexusFile(String dirPath, int scanRank) throws NexusException {
		final String filePath = dirPath + model.getName() + DummyMalcolmDevice.FILE_EXTENSION_HDF5;
		logger.info("Creating nexus file " + filePath);
		final TreeFile treeFile = NexusNodeFactory.createTreeFile(filePath);
		final NXroot root = NexusNodeFactory.createNXroot();
		treeFile.setGroupNode(root);
		final NXentry entry = NexusNodeFactory.createNXentry();
		root.setEntry(entry);
		
		// add an entry to the unique keys collection
		final String[] uniqueKeysDatasetPathSegments = DummyMalcolmDevice.UNIQUE_KEYS_DATASET_PATH.split("/");
		final NXcollection ndAttributesCollection = NexusNodeFactory.createNXcollection();
		entry.setCollection(uniqueKeysDatasetPathSegments[2], ndAttributesCollection);
		addDataset(DummyMalcolmDevice.DATASET_NAME_UNIQUE_KEYS, ndAttributesCollection.initializeLazyDataset(
				uniqueKeysDatasetPathSegments[3], scanRank, String.class), scanRank); 
		
		// create an NXdata 
		final Map<String, DataNode> axesDemandDataNodes = new HashMap<>();
		for (DummyMalcolmDatasetModel datasetModel : model.getDatasets()) {
			final String datasetName = datasetModel.getName();
			final NXdata dataGroup = NexusNodeFactory.createNXdata();
			entry.setData(datasetName, dataGroup);
			// initialize the dataset. The scan rank is added to the dataset rank 
			
			addDataset(datasetName,  dataGroup.initializeLazyDataset(datasetName, 
					scanRank + datasetModel.getRank(), datasetModel.getDtype()), scanRank, getDataShape(datasetModel));
			// add the demand values for the axes
			for (String axisName : axesToMove) {
				DataNode axisDemandDataNode = axesDemandDataNodes.get(axisName);
				final String dataNodeName = axisName + "_set";
				if (axisDemandDataNode == null) {
					// create demand dataset (has rank 1)
					addDataset(axisName, dataGroup.initializeLazyDataset(dataNodeName, 1, Double.class), scanRank);
					axisDemandDataNode = dataGroup.getDataNode(dataNodeName);
					axesDemandDataNodes.put(axisName, axisDemandDataNode);
				} else {
					// create a link to the existing demand dataset in the same file
					dataGroup.addDataNode(dataNodeName, axisDemandDataNode);
				}
			}
		}
		
		// save the nexus tree to disk
		nexusFile = saveNexusFile(treeFile);
	}
	
	private static int[] getDataShape(DummyMalcolmDatasetModel datasetModel) {
		int[] shape = datasetModel.getShape();
		if (shape == null) {
			shape = new int[datasetModel.getRank()];
			Arrays.fill(shape, 64); // e.g. a 64x64 image if rank is 2
			datasetModel.setShape(shape);
		}
		
		return shape;
	}
	
	@Override
	public void writePosition(IPosition position) throws Exception {
		for (DummyMalcolmDatasetModel datasetModel : model.getDatasets()) {
			// create the data to write into the dataset
			final int[] dataShape = getDataShape(datasetModel);
			final IDataset data = Random.rand(dataShape);
			writeData(datasetModel.getName(), position, data);
		}
		
		// write the demand position for each malcolm controlled axis
		for (String axisName : axesToMove) {
			writeDemandData(axisName, position);
		}
		
		// write unique key
		final int uniqueKey = position.getStepIndex() + 1;
		final IDataset newPositionData = DatasetFactory.createFromObject(uniqueKey);
		writeData(DummyMalcolmDevice.DATASET_NAME_UNIQUE_KEYS, position, newPositionData);
		nexusFile.flush();
	}

	@Override
	public String getName() {
		return model.getName();
	}

	public DummyMalcolmControlledDetectorModel getModel() {
		return model;
	}

	@Override
	public List<DummyMalcolmDatasetModel> getDatasetModels() {
		return model.getDatasets();
	}

	@Override
	public String toString() {
		return "DummyMalcolmControlledDetector [name=" + getName() + ", model=" + model + ", axesToMove=" + axesToMove + "]";
	}
}