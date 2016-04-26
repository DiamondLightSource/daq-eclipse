package uk.ac.diamond.daq.activemq.connector;

import java.net.URI;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.scanning.api.event.IEventConnectorService;

/**
 * This class is temporarily in this plugin and needs to be moved out of it once:
 * 1. We move the ActiveMQ dependency to bundle imports rather than jar file.
 * 2. We create a bundle called org.eclipse.scanning.event.activemq to donate the dependency
 * 3. We start the scanning eclipse project and get ActiveMQ ip checked (rather large unless already done might be hard).
 * <p>
 * JSON marshalling is done by delegating to the new JsonMarshaller service which encapsulates all JSON interactions
 * behind one cohesive interface.
 * 
 * @author Matthew Gerring
 * @author Colin Palmer
 *
 */
public class ActivemqConnectorService implements IEventConnectorService {

	private static IMarshallerService jsonMarshaller;

	public static void setJsonMarshaller(IMarshallerService jsonMarshaller) {
		ActivemqConnectorService.jsonMarshaller = jsonMarshaller;
	}

	static {
		System.out.println("Started " + ActivemqConnectorService.class.getSimpleName());
	}

	/**
	 * Default public constructor - for testing purposes only! Otherwise use OSGi to get the service.
	 */
	public ActivemqConnectorService() {
	}

	@Override
	public Object createConnectionFactory(URI uri) {
		return new ActiveMQConnectionFactory(uri);
	}

	@Override
	public String marshal(Object anyObject) throws Exception {
		checkJsonMarshaller();
		return jsonMarshaller.marshal(anyObject);
	}

	@Override
	public <U> U unmarshal(String json, Class<U> beanClass) throws Exception {
		checkJsonMarshaller();
		return jsonMarshaller.unmarshal(json, beanClass);
	}

	private void checkJsonMarshaller() {
		if (jsonMarshaller == null) {
			// OSGi should always provide the JSON marshaller. If it's not present, probably someone is calling this
			// constructor directly and may have forgotten to set the JSON marshaller first, so we print a warning
			String msg = this.getClass().getSimpleName() + " needs an IJsonMarshaller to function correctly";
			System.err.println(msg);
			throw new NullPointerException(msg);
		}
	}
}
