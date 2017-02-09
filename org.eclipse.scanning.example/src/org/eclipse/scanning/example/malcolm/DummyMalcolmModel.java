package org.eclipse.scanning.example.malcolm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.ITimeoutable;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import org.springframework.util.StringUtils;

/**
 * A Malcolm Model for a {@link DummyMalcolmDevice}. This model describes which nexus files
 * and datasets the dummy malcolm device should create. A {@link DummyMalcolmControlledDetectorModel}
 * should be added for each device (i.e. detector, scannable) that is being simulated by the
 * real malcolm device.
 * 
 * @author Matthew Dickie
 */
public class DummyMalcolmModel extends MalcolmModel implements ITimeoutable, Configurable {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyMalcolmModel.class);

	// timeout is added to the dummy model so that it can be increased for debugging purposes
	private long timeout = -1;
	
	private List<String> monitorNames;

	@Override
	public void configure() throws FactoryException {

		if (StringUtils.isEmpty(getName())) {
			final String message = "Name not set";
			logger.error(message);
			throw new FactoryException(message);
		}

		if (getAxesToMove() == null) {
			final String message = "Axes to move not set";
			logger.error(message);
			throw new FactoryException(message);
		}
	}
	
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public List<String> getMonitorNames() {
		if (monitorNames == null) {
			return Collections.emptyList();
		}
		return monitorNames;
	}

	public void setMonitorNames(List<String> monitorNames) {
		this.monitorNames = monitorNames;
	}

	@Override
	public String toString() {
		return "DummyMalcolmModel [name = " + getName() + ", timeout=" + timeout
				+ ", monitorNames=" + monitorNames + ", fileDir=" + getFileDir() + ", axesToMove=" + getAxesToMove()
				+ ", getExposureTime()=" + getExposureTime() + "]";
	}

}
