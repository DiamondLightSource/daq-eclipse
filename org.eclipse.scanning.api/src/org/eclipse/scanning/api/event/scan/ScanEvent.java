package org.eclipse.scanning.api.event.scan;

import java.util.EventObject;

public class ScanEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6787226667503629937L;

	
	public ScanEvent(ScanBean bean) {
		super(bean);
	}

	public ScanBean getBean() {
		return (ScanBean)getSource();
	}
	
	public String toString() {
		return getBean().toString();
	}
}
