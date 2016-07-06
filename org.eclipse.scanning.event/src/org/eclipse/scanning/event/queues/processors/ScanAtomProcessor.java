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
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.ScanAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanAtomProcessor extends AbstractQueueProcessor<ScanAtom> {
	
	private static Logger logger = LoggerFactory.getLogger(ScanAtomProcessor.class);
	
	//Scanning infrastructure
	private final IEventService eventService;
	private IPublisher<ScanBean> scanPublisher;
	private ISubmitter<ScanBean> scanSubmitter;
	private ISubscriber<QueueListener<ScanAtom, ScanBean>> scanSubscriber;
	private QueueListener<ScanAtom, ScanBean> queueListener;
	
	//Processor operation
	private final ScanBean scanBean;
	
	public ScanAtomProcessor() {
		eventService = QueueServicesHolder.getEventService();
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
		final URI scanBrokerURI;
		final String scanSubmitQueueName, scanStatusTopicName; 
		if (queueBean.getScanBrokerURI() != null) {
			try {
				scanBrokerURI = new URI(queueBean.getScanBrokerURI()); //FIXME This is idiotic
			} catch (URISyntaxException usEx) {
				broadcaster.broadcast(Status.FAILED, "Failed to set broker URI: \""+usEx.getMessage()+"\" (Reason: \""+usEx.getReason()+"\").");
				logger.error("Failed to set scanning service broker URI for '"+queueBean.getName()+"': \""+usEx.getMessage()+"\" (Reason: \""+usEx.getReason()+"\").");
				throw new EventException("Failed to set broker URI", usEx);
			}
		} else {
			scanBrokerURI = QueueServicesHolder.getQueueService().getURI(); //TODO This should point at Matt G's config the EventService
		}
		if (queueBean.getScanSubmitQueueName() != null) {
			scanSubmitQueueName = queueBean.getScanSubmitQueueName();
		} else {
			scanSubmitQueueName = IEventService.SUBMISSION_QUEUE;
		}
		if (queueBean.getScanStatusTopicName() != null) {
			scanStatusTopicName = queueBean.getScanStatusTopicName();
		} else {
			scanStatusTopicName = IEventService.STATUS_TOPIC;
		}

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
		//configPercent+0.5 so the QueueListener won't set parent 100% complete
		queueListener = new QueueListener<>(broadcaster, queueBean, configPercent+0.5, processorLatch, scanBean);
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
				"Waiting for scan to complete.");
		processorLatch.await();

		//Post-match analysis
		if (isTerminated()) {
			if (!queueListener.isChildCommand()) {
				commandScanBean(Status.REQUEST_TERMINATE);
			}
			tidyScanActors();
			return;
		}
		
		if (queueBean.getPercentComplete() >= 99.5) {
			//Completed successfully
			broadcaster.broadcast(Status.COMPLETE, 100d, "Scan completed.");
		} else {
			//Scan failed - don't set anything here as messages should have 
			//been updated elsewhere
			broadcaster.broadcast(Status.FAILED);
		}
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

}
