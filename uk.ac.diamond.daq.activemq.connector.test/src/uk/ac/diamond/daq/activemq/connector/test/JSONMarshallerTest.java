package uk.ac.diamond.daq.activemq.connector.test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;
import uk.ac.diamond.daq.activemq.connector.internal.Activator;
import uk.ac.diamond.daq.activemq.connector.test.testobject.Animal;
import uk.ac.diamond.daq.activemq.connector.test.testobject.Bird;
import uk.ac.diamond.daq.activemq.connector.test.testobject.Cat;
import uk.ac.diamond.daq.activemq.connector.test.testobject.Person;

/**
 * Unit tests for the Jackson JSON marshaller.
 * <p>
 * This class is abstract to allow subclasses to set up the marshaller as required before the tests are run, and
 * to provide values for the JSON strings used in the tests.
 * <p>
 * If the marshaller settings are changed, the new JSON string produced in each test can be written to std out by
 * uncommenting the relevant line in tearDown(), allowing it to be copied into the Java code to update the tests.
 *
 * @author Colin Palmer
 *
 */
public class JSONMarshallerTest {

	private String jsonForJim = "{\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=1.2.0.test&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Person\",\n  \"name\" : \"Jim\",\n  \"pet\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Bird\",\n    \"name\" : \"Polly\",\n    \"feathers\" : \"Green\"\n  }\n}";
	private String jsonForJohn = "{\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=1.2.0.test&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Person\",\n  \"name\" : \"John\",\n  \"pet\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Cat\",\n    \"name\" : \"Felix\",\n    \"whiskers\" : \"Luxuriant\"\n  }\n}";
	private String jsonForFelix = "{\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}";
	private String jsonForAnimalArray = "[ \"bundle=&version=&class=[Luk.ac.diamond.daq.activemq.connector.test.testobject.Animal;\", [ {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}, {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Bird\",\n  \"name\" : \"Polly\",\n  \"feathers\" : \"Green\"\n}, {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n} ] ]";
	private String jsonForAnimalList = "[ \"bundle=&version=&class=java.util.ArrayList\", [ {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}, {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Bird\",\n  \"name\" : \"Polly\",\n  \"feathers\" : \"Green\"\n}, {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n} ] ]";
	private String jsonForAnimalSet = "[ \"bundle=&version=&class=java.util.HashSet\", [ {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}, {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Bird\",\n  \"name\" : \"Polly\",\n  \"feathers\" : \"Green\"\n} ] ]";
	private String jsonForAnimalMap = "{\n  \"@bundle_and_class\" : \"bundle=&version=&class=java.util.HashMap\",\n  \"Polly\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Bird\",\n    \"name\" : \"Polly\",\n    \"feathers\" : \"Green\"\n  },\n  \"Felix\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Cat\",\n    \"name\" : \"Felix\",\n    \"whiskers\" : \"Luxuriant\"\n  },\n  \"John\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=1.2.0.test&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Person\",\n    \"name\" : \"John\",\n    \"pet\" : {\n      \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Cat\",\n      \"name\" : \"Felix\",\n      \"whiskers\" : \"Luxuriant\"\n    }\n  },\n  \"Jim\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=1.2.0.test&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Person\",\n    \"name\" : \"Jim\",\n    \"pet\" : {\n      \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.test.testobject.Bird\",\n      \"name\" : \"Polly\",\n      \"feathers\" : \"Green\"\n    }\n  }\n}";
	private String testString = "Hello world!";
	private String jsonForTestString = "\"Hello world!\"";
	private int testInt = -56;
	private String jsonForTestInt = "-56";
	private long testLong = 1234567890L;
	private String jsonForTestLong = "[ \"bundle=&version=&class=java.lang.Long\", 1234567890 ]";

	private ActivemqConnectorService marshaller;

	private String json;

	// Test objects
	private Bird polly;
	private Cat felix;
	private Person jim;
	private Person john;

	// Mocks
	@Mock private Bundle exampleBundleV1;
	@Mock private Bundle exampleBundleV2;
	@Mock private Bundle otherExampleBundle;
	@Mock private BundleContext bundleContext;

