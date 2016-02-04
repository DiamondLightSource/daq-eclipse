package uk.ac.diamond.daq.activemq.connector;

import java.net.URI;
import java.util.Collection;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.points.IPosition;

import uk.ac.diamond.daq.activemq.connector.internal.BundleAndClassNameIdResolver;
import uk.ac.diamond.daq.activemq.connector.internal.BundleProvider;
import uk.ac.diamond.daq.activemq.connector.internal.OSGiBundleProvider;
import uk.ac.diamond.daq.activemq.connector.internal.PositionDeserializer;
import uk.ac.diamond.daq.activemq.connector.internal.PositionSerializer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * This class is temporarily in this plugin and needs to be moved out of it once:
 * 1. We move the ActiveMQ dependency to bundle imports rather than jar file.
 * 2. We create a bundle called org.eclipse.scanning.event.activemq to donate the dependency
 * 3. We start the scanning eclipse project and get ActiveMQ ip checked (rather large unless already done might be hard).
 * 
 * @author Matthew Gerring
 *
 */
public class ActivemqConnectorService implements IEventConnectorService {

	private BundleProvider bundleProvider;
	private ObjectMapper osgiMapper;
	private ObjectMapper nonOsgiMapper;

	static {
		System.out.println("Started "+IEventConnectorService.class.getSimpleName());
	}

	/**
	 * Default public constructor - for testing purposes only! Otherwise use OSGi to get the service.
	 */
	public ActivemqConnectorService() {
		this(new OSGiBundleProvider());
	}

	/**
	 * Constructor for testing to allow a BundleProvider to be injected.
	 *
	 * @param bundleProvider
	 */
	public ActivemqConnectorService(BundleProvider bundleProvider) {
		this.bundleProvider = bundleProvider;
	}

	@Override
	public Object createConnectionFactory(URI uri) {
		return new ActiveMQConnectionFactory(uri);
	}

	/**
	 * Serialize the given object to a JSON string
	 * <p>
	 * Objects to be marshalled with this implementation should have no-arg constructors, and getters and setters for
	 * their fields. Primitive and collection types (arrays, Collections and Maps) should work correctly. More
	 * complicated types (generics other than collections, inner classes etc) might or might not work properly.
	 */
	@Override
	public String marshal(Object anyObject) throws Exception {
		if (osgiMapper==null) osgiMapper = createJacksonMapper();
		String json = osgiMapper.writeValueAsString(anyObject);
//		System.out.println(json);
		return json;
	}

	/**
	 * Deserialize the given JSON string as an instance of the given class
	 * <p>
	 * This method will try to find the correct classes for deserialization if possible. If you still have problems
	 * with ClassNotFoundExceptions, one option which might help is to try setting the thread context classloader
	 * before calling the unmarshal method:
	 * <p>
	 * <pre>
	 * ClassLoader tccl = Thread.currentThread().getContextClassLoader();
	 * try {
	 *     Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
	 *     // call the unmarshaller
	 * } finally {
	 *     Thread.currentThread().setContextClassLoader(tccl);
	 * }
	 * </pre>
	 */
	@Override
	public <U> U unmarshal(String string, Class<U> beanClass) throws Exception {
		try {
			if (osgiMapper == null) osgiMapper = createJacksonMapper();
			if (beanClass != null) {
				return osgiMapper.readValue(string, beanClass);
			}
			// If bean class is not supplied, try using Object
			@SuppressWarnings("unchecked")
			U result = (U) osgiMapper.readValue(string, Object.class);
			return result;
		} catch (JsonMappingException jme) {
			jme.printStackTrace();
			// Possibly no bundle and class information in the JSON - fall back to old mapper
			if (nonOsgiMapper == null) nonOsgiMapper = createNonOsgiMapper();
			return (U) nonOsgiMapper.readValue(string, beanClass);
		}
	}

	private final ObjectMapper createJacksonMapper() {

		ObjectMapper mapper = new ObjectMapper();

		// Use custom serialization for IPosition objects
		// (Otherwise all IPosition subclasses will need to become simple beans, i.e. no-arg constructors with getters
		// and setters for all fields. MapPosition.getNames() caused problems because it just returns keys from the map
		// and has no corresponding setter.)
		SimpleModule module = new SimpleModule();
		module.addSerializer(IPosition.class,   new PositionSerializer());
		module.addDeserializer(IPosition.class, new PositionDeserializer());
		mapper.registerModule(module);

		// Be careful adjusting these settings - changing them will probably cause various unit tests to fail which
		// check the exact contents of the serialized JSON string
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.setDefaultTyping(createOSGiTypeIdResolver(mapper));
		return mapper;
	}

	/**
	 * Create a TypeResolverBuilder which will add bundle and class name information to JSON-serialized objects to
	 * allow the correct classes to be loaded during deserialization.
	 * <p>
	 * NOTE: this strongly relies on the exact implementation of the Jackson library - it was written to work with
	 * version 2.2.0 and has not been tested with any other version.
	 *
	 * @return the customised TypeResolverBuilder for use in an OSGi environment
	 */
	private TypeResolverBuilder<?> createOSGiTypeIdResolver(ObjectMapper mapper) {
		TypeResolverBuilder<?> typer = new OSGiTypeResolverBuilder();
		typer = typer.init(JsonTypeInfo.Id.CUSTOM, null);
		typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
		typer = typer.typeProperty("@bundle_and_class");
		return typer;
	}

	/**
	 * A TypeResolverBuilder for use in an OSGi environment.
	 */
	private class OSGiTypeResolverBuilder extends DefaultTypeResolverBuilder {
		private static final long serialVersionUID = 1L;

		public OSGiTypeResolverBuilder() {
			this(null);
		}

		public OSGiTypeResolverBuilder(DefaultTyping typing) {
			super(typing);
		}

		// Override StdTypeResolverBuilder#idResolver() to return our custom BundleAndClassNameIdResolver
		// (We need this override, rather than just providing a custom resolver in StdTypeResolverBuilder#init(), because
		//  the default implementation does not normally pass the base type to the custom resolver but we need it.)
		@Override
		protected TypeIdResolver idResolver(MapperConfig<?> config,
				JavaType baseType, Collection<NamedType> subtypes,
				boolean forSer, boolean forDeser) {
			return new BundleAndClassNameIdResolver(baseType, config.getTypeFactory(), bundleProvider);
		}

		// Override DefaultTypeResolverBuilder#useForType() to add type information to all except primitive types
		@Override
		public boolean useForType(JavaType type) {
			while (type.isArrayType()) {
				type = type.getContentType();
			}
			return !type.isPrimitive();
		}
	}

	private final ObjectMapper createNonOsgiMapper() {

		ObjectMapper mapper = new ObjectMapper();

		// Use custom serialization for IPosition objects
		// (Otherwise all IPosition subclasses will need to become simple beans, i.e. no-arg constructors with getters
		// and setters for all fields. MapPosition.getNames() caused problems because it just returns keys from the map
		// and has no corresponding setter.)
		SimpleModule module = new SimpleModule();
		module.addSerializer(IPosition.class,   new PositionSerializer());
		module.addDeserializer(IPosition.class, new PositionDeserializer());
		mapper.registerModule(module);

		// Be careful adjusting these settings - changing them will probably cause various unit tests to fail which
		// check the exact contents of the serialized JSON string
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		return mapper;
	}
}
