package org.eclipse.scanning.event.ui.view;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

class DelegatingSelectionProvider implements ISelectionProvider, ISelectionChangedListener {

	private Set<ISelectionChangedListener> listeners;
	private ISelectionProvider             wrapped;
	
	public DelegatingSelectionProvider(ISelectionProvider wrapped) {
		this.wrapped = wrapped;
		this.listeners = new HashSet<>();
		wrapped.addSelectionChangedListener(this);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		fireSelection(event.getSelection());
	}
	
	/**
	 * Call to programmatically fire a selection.
	 * @param selection
	 */
	public void fireSelection(ISelection selection) {
		if (listeners.isEmpty()) return;
		SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		ISelectionChangedListener[] sl = listeners.toArray(new ISelectionChangedListener[listeners.size()]);
		for (ISelectionChangedListener s : sl) s.selectionChanged(e);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return wrapped.getSelection();
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
		wrapped.setSelection(selection); // Causes listeners to fire
	}

	public void dispose() {
		wrapped.removeSelectionChangedListener(this);
		listeners.clear();
	}

}