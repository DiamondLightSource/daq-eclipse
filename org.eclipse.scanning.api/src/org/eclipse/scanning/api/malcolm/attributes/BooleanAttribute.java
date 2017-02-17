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
package org.eclipse.scanning.api.malcolm.attributes;

/**
 * 
 * Encapsulates a boolean attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class BooleanAttribute extends MalcolmAttribute {
	public static final String BOOLEAN_ID = "malcolm:core/BooleanMeta:";
	
	boolean value;

	@Override
	public Boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}
	
}
