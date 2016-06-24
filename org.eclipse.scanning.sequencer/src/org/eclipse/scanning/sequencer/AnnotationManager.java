package org.eclipse.scanning.sequencer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.annotation.scan.DeviceAnnotations;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * The device manager parses annotations and allows methods to be 
 * efficiently called during a scan to notify of progress.
 * 
 * If attemps to parse all the reflection stuff up-front so that a call
 * to invoke(...) during the scan can be as efficiently despatched using
 * method.invoke(...) as possible.
 * 
 * This class could be made into a general purpose annotation parsing
 * and method valling class once tested.
 * 
 * @author Matthew Gerring
 *
 */
public class AnnotationManager {
	
	private static final Logger logger = LoggerFactory.getLogger(AnnotationManager.class);

	private Map<Class<? extends Annotation>, Collection<MethodWrapper>> annotationMap;
	public AnnotationManager() {
		annotationMap = new Hashtable<>(31);
	}
	
	public void addDevices(Object... ds) {
		for (Object object : ds) processAnnotations(object);
	}
	public void addDevices(Collection<?> ds) {
		for (Object object : ds) processAnnotations(object);
	}
	
	private void processAnnotations(Object object) {
		if (object==null) return;
		final Method[] methods = object.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			final Annotation[] annotations = methods[i].getAnnotations();
			if (annotations!=null) for (Annotation annotation : annotations) {
				if (DeviceAnnotations.isDeviceAnnotation(annotation)) {
					Class<? extends Annotation> clazz = annotation.annotationType();
					Collection<MethodWrapper> ms = annotationMap.get(clazz);
					if (ms == null) {
						ms = new ArrayList<>(31);
						annotationMap.put(clazz, ms);
					}
					ms.add(new MethodWrapper(object, methods[i]));
				}
			}
		}
	}

	/**
	 * Notify the methods with this annotation that it happened. 
	 * Optionally provide some context which the system will try to insert into the
	 * argument list when it is called.
	 * 
	 * @param annotation like &#64;ScanStart etc.
	 * @param context, extra things like ScanInformation, IPosition etc.
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void invoke(Class<? extends Annotation> annotation, Object... context) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		final Collection<MethodWrapper> annotations = annotationMap.get(annotation);
		if (annotations!=null) for (MethodWrapper wrapper : annotations) wrapper.invoke(context);
	}
	
	private class MethodWrapper {
		
		private Object          instance;
		private Method          method;
		private List<Class<?>>  argClasses;
		private Object[]        arguments; // Must be object[] for speed and is not variable
		
		MethodWrapper(Object instance, Method method) {
			this.instance = instance;
			this.method   = method;
			
			final Class<?>[] args = method.getParameterTypes();
			this.argClasses = args!=null?Arrays.asList(args):null;
			
			if (args!=null) {
				this.arguments= new Object[args.length];
				for (int i = 0; i < args.length; i++) {
					if (args[i] == IPosition.class) continue;
					if (args[i] == ScanInformation.class) continue;
				    // Find OSGi service for it, if any.
					try {
						arguments[i] = SequencerActivator.getService(args[i]);
					} catch (Exception ne) {
						logger.warn("Unable to find OSGi service for "+args[i]);
					}
				}
			}
		}
		
		public void invoke(Object... context) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			
			if (arguments!=null) { // Put the context into the args (if there are any)
				
				if (context!=null) for (int i = 0; i < context.length; i++) {
                   if (argClasses.contains(context[i].getClass())) {
                	   final int index = argClasses.indexOf(context[i].getClass());
                	   arguments[index] = context[i];
                   }
				}
				method.invoke(instance, arguments);
			} else {
				method.invoke(instance);
			}
		}
	}
}
