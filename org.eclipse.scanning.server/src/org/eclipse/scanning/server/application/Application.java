/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.server.application;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * This application is used to start and stop data acquisition objects
 * inside an OSGi container. For instance one could start various
 * servlets for easily testing server functionality.
 * 
 * Arguments:
 * 
 * -xml   Simple spring file that creates objects
 * 
 * 
 * @author Matthew Gerring
 *
 */
public class Application implements IApplication {


	private Map<String, Object> objects;
	private CountDownLatch latch;

	@Override
	public Object start(IApplicationContext context) throws Exception {
			
		this.latch   = new CountDownLatch(1);

		final Map<?, ?>      args    = context.getArguments();
		final String[] configuration = (String[])args.get("application.args");
        
		Map<String, String> conf = new HashMap<String, String>(7);
		for (int i = 0; i < configuration.length; i++) {
			final String pkey = configuration[i];
			if (pkey.startsWith("-")) {
				conf.put(pkey.substring(1), configuration[i+1]);
			}
		}
		
		this.objects = create(conf.get("xml"));
		latch.await();
		
		return objects;
	}

	/**
	 * Manually parse spring XML to create the objects.
	 * This means that the example has no dependency on a
	 * particular spring version and can be part of the open
	 * source project.
	 * 
	 * @param conf
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> create(String path) throws Exception {
				
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    
	    Document doc = builder.parse(new File(path));
	    doc.getDocumentElement().normalize();
	    NodeList  nl = doc.getElementsByTagName("bean");
	    
	    Map<String, Object>    objects = new HashMap<>();
	    Map<String, NamedList> lists   = new HashMap<>();
	    
	    for (int i = 0; i < nl.getLength(); i++) {
			
	    	Element bean = (Element)nl.item(i);
	    	if (!bean.hasChildNodes()) continue;
	    	
			final String className = bean.getAttributes().getNamedItem("class").getNodeValue();
			
			Node initNode = bean.getAttributes().getNamedItem("init-method");
			final String init      = initNode!=null ? bean.getAttributes().getNamedItem("init-method").getNodeValue() : null;
			
			final String id = bean.getAttributes().getNamedItem("id").getNodeValue();

			// Look for parameters
			// bundle, broker, submitQueue, statusSet, statusTopic, durable;	
			NodeList props = bean.getElementsByTagName("property");
			final Map<String,String> conf = new HashMap<>();
			for (int j = 0; j < props.getLength(); j++) {
				Node prop = props.item(j);
				String name = prop.getAttributes().getNamedItem("name").getNodeValue();
				Node value = prop.getAttributes().getNamedItem("value");
				if (value!=null) {
				    conf.put(name, value.getNodeValue());
				} else {
					NodeList children = prop.getChildNodes();
					final List<String> refs = new ArrayList<>();
					for (int k = 0; k < children.getLength(); k++) {
						Node list = children.item(k);
						if (!list.hasChildNodes()) continue;
						NodeList rs = list.getChildNodes();
						for (int l = 0; l < rs.getLength(); l++) {
							Node item = rs.item(l);
							NamedNodeMap attr = item.getAttributes();
							if (attr==null) continue;
							Node ref  = attr.getNamedItem("bean");
							refs.add(ref.getNodeValue());
						}
					}
				    lists.put(id, new NamedList(name, refs));
				}
			}
			Object created = createObject(className, init, conf);
			objects.put(id, created);
		}
	    
	    // We process the lists to wire together objects
	    for (String id : lists.keySet()) {
	    	final NamedList namedList  = lists.get(id);
			final Object    object     = objects.get(id);
			if (object!=null) {
				final List<Object> listValue = getObjects(objects, namedList);
				setValue(object, namedList.getName(), listValue, List.class);
			}
		}
	    
	    nl = doc.getElementsByTagName("osgi:service");
	    if (nl!=null) for (int i = 0; i < nl.getLength(); i++) {
	    	
	    	Element service = (Element)nl.item(i);
			final String ref = service.getAttributes().getNamedItem("ref").getNodeValue();
            final Object obj = objects.get(ref);
            
			final String interfase = service.getAttributes().getNamedItem("interface").getNodeValue();
			final Bundle bundle    = Platform.getBundle("org.eclipse.scanning.api");
			final Class  clazz     = bundle.loadClass(interfase);

			Activator.registerService(clazz, obj);
	    }
	    
		return objects;
	}

	private static List<Object> getObjects(Map<String, Object> objects, NamedList namedList) {
		final List<Object> ret = new ArrayList<>();
		for (String id : namedList.getRefs()) {
			ret.add(objects.get(id));
		}
		return ret;
	}

	private static Object createObject(String className, String initMethod, Map<String, String> conf) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		
		// Must have a bundle
		String bundleName = conf.remove("bundle");
		if (bundleName==null) bundleName = "org.eclipse.scanning.server";
		final Bundle bundle = Platform.getBundle(bundleName);
	
		final Class<?> clazz = bundle.loadClass(className);
		
		Object instance = clazz.newInstance();
		for (String fieldName : conf.keySet()) {
			final Object value      = getValue(conf, fieldName);
			setValue(clazz, instance, fieldName, value, null);
		}

		if (initMethod!=null) {
			Method method = clazz.getMethod(initMethod);
			method.invoke(instance);
		}
		System.out.println("Started Server Extension "+clazz.getSimpleName()+" using "+initMethod);
		return instance;
	}

	private static void setValue(Object instance, final String fieldName, final Object value, final Class<?> valueClass) throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException {
		setValue(instance.getClass(), instance, fieldName, value, valueClass);
	}
	
	private static void setValue(final Class<?> clazz, Object instance, final String fieldName, final Object value, Class<?> valueClass) throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException {
		final String setterName = getSetterName(fieldName);
		if (valueClass==null) valueClass = value.getClass();
		Method method = getMethod(clazz, setterName, valueClass);
		method.invoke(instance, value);
	}

	private static Method getMethod(Class<?> clazz, String setterName, Class<? extends Object> valueClass) throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		try {
			return clazz.getMethod(setterName, valueClass);
		} catch (Exception ne) {
			return clazz.getMethod(setterName, (Class<?>)valueClass.getField("TYPE").get(null));
		}
	}

	private static Object getValue(Map<String, String> conf, String fieldName) {
		
		String val = conf.get(fieldName);
		val = replaceProperties(val); // Insert any system properties that the user used.
		
		if ("true".equalsIgnoreCase(val)) {
			return Boolean.TRUE;
		} else if ("false".equalsIgnoreCase(val)) {
			return Boolean.FALSE;
		} else {
			// There are faster ways to do this
			// but they are not required here...
			try {
				return Integer.parseInt(val);
			} catch (Exception ne) {
				try {
					return Double.parseDouble(val);
				} catch (Exception ignored) {
					
				}
			}
		}
		return val; // The String
	}

	/**
	 * Not very efficient but no dependencies required and does job.
	 * @param val
	 * @return
	 */
	private static String replaceProperties(String val) {
		final Properties props = System.getProperties();
		for (Object name : props.keySet()) {
			val = val.replace("${"+name+"}", props.getProperty(name.toString()));
		}
		return val;
	}

	@Override
	public void stop() {
		if (objects!=null) for (Object object : objects.values()) {
			try {
				Method disconnect = object.getClass().getMethod("disconnect");
				disconnect.invoke(object);
			} catch (Exception ne) {
				continue;
			}
		}
		latch.countDown();
	}
	
	private static String getSetterName(final String fieldName) {
		if (fieldName == null) return null;
		return getName("set", fieldName);
	}
	private static String getName(final String prefix, final String fieldName) {
		return prefix + getFieldWithUpperCaseFirstLetter(fieldName);
	}
	public static String getFieldWithUpperCaseFirstLetter(final String fieldName) {
		return fieldName.substring(0, 1).toUpperCase(Locale.US) + fieldName.substring(1);
	}


}
