package uk.ac.diamond.daq.activemq.connector;

import java.net.URI;
import java.util.Collection;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.points.IPosition;

import uk.ac.diamond.daq.activemq.connector.internal.BundleAndClassNameIdResolver;
import uk.ac.diamond.daq.activemq.connector.internal.BundleProvider;
import uk.ac.diamond.daq.activemq.connector.internal.OSGiBundleProvider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
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

	private BundleProvider bundleProvider;
	private ObjectMapper mapper;

	@Override
	public String marshal(Object anyObject) throws Exception {
		if (mapper==null) mapper = createJacksonMapper();
		String json = mapper.writeValueAsString(anyObject);
		System.out.println(json);
		return json;
	}

	@Override
	public <U> U unmarshal(String string, Class<U> beanClass) throws Exception {
		if (mapper==null) mapper = createJacksonMapper();
//		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
//		try {
//			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		return (U)mapper.readValue(string, beanClass);
//		} finally {
//			Thread.currentThread().setContextClassLoader(tccl);
//		}
	}

	private final ObjectMapper createJacksonMapper() {

		ObjectMapper mapper = new ObjectMapper();

		// TODO can probably remove this if type serialization works properly
		SimpleModule module = new SimpleModule();
		module.addSerializer(IPosition.class,   new PositionSerializer());
		module.addDeserializer(IPosition.class, new PositionDeserializer());
//		module.addSerializer(State.class,   new StateSerializer());
//		module.addDeserializer(State.class, new StateDeserializer());
//		module.addSerializer(Type.class,    new TypeSerializer());
//		module.addDeserializer(Type.class,  new TypeDeserializer());
		mapper.registerModule(module);

		// Be careful adjusting these settings - changing them will probably cause various unit tests to fail which
		// check the exact contents of the serialized JSON string
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		// TODO can we remove this and just use the OSGi resolver all the time?
		if (inOSGiFramework()) {
			mapper.setDefaultTyping(createOSGiTypeIdResolver(mapper));
		} else {
			mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
		}
		return mapper;
	}

	
	/**
	 * Are we running in an OSGi environment?
	 * <p>
	 * Deliberately <code>private</code>. For testing, rather than overriding this method, pass a mock BundleContext to the Activator.
	 *
	 * @return <code>true</code> if a BundleContext is available
	 */
	private boolean inOSGiFramework() {
		return (Activator.getContext() != null);
	}

	/**
	 * Create a TypeResolverBuilder which will add bundle and class name information to JSON-serialized objects, and use this information
	 * to load the correct classes for deserialization. The TypeIdResolver relies on the OSGi BundleContext (obtained from the Activator)
	 * to link classes to the correct bundles, so will only work in an OSGi environment.
	 * <p>
	 * NOTE: this strongly relies on the exact implementation of the Jackson library - it was written to work with version 2.2.0 and has
	 * not been tested with any other version.
	 *
	 * @return the customised TypeResolverBuilder for use in an OSGi environment
	 */
	private TypeResolverBuilder<?> createOSGiTypeIdResolver(ObjectMapper mapper) {
		// Override StdTypeResolverBuilder#idResolver() to return our custom BundleAndClassNameIdResolver
		// (We need this override, rather than just providing a custom resolver in StdTypeResolverBuilder#init(), because
		//  the default implementation does not pass the base type to the custom resolver, but the base type is needed.)
		TypeResolverBuilder<?> typer = new DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL) {
			private static final long serialVersionUID = 1L;
			@Override
			protected TypeIdResolver idResolver(MapperConfig<?> config,
					JavaType baseType, Collection<NamedType> subtypes,
					boolean forSer, boolean forDeser) {
				return new BundleAndClassNameIdResolver(baseType, config.getTypeFactory(), bundleProvider);
			}
		};
		typer = typer.init(JsonTypeInfo.Id.CUSTOM, null);
		typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
		typer = typer.typeProperty("@bundle_and_class");
		return typer;
	}

	@Override
	public Object createConnectionFactory(URI uri) {
		return new ActiveMQConnectionFactory(uri);
	}
}
