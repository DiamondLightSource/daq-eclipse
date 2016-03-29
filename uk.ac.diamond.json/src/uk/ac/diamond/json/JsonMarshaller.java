package uk.ac.diamond.json;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IOrientableROI;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.IPosition;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;

import uk.ac.diamond.json.api.IJsonMarshaller;
import uk.ac.diamond.json.internal.BundleAndClassNameIdResolver;
import uk.ac.diamond.json.internal.BundleProvider;
import uk.ac.diamond.json.internal.OSGiBundleProvider;
import uk.ac.diamond.json.internal.PositionDeserializer;
import uk.ac.diamond.json.internal.PositionSerializer;
import uk.ac.diamond.json.mixin.roi.CircularROIMixIn;
import uk.ac.diamond.json.mixin.roi.IOrientableROIMixIn;
import uk.ac.diamond.json.mixin.roi.IROIMixIn;
import uk.ac.diamond.json.mixin.roi.IRectangularROIMixIn;
import uk.ac.diamond.json.mixin.roi.LinearROIMixIn;
import uk.ac.diamond.json.mixin.roi.PolylineROIMixIn;
import uk.ac.diamond.json.mixin.roi.RectangularROIMixIn;

/**
 * JSON marshaller implementation which allows objects to be converted to and from JSON strings
 * <p>
 * This implementation adds OSGi type information to encoded JSON strings and will use it if present when deserializing.
 * <p>
 * If type information is not present in a JSON string it will attempt to deserialize it anyway, but in an OSGi
 * environment this will probably fail for anything except primitive and core Java types since other classes will not be
 * available to this bundle's classloader. If classloading fails, it might still be possible to make deserialization
 * work correctly in some cases by setting the thread context classloader before calling the unmarshal method, since
 * some of the required classes (but not necessarily all) might be available to the classloader which loaded the
 * caller's class:
 * <pre>
 * ClassLoader tccl = Thread.currentThread().getContextClassLoader();
 * try {
 * 	Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
 * 	// call the unmarshaller
 * } finally {
 * 	Thread.currentThread().setContextClassLoader(tccl);
 * }
 * </pre>
 *
 * @author Colin Palmer
 */
public class JsonMarshaller implements IJsonMarshaller {

	private static final String TYPE_INFO_FIELD_NAME = "@bundle_and_class";

	private BundleProvider bundleProvider;
	private ObjectMapper osgiMapper;
	private ObjectMapper nonOsgiMapper;

	static {
		System.out.println("Started " + IJsonMarshaller.class.getSimpleName());
	}

	/**
	 * Default public constructor - for testing purposes only! Otherwise use OSGi to get the service.
	 */
	public JsonMarshaller() {
		this(new OSGiBundleProvider());
	}

	/**
	 * Constructor for testing to allow a BundleProvider to be injected.
	 *
	 * @param bundleProvider
	 */
	public JsonMarshaller(BundleProvider bundleProvider) {
		this.bundleProvider = bundleProvider;
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
		} catch (JsonMappingException | IllegalArgumentException ex) {
			if ((ex instanceof JsonMappingException && ex.getMessage().contains(TYPE_INFO_FIELD_NAME))
					|| ex instanceof IllegalArgumentException && ex.getCause() instanceof ClassNotFoundException) {
				// No bundle and class information in the JSON - fall back to old mapper in case JSON has come from an older version
				if (nonOsgiMapper == null) nonOsgiMapper = createNonOsgiMapper();
				return nonOsgiMapper.readValue(string, beanClass);
			}
			throw ex;
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

		// Add mix-in annotations for ROIs
		module.setMixInAnnotation(IROI.class, IROIMixIn.class);
		module.setMixInAnnotation(IOrientableROI.class, IOrientableROIMixIn.class);
		module.setMixInAnnotation(IRectangularROI.class, IRectangularROIMixIn.class);
		module.setMixInAnnotation(RectangularROI.class, RectangularROIMixIn.class);
		module.setMixInAnnotation(CircularROI.class, CircularROIMixIn.class);
		module.setMixInAnnotation(LinearROI.class, LinearROIMixIn.class);
		module.setMixInAnnotation(PolylineROI.class, PolylineROIMixIn.class);

		mapper.registerModule(module);

		// Be careful adjusting these settings - changing them will probably cause various unit tests to fail which
		// check the exact contents of the serialized JSON string
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setDefaultTyping(createOSGiTypeIdResolver());
		mapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
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
	private TypeResolverBuilder<?> createOSGiTypeIdResolver() {
		TypeResolverBuilder<?> typer = new OSGiTypeResolverBuilder();
		typer = typer.init(JsonTypeInfo.Id.CUSTOM, null);
		typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
		typer = typer.typeProperty(TYPE_INFO_FIELD_NAME);
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

		// Override DefaultTypeResolverBuilder#useForType() to add type information to all except primitive and final
		// core Java types
		@Override
		public boolean useForType(JavaType type) {
			while (type.isArrayType()) {
				type = type.getContentType();
			}
			boolean isNotPrimitive = !type.isPrimitive();
			boolean isFinal = type.isFinal();
			boolean isCoreJavaClass = type.getRawClass().getName().startsWith("java.");
			boolean isNotFinalCoreJavaClass = !(isFinal && isCoreJavaClass);
			return isNotPrimitive && isNotFinalCoreJavaClass;
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
