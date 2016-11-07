package org.eclipse.scanning.malcolm.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.nexus.IMultipleNexusDevice;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.connector.MessageGenerator;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.models.OneDetectorTestMappingModel;
import org.eclipse.scanning.api.malcolm.models.TwoDetectorTestMappingModel;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Base class for Malcolm devices
 * 
 * <T> the model class for this malcolm device
 *
 */
public abstract class AbstractMalcolmDevice<T> extends AbstractRunnableDevice<T>
		implements IMalcolmDevice<T>, IMultipleNexusDevice {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractMalcolmDevice.class);
	
	private static final String ATTRIBUTE_NAME_DATASETS = "datasets";

	public static final String DATASETS_TABLE_COLUMN_NAME = "name";
	public static final String DATASETS_TABLE_COLUMN_FILENAME = "filename";
	public static final String DATASETS_TABLE_COLUMN_TYPE = "type";
	public static final String DATASETS_TABLE_COLUMN_PATH = "path";
	public static final String DATASETS_TABLE_COLUMN_UNIQUEID = "uniqueid";
	public static final String DATASETS_TABLE_COLUMN_RANK = "rank";
	
	private String filePath;
	
	// Events
	protected MalcolmEventDelegate eventDelegate;
	
	// Connection to serialization to talk to the remote object
	protected MessageGenerator<MalcolmMessage> connectionDelegate;
	
	public AbstractMalcolmDevice(IMalcolmConnectorService<MalcolmMessage> connector,
			IRunnableDeviceService runnableDeviceService) throws MalcolmDeviceException {
		super(runnableDeviceService);
   		this.connectionDelegate = connector.createDeviceConnection(this);
   		this.eventDelegate = new MalcolmEventDelegate(getName(), connector);
   		setRole(DeviceRole.MALCOLM);
	}
		
	/**
	 * Enacts any pre-actions or conditions before the device attempts to run the task block.
	 *  
	 * @throws Exception
	 */
	protected void beforeExecute() throws Exception {
        logger.debug("Entering beforeExecute, state is " + getDeviceState());	
	}
	
	
	/**
	 * Enacts any post-actions or conditions after the device completes a run of the task block.
	 *  
	 * @throws Exception
	 */
	protected void afterExecute() throws Exception {
        logger.debug("Entering afterExecute, state is " + getDeviceState());	
	}
	
	protected void setTemplateBean(MalcolmEventBean bean) {
		eventDelegate.setTemplateBean(bean);
	}
	
	public void start(final IPosition pos) throws ScanningException, InterruptedException {
		
		final List<Throwable> exceptions = new ArrayList<>(1);
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					AbstractMalcolmDevice.this.run(pos);
				} catch (ScanningException|InterruptedException e) {
					e.printStackTrace();
					exceptions.add(e);
				}
			}
		}, "Device Runner Thread "+getName());
		thread.start();
		
		// We delay by 500ms just so that we can 
		// immediately throw any connection exceptions
		Thread.sleep(500);
		
		if (!exceptions.isEmpty()) throw new ScanningException(exceptions.get(0));
	}

	protected void close() throws Exception {
		eventDelegate.close();
	}
	
	@Override
	public void dispose() throws MalcolmDeviceException {
		try {
			try {
			    if (getDeviceState().isRunning()) abort();
			} finally {
			   close();
			}
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, "Cannot dispose of '"+getName()+"'!", e);
		}
	}
	
	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
		try {
			MalcolmTable datasetsTable = getAttributeValue(ATTRIBUTE_NAME_DATASETS);
			MalcolmNexusObjectBuilder malcolmNexusBuilder = new MalcolmNexusObjectBuilder();
			return malcolmNexusBuilder.buildNexusObjects(datasetsTable, info);
		} catch (Exception e) {
			throw new NexusException("Could not create nexus objects for malcolm device " + getName(), e);
		}
	}

	@Override
	public void addMalcolmListener(IMalcolmListener l) {
		eventDelegate.addMalcolmListener(l);
	}

	@Override
	public void removeMalcolmListener(IMalcolmListener l) {
		eventDelegate.removeMalcolmListener(l);
	}

	protected void sendEvent(MalcolmEventBean event) throws Exception {
		eventDelegate.sendEvent(event);
	}

	protected String getFileName() {
		String filePath = getFilePath();
		if (filePath ==null) return null;
		filePath = filePath.replace('\\', '/');
		return filePath.substring(filePath.lastIndexOf('/')+1);
	}
	
	public String getFilePath() {
		
		if (model !=null) {
			// Malcolm v1 hack because the models are not standardised for V1
			if (model instanceof OneDetectorTestMappingModel)return ((OneDetectorTestMappingModel)model).getHdf5File();
			// Malcolm v1 hack because the models are not standardised for V1
			if (model instanceof TwoDetectorTestMappingModel)return ((TwoDetectorTestMappingModel)model).getHdf5File1();
			// Malcolm v1 hack because the models are not standardised for V1
			if (model instanceof Map)return ((Map)model).get("file").toString(); // Causes an exception if not there.
		}
		
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
