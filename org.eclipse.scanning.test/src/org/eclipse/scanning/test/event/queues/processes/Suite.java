package org.eclipse.scanning.test.event.queues.processes;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	MonitorAtomProcessTest.class,
	MoveAtomProcessTest.class,
	QueueListenerTest.class,
	ScanAtomProcessTest.class,
	SubTaskAtomProcessTest.class,
	TaskBeanProcessTest.class
})
public class Suite {

}
