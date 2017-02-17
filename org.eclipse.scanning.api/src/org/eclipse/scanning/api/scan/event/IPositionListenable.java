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
package org.eclipse.scanning.api.scan.event;

public interface IPositionListenable {
	/**
	 * Use to be notified as levels / positions are reached.
	 * Not usually necessary as setPosition is blocking until
	 * the position is reached but useful for other objects 
	 * which need to change when new positions are reached.
	 * 
	 * @param listener
	 */
	void addPositionListener(IPositionListener listener);
	
	/**
	 * Use to be notified as levels / positions are reached.
	 * @param listener
	 */
	void removePositionListener(IPositionListener listener);

}
