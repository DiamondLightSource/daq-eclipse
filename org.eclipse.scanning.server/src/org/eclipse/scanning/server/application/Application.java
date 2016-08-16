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
		
		PseudoSpringParser parser = new PseudoSpringParser();
		this.objects = parser.parse(conf.get("xml"));
		latch.await();
		
		return objects;
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
	

}
