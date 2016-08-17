package org.eclipse.scanning.device.ui.points;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.richbeans.widgets.table.SeriesTable;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class GeneratorDescriptor<T extends IScanPathModel> implements ISeriesItemDescriptor {
	
	private final IPointGenerator<T> generator;
	private final SeriesTable        table;

	public GeneratorDescriptor(SeriesTable table, String id, IPointGeneratorService pservice) throws GeneratorException {
		this.generator = (IPointGenerator<T>)pservice.createGenerator(id);
		this.table = table;
	}
	public GeneratorDescriptor(SeriesTable table, T model, IPointGeneratorService pservice) throws GeneratorException {
		this.generator = pservice.createGenerator(model);
		this.table = table;
	}

	@Override
	public String toString() {
		return "GeneratorDescriptor ["+generator.getLabel()+"]";
	}

	@Override
	public IPointGenerator<?> getSeriesObject() throws GeneratorException {
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
		
		if (IPointGenerator.class==clazz) return generator;
		if (List.class == clazz)          return getGenerators();
		if (CompoundModel.class == clazz) return new CompoundModel(getModels()); // TODO Regions?
		return null;
	}
	
	private List getModels() {
		
		List<IPointGenerator<?>> gens = getGenerators();
		List     ret  = new ArrayList<>(gens.size());
		for (IPointGenerator<?> gen : gens) ret.add(gen.getModel());
		return ret;
	}
	private List<IPointGenerator<?>> getGenerators() {
		// They can only ask for a list of the series of 
		// generators which we are editing.
		final List<ISeriesItemDescriptor> descriptors = table.getSeriesItems();
		final List<IPointGenerator<?>>    gens        = new ArrayList<>();
		for (ISeriesItemDescriptor des : descriptors) {
			if (des instanceof GeneratorDescriptor<?>) {
				GeneratorDescriptor<?> gdes = (GeneratorDescriptor<?>)des;
				gens.add(gdes.generator);
			}
		}
		return gens;
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
		
		Image icon = icons.get(generator.getId());
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
	public T getModel() {
		return generator.getModel();
	}
}
