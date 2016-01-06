package org.eclipse.scanning.api.event.scan;

import org.eclipse.scanning.api.event.IEventListener;


/**
 * Listener for scan events. The listener will be notified if
 * any scans are made active or inactive. In order to listen
 * for a given scan a listener must be added and then have its
 * scanId registered for that scan. 
 * 
 * @author Matthew Gerring
 *
 */
public interface IScanListener extends IEventListener<ScanBean> {
		
	/**
	 * Called by all broadcast events
	 * @param evt
	 */
	void scanEventPerformed(ScanEvent evt);
	
	/**
	 * Called to notify that state was changed. Happens in addition to
	 * scanEventPerformed(...) when state changes. For instance from IDLE to CONFIGURED
	 * CONFIGURED to RUNNING etc.
	 * 
	 * @param evt
	 */
	void scanStateChanged(ScanEvent evt) ;

	
	/**
	 * Convenience class for extending. For instance in the case where the UUID is null.
	 * @author Matthew Gerring
	 *
	 */
	public class Stub implements IScanListener{

		@Override
		public void scanEventPerformed(ScanEvent evt) {
			
		}

		@Override
		public void scanStateChanged(ScanEvent evt) {
			
		}

		@Override
		public Class<ScanBean> getBeanClass() {
			return ScanBean.class;
		}
		
	}
}
