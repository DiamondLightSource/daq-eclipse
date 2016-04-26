package org.eclipse.scanning.event.queues.processors;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.ServiceHolder;
import org.eclipse.scanning.event.queues.beans.ScanAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScanAtomProcessor takes the fields of a {@link ScanAtom} and from them makes
 * a {@link ScanBean}, which is then submitted to the scan event service.
 * 
 * The processor uses a {@link QueueListener} to monitor the process of the 
 * scan and pass up messages to the rest of the queue.
 * 
 * TODO Add test of wrong bean type before cast.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Bean implementing {@link Queueable}, but must be a 
 *            {@link ScanAtom}.
 */
public class ScanAtomProcessor implements IQueueProcessor {

	private static Logger logger = LoggerFactory.getLogger(ScanAtomProcess.class);

	@Override
	public <T extends Queueable> IConsumerProcess<T> makeProcess(T bean,
			IPublisher<T> publisher, boolean blocking) throws EventException {
		return new ScanAtomProcess<T>(bean, publisher, blocking);
	}


	class ScanAtomProcess <T extends Queueable> extends AbstractQueueProcessor<T> {

		private final String submitQueueName;
		private final String statusQueueName;
		private final String statusTopicName;
		private final URI uri;
		private final IEventService eventService;
		private ISubmitter<ScanBean> scanSubmitter;
		private ISubscriber<IBeanListener<ScanBean>> scanSubscriber;

		private ScanAtom atom;

		public ScanAtomProcess(T bean, IPublisher<T> publisher, boolean blocking) throws EventException {
			super(bean, publisher);
			this.blocking = blocking;

			//We know the bean is of type MoveAtom as this processor wouldn't get
			//called otherwise
			atom = (ScanAtom) bean;

			submitQueueName = atom.getScanSubmitQueueName();
			statusQueueName = atom.getScanStatusQueueName();
			statusTopicName = atom.getScanStatusTopicName();
			try {
				uri = new URI(atom.getScanConsumerURI());
			} catch (URISyntaxException e) {
				logger.error("Failed to set URI of scan service consumer.");
				bean.setMessage("Failed to set URI of scan service consumer: "+e.getMessage());
				broadcast(bean, Status.FAILED);
				throw new EventException(e);
			}
			eventService = ServiceHolder.getEventService();
		}

		@Override
		public void execute() throws EventException {
			//This is the percentage of the ScanAtom given over to config. 
			final double beanConfigPercent = 5d;

			broadcast(bean, Status.RUNNING);

			//Create a scan request from the configuration in the ScanAtom
			bean.setMessage("Creating scan request from configured values");
			broadcast(bean, Status.RUNNING);
			ScanRequest<?> scanReq = new ScanRequest<>();
			scanReq.setModels(atom.getPathModels());
			scanReq.setDetectors(atom.getDetectorModels());
			scanReq.setMonitorNames(atom.getMonitors());
			broadcast(bean, beanConfigPercent/4);

			//Make ScanBean from request ScanAtom
			ScanBean scan = new ScanBean();
			String scanUID = scan.getUniqueId()!=null ? scan.getUniqueId() : UUID.randomUUID().toString();
			scan.setBeamline(atom.getBeamline());
			scan.setName(atom.getName());
			scan.setHostName(atom.getHostName());
			scan.setUserName(atom.getUserName());
			scan.setScanRequest(scanReq);

			//Create scan subscriber & submitter
			bean.setMessage("Creating Event Service submitter.");
			broadcast(bean, Status.RUNNING, bean.getPercentComplete()+(beanConfigPercent/4));
			createScanSubscriber(scanUID, beanConfigPercent);
			try {
				scanSubmitter = eventService.createSubmitter(uri, submitQueueName);
				scan.setStatus(Status.SUBMITTED);
				scanSubmitter.submit(scan);
				scanSubmitter.disconnect();
			} catch(EventException e) {
				logger.error("Failed to submit ScanBean to Scan event service.");
				bean.setMessage("Failed to submit ScanBean to Scan event service: "+e.getMessage());
				broadcast(bean, Status.FAILED);
				throw new EventException(e);
			}
			bean.setMessage("Waiting for scan to complete");
			broadcast(bean, bean.getPercentComplete()+(beanConfigPercent/2));

			//Wait while the scan runs in a separate process
			while (!runComplete) {
				try {
					Thread.sleep(loopSleepTime);
				} catch(InterruptedException e) {
					throw new EventException(e);
				}

				if (runComplete) {
					scanSubscriber.disconnect();
					break;
				}

				if(terminated) {
					//Terminate the child scan
					scan.setStatus(Status.REQUEST_TERMINATE);
					IPublisher<ScanBean> scanTerminator = eventService.createPublisher(uri, statusTopicName);
					scanTerminator.broadcast(scan);
					scanTerminator.disconnect();

					//Set bean status and exit the loop
					bean.setStatus(Status.TERMINATED);
					publisher.broadcast(bean);
					break;
				}
			}

		}

		@Override
		public void terminate() throws EventException {
			terminated = true;
		}
		/**
		 * Creates {@link ISubscriber} to listen for percent complete & status 
		 * changes to beans.
		 *  
		 * @throws EventException If listener cannot be added.
		 */
		private void createScanSubscriber(String beanID, double configPercent) throws EventException {
			if(beanID == null) {
				logger.error("ScanBean ID not set. Cannot follow processing.");
				broadcast(bean, Status.FAILED);
				throw new EventException("ScanBean ID not set");
			}

			scanSubscriber = eventService.createSubscriber(uri, statusTopicName);
			scanSubscriber.addListener(new QueueListener<ScanBean, T>(bean, this, beanID, configPercent));
		}

		public String getSubmitQueueName() {
			return submitQueueName;
		}

		public String getStatusQueueName() {
			return statusQueueName;
		}

		public String getStatusTopicName() {
			return statusTopicName;
		}

		public URI getUri() {
			return uri;
		}

	}

}

