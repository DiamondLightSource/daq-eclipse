package org.eclipse.scanning.event.queues;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.event.queues.processors.MoveAtomProcessor;
import org.eclipse.scanning.event.queues.processors.ScanAtomProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueProcessorFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(QueueProcessorFactory.class);
	
	private static final Map<String, Class<? extends IQueueProcessor<? extends Queueable>>> PROCESSORS;
	
	static {
		PROCESSORS = new HashMap<>();
		initialize();
	}
	
	/**
	 * No argument constructor for OSGi.
	 * As this is a static class there is no need to use it.
	 */
	public QueueProcessorFactory() {
		
	}
	
	/**
	 * This registers the default processors within the map. It removes any 
	 * entries in the map before doing this (useful for tests).
	 */
	public static void initialize() {
		if (PROCESSORS.size() > 0) {
			PROCESSORS.clear();
		}
		
		//Register default processors
		try {
			QueueProcessorFactory.registerProcessors(MoveAtomProcessor.class,
					ScanAtomProcessor.class);//AtomQueueProcessor.class, MonitorAtomProcessor.class,
		} catch (EventException evEx) {
			logger.error("Initial configuration of QueueProcessorFactory failed. Could not register processor(s): "+evEx.getMessage());
		}
	}
	
	/**
	 * Supply a series of processors to be registered with the factory.
	 * 
	 * @param clazzez Two or more processors to be registered.
	 * @throws EventException Registering processor is unsuccessful.
	 */
	@SafeVarargs
	public static void registerProcessors(Class<? extends IQueueProcessor<? extends Queueable>>... clazzez) throws EventException {
		for (Class<? extends IQueueProcessor<? extends Queueable>> clazz : clazzez) {
			registerProcessor(clazz);
		}
	}
	
	/**
	 * Adds a given processor to the PROCESSORS map, with the key set as the 
	 * class name of the bean which is processes.
	 * 
	 * @param clazz
	 * @throws EventException If getting the String name of the bean or adding 
	 *                        the processor to the map fails.
	 */
	public static void registerProcessor(Class<? extends IQueueProcessor<? extends Queueable>> clazz) throws EventException {
		try {
			String beanClassName = clazz.newInstance().getBeanClass().getName();
			PROCESSORS.put(beanClassName, clazz);
		} catch (Exception ex) {
			logger.error("Failed to register processor '"+clazz.getName()+"': \""+ex.getMessage()+"\".");
			throw new EventException("Failed to register processor", ex);
		}
		
	}
	
	/**
	 * Return the registry of processors.
	 * 
	 * @return Map<String, Class> of processors and with bean class names as 
	 *         keys.
	 */
	public static Map<String, Class<? extends IQueueProcessor<? extends Queueable>>> getProcessors() {
		return PROCESSORS;
	}
	
	/**
	 * Return a new instance of a processor capable of processing the supplied 
	 * bean type.
	 * 
	 * @param beanClassName String fully qualified class name of bean.
	 * @return IQueueProcessor instance of class capable of processing bean.
	 * @throws EventException If no processor registered for bean type or if 
	 *                        processor cannot be instantiated.
	 */
	public static IQueueProcessor<? extends Queueable> getProcessor(String beanClassName) throws EventException {
		Class<? extends IQueueProcessor<? extends Queueable>> clazz = PROCESSORS.get(beanClassName);
		
		if (clazz == null) throw new EventException("No processor registered for bean type '"+beanClassName+"'");
		try {
			return clazz.newInstance();
		} catch (Exception ex) {
			logger.error("Could not create new instance of class '"+clazz.getName()+"'.");
			throw new EventException("Could not create new instance of processor class", ex);
		}
	}

}
