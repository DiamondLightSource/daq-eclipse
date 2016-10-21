package org.eclipse.scanning.test.event.queues.processors;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	MoveAtomProcessorTest.class,
	QueueListenerTest.class,
	ScanAtomProcessorTest.class,
	SubTaskAtomProcessorTest.class,
	TaskBeanProcessorTest.class
})
public class Suite {

}
