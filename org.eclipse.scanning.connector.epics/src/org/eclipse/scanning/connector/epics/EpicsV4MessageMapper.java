package org.eclipse.scanning.connector.epics;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.connector.epics.custommarshallers.BoundingBoxDeserialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.BoundingBoxSerialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.CircularROIDeserialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.CircularROISerialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.GridModelDeserialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.GridModelSerialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.IPointGeneratorSerialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.MalcolmMessageSerialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.PyDictionarySerialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.RectangularROIDeserialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.RectangularROISerialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.SpiralModelDeserialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.SpiralModelSerialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.StepModelDeserialiser;
import org.eclipse.scanning.connector.epics.custommarshallers.StepModelSerialiser;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvmarshaller.marshaller.PVMarshaller;
import org.python.core.PyDictionary;

public class EpicsV4MessageMapper {
	
	private PVMarshaller marshaller;
	
	private static String ERROR_TYPE = "malcolm:core/Error:";
	
	private static String TYPE_ID_KEY = "type"; // TODO change to 'typeid' when change is made in the toDict
	
	public EpicsV4MessageMapper() {
		marshaller = new PVMarshaller();
		marshaller.registerMapTypeIdKey(TYPE_ID_KEY);
		marshaller.registerSerialiser(IPointGenerator.class, new IPointGeneratorSerialiser());
		marshaller.registerSerialiser(PyDictionary.class, new PyDictionarySerialiser());
		marshaller.registerSerialiser(MalcolmMessage.class, new MalcolmMessageSerialiser());
		
		marshaller.registerSerialiser(SpiralModel.class, new SpiralModelSerialiser());
		marshaller.registerDeserialiser("SpiralModel", new SpiralModelDeserialiser());
		marshaller.registerSerialiser(StepModel.class, new StepModelSerialiser());
		marshaller.registerDeserialiser("StepModel", new StepModelDeserialiser());
		marshaller.registerSerialiser(GridModel.class, new GridModelSerialiser());
		marshaller.registerDeserialiser("GridModel", new GridModelDeserialiser());
		marshaller.registerSerialiser(CircularROI.class, new CircularROISerialiser());
		marshaller.registerDeserialiser("CircularROI", new CircularROIDeserialiser());
		marshaller.registerSerialiser(RectangularROI.class, new RectangularROISerialiser());
		marshaller.registerDeserialiser("RectangularROI", new RectangularROIDeserialiser());
		marshaller.registerSerialiser(BoundingBox.class, new BoundingBoxSerialiser());
		marshaller.registerDeserialiser("BoundingBox", new BoundingBoxDeserialiser());
	}
	
	public PVStructure convertMalcolmMessageToPVStructure(MalcolmMessage malcolmMessage) throws Exception {
				
		PVStructure pvRequest = marshaller.toPVStructure(malcolmMessage);
				
		return pvRequest;
	}
	
	public MalcolmMessage convertCallPVStructureToMalcolmMessage(PVStructure structure, MalcolmMessage message) throws Exception {
		MalcolmMessage result = new MalcolmMessage();
		result.setType(Type.RETURN);
		result.setEndpoint(message.getEndpoint());
		result.setId(message.getId());
				
		if (structure.getStructure().getID().startsWith(ERROR_TYPE)) {
			result.setType(Type.ERROR);
			PVString errorMessage = structure.getSubField(PVString.class, "message");
			result.setMessage(errorMessage.get());
		} else {
			Object returnedObject = marshaller.fromPVStructure(structure, Object.class);
			
			result.setValue(returnedObject);
		}
		
		return result;
	}
	
	public MalcolmMessage convertSubscribeUpdatePVStructureToMalcolmMessage(PVStructure structure, MalcolmMessage message) throws Exception {
		MalcolmMessage result = new MalcolmMessage();
		result.setType(Type.UPDATE);
		result.setEndpoint(message.getEndpoint());
		result.setId(message.getId());
				
		Object returnedObject = getEndpointObjectFromPVStructure(structure, message.getEndpoint());
		
		result.setValue(returnedObject);
		
		return result;
	}
	
	public MalcolmMessage convertGetPVStructureToMalcolmMessage(PVStructure structure, MalcolmMessage message)
	{
		MalcolmMessage result = new MalcolmMessage();
		result.setType(Type.RETURN);
		result.setEndpoint(message.getEndpoint());
		result.setId(message.getId());
						
		if (structure.getStructure().getID().startsWith(ERROR_TYPE)) {
			result.setType(Type.ERROR);
			PVString errorMessage = structure.getSubField(PVString.class, "message");
			result.setMessage(errorMessage.get());
		} else {
			try {
				result.setValue(getEndpointObjectFromPVStructure(structure, message.getEndpoint()));
			} catch (Exception e) {
				e.printStackTrace();
				result.setMessage("Error converting " + message.getEndpoint() + ": " + e.getMessage());
				result.setType(Type.ERROR); 
			}
		}
		
		return result;
	}
	
	public void populatePutPVStructure(PVStructure pvStructure, MalcolmMessage message) throws Exception {
		PVField endPointField = pvStructure.getSubField(message.getEndpoint());
		if (endPointField.getField().getType().equals(org.epics.pvdata.pv.Type.union)) {
			// Create from scratch for union
			PVStructure newStructure = marshaller.toPVStructure(message.getValue());
			PVUnion unionField = (PVUnion)endPointField;
			unionField.set(newStructure);
		} else {
			marshaller.setFieldWithValue(pvStructure, message.getEndpoint(), message.getValue());
		}
	}
	
	public PVStructure pvMarshal(Object anyObject) throws Exception {
		return marshaller.toPVStructure(anyObject);
	}

	public <U> U pvUnmarshal(PVStructure anyObject, Class<U> beanClass) throws Exception {
		return marshaller.fromPVStructure(anyObject, beanClass);
	} 
	
	private Object getEndpointObjectFromPVStructure(PVStructure pvStructure, String endpoint) throws Exception {
		
		PVStructure parentStructure = null;
		String[] requestArray = endpoint.split("\\.");
		
		if (requestArray.length == 1) {
			parentStructure = pvStructure;
		} else {
			String parentStructureString = "";
			for (int i = 0; i < requestArray.length - 1; i++) {
				parentStructureString += requestArray[i];
			}
			parentStructure = pvStructure.getStructureField(parentStructureString);
		}

		if (endpoint.contains(",")) {
			return marshaller.fromPVStructure(pvStructure, Object.class);
		} else {
			return marshaller.getObjectFromField(parentStructure, requestArray[requestArray.length-1]);
		}
	}
}
