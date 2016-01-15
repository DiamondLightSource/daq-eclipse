package uk.ac.diamond.daq.activemq.connector;

import static org.junit.Assert.*;
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

import uk.ac.diamond.daq.activemq.connector.testobject.Animal;
import uk.ac.diamond.daq.activemq.connector.testobject.Bird;
import uk.ac.diamond.daq.activemq.connector.testobject.Cat;
import uk.ac.diamond.daq.activemq.connector.testobject.Person;

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
public abstract class JSONMarshallerTestBase {

	protected String jsonForJim;
	protected String jsonForJohn;
	protected String jsonForFelix;
	protected String jsonForAnimalArray;
	protected String jsonForAnimalList;
	protected String jsonForAnimalSet;
	protected String jsonForAnimalMap;
	protected String testString = "Hello world!";
	protected String jsonForTestString = "\"Hello world!\"";
	protected int testInt = -56;
	protected String jsonForTestInt = "-56";
	protected long testLong = 1234567890L;
	protected String jsonForTestLong = "1234567890";

	protected ActivemqConnectorService marshaller;

	private String json;

	// Test objects
	private Bird polly;
	private Cat felix;
	private Person jim;
	private Person john;

	@Before
	public void createTestObjects() throws Exception {

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
			System.out.println("JSON: " + json);

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