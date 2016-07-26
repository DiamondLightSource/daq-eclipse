package org.eclipse.scanning.device.ui.points;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class GeneratorDescriptor implements ISeriesItemDescriptor {
	
	private IPointGenerator<?> generator;
	private String id;
	private IPointGeneratorService pservice;

	public GeneratorDescriptor(String id, IPointGeneratorService pservice) throws GeneratorException {
		this.id = id;
		this.pservice  = pservice;
		this.generator = pservice.createGenerator(id);
	}

	@Override
	public String toString() {
		return "GeneratorDescriptor ["+generator.getLabel()+"]";
	}

	@Override
	public IPointGenerator<?> getSeriesObject() throws GeneratorException {
		if (generator==null) generator = pservice.createGenerator(id);
		return generator;
	}

	@Override
	public String getName() {
		String id = generator.getId();
		if (id == null) id = generator.getClass().getName();
		return id;
	}

	@Override
	public String getLabel() {
		String label = generator.getLabel();
		if (label == null) label = generator.getClass().getSimpleName();
		return label;
	}

	@Override
	public String getDescription() {
		String desc =  generator.getDescription();
		if (desc == null) desc = "Generator called '"+generator.getClass().getSimpleName()+"'";
		return desc;
	}

	@Override
	public boolean isFilterable() {
		return true;
	}

	@Override
	public Object getAdapter(Class clazz) {
		// TODO What is required for the model fields? 
		return null;
	}

	public boolean isVisible() {
		return generator.isVisible(); // Would implement extension point to provide visible if this is needed.
	}

	/**
	 * Checks if a given string is in the name or category of this descriptor
	 * @param contents
	 * @return
	 */
	public boolean matches(String contents) {
		if (contents  == null || "".equals(contents)) return true;
		if (getName().toLowerCase().contains(contents.toLowerCase())) return true;
		return false;
	}

	/**
	 * When there are scanning categories which support different particular scan algorithms,
	 * we will set the categories by extension point. For now all are 'Solstice'
	 * @return
	 */
	public String getCategoryLabel() {
		return "Solstice";
	}

	private static Map<String, Image> icons;
	private static Image              defaultImage;
	
	public Image getImage() {
		if (icons==null) createIcons();
		
		Image icon = icons.get(id);
		if (icon != null) return icon;
		
		if (generator.getIconPath()!=null) {
			icon = Activator.getImageDescriptor(generator.getIconPath()).createImage();
			icons.put(generator.getId(), icon);
			return icon;
		}
		
		if (defaultImage==null) defaultImage = Activator.getImageDescriptor("icons/scanner--arrow.png").createImage();
		return defaultImage;
	}

	private void createIcons() {
		icons   = new HashMap<String, Image>(7);
		
		final IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.scanning.api.generator");
		for (IConfigurationElement e : eles) {
			final String     identity = e.getAttribute("id");
				
			final String icon = e.getAttribute("icon");
			if (icon !=null) {
				final String   cont  = e.getContributor().getName();
				final Bundle   bundle= Platform.getBundle(cont);
				final URL      entry = bundle.getEntry(icon);
				final ImageDescriptor des = ImageDescriptor.createFromURL(entry);
				icons.put(identity, des.createImage());		
			}
			
		}
	}

	public String getId() {
		return id;
	} 
}
