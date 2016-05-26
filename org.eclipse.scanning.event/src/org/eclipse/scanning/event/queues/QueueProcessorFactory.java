package org.eclipse.scanning.event.queues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueProcessorFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(QueueProcessorFactory.class);
	
	private static final Map<String, Class<? extends IQueueProcessor<? extends Queueable>>> PROCESSORS;
	private static final Map<String, String> ATOMBEANTYPES;
	
	static {
		PROCESSORS = new HashMap<>();
		ATOMBEANTYPES = new HashMap<>();
	}
	
	/**
	 * No argument constructor for OSGi.
	 * As this is a static class there is no need to use it.
	 */
	public QueueProcessorFactory() {
		
	}
	
	@SafeVarargs
	public static void registerProcessors(Class<? extends IQueueProcessor<? extends Queueable>>... clazzez) {
		//TODO
	}
	
	public static void registerProcessor(Class<? extends IQueueProcessor<? extends Queueable>> clazz) throws EventException {
		//Add processor to PROCESSORS map
		String processorClassName = clazz.getName();
		try {
			PROCESSORS.put(processorClassName, clazz);
		} catch (Exception ex) {
			logger.error("Failed to register processor "+processorClassName+": "+ex.getMessage());
			throw new EventException("Failed to register processor: "+ex.getMessage());
		}
		
		
		//Add atom/bean classnames processed by processor to ATOMBEANTYPES map
		try {
			IQueueProcessor<? extends Queueable> proc = clazz.newInstance();
			List<String> beanTypes = proc.getAtomBeanTypes();
			for (String type : beanTypes) {
				ATOMBEANTYPES.put(type, processorClassName);
			}
		} catch (Exception ex) {
			logger.error("Failed to register atom/bean type for "+processorClassName+": "+ex.getMessage());
			throw new EventException("Failed to register atom/bean type for "+processorClassName+": "+ex.getMessage());
		}
	}
	
	public static Map<String, Class<? extends IQueueProcessor<? extends Queueable>>> getProcessors() {
		return PROCESSORS;
	}
	
	public static Map<String, String> getAtomBeanTypes() {
		return ATOMBEANTYPES;
	}
	
	public static IQueueProcessor<? extends Queueable> getProcessor(String atomBeanClassName) throws EventException {
		//TODO
		return null;
	}

}
