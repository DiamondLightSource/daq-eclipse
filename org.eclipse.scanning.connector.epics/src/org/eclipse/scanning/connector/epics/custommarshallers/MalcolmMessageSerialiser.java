package org.eclipse.scanning.connector.epics.custommarshallers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureSerialiser;
import org.epics.pvmarshaller.marshaller.serialisers.Serialiser;

/**
 * Custom serialiser for the MalcolmMessage class
 * 
 * @author Matt Taylor
 *
 */
public class MalcolmMessageSerialiser implements IPVStructureSerialiser<MalcolmMessage> {

	private Convert convert = ConvertFactory.getConvert();
	private FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	
	@Override
	public Structure buildStructure(Serialiser serialiser, MalcolmMessage msg) throws Exception {
		Structure structure = null;

		switch (msg.getType()) {
		case CALL:

			Structure methodStructure = fieldCreate.createFieldBuilder().
				add("method", ScalarType.pvString).
				createStructure();

			Field field = null;
			
			if (msg.getArguments() != null) {
				
				if (msg.getArguments() instanceof Map) {
					ParamMap paramMap = new ParamMap();
					paramMap.setParameters((Map)msg.getArguments());
					field = serialiser.buildStructure(paramMap).getField("parameters");
				} else {
					throw new Exception("Argument to call '" + msg.getMethod() + "' was not a Map (" + msg.getArguments().getClass() + ")");
				}
				
			} else {
				field = fieldCreate.createFieldBuilder().
						createStructure();
			}
			structure = fieldCreate.createFieldBuilder().
				add("method", methodStructure).
				add("parameters", field).
				createStructure();
			break;
		case GET:
			structure = fieldCreate.createFieldBuilder().
				add("type", ScalarType.pvString).
				add("id", ScalarType.pvLong).
				addArray("endpoint", ScalarType.pvString).
				createStructure();
			break;
		case PUT:
			structure = fieldCreate.createFieldBuilder().
				add("value", fieldCreate.createVariantUnion()).
				createStructure();
			break;
		default:
			throw new Exception("Unexpected MalcolmMessage type");
		}

		return structure;
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, MalcolmMessage msg, PVStructure pvStructure) throws Exception {
		PVString type = pvStructure.getSubField(PVString.class, "type");
		PVLong id = pvStructure.getSubField(PVLong.class, "id");
		PVStringArray endpoint = pvStructure.getSubField(PVStringArray.class, "endpoint");
		PVStructure parameters = pvStructure.getStructureField("parameters");
		PVUnion value = pvStructure.getSubField(PVUnion.class, "value");
		
		switch (msg.getType()) {
		case CALL:
			PVStructure methodName = pvStructure.getStructureField("method");
			PVString method = methodName.getSubField(PVString.class, "method");
			method.put(msg.getMethod());
			
			if (msg.getArguments() != null) {
				ParamMap paramMap = new ParamMap();
				paramMap.setParametersFromObject((Map)msg.getArguments());
				PVStructure params = serialiser.toPVStructure(paramMap);
				convert.copyStructure(params.getStructureField("parameters"), parameters);
			}
			break;
		case GET:
			type.put("Get");
			id.put(msg.getId());
			String[] getEndpointArray = msg.getEndpoint().split("\\.");
			endpoint.put(0, getEndpointArray.length, getEndpointArray, 0);
			break;
		case PUT:
			type.put("Put");
			id.put(msg.getId());
			String[] putEndpointArray = msg.getEndpoint().split("\\.");
			endpoint.put(0, putEndpointArray.length, putEndpointArray, 0);
			PVStructure valueStructure = serialiser.toPVStructure(msg.getValue());
			value.set(valueStructure);
			break;
		default:
			throw new Exception("Unknown MalcolmMessage type");
		}
	}
	
	private class ParamMap {
		private LinkedHashMap<String, Object> parameters;

		public LinkedHashMap<String, Object> getParameters() {
			return parameters;
		}

		public void setParameters(Map<String, Object> mapObj) {
			this.parameters = new LinkedHashMap<>();
			for (String key : mapObj.keySet()) {
				this.parameters.put(key, mapObj.get(key));
			}
		}
		
		public void setParametersFromObject(Object parametersObject) throws Exception {
			if (parametersObject instanceof Map) {
				Map<String, Object> mapObj = (Map)parametersObject;
				setParameters(mapObj);
			} else {
				throw new Exception("Non Map Parameters not supported"); // TODO make this work
			}
		}
	}
}
