package org.eclipse.scanning.test.event.queues.mocks;

import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IPositioner;

public class MockPositioner implements IPositioner {
	
	private IPosition pos;
	private boolean aborted;

	@Override
	public void addPositionListener(IPositionListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePositionListener(IPositionListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setPosition(IPosition position) throws ScanningException,
			InterruptedException {
		try {
			Thread.sleep(2000);
		} catch(InterruptedException ex) {
			//This might well happen...
		}
		
		//This is to test positioning failing.
		if(position.getNames().contains("BadgerApocalypseButton") && position.get("BadgerApocalypseButton").equals("pushed")) {
			throw new ScanningException("The badger apocalypse cometh!");
		}
		
		pos = position;
		try {
			Thread.sleep(2500);
		} catch(InterruptedException ex) {
			//This might well happen...
		}
		return true;
	}

	@Override
	public IPosition getPosition() throws ScanningException {
		return pos;
	}

	@Override
	public List<IScannable<?>> getMonitors() throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMonitors(List<IScannable<?>> monitors)
			throws ScanningException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMonitors(IScannable<?>... monitors) throws ScanningException {
		// TODO Auto-generated method stub

	}

	@Override
	public void abort() {
		aborted = true;
	}
	
	public boolean isAborted() {
		return aborted;
	}

}
