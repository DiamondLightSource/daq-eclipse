package org.eclipse.scanning.sequencer.preprocess;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scanning.api.scan.process.AbstractPreprocessor;
import org.eclipse.scanning.api.scan.process.IPreprocessingService;
import org.eclipse.scanning.api.scan.process.IPreprocessor;

public class PreprocessingService implements IPreprocessingService {
	
	private Map<String, IPreprocessor> preprocessors;

	@Override
	public IPreprocessor getPreprocessor(String name) {
		if (preprocessors==null) preprocessors = createPreprocessors();
		return preprocessors.get(name);
	}

	private Map<String, IPreprocessor> createPreprocessors() {
		Map<String, IPreprocessor> ret = new HashMap<>(7);
		if (Platform.getExtensionRegistry()!=null) {
			final IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.scanning.api.preprocessor");
			for (IConfigurationElement e : eles) {
				
				final String     name        = e.getAttribute("name");
				final String     label       = e.getAttribute("label");
				final String     description = e.getAttribute("description");
				
				try {
					final AbstractPreprocessor pre = (AbstractPreprocessor)e.createExecutableExtension("class");
					pre.setName(name);
					pre.setLabel(label);
					pre.setDescription(description);
					ret.put(name, pre);
					
				} catch (Exception ne) {
					ne.printStackTrace();
					continue;
				}
			}
		}
		return ret;
	}

}
