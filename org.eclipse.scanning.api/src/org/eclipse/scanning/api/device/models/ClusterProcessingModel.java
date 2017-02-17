/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.device.models;

import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FileType;

public class ClusterProcessingModel {
	
	private String name;
	
	@FieldDescriptor(device=DeviceType.RUNNABLE, hint="The name of the detector whose output we will process")
	private String detectorName;
	
	@FieldDescriptor(file=FileType.EXISTING_FILE, hint="The full path of the processing file")
	private String processingFilePath;
	
	// TODO more fields to add, ask Jake
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	public String getProcessingFilePath() {
		return processingFilePath;
	}

	public void setProcessingFilePath(String processingFileName) {
		this.processingFilePath = processingFileName;
	}
	
}
