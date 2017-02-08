package org.eclipse.scanning.sequencer.nexus;

import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_CMD;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_FINISHED;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_MODELS;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_RANK;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_UNIQUE_KEYS;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.GROUP_NAME_KEYS;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.GROUP_NAME_SOLSTICE_SCAN;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.PROPERTY_NAME_UNIQUE_KEYS_PATH;

import java.io.File;
import java.util.List;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.LazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The scan points writer creates and writes to the unique keys and points
 * datasets in a nexus file. The unique keys dataset can be used to track how far the scan has
 * progressed.
 * @author Matthew Dickie
 */
public class ScanPointsWriter implements INexusDevice<NXcollection>, IPositionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ScanPointsWriter.class);

	// Nexus
	private List<NexusObjectProvider<?>> nexusObjectProviders = null;
	private NexusObjectWrapper<NXcollection> nexusProvider = null;
	
	// Writing Datasets
	private ILazyWriteableDataset uniqueKeys = null;
	private ILazyWriteableDataset scanFinished = null;

	// State
	private boolean malcolmScan = false;
	private final ScanModel model;
	
	public ScanPointsWriter(ScanModel model) {
		this.model = model;
	}

	public void setNexusObjectProviders(List<NexusObjectProvider<?>> nexusObjectProviders) {
		this.nexusObjectProviders = nexusObjectProviders;
	}
	
	public void setMalcolmScan(boolean malcolmScan) {
		this.malcolmScan = malcolmScan;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.INexusDevice#getNexusProvider(org.eclipse.dawnsci.nexus.NexusScanInfo)
	 */
	@Override
	public NexusObjectProvider<NXcollection> getNexusProvider(NexusScanInfo info) {
		
		// Note: NexusScanFileManager relies on this method returning the same object each time
		if (nexusProvider == null) {
			NXcollection scanPointsCollection = createNexusObject(info);
			nexusProvider = new NexusObjectWrapper<NXcollection>(GROUP_NAME_SOLSTICE_SCAN, scanPointsCollection);
			nexusProvider.setPrimaryDataFieldName(FIELD_NAME_UNIQUE_KEYS);
			nexusProvider.setAxisDataFieldNames(FIELD_NAME_UNIQUE_KEYS);
		}
		
		return nexusProvider;
	}

	public NXcollection createNexusObject(NexusScanInfo info) {
		final NXcollection scanPointsCollection = NexusNodeFactory.createNXcollection();
		
		// add a field for the scan rank
		scanPointsCollection.setField(FIELD_NAME_SCAN_RANK, info.getRank());
		
		try {
		    String cmd = ServiceHolder.getParserService().getCommand(model.getBean().getScanRequest(), true);
			scanPointsCollection.setField(FIELD_NAME_SCAN_CMD,  cmd);
		} catch (Exception ne) {
			logger.error("Unable to write scan command", ne);
		}
		
		try {
			List<?> models = model.getBean().getScanRequest().getCompoundModel().getModels();
			String json = ServiceHolder.getMarshallerService().marshal(models);
			scanPointsCollection.setField(FIELD_NAME_SCAN_MODELS, json);
		} catch (Exception ne) {
			logger.error("Unable to write point models", ne);
		}

		// create the scan finished dataset and set the initial value to false
//		scanFinished = scanPointsCollection.initializeFixedSizeLazyDataset(
//				FIELD_NAME_SCAN_FINISHED, new int[] { 1 }, Dataset.INT32);
		// TODO: workaround for bug in HD5 loader, do not set size limit 
		scanFinished = new LazyWriteableDataset(FIELD_NAME_SCAN_FINISHED, Integer.class,
				new int[] { 1 }, new int[] { -1 }, new int[] { 1 }, null);
		scanFinished.setFillValue(0);
		scanPointsCollection.createDataNode(FIELD_NAME_SCAN_FINISHED, scanFinished);

		// create a sub-collection for the unique keys field and keys from each external file
		final NXcollection keysCollection = NexusNodeFactory.createNXcollection();
		scanPointsCollection.addGroupNode(GROUP_NAME_KEYS, keysCollection);
		
		// create the unique keys dataset (not for malcolm scans)
		if (!malcolmScan) {
			uniqueKeys = keysCollection.initializeLazyDataset(FIELD_NAME_UNIQUE_KEYS, info.getRank(), Integer.class);
		}

		// set chunking for lazy datasets
		if (info.getRank() > 0) {
			final int[] chunk = info.createChunk(false, 8);
			if (!malcolmScan) {
				uniqueKeys.setFillValue(0);
				uniqueKeys.setChunking(chunk);
			}
		}
		
		// add external links to the unique key datasets for each external HD5 file
		addLinksToExternalFiles(keysCollection);
		
		return scanPointsCollection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.scanning.api.scan.event.IPositionListener#positionPerformed(org.eclipse.scanning.api.scan.PositionEvent)
	 */
	@Override
	public void positionPerformed(PositionEvent event) throws ScanningException {
		try {
			writePosition(event.getPosition());
		} catch (Exception e) {
			throw new ScanningException("Could not write position to NeXus file", e);
		}
	}

	/**
	 * Called when the scan completes to write '1' to the scan finished dataset.
	 * @throws ScanningException
	 */
	public void scanFinished() throws ScanningException {
		// Note: we don't use IRunListener.runPerformed as other run listeners expect the
		// file to be closed when that method is called
		final Dataset dataset = DatasetFactory.createFromObject(IntegerDataset.class, 1, null);
		try {
			this.scanFinished.setSlice(null, dataset,
					new int[] { 0 }, new int[] { 1 }, new int[] { 1 });
		} catch (Exception e) {
			throw new ScanningException("Could not write scanFinished to NeXus file", e);
		}
	}

	/**
	 * For each device, if that device writes to an external file, create an external link
	 * within the unique keys collection to the unique keys dataset in that file.
	 * The unique keys are required in order for live processing to take place - we need to know
	 * how much data has been written to each file - devices may flush their data to file at
	 * different times.
	 * @param uniqueKeysCollection unique keys collection to add any external links to
	 */
	private void addLinksToExternalFiles(final NXcollection uniqueKeysCollection) {
		if (nexusObjectProviders == null) throw new IllegalStateException("nexusObjectProviders not set");
		
		for (NexusObjectProvider<?> nexusObjectProvider : nexusObjectProviders) {
			String uniqueKeysPath = (String) nexusObjectProvider.getPropertyValue(PROPERTY_NAME_UNIQUE_KEYS_PATH);
			if (uniqueKeysPath != null) {
				for (String externalFileName : nexusObjectProvider.getExternalFileNames()) {
					// we just use the final segment of the file name as the dataset name,
					// This assumes that we won't have files with the same name in different dirs
					// Note: the name doesn't matter for processing purposes 
					String datasetName = new File(externalFileName).getName();
					if (uniqueKeysCollection.getSymbolicNode(datasetName) == null) {
						uniqueKeysCollection.addExternalLink(datasetName, externalFileName,
								uniqueKeysPath);
					}
				}
			}
		}
	}

	/**
	 * Write the given position to the NexusFile.
	 * The unique key of the position is added to the <code>uniqueKeys</code> dataset,
	 * and the position as a string, as returned by {@link IPosition#toString()},
	 * is added to the <code>points</code>
	 * @param position
	 * @throws Exception
	 */
	private void writePosition(IPosition position) throws Exception {
		if (!malcolmScan) {
			IScanSlice rslice = IScanRankService.getScanRankService().createScanSlice(position);
			SliceND sliceND = new SliceND(uniqueKeys.getShape(), uniqueKeys.getMaxShape(), rslice.getStart(), rslice.getStop(), rslice.getStep());
			final int uniqueKey = position.getStepIndex() + 1;
			final Dataset newActualPosition = DatasetFactory.createFromObject(uniqueKey);
			uniqueKeys.setSlice(null, newActualPosition, sliceND);
		}
	}

}