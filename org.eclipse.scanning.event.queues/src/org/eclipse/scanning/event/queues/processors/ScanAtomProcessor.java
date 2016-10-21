package org.eclipse.scanning.event.queues.processors;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.event.queues.ServicesHolder;
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
 * @author Michael Wharmby
 * 
 */
public class ScanAtomProcessor extends AbstractQueueProcessor<ScanAtom> {
	
	private static Logger logger = LoggerFactory.getLogger(ScanAtomProcessor.class);
	
	//Scanning infrastructure
	private final IEventService eventService;
	private IPublisher<ScanBean> scanPublisher;
	private ISubmitter<ScanBean> scanSubmitter;
	private ISubscriber<QueueListener<ScanAtom, ScanBean>> scanSubscriber;
	private QueueListener<ScanAtom, ScanBean> queueListener;
	
	//For processor operation
	private final ScanBean scanBean;
	
	/**
	 * Create a ScanAtomProcessor which can be used by a {@link QueueProcess}. 
	 * Constructor configures the {@link IEventService} using the instance 
	 * specified in the {@link ServicesHolder}. Additionally, a new 
	 * {@link ScanBean} is created which will be configured with the details 
	 * of from the {@link ScanAtom}.
	 */
	public ScanAtomProcessor() {
		eventService = ServicesHolder.getEventService();
		scanBean = new ScanBean();
		scanBean.setStatus(Status.NONE);
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		setExecuted();
		if (!(queueBean.equals(broadcaster.getBean()))) throw new EventException("Beans on broadcaster and processor differ");
		
		//Percentage alloted to scan configuration
		final Double configPercent = 5d;
		
		//Get scanning service configuration
		broadcaster.broadcast(Status.RUNNING, "Reading scanning service configuration.");
		final URI scanBrokerURI = getScanBrokerURI();
		final String scanSubmitQueueName = getScanSubmitQueue();
		final String scanStatusTopicName = getScanStatusTopic();

		//Create the ScanRequest
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.15,
				"Creating scan request from configured values.");
		ScanRequest<?> scanReq = new ScanRequest<>();
		scanReq.setCompoundModel(new CompoundModel(queueBean.getPathModels()));
		scanReq.setDetectors(queueBean.getDetectorModels());
		scanReq.setMonitorNames(queueBean.getMonitors());

