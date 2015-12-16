package uk.ac.diamond.daq.activemq.connector;

import java.net.URI;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.points.IPosition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * This class is temporarily in this plugin and needs to be moved out of it once:
 * 1. We move the ActiveMQ dependency to bundle imports rather than jar file.
 * 2. We create a bundle called org.eclipse.scanning.event.activemq to donate the dependency
 * 3. We start the scanning eclipse project and get ActiveMQ ip checked (rather large unless already done might be hard).
 * 
 * @author fcp94556
 *
 */
public class ActivemqConnectorService implements IEventConnectorService {

	static {
		System.out.println("Started "+IEventConnectorService.class.getSimpleName());
	}

	private ObjectMapper mapper;


	@Override
	public String marshal(Object anyObject) throws Exception {
		if (mapper==null) mapper = createJacksonMapper();
		return mapper.writeValueAsString(anyObject);
	}


	@Override
	public <U> U unmarshal(String string, Class<U> beanClass) throws Exception {
		if (mapper==null) mapper = createJacksonMapper();
		return (U)mapper.readValue(string, beanClass);
	}

	private final ObjectMapper createJacksonMapper() {
		
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(IPosition.class,   new PositionSerializer());
		module.addDeserializer(IPosition.class, new PositionDeserializer());
//		module.addSerializer(State.class,   new StateSerializer());
//		module.addDeserializer(State.class, new StateDeserializer());
//		module.addSerializer(Type.class,    new TypeSerializer());
//		module.addDeserializer(Type.class,  new TypeDeserializer());
//		mapper.registerModule(module);	
//		mapper.setSerializationInclusion(Include.NON_NULL);
		return mapper;
	}


	@Override
	public Object createConnectionFactory(URI uri) {
		return new ActiveMQConnectionFactory(uri);		
	}

}
