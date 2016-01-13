package uk.ac.diamond.daq.activemq.connector;

import org.junit.Before;

public class JSONMarshallerWithoutOSGiTest extends JSONMarshallerTestBase {

	@Before
	public void setUp() throws Exception {
		jsonForJim = "{\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Person\",\n  \"name\" : \"Jim\",\n  \"pet\" : {\n    \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n    \"name\" : \"Polly\",\n    \"feathers\" : \"Green\"\n  }\n}";
		jsonForJohn = "{\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Person\",\n  \"name\" : \"John\",\n  \"pet\" : {\n    \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n    \"name\" : \"Felix\",\n    \"whiskers\" : \"Luxuriant\"\n  }\n}";
		jsonForFelix = "{\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}";
		jsonForAnimalArray = "[ \"[Luk.ac.diamond.daq.activemq.connector.testobject.Animal;\", [ {\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}, {\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n  \"name\" : \"Polly\",\n  \"feathers\" : \"Green\"\n}, {\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n} ] ]";
		jsonForAnimalList = "[ \"java.util.ArrayList\", [ {\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}, {\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n  \"name\" : \"Polly\",\n  \"feathers\" : \"Green\"\n}, {\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n} ] ]";
		jsonForAnimalSet = "[ \"java.util.HashSet\", [ {\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}, {\n  \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n  \"name\" : \"Polly\",\n  \"feathers\" : \"Green\"\n} ] ]";
		jsonForAnimalMap = "{\n  \"@class\" : \"java.util.HashMap\",\n  \"Polly\" : {\n    \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n    \"name\" : \"Polly\",\n    \"feathers\" : \"Green\"\n  },\n  \"Felix\" : {\n    \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n    \"name\" : \"Felix\",\n    \"whiskers\" : \"Luxuriant\"\n  },\n  \"John\" : {\n    \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Person\",\n    \"name\" : \"John\",\n    \"pet\" : {\n      \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n      \"name\" : \"Felix\",\n      \"whiskers\" : \"Luxuriant\"\n    }\n  },\n  \"Jim\" : {\n    \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Person\",\n    \"name\" : \"Jim\",\n    \"pet\" : {\n      \"@class\" : \"uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n      \"name\" : \"Polly\",\n      \"feathers\" : \"Green\"\n    }\n  }\n}";

		marshaller = new ActivemqConnectorService();
	}
}