/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.scanning.api.event.status;

/**
 * States of jobs on the cluster.
 * 
 * @author Matthew Gerring
 *
 */
public enum Status { // TODO Should this be called QueueStatus to avoid confusion?

	SUBMITTED, QUEUED, RUNNING, TERMINATED, REQUEST_TERMINATE, FAILED, COMPLETE, UNFINISHED, NONE;

	/**
	 * 
	 * @return true if the run was taken from the queue and something was actually executed on it.
	 */
	public boolean isStarted() {
		return this!=SUBMITTED;
	}
	
	public boolean isFinal() {
		return this==TERMINATED || this==FAILED || this==COMPLETE || this==UNFINISHED || this==NONE;
	}
}
