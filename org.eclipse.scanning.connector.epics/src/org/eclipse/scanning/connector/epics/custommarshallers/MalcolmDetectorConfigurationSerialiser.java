package org.eclipse.scanning.connector.epics.custommarshallers;

import org.eclipse.scanning.api.malcolm.models.MalcolmDetectorConfiguration;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Structure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureSerialiser;
import org.epics.pvmarshaller.marshaller.serialisers.Serialiser;

/**
 * Custom serialiser for MalcolmDetectorConfiguration.
 * @author Matt Taylor
 *
 */
public class MalcolmDetectorConfigurationSerialiser implements IPVStructureSerialiser<MalcolmDetectorConfiguration> {

	@Override
	public Structure buildStructure(Serialiser serialiser, MalcolmDetectorConfiguration detectorConfiguration) throws Exception {
		MalcolmDetectorConfigurationModel model = new MalcolmDetectorConfigurationModel();
		model.setModel(detectorConfiguration.getModel());
		Structure parentStructure = serialiser.buildStructure(model);
		Field modelField = parentStructure.getField("model");
		if (modelField instanceof Structure) {
			Structure modelStructure = (Structure)modelField;
			return modelStructure;
		} else {
			return parentStructure;
		}
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, MalcolmDetectorConfiguration detectorConfiguration, PVStructure pvStructure) throws Exception {
		MalcolmDetectorConfigurationModel model = new MalcolmDetectorConfigurationModel();
		model.setModel(detectorConfiguration.getModel());
		PVStructure parentPVStructure = serialiser.toPVStructure(model);

		ConvertFactory.getConvert().copyStructure(parentPVStructure.getStructureField("model"), pvStructure);
	}
	
	private class MalcolmDetectorConfigurationModel {
		private Object model;

		@SuppressWarnings("unused")
		public Object getModel() {
			return model;
		}

		public void setModel(Object model) {
			this.model = model;
		}
		
	}
}
