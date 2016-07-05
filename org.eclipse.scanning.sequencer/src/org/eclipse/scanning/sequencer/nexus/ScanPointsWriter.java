package org.eclipse.scanning.sequencer.nexus;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.Dtype;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyWriteableDataset;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;

/**
 * The scan points writer creates and writes to the unique keys and points
 * datasets in a nexus file. The unique keys dataset can be used to track how far the scan has
 * progressed.
 * @author Matthew Dickie
 */
public class ScanPointsWriter implements INexusDevice<NXcollection>, IPositionListener {

	public static final String GROUP_NAME_SOLSTICE_SCAN = "solstice_scan";
	
	public static final String FIELD_NAME_UNIQUE_KEYS = "uniqueKeys";
	
	public static final String FIELD_NAME_POINTS = "points";
	
	public static final String FIELD_NAME_SCAN_FINISHED = "scan_finished";
	
	public static final String UNIQUE_KEYS_PATH_IN_EXTERNAL_FILE = "/entry/NDArrayUniqueId/NDArrayUniqueId";

	private List<NexusObjectProvider<?>> nexusObjectProviders = null;
	
	private ILazyWriteableDataset uniqueKeys = null;

	private ILazyWriteableDataset points = null;
	
	private ILazyWriteableDataset scanFinished = null;

	private NexusObjectWrapper<NXcollection> nexusProvider = null;
	
	public void setNexusObjectProviders(List<NexusObjectProvider<?>> nexusObjectProviders) {
		this.nexusObjectProviders = nexusObjectProviders;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.nexus.INexusDevice#getNexusProvider(org.eclipse.dawnsci.nexus.NexusScanInfo)
	 */
	@Override
	public NexusObjectProvider<NXcollection> getNexusProvider(NexusScanInfo info) {
		// Note: NexusScanFileManager relies on this method returning the same object each time
		if (nexusProvider == null) {
			NXcollection scanPointsCollection = createNexusObject(info);
			nexusProvider = new NexusObjectWrapper<NXcollection>(
					GROUP_NAME_SOLSTICE_SCAN, scanPointsCollection);
			nexusProvider.setPrimaryDataFieldName(FIELD_NAME_UNIQUE_KEYS);
			nexusProvider.setAxisDataFieldNames(FIELD_NAME_UNIQUE_KEYS, FIELD_NAME_POINTS);
		}
		
		return nexusProvider;
	}

	public NXcollection createNexusObject(NexusScanInfo info) {
		final NXcollection scanPointsCollection = NexusNodeFactory.createNXcollection();
		// create the unique keys and scan points datasets
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

		// create the scan finished dataset and set the initial value to false
//		scanFinished = scanPointsCollection.initializeFixedSizeLazyDataset(
//				FIELD_NAME_SCAN_FINISHED, new int[] { 1 }, Dataset.INT32);
		// TODO: workaround for bug in HD5 loader, do not set size limit 
		scanFinished = new LazyWriteableDataset(FIELD_NAME_SCAN_FINISHED, Dtype.INT32, new int[] { 1 },
				new int[] { -1 }, null, null);
		scanFinished.setFillValue(0);
		scanPointsCollection.createDataNode(FIELD_NAME_SCAN_FINISHED, scanFinished);
		
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
		final Dataset dataset = IntegerDataset.createFromObject(1);
		try {
			this.scanFinished.setSlice(null, dataset,
					new int[] { 0 }, new int[] { 1 }, new int[] { 1 });
		} catch (Exception e) {
			throw new ScanningException("Could not write scanFinished to NeXus file", e);
		}
	}

	/**
	 * For each device, if that device writes to an external file, create an external link
	 * within the scan points collection to the unique keys dataset in that file
	 * @param scanPointsCollection scan points collection to add any external links to
	 */
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

	/**
	 * Write the given position to the NexusFile.
	 * The unique key of the position is added to the <code>uniqueKeys</code> dataset,
	 * and the position as a string, as returned by {@link IPosition#toString()},
	 * is added to the <code>points</code>
	 * @param position
	 * @throws Exception
	 */
	private void writePosition(IPosition position) throws Exception {
		IScanSlice rslice = IScanRankService.getScanRankService().createScanSlice(position);
		SliceND sliceND = new SliceND(uniqueKeys.getShape(), uniqueKeys.getMaxShape(), rslice.getStart(), rslice.getStop(), rslice.getStep());
		
		final int uniqueKey = position.getStepIndex() + 1;
		final Dataset newActualPosition = DatasetFactory.createFromObject(uniqueKey);
		uniqueKeys.setSlice(null, newActualPosition, sliceND);
		
		final Dataset point = DatasetFactory.createFromObject(position.toString());
		points.setSlice(null, point, sliceND);
	}

}