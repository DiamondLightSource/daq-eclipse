package org.eclipse.scanning.example.detector;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.DelegateNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * This device mimicks telling EPICS to do a constant velcity scan down a line.
 * It basically is like a 1D malcolm device running in a scan. This allows I18
 * to continue doing a CVscan with the new scanning for mapping by creating a 
 * device to do the cv scan.
 * 
 * It writes an HDF5 file for the line (actually random data)
 * 
 * @author Matthew Gerring
 *
 */
public class ConstantVelocityDevice extends AbstractRunnableDevice<ConstantVelocityModel> implements IWritableDetector<ConstantVelocityModel>, INexusDevice<NXdetector> {

	private ILazyWriteableDataset context;
	private IDataset              data;

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) {
		return new DelegateNexusProvider<NXdetector>(getName(), NexusBaseClass.NX_DETECTOR, info, this);
	}

	@Override
	public NXdetector createNexusObject(NexusNodeFactory nodeFactory, NexusScanInfo info) {
		
		final NXdetector detector = nodeFactory.createNXdetector();
		// We add 2 to the scan rank to include the image
		int rank = info.getRank()+3; // scan rank plus three dimensions for the CV scan.
		
		context = detector.initializeLazyDataset(NXdetector.NX_DATA, rank, Dataset.FLOAT64);
		
		// Setting chunking is a very good idea if speed is required.
		int[] chunk = info.createChunk(model.getLineSize(), model.getChannelCount(), model.getSpectraSize());
		context.setChunking(chunk);
		
		try {
			Attributes.registerAttributes(detector, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return detector;
	}

	@Override
	public void run(IPosition pos) throws ScanningException, InterruptedException {
		// TODO Real device would tell EPICS to run the line scan now.
		// To simulate this, we create a line using the definition in the model
		// EPICS might write an HDF5 file with this data rather than the data 
		// being in memory.
		data = Random.rand(new int[]{model.getLineSize(), model.getChannelCount(), model.getSpectraSize()});
	}

	@Override
	public boolean write(IPosition pos) throws ScanningException {
		try {
			// In a real CV Scan the write step could be to either link in the HDF5 or read in its data 
			// and write a new record. Avoiding reading in the HDF5 being preferable.
			SliceND sliceND = NexusScanInfo.createLocation(context, pos.getNames(), pos.getIndices(), model.getLineSize(), model.getChannelCount(), model.getSpectraSize());
			context.setSlice(null, data, sliceND);

		} catch (Exception e) {
			throw new ScanningException(e.getMessage(), e); 
		}

		return true;
	}

	@Override
	public void configure(ConstantVelocityModel model) throws ScanningException {	
		super.configure(model);
		setName(model.getName());
	}

}
