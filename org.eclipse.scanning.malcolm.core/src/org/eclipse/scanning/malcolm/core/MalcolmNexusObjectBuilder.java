package org.eclipse.scanning.malcolm.core;

import static org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice.DATASETS_TABLE_COLUMN_FILENAME;
import static org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice.DATASETS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice.DATASETS_TABLE_COLUMN_PATH;
import static org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice.DATASETS_TABLE_COLUMN_RANK;
import static org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice.DATASETS_TABLE_COLUMN_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmTable;

/**
 * A helper class that knows how to build the NeXus objects and the {@link NexusObjectProvider}s
 * that wrap and describe them for an {@link IMalcolmDevice}.
 * 
 * @author Matthew Dickie
 */
class MalcolmNexusObjectBuilder {

	private Map<String, NexusObjectWrapper<NXobject>> nexusWrappers;
	
	public List<NexusObjectProvider<?>> buildNexusObjects(MalcolmTable datasetsTable,
			NexusScanInfo scanInfo) {
		nexusWrappers = new HashMap<>();
		
		for (Map<String, Object> datasetRow : datasetsTable) {
			final String datasetFullName = (String) datasetRow.get(DATASETS_TABLE_COLUMN_NAME);
			final String filename = (String) datasetRow.get(DATASETS_TABLE_COLUMN_FILENAME);
			final String datasetPath = (String) datasetRow.get(DATASETS_TABLE_COLUMN_PATH);
			final int datasetRankPerPos = ((Integer) datasetRow.get(DATASETS_TABLE_COLUMN_RANK)).intValue();
			final MalcolmDatasetType datasetType = MalcolmDatasetType.valueOf(
					((String) datasetRow.get(DATASETS_TABLE_COLUMN_TYPE)).toUpperCase());

			final int datasetRank = scanInfo.getRank() + datasetRankPerPos;
			final String[] nameSegments = datasetFullName.split("\\.");
			final String deviceName = nameSegments[0];
			final String datasetName = nameSegments[1];
			
			// get the nexus object and its wrapper, creating it if necessary
			NexusObjectWrapper<NXobject> nexusWrapper = getNexusProvider(deviceName,
					datasetType.getNexusBaseClass(), filename);
			NXobject nexusObject = nexusWrapper.getNexusObject();

			// configure the nexus wrapper to describe the wrapped nexus object appropriately 
			nexusWrapper.addExternalLink(nexusObject, datasetName, datasetPath, datasetRank);
			switch (datasetType) {
				case PRIMARY: {
					nexusWrapper.setPrimaryDataFieldName(datasetName);
					break;
				}
				case SECONDARY: {
					nexusWrapper.addAdditionalPrimaryDataFieldName(datasetName);
					break;
				}
				case MONITOR: {
					nexusWrapper.addAxisDataFieldName(datasetName);
					break;
				}
				case POSITION_VALUE: {
					nexusWrapper.addAxisDataFieldName(datasetName);
					break;
				}
				case POSITION_SET: {
					nexusWrapper.addAxisDataFieldName(datasetName);
					nexusWrapper.setDefaultAxisDataFieldName(datasetName);
					break;
				}
			}
		}
		
		return new ArrayList<>(nexusWrappers.values());
	}
	
	private NexusObjectWrapper<NXobject> getNexusProvider(String deviceName,
			NexusBaseClass nexusBaseClass, String filename) {
		if (nexusWrappers.containsKey(deviceName)) {
			return nexusWrappers.get(deviceName);
		}
		
		final NexusObjectWrapper<NXobject> nexusWrapper = createNexusWrapper(deviceName,
				nexusBaseClass, filename);
		nexusWrappers.put(deviceName, nexusWrapper);
		
		return nexusWrapper;
	}

	private NexusObjectWrapper<NXobject> createNexusWrapper(String deviceName,
			NexusBaseClass nexusBaseClass, String filename) {
		NXobject nexusObject = NexusNodeFactory.createNXobjectForClass(nexusBaseClass);
		NexusObjectWrapper<NXobject> nexusProvider = new NexusObjectWrapper<>(deviceName, nexusObject);
		nexusProvider.setExternalFileName(filename);
		
		return nexusProvider;
	}
	

}
