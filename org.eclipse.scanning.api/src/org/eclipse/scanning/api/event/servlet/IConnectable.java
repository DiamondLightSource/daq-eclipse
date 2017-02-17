/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.event.servlet;

import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.EventException;

public interface IConnectable {
	/**
	 * Should called to start the servlet.
	 * @param uri, a string representation of the activemq uri.
	 */
	public void connect() throws EventException, URISyntaxException;

	/**
	 * Should called to stop the servlet but if it is not called
	 * the servlet will run the lifetime of the server.
	 * 
	 * This is acceptable if it is a service client(s) may demand at
	 * any time.
	 */
	public void disconnect() throws EventException;

}
