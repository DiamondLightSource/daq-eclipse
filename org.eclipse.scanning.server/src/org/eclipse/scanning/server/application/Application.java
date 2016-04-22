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

import org.apache.log4j.ConsoleAppender;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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


	private List<Object> objects;
	private CountDownLatch latch;

	@Override
	public Object start(IApplicationContext context) throws Exception {
	
		org.apache.log4j.Logger.getRootLogger().addAppender(new ConsoleAppender());
		
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
		this.latch   = new CountDownLatch(1);
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
	public static List<Object> create(String path) throws Exception {
				
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    
	    Document doc = builder.parse(new File(path));
	    doc.getDocumentElement().normalize();
	    NodeList  nl = doc.getElementsByTagName("bean");
	    
	    List<Object> objects = new ArrayList<>();
	    for (int i = 0; i < nl.getLength(); i++) {
			
	    	Element bean = (Element)nl.item(i);
	    	if (!bean.hasChildNodes()) continue;
	    	
			final String className = bean.getAttributes().getNamedItem("class").getNodeValue();
			final String init      = bean.getAttributes().getNamedItem("init-method").getNodeValue();
			
			// Look for parameters
			// bundle, broker, submitQueue, statusSet, statusTopic, durable;	
			NodeList params = bean.getElementsByTagName("property");
			final Map<String,String> conf = new HashMap<>();
			for (int j = 0; j < params.getLength(); j++) {
				Node param = params.item(j);
				conf.put(param.getAttributes().getNamedItem("name").getNodeValue(), param.getAttributes().getNamedItem("value").getNodeValue());
			}
			Object created = createObject(className, init, conf);
			objects.add(created);
		}
	    
		return objects;
	}

	private static Object createObject(String className, String initMethod, Map<String, String> conf) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		
		// Must have a bundle
		String bundleName = conf.remove("bundle");
		if (bundleName==null) bundleName = "org.eclipse.scanning.server";
		final Bundle bundle = Platform.getBundle(bundleName);
	
		final Class<?> clazz = bundle.loadClass(className);
		
		Object instance = clazz.newInstance();
		for (String fieldName : conf.keySet()) {
			final String setterName = getSetterName(fieldName);
			final Object value      = getValue(conf, fieldName);
			Method method = getMethod(clazz, setterName, value.getClass());
			method.invoke(instance, value);
		}

		Method method = clazz.getMethod(initMethod);
		method.invoke(instance);
		System.out.println("Started Server Extension "+clazz.getSimpleName()+" using "+initMethod);
		return instance;
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
		for (Object object : objects) {
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
