package org.eclipse.scanning.api.scan.event;

import java.util.EventListener;

import org.eclipse.scanning.api.scan.ScanningException;


/**
 * A listener which is fired before and after a device is run.
 * 
 * @author Matthew Gerring
 *
 */
public interface IRunListener extends EventListener {

	/**
	 * Called before a run() is made on the device. Can
	 * be used to modify the model before a given run of the device.
	 * @param evt
	 * @throws scanning exception which will terminate the scan
	 */
	void runWillPerform(RunEvent evt) throws ScanningException;
	
	/**
	 * Used to notify that a given device as been run.
	 * @param evt
	 * @throws scanning exception which will terminate the scan
	 */
	void runPerformed(RunEvent evt) throws ScanningException;
	
	/**
	 * Called before a run() is made on the device. Can
	 * be used to modify the model before a given run of the device.
	 * @param evt
	 * @throws scanning exception which will terminate the scan
	 */
	void writeWillPerform(RunEvent evt) throws ScanningException;
	
	/**
	 * Used to notify that a given device as been run.
	 * @param evt
	 * @throws scanning exception which will terminate the scan
	 */
	void writePerformed(RunEvent evt) throws ScanningException;

	
	public class Stub implements IRunListener {

		@Override
		public void runWillPerform(RunEvent evt) throws ScanningException{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void runPerformed(RunEvent evt) throws ScanningException{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeWillPerform(RunEvent evt) throws ScanningException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writePerformed(RunEvent evt) throws ScanningException {
			// TODO Auto-generated method stub
			
		}
		
	}
}