	@Before
	public void setUp() throws Exception {
		createTestObjects();
		MockitoAnnotations.initMocks(this);

		when(exampleBundleV1.getSymbolicName()).thenReturn(Person.BUNDLE_NAME_FOR_TESTING);
		when(exampleBundleV1.getVersion()).thenReturn(new Version(Person.BUNDLE_VERSION_FOR_TESTING));
		MockClassLoaderAnswer exampleV1Answer = new MockClassLoaderAnswer(Person.class, Animal.class);
		when(exampleBundleV1.loadClass(any())).thenAnswer(exampleV1Answer);

		when(exampleBundleV2.getSymbolicName()).thenReturn(Bird.BUNDLE_NAME_FOR_TESTING);
		when(exampleBundleV2.getVersion()).thenReturn(new Version(Bird.BUNDLE_VERSION_FOR_TESTING));
		when(exampleBundleV2.loadClass(any())).thenAnswer(new MockClassLoaderAnswer(Bird.class));

		when(otherExampleBundle.getSymbolicName()).thenReturn(Cat.BUNDLE_NAME_FOR_TESTING);
		when(otherExampleBundle.getVersion()).thenReturn(Version.emptyVersion);
		when(otherExampleBundle.loadClass(any())).thenAnswer(new MockClassLoaderAnswer(Cat.class));

		when(bundleContext.getBundles()).thenReturn(new Bundle[] { exampleBundleV1, exampleBundleV2, otherExampleBundle });
		new Activator().start(bundleContext);

		TestBundleProvider bundleProvider = new TestBundleProvider();
		bundleProvider.registerBundleForClass(Person.class, exampleBundleV1);
		bundleProvider.registerBundleForClass(Animal.class, exampleBundleV1);
		bundleProvider.registerBundleForClass(Bird.class, exampleBundleV2);
		bundleProvider.registerBundleForClass(Cat.class, otherExampleBundle);

		marshaller = new ActivemqConnectorService(bundleProvider);
	}

	private void createTestObjects() {

		polly = new Bird();
		polly.setName("Polly");
		polly.setFeathers("Green");

		felix = new Cat();
		felix.setName("Felix");
		felix.setWhiskers("Luxuriant");

		jim = new Person();
		jim.setName("Jim");
		jim.setPet((Animal) polly);

		john = new Person();
		john.setName("John");
		john.setPet((Animal) felix);
	}

	@After
	public void tearDown() throws Exception {
		if (json != null) {
			// So we can see what's going on
//			System.out.println("JSON: " + json);

			// To make it easy to replace expected JSON values in the code when we're sure they're correct
			@SuppressWarnings("unused")
			String javaLiteralForJSONString = '"' + StringEscapeUtils.escapeJava(json) + '"';
//			System.out.println("Java literal:\n" + javaLiteralForJSONString);
		}
		json = null;
		marshaller = null;
	}

	@Test
	public void testSerializationOfJim() throws Exception {
		json = marshaller.marshal((Object) jim);
		assertEquals(jsonForJim, json);
	}

	@Test
	public void testSerializationOfJohn() throws Exception {
		json = marshaller.marshal((Object) john);
		assertEquals(jsonForJohn, json);
	}

	@Test
	public void testDeserialisationOfJim() throws Exception {
		Person deserializedJim = marshaller.unmarshal(jsonForJim, Person.class);
		assertEquals("Jim", deserializedJim.getName());
		assertThat(deserializedJim.getPet(), is(instanceOf(Bird.class)));
		Bird deserializedPolly = (Bird) deserializedJim.getPet();
		assertEquals("Polly", deserializedPolly.getName());
		assertEquals("Green", deserializedPolly.getFeathers());
	}

