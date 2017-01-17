package org.eclipse.scanning.device.ui.device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ScanningException;

class ScannableContentProvider implements IStructuredContentProvider {

	private final IScannableDeviceService cservice;
	private final Map<String, IScannable<?>> content;

	private Viewer viewer;
	
	public ScannableContentProvider(IScannableDeviceService cservice) {
		this.cservice = cservice;
		this.content = new LinkedHashMap<>();
	}

	@Override
	public void dispose() {
		content.clear();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		this.viewer = viewer;
        content.clear();
        
		for (String name : (List<String>)newInput) {
			try {
				content.put(name, cservice.getScannable(name));
			} catch (ScanningException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public Object[] getElements(Object inputElement) {
		return content.values().toArray(new IScannable[content.size()]);
	}

	public void replace(IScannable<?> oscannable, IScannable<?> nscannable) {
		
		Map<String, IScannable<?>> copy = new LinkedHashMap<>(content);
		content.clear();
		for (String name : copy.keySet()) {
			if (oscannable.getName()==name || name.equals(oscannable.getName())) {
				content.put(nscannable.getName(), nscannable);
			} else {
				content.put(name, copy.get(name));
			}
		}
		viewer.refresh();
	}

	public void insert(IScannable<?> sscannable, IScannable<?> nscannable) {
		Map<String, IScannable<?>> copy = new LinkedHashMap<>(content);
		content.clear();
		for (String name : copy.keySet()) {
			if (sscannable.getName()==name || name.equals(sscannable.getName())) {
				content.put(sscannable.getName(), sscannable);
				content.put(nscannable.getName(), nscannable);
			} else {
				content.put(name, copy.get(name));
			}
		}
		viewer.refresh();		
	}

	public IScannable<?> last() {
		IScannable<?> ret = null;
		for (IScannable<?> s : content.values()) {
			ret = s;
		}
		return ret;
	}

	public Collection<String> getActivatedScannables() {
		List<String> scannables = new ArrayList<>();
		for (String name : content.keySet()) {
			if (content.get(name).isActivated()) scannables.add(name);
		}
		return scannables;
	}

	public void remove(IScannable<?> sscannable) {
		content.remove(sscannable.getName());
		viewer.refresh();
	}

	public void add(String name) throws ScanningException {
		if (!content.containsKey(name)) content.put(name, cservice.getScannable(name));
	}

}
