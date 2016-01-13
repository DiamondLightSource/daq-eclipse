package uk.ac.diamond.daq.activemq.connector;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import uk.ac.diamond.daq.activemq.connector.internal.MockClassLoaderAnswer;
import uk.ac.diamond.daq.activemq.connector.internal.TestBundleProvider;
import uk.ac.diamond.daq.activemq.connector.testobject.Animal;
import uk.ac.diamond.daq.activemq.connector.testobject.Bird;
import uk.ac.diamond.daq.activemq.connector.testobject.Cat;
import uk.ac.diamond.daq.activemq.connector.testobject.Person;

public class JSONMarshallerOSGiSimulationTest extends JSONMarshallerTestBase {

	@Mock private Bundle exampleBundleV1;
	@Mock private Bundle exampleBundleV2;
	@Mock private Bundle otherExampleBundle;
	@Mock private BundleContext bundleContext;

	@Before
	public void setUp() throws Exception {
		jsonForJim = "{\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=1.2.0.test&class=uk.ac.diamond.daq.activemq.connector.testobject.Person\",\n  \"name\" : \"Jim\",\n  \"pet\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n    \"name\" : \"Polly\",\n    \"feathers\" : \"Green\"\n  }\n}";
		jsonForJohn = "{\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=1.2.0.test&class=uk.ac.diamond.daq.activemq.connector.testobject.Person\",\n  \"name\" : \"John\",\n  \"pet\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n    \"name\" : \"Felix\",\n    \"whiskers\" : \"Luxuriant\"\n  }\n}";
		jsonForFelix = "{\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}";
		jsonForAnimalArray = "[ \"bundle=&version=&class=[Luk.ac.diamond.daq.activemq.connector.testobject.Animal;\", [ {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}, {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n  \"name\" : \"Polly\",\n  \"feathers\" : \"Green\"\n}, {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n} ] ]";
		jsonForAnimalList = "[ \"bundle=&version=&class=java.util.ArrayList\", [ {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}, {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n  \"name\" : \"Polly\",\n  \"feathers\" : \"Green\"\n}, {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n} ] ]";
		jsonForAnimalSet = "[ \"bundle=&version=&class=java.util.HashSet\", [ {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n  \"name\" : \"Felix\",\n  \"whiskers\" : \"Luxuriant\"\n}, {\n  \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n  \"name\" : \"Polly\",\n  \"feathers\" : \"Green\"\n} ] ]";
		jsonForAnimalMap = "{\n  \"@bundle_and_class\" : \"bundle=&version=&class=java.util.HashMap\",\n  \"Polly\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n    \"name\" : \"Polly\",\n    \"feathers\" : \"Green\"\n  },\n  \"Felix\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n    \"name\" : \"Felix\",\n    \"whiskers\" : \"Luxuriant\"\n  },\n  \"John\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=1.2.0.test&class=uk.ac.diamond.daq.activemq.connector.testobject.Person\",\n    \"name\" : \"John\",\n    \"pet\" : {\n      \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.other_example&version=0.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Cat\",\n      \"name\" : \"Felix\",\n      \"whiskers\" : \"Luxuriant\"\n    }\n  },\n  \"Jim\" : {\n    \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=1.2.0.test&class=uk.ac.diamond.daq.activemq.connector.testobject.Person\",\n    \"name\" : \"Jim\",\n    \"pet\" : {\n      \"@bundle_and_class\" : \"bundle=uk.ac.diamond.daq.test.example&version=2.0.0&class=uk.ac.diamond.daq.activemq.connector.testobject.Bird\",\n      \"name\" : \"Polly\",\n      \"feathers\" : \"Green\"\n    }\n  }\n}";

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
}