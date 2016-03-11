package org.eclipse.scanning.test.event;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	SerializationTest.class,
	ScanEventTest.class,
	ConsumerTest.class,
	MappingScanTest.class,
	AnyBeanEventTest.class,
	HeartbeatTest.class,
	MConsumerTest.class
})
public class Suite {

	
}
