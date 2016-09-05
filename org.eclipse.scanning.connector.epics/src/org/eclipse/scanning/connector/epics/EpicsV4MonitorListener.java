package org.eclipse.scanning.connector.epics;

import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.epics.pvaClient.PvaClientMonitor;

public class EpicsV4MonitorListener {

	private IMalcolmListener<MalcolmMessage> malcolmListener;
	private PvaClientMonitor monitor;

	public EpicsV4MonitorListener(IMalcolmListener<MalcolmMessage> malcolmListener, PvaClientMonitor monitor) {
		this.malcolmListener = malcolmListener;
		this.monitor = monitor;
	}
	
	public IMalcolmListener<MalcolmMessage> getMalcolmListener() {
		return malcolmListener;
	}
	
	public void setMalcolmListener(IMalcolmListener<MalcolmMessage> malcolmListener) {
		this.malcolmListener = malcolmListener;
	}
	
	public PvaClientMonitor getMonitor() {
		return monitor;
	}
	
	public void setMonitor(PvaClientMonitor monitor) {
		this.monitor = monitor;
	}
	
}