package org.eclipse.scanning.sequencer.nexus;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.DelegateNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;

public class ScanPointsWriter implements INexusDevice<NXcollection>, IPositionListener {

	public static final String GROUP_NAME_SOLSTICE_SCAN = "solstice_scan";
	
	public static final String FIELD_NAME_UNIQUE_KEYS = "uniqueKeys";
	
	public static final String FIELD_NAME_POINTS = "points";
	
	public static final String UNIQUE_KEYS_PATH_IN_EXTERNAL_FILE = "/entry/NDArrayUniqueId/NDArrayUniqueId";

	private List<NexusObjectProvider<?>> nexusObjectProviders = null;
	
	private ILazyWriteableDataset uniqueKeys = null;

	private ILazyWriteableDataset points = null;
	
	public void setNexusObjectProviders(List<NexusObjectProvider<?>> nexusObjectProviders) {
		this.nexusObjectProviders = nexusObjectProviders;
	}
	
	@Override
	public NexusObjectProvider<NXcollection> getNexusProvider(NexusScanInfo info) {
		DelegateNexusProvider<NXcollection> nexusProvider = new DelegateNexusProvider<NXcollection>(
				GROUP_NAME_SOLSTICE_SCAN, NexusBaseClass.NX_COLLECTION,
				info, this);
		nexusProvider.setPrimaryDataField(FIELD_NAME_UNIQUE_KEYS);
		nexusProvider.setDataFields(FIELD_NAME_UNIQUE_KEYS, FIELD_NAME_POINTS);
		
		return nexusProvider;
	}

	@Override
	public NXcollection createNexusObject(NexusNodeFactory nodeFactory, NexusScanInfo info) {
		final NXcollection scanPointsCollection = nodeFactory.createNXcollection();
		uniqueKeys = scanPointsCollection.initializeLazyDataset(
				FIELD_NAME_UNIQUE_KEYS, info.getRank(), Dataset.INT32);
		points = scanPointsCollection.initializeLazyDataset(
				FIELD_NAME_POINTS, info.getRank(), Dataset.STRING);
		
		// set chunking
		final int[] chunk = new int[info.getRank()];
		Arrays.fill(chunk, 1);
		uniqueKeys.setChunking(chunk);
		points.setChunking(chunk);
		
		// add external links to the unique key datasets for each external HD5 file
		addLinksToExternalFiles(scanPointsCollection);
		
		return scanPointsCollection;
	}

	@Override
	public void positionPerformed(PositionEvent event) throws ScanningException {
		try {
			writePosition(event.getPosition());
		} catch (Exception e) {
			throw new ScanningException("Could not write position to NeXus file", e);
		}
	}
	
	@Override
	public boolean positionWillPerform(PositionEvent event) {
		return true;
	}

	@Override
	public void levelPerformed(PositionEvent event) {
		// Do nothing
	}

	private void addLinksToExternalFiles(final NXcollection scanPointsCollection) {
		if (nexusObjectProviders == null) throw new IllegalStateException("nexusObjectProviders not set");
		
		for (NexusObjectProvider<?> nexusObjectProvider : nexusObjectProviders) {
			String externalFileName = nexusObjectProvider.getExternalFileName();
			if (externalFileName != null) {
				// TODO check handling of slashes, should '..' also be handled?
				// also chop off file extension?  
				String datasetName = externalFileName.replace("/", "__");
				scanPointsCollection.addExternalLink(externalFileName, datasetName,
						UNIQUE_KEYS_PATH_IN_EXTERNAL_FILE);
			}
		}
	}

	private void writePosition(IPosition position) throws Exception {
		SliceND sliceND = NexusScanInfo.createLocation(uniqueKeys,
				position.getNames(), position.getIndices());
		
		final int stepIndex = position.getStepIndex();
		final Dataset newActualPosition = DatasetFactory.createFromObject(stepIndex);
		uniqueKeys.setSlice(null, newActualPosition, sliceND);
		
		final Dataset point = DatasetFactory.createFromObject(position.toString());
		points.setSlice(null, point, sliceND);
	}

}