package org.eclipse.scanning.event.queues;

import java.util.Map;

import org.eclipse.scanning.api.event.queues.IQueueProcessor;

public class QueueProcessorFactory {
	
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
	
	public static void registerProcessor(Class<? extends IQueueProcessor> clazz) {
		//TODO
	}
	
	public static Map<String, IQueueProcessor> getProcessors() {
		//TODO
		return null;
	}
	
	public static IQueueProcessor getProcessor(String atomBeanClassName) {
		//TODO
		return null;
	}

}
