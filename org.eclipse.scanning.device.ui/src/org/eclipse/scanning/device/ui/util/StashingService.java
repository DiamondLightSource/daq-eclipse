package org.eclipse.scanning.device.ui.util;

import java.io.File;

import org.eclipse.scanning.api.stashing.IStashing;
import org.eclipse.scanning.api.stashing.IStashingService;
import org.eclipse.scanning.device.ui.ServiceHolder;

public class StashingService implements IStashingService {

	@Override
	public IStashing createStash(String path) {
		return new Stashing(path, ServiceHolder.getMarshallerService());
	}

	@Override
	public IStashing createStash(File file) {
		return new Stashing(file, ServiceHolder.getMarshallerService());
	}

}
