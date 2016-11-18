package org.eclipse.scanning.test.event.queues;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	_QueueControllerServiceTest.class,
	QueueControllerServiceTest.class,
	QueueProcessCreatorTest.class,
	QueueProcessorFactoryTest.class,
	QueueProcessTest.class,
	QueueResponseProcessTest.class,
	QueueServiceTest.class,
	QueueTest.class
})
public class Suite {

}
