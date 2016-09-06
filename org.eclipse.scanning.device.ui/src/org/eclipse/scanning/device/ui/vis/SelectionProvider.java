/*-
 *******************************************************************************
 * Copyright (c) 2011, 2014 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.device.ui.vis;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.dawnsci.plotting.api.PlottingSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * 
 * A thread unsafe selection provider, setSelection must be called from the UI thread.
 * There is a thread safe provider at {@link PlottingSelectionProvider}
 * 
 * @author Matthew Gerring
 *
 */
public class SelectionProvider implements ISelectionProvider {

	private Set<ISelectionChangedListener> listeners;
	private ISelection currentSelection;
	
	public SelectionProvider() {
		listeners      = new HashSet<ISelectionChangedListener>(11);
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return currentSelection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Method calls listener in background thread mto make frequent updates possible.
	 */
	@Override
	public void setSelection(ISelection selection) {
		
		if (listeners.isEmpty()) return;
		currentSelection = selection;
		SelectionChangedEvent e = new SelectionChangedEvent(this, currentSelection);
		ISelectionChangedListener[] sl = listeners.toArray(new ISelectionChangedListener[listeners.size()]);
		for (ISelectionChangedListener s : sl) s.selectionChanged(e);
	}

	public void clear() {
		if (listeners!=null) listeners.clear();
	}

}
