package org.eclipse.scanning.connector.epics.custommarshallers;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.points.PySerializable;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Structure;
import org.epics.pvmarshaller.marshaller.api.IPVStructureSerialiser;
import org.epics.pvmarshaller.marshaller.serialisers.Serialiser;
import org.python.core.PyDictionary;

/**
 * Custom serialiser for IPointGenerator.
 * TODO - make this non 'test' and finalise custom serialisation strategy for IPointGenerator 
 * @author Matt Taylor
 *
 */
public class IPointGeneratorSerialiser implements IPVStructureSerialiser<IPointGenerator> {

	@Override
	public Structure buildStructure(Serialiser serialiser, IPointGenerator generator) throws Exception {
		if (generator instanceof PySerializable) {
			PySerializable pySerializableGenerator = (PySerializable)generator;
			PyDictionary generatorAsMap = pySerializableGenerator.toDict();
			return serialiser.buildStructure(generatorAsMap);
		} else {
			throw new Exception("IPointGenerator wasn't PySerializable. Unable to serialise");
		}
	}

	@Override
	public void populatePVStructure(Serialiser serialiser, IPointGenerator generator, PVStructure pvStructure) throws Exception {
		if (generator instanceof PySerializable) {
			PySerializable pySerializableGenerator = (PySerializable)generator;
			PyDictionary generatorAsMap = pySerializableGenerator.toDict();
			serialiser.setValues(generatorAsMap, pvStructure);
		} else {
			throw new Exception("IPointGenerator wasn't PySerializable. Unable to serialise");
		}
	}
	
}