		//Configure the ScanBean & set the ScanRequest
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.35);
		if (scanBean.getUniqueId() == null) scanBean.setUniqueId(UUID.randomUUID().toString());
		scanBean.setBeamline(queueBean.getBeamline());
		scanBean.setName(queueBean.getName());
		scanBean.setHostName(queueBean.getHostName());
		scanBean.setUserName(queueBean.getUserName());
		scanBean.setScanRequest(scanReq);
		
		//Create the scanning service infrastructure & submit ScanBean
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.35,
				"Submitting bean to scanning service.");
		scanPublisher = eventService.createPublisher(scanBrokerURI, scanStatusTopicName);
		scanSubscriber = eventService.createSubscriber(scanBrokerURI, scanStatusTopicName);
		queueListener = new QueueListener<>(broadcaster, queueBean, processorLatch, scanBean);
		try {
			scanSubscriber.addListener(queueListener);
		} catch (EventException evEx) {
			broadcaster.broadcast(Status.FAILED, "Failed to add QueueListener to scan subscriber; unable to monitor queue. Cannot continue: \""+evEx.getMessage()+"\".");
			logger.error("Failed to add QueueListener to scan subscriber for '"+queueBean.getName()+"'; unable to monitor queue. Cannot continue: \""+evEx.getMessage()+"\".");
			throw new EventException("Failed to add QueueListener to scan subscriber", evEx);
		}
		scanSubmitter = eventService.createSubmitter(scanBrokerURI, scanSubmitQueueName);
		scanBean.setStatus(Status.SUBMITTED);
		try {
			scanSubmitter.submit(scanBean);
			scanSubmitter.disconnect();
		} catch (EventException evEx) {
			commandScanBean(Status.REQUEST_TERMINATE); //Just in case the submission worked, but the disconnect didn't, stop the runnning process
			broadcaster.broadcast(Status.FAILED, "Failed to submit scan bean to scanning system: \""+evEx.getMessage()+"\".");
			logger.error("Failed to submit scan bean '"+scanBean.getName()+"' to scanning system: \""+evEx.getMessage()+"\".");
			throw new EventException("Failed to submit scan bean to scanning system", evEx);
		}
		
		//Allow scan to run
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.15,
				"Waiting for scan to complete...");
		processorLatch.await();

		//Post-match analysis
		if (isTerminated()) {
			if (queueListener.isChildCommand()) {
				broadcaster.broadcast("Scan aborted before completion (requested).");
			} else {
				broadcaster.broadcast("Scan aborted from scanning service.");
				commandScanBean(Status.REQUEST_TERMINATE);
			}
			tidyScanActors();
			return;
		} else if (queueBean.getPercentComplete() >= 99.5) {
			//Completed successfully
			broadcaster.broadcast(Status.COMPLETE, 100d, "Scan completed.");
		} else {
			//Scan failed - don't set anything here as messages should have 
			//been updated elsewhere
			broadcaster.broadcast(Status.FAILED);
		}
		
		////This should be run after we've reported the queue final state
		tidyScanActors();
	}

	@Override
	public void pause() throws EventException {
		if (!queueListener.isChildCommand()) {
			commandScanBean(Status.REQUEST_PAUSE);
		}
		//TODO More?
	}

	@Override
	public void resume() throws EventException {
		if (!queueListener.isChildCommand()) {
			commandScanBean(Status.REQUEST_RESUME);
		}
		//TODO More?
	}

	@Override
	public void terminate() throws EventException {
		setTerminated();
		processorLatch.countDown();
	}

	@Override
	public Class<ScanAtom> getBeanClass() {
		return ScanAtom.class;
	}
	
	/**
	 * Send instructions to the child ScanBean.
	 * 
	 * @param command the new State of the ScanBean.
	 * @throws EventException In case broadcasting fails.
	 */
	private void commandScanBean(Status command) throws EventException {
		if (scanPublisher == null) {
			broadcaster.broadcast(Status.FAILED, "Scan Publisher not initialised. Cannot send commands to scanning system");
			logger.error("Scan publisher not initialised. Cannot send commands to scanning system for '"+queueBean.getName()+"'.");
			throw new EventException("Scan publisher not initialised. Cannot send commands to scanning system");
		}
		if (!command.isRequest()) {
			logger.warn("Command \""+command+"\" to ScanBean '"+scanBean.getName()+"' is not a request. Unexpected behaviour may result.");
		}
		scanBean.setStatus(command);
		scanPublisher.broadcast(scanBean);
	}
	
	/**
	 * Clean up EventService objects which interact with the scan child queue.
	 * @throws EventException
	 */
	private void tidyScanActors() throws EventException {
		scanPublisher.disconnect();
		scanSubscriber.disconnect();
	}
	
	/**
	 * Get the URI of the broker from Spring properties or from other objects.
	 * 
	 * @return String URI for the scan broker.
	 * @throws EventException If no URI found in config or URI syntax is wrong.
	 */
	private URI getScanBrokerURI() throws EventException {
		String uri = queueBean.getScanBrokerURI();
		if (uri == null) uri = System.getProperty("org.eclipse.scanning.broker.uri");
		if (uri == null) uri = System.getProperty("gda.activemq.broker.uri");
		if (uri == null) uri = System.getProperty("org.eclipse.scanning.queueservice.broker.uri");
		if (uri == null) uri = ServicesHolder.getQueueService().getURIString();
		
		try {
			if (uri == null) {
				broadcaster.broadcast(Status.FAILED, "Failed to set broker URI: \"No URI found in config.\"");
				logger.error("Failed to set scanning service broker URI for '"+queueBean.getName()+"': \"No URI found in config.\"");
				throw new EventException("No URI found in config.");
			}
			return new URI(uri);	
		} catch (URISyntaxException usEx) {
			broadcaster.broadcast(Status.FAILED, "Failed to set broker URI: \""+usEx.getMessage()+"\" (Reason: \""+usEx.getReason()+"\").");
			logger.error("Failed to set scanning service broker URI for '"+queueBean.getName()+"': \""+usEx.getMessage()+"\" (Reason: \""+usEx.getReason()+"\").");
			throw new EventException("Failed to set broker URI", usEx);
		}
	}
	
	/**
	 * Get the name of the submission queue from Spring properties or from 
	 * other objects.
	 * 
	 * @return String name of submission queue.
	 * @throws EventException If no queue name found in config.
	 */
	private String getScanSubmitQueue() throws EventException {
		String submitQueue = queueBean.getScanSubmitQueueName();
		//TODO Add scanning centric properties
		if (submitQueue == null) submitQueue = System.getProperty("org.eclipse.scanning.queueservice.scansubmit.queue");
		if (submitQueue == null) submitQueue = IEventService.SUBMISSION_QUEUE;
		
		if (submitQueue == null) {
			broadcaster.broadcast(Status.FAILED, "Failed to set scan submission queue name: \"No value found in config.\"");
			logger.error("Failed to set scan submission queue name for '"+queueBean.getName()+"': \"No value found in config.\"");
			throw new EventException("No submission queue name found in config.");
		}
		return submitQueue;
	}
	
	/**
	 * Get name of the status set from Spring properties or from other objects.
	 * 
	 * @return String name of status set
	 * @throws EventException If no status set name found in config.
	 */
	private String getScanStatusTopic() throws EventException {
		String statusTopic = queueBean.getScanStatusTopicName();
		//TODO Add scanning centric properties
		if (statusTopic == null) statusTopic = System.getProperty("org.eclipse.scanning.queueservice.scanstatus.topic");
		if (statusTopic == null) statusTopic = IEventService.STATUS_TOPIC;
		
		if (statusTopic == null) {
			broadcaster.broadcast(Status.FAILED, "Failed to set scan status topic name: \"No value found in config.\"");
			logger.error("Failed to set scan status topic name for '"+queueBean.getName()+"': \"No value found in config.\"");
			throw new EventException("No status topic name found in config.");
		}
		return statusTopic;
	}

}