	@Test
	public void testDeserialisationOfJohn() throws Exception {
		Person deserializedJohn = marshaller.unmarshal(jsonForJohn, Person.class);
		assertEquals("John", deserializedJohn.getName());
		assertThat(deserializedJohn.getPet(), is(instanceOf(Cat.class)));
		Cat deserializedFelix = (Cat) deserializedJohn.getPet();
		assertEquals("Felix", deserializedFelix.getName());
		assertEquals("Luxuriant", deserializedFelix.getWhiskers());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeserialisationOfJohnAsAnimal() throws Exception {
		marshaller.unmarshal(jsonForJohn, Animal.class);
	}

	@Test
	public void testSerialisationOfFelix() throws Exception {
		json = marshaller.marshal((Object) felix);
		assertEquals(jsonForFelix, json);
	}

	@Test
	public void testDeserialisationOfConcreteBeanBAsAbstractBean() throws Exception {
		Animal deserializedFelix = marshaller.unmarshal(jsonForFelix, Animal.class);
		assertEquals("Felix", deserializedFelix.getName());
		assertThat(deserializedFelix, is(instanceOf(Cat.class)));
		assertEquals("Luxuriant", ((Cat) deserializedFelix).getWhiskers());
	}

	@Test
	public void testArraySerialization() throws Exception {
		Object[] animalArray = new Animal[] { felix, polly, felix };
		json = marshaller.marshal(animalArray);
		assertEquals(jsonForAnimalArray, json);
	}

	@Test
	public void testArrayDeserializationAsAnimalArray() throws Exception {
		Animal[] animalArray = marshaller.unmarshal(jsonForAnimalArray, Animal[].class);
		assertThat(animalArray[0], is(instanceOf(Cat.class)));
		assertThat(animalArray[1], is(instanceOf(Bird.class)));
		assertThat(animalArray[2], is(instanceOf(Cat.class)));
		assertThat(animalArray[0].getName(), is("Felix"));
	}

	@Test
	public void testArrayDeserializationAsObjectArray() throws Exception {
		Object[] objectArray = marshaller.unmarshal(jsonForAnimalArray, Object[].class);
		assertThat(objectArray[0], is(instanceOf(Cat.class)));
		assertThat(objectArray[1], is(instanceOf(Bird.class)));
		assertThat(objectArray[2], is(instanceOf(Cat.class)));
		assertThat(((Cat) objectArray[0]).getWhiskers(), is(equalTo("Luxuriant")));
	}

	@Test
	public void testListSerialization() throws Exception {
		List<Animal> animalList = Arrays.asList(felix, polly, felix);
		json = marshaller.marshal(animalList);
		assertEquals(jsonForAnimalList, json);
	}

	@Test
	public void testListDeserialization() throws Exception {
		@SuppressWarnings({ "unchecked" })
		List<Animal> animalList = marshaller.unmarshal(jsonForAnimalList, List.class);
		assertThat(animalList.get(0), is(instanceOf(Cat.class)));
		assertThat(animalList.get(1), is(instanceOf(Bird.class)));
		assertThat(animalList.get(2), is(instanceOf(Cat.class)));
		assertThat(animalList.get(0).getName(), is("Felix"));
	}

	@Test
	public void testSetDeserialization() throws Exception {
		@SuppressWarnings({ "unchecked" })
		Set<Animal> animalSet = marshaller.unmarshal(jsonForAnimalSet, Set.class);
		assertThat(animalSet.size(), is(equalTo(2)));
	}

	@Test
	public void testSetSerialization() throws Exception { // also relies on deserialization
		Set<Animal> originalSet = new HashSet<>(Arrays.asList(felix, polly, felix));
		json = marshaller.marshal(originalSet);
		@SuppressWarnings("unchecked")
		Set<Animal> deserializedSet = marshaller.unmarshal(json, Set.class);
		assertEquals(deserializedSet, originalSet);
	}

	@Test
	public void testMapSerialization() throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(jim.getName(), jim);
		map.put(john.getName(), john);
		map.put(felix.getName(), felix);
		map.put(polly.getName(), polly);
		json = marshaller.marshal(map);
		assertEquals(jsonForAnimalMap, json);
	}

	@Test
	public void testMapDeserialization() throws Exception {
		Object object = marshaller.unmarshal(jsonForAnimalMap, Object.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) object;
		assertThat(map.size(), is(equalTo(4)));
		assertThat(map.get(jim.getName()), is(equalTo(jim)));
		assertThat(map.get(john.getName()), is(equalTo(john)));
		assertThat(map.get(felix.getName()), is(equalTo(felix)));
		assertThat(map.get(polly.getName()), is(equalTo(polly)));
	}

	@Test
	public void testIntSerialization() throws Exception {
		json = marshaller.marshal(testInt);
		assertEquals(jsonForTestInt, json);
	}

	@Test
	public void testIntDeserialization() throws Exception {
		Object result = marshaller.unmarshal(jsonForTestInt, Object.class);
		assertThat(result, is(equalTo(testInt)));
	}

	@Test
	public void testLongSerialization() throws Exception {
		json = marshaller.marshal(testLong);
		assertEquals(jsonForTestLong, json);
	}

	@Test
	public void testLongDeserialization() throws Exception {
		Object result = marshaller.unmarshal(jsonForTestLong, Object.class);
		assertThat(result, is(equalTo(testLong)));
	}

	@Test
	public void testStringSerialization() throws Exception {
		json = marshaller.marshal(testString);
		assertEquals(jsonForTestString, json);
	}

	@Test
	public void testStringDeserialization() throws Exception {
		Object result = marshaller.unmarshal(jsonForTestString, Object.class);
		assertEquals(testString, result);
	}
}