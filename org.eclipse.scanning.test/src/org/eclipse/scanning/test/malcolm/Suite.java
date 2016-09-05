package org.eclipse.scanning.test.malcolm;
import org.eclipse.scanning.test.malcolm.mock.MockAbortingMalcolmTest;
import org.eclipse.scanning.test.malcolm.mock.MockCommunicationMalcolmTest;
import org.eclipse.scanning.test.malcolm.mock.MockMultipleClientTest;
import org.eclipse.scanning.test.malcolm.mock.MockPausingMalcolmTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	MockAbortingMalcolmTest.class,
	MockCommunicationMalcolmTest.class,
	MockMultipleClientTest.class,
	MockPausingMalcolmTest.class
})
public class Suite {

}
