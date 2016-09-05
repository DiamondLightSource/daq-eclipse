/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.example.xcen.consumer;

import java.io.File;
import java.io.IOException;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.AbstractPausableProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.example.xcen.beans.XcenBean;
import org.eclipse.scanning.server.servlet.Services;

/**
 * Rerun of several collections as follows:
 * o Write the Xia2 command file, automatic.xinfo
 * o Runs Xia2 with file
 * o Progress reported by stating xia2.txt
 * o Runs xia2 html to generate report.
 * 
 * @author Matthew Gerring
 *
 */
public class XcenProcess extends AbstractPausableProcess<XcenBean>{
	
	private String processingDir;
	
	public XcenProcess(XcenBean bean, IPublisher<XcenBean> status) {
		
		super(bean, status);
				
        final String runDir;
        
        if (bean.getRunDirectory()==null) bean.setRunDirectory("xcenrun");
               
		if (isWindowsOS()) {
			// We are likely to be a test consumer, anyway the unix paths
			// from ISPyB will certainly not work, so we process in C:/tmp/
			runDir  = "C:/tmp/"+bean.getRunDirectory();
		} else {
			runDir  = "/tmp/"+bean.getRunDirectory();
		}

 		final File   xcenDir = getUnique(new File(runDir), "Xcen_", 1);
 		xcenDir.mkdirs();
		
	    processingDir = xcenDir.getAbsolutePath();
		bean.setRunDirectory(processingDir);
		
 		try {
			setLoggingFile(new File(xcenDir, "xcenJavaProcessLog.txt"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// We record the bean so that reruns of reruns are possible.
		try {
			writeProjectBean(processingDir, "xcenBean.json");
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		
		// Right we a starting the reconstruction, tell them.
		bean.setStatus(Status.RUNNING);
		bean.setPercentComplete(0d);
		broadcast(bean);
				
		// TODO Remove this, it is just to give an idea of how something can report progress to the UI.
		dryRun(100, false);
		
		if (!isCancelled()) {
			XcenBean xbean = (XcenBean)bean;
			xbean.setStatus(Status.COMPLETE);
			xbean.setPercentComplete(100);
			xbean.setMessage("Dry run complete (no software run)");
			xbean.setX(Math.random()*100);
			xbean.setY(Math.random()*100);
			xbean.setZ(Math.random()*100);
			broadcast(bean);
		}
		
	}

	/**
	 * TODO Please implement the running of xcen properly
	 */
	protected void runDataCollection() throws EventException {
		
		// TODO Run a data collection			

		bean.setStatus(Status.COMPLETE);
		bean.setMessage("Reconstruction run completed normally");
		bean.setPercentComplete(100);
		broadcast(bean);


	}

	@Override
	public void doTerminate() throws EventException {
		setCancelled(true);
	}

	public String getProcessingDir() {
		return processingDir;
	}

	public void setProcessingDir(String processingDir) {
		this.processingDir = processingDir;
	}

}
