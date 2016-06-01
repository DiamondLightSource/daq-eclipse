package org.eclipse.scanning.test.event;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	SerializationTest.class,
	ScanEventTest.class,
	// ConsumerTest.class,
	PauseTest.class,
	MappingScanTest.class,
	AnyBeanEventTest.class,
	HeartbeatTest.class,
	RequesterTest.class
	// MConsumerTest.class  Takes too long! TODO Make shorter
})
public class Suite {

	
}
