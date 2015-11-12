/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.event.ui.view;

import org.eclipse.scanning.api.event.status.StatusBean;

public interface IResultOpenHandler {

	/**
	 * Defines if this handler can open the result in this bean.
	 * @param bean
	 * @return
	 */
	public boolean isHandled(StatusBean bean);
	
	/**
	 * Called to open the result from the beam.
	 * @param bean
	 * @throws Exception if something unexpected goes wrong
	 * @return true if result open handled ok, false otherwise.
	 */
	public boolean open(StatusBean bean) throws Exception;
}
