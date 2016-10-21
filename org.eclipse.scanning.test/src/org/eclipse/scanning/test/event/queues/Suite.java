package org.eclipse.scanning.test.event.queues;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	QueueControllerServiceTest.class,
	QueueProcessCreatorTest.class,
	QueueProcessorFactoryTest.class,
	QueueProcessTest.class,
	QueueServiceTest.class,
	QueueTest.class
})
public class Suite {

}
