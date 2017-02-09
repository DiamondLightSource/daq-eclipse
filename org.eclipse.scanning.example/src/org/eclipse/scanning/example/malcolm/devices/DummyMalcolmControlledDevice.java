package org.eclipse.scanning.example.malcolm.devices;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;

/**
 * Abstract superclass for a dummy malcolm controlled device which writes nexus.
 */
public abstract class DummyMalcolmControlledDevice implements IDummyMalcolmControlledDevice {
	
	private final String name;

	private Map<String, ILazyWriteableDataset> datasets = new HashMap<>();
	
	protected NexusFile nexusFile = null;
	
	public DummyMalcolmControlledDevice(String name) {
		this.name = name;
	}

	protected void addDataset(String datasetName, ILazyWriteableDataset dataset, int scanRank, int... datashape) {
		datasets.put(datasetName, dataset);
		dataset.setChunking(createChunk(dataset, scanRank, datashape));
	}
	
	private int[] createChunk(ILazyWriteableDataset dataset, int scanRank, int... datashape) {
		if (dataset.getRank() == 1) {
			return new int[] { 1 };
		}
		
		final int[] chunk = new int[scanRank + datashape.length];
		Arrays.fill(chunk, 1);
		if (datashape.length > 0) {
			int index = 0;
			for (int i = datashape.length; i > 0; i--) {
				chunk[chunk.length - i] = datashape[index];
				index++;
			}
		} else {
			chunk[chunk.length - 1] = 8;
		}
		return chunk;
	}
	
	protected void writeData(String datasetName, IPosition position, IDataset data) throws DatasetException {
		final ILazyWriteableDataset dataset = datasets.get(datasetName);
		final IScanSlice slice = IScanRankService.getScanRankService().createScanSlice(position, data.getShape());
		final SliceND sliceND = new SliceND(dataset.getShape(), dataset.getMaxShape(),
				slice.getStart(), slice.getStop(), slice.getStep());
		dataset.setSlice(null, data, sliceND);
	}
	
	protected void writeDemandData(String datasetName, IPosition position) throws DatasetException {
		final double demandValue = ((Double) position.get(datasetName)).doubleValue();
		final ILazyWriteableDataset dataset = datasets.get(datasetName);
		
		final int index = position.getIndex(datasetName);
		final int[] startPos = new int[] { index };
		final int[] stopPos = new int[] { index + 1 };
		dataset.setSlice(null, DatasetFactory.createFromObject(demandValue), startPos, stopPos, null);
	}
	
	@Override
	public void closeNexusFile() throws NexusException {
		if (nexusFile!=null) {
			nexusFile.flush();
			nexusFile.close();
		}
	}

	protected static NexusFile saveNexusFile(TreeFile nexusTree) throws NexusException {
		final INexusFileFactory nff = ServiceHolder.getNexusFileFactory();
		final NexusFile file = nff.newNexusFile(nexusTree.getFilename(), true);
		file.createAndOpenToWrite();
		file.addNode("/", nexusTree.getGroupNode());
		file.flush();
		
		return file;
	}

	public String getName() {
		return name;
	}
}