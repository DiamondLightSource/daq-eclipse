package org.eclipse.scanning.example.detector;

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.scanning.api.IAttributeContainer;

/**
 * 
 * Class to deal with attribute registration into the nexus file.
 * 
 * Can be used as delegate or static method.
 * 
 * @author Matthew Gerring
 *
 */
public class Attributes {

	private IAttributeContainer container;

	public Attributes(IAttributeContainer container) {
		this.container = container;
	}
	
	public void registerAttributes(NXobject positioner) throws Exception {
		registerAttributes(positioner, container);
	}
	/**
	 * 
	 * @param positioner
	 * @param container
	 * @throws Exception
	 */
	public static void registerAttributes(NXobject positioner, IAttributeContainer container) throws Exception {
		// We create the attributes, if any
		positioner.setField("name", container.getName());
		if (container.getAttributeNames()!=null) for(String attrName : container.getAttributeNames()) {
			positioner.setField(attrName, container.getAttribute(attrName));
		}
	}

}
