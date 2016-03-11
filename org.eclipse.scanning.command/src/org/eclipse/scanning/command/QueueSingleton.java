package org.eclipse.scanning.command;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;


// The idea of this singleton is that Python can import it and thereby talk to
// Java. This is in contrast to the notion of Java passing a queue to Python.
// It seems like both approaches have downsides.
public class QueueSingleton {
	public static final BlockingQueue<ScanRequest<IROI>> INSTANCE =
			new SynchronousQueue<ScanRequest<IROI>>();

	private QueueSingleton() {}
}
