package org.eclipse.scanning.test.scan.mock;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.ScanningException;

public class MockReadableDetector extends AbstractRunnableDevice<MockDetectorModel> implements IWritableDetector<MockDetectorModel> {
	
	public MockReadableDetector() {
		super();
	}
	
	public MockReadableDetector(String name) {
		super();
		setName(name);
	}
		
	@Override
	public void run(IPosition pos) throws ScanningException {
		try {
			Thread.sleep((long)(getModel().getCollectionTime()*1000));
			getModel().setRan(getModel().getRan()+1);
		} catch (Exception ne) {
			throw new ScanningException("Cannot to do readout", ne);
		}
	}
	
	@Override
	public boolean write(IPosition position) throws ScanningException {
		
		IDataset next = Random.rand(new int[]{1024, 1024});
		getModel().setRead(getModel().getRead()+1);
		// TODO write next somewhere?
		
		return true;
	}

	@Override
	public String toString() {
		return "MockDetector [level=" + getLevel() + ", name=" + getName() +  "]";
	}

	@Override
	public void configure(MockDetectorModel model) throws ScanningException {
		setModel(model);
	}

	@Override
	public void abort() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}

	@Override
	public void pause() throws ScanningException {
        try {
	        setState(DeviceState.PAUSING);
	        Thread.sleep(100);
	        setState(DeviceState.PAUSED);
		} catch (Exception e) {
			throw new ScanningException(e);
		}
	}

	@Override
	public void resume() throws ScanningException {
        try {
			setState(getModel()!=null ? DeviceState.READY : DeviceState.IDLE);
		} catch (Exception e) {
			throw new ScanningException(this, e);
		}
  	}
}
