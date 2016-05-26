package org.eclipse.scanning.event.queues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;

public class QueueProcessorFactory {
	
	private static final Map<String, Class<? extends IQueueProcessor>> PROCESSORS;
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
	public static void registerProcessors(Class<? extends IQueueProcessor>... clazzez) {
		//TODO
	}
	
	public static void registerProcessor(Class<? extends IQueueProcessor> clazz) throws EventException {
		//Add processor to PROCESSORS map
		String processorClassName = clazz.getName();
		try {
			PROCESSORS.put(processorClassName, clazz);
		} catch (Exception ex) {
			throw new EventException("Failed to register processor: "+ex.getMessage());
		}
		
		
		//Add atom/bean classnames processed by processor to ATOMBEANTYPES map
		try {
			IQueueProcessor proc = clazz.newInstance();
			List<String> beanTypes = proc.getAtomBeanTypes();
			for (String type : beanTypes) {
				ATOMBEANTYPES.put(type, processorClassName);
			}
		} catch (Exception ex) {
			throw new EventException("Failed to register beantype(s): "+ex.getMessage());
		}
	}
	
	public static Map<String, Class<? extends IQueueProcessor>> getProcessors() {
		return PROCESSORS;
	}
	
	public static Map<String, String> getAtomBeanTypes() {
		return ATOMBEANTYPES;
	}
	
	public static IQueueProcessor getProcessor(String atomBeanClassName) throws EventException {
		//TODO
		return null;
	}

}
