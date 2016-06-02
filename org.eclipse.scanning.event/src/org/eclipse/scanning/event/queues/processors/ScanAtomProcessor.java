package org.eclipse.scanning.event.queues.processors;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
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
	private ISubscriber<QueueListener> scanSubscriber; //TODO
	
	//Processor operation
	private CountDownLatch scanLatch = new CountDownLatch(1);
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
				scanBrokerURI = new URI(queueBean.getScanBrokerURI());
			} catch (URISyntaxException usEx) {
				broadcaster.broadcast(Status.FAILED, "Failed to set broker URI: \""+usEx.getMessage()+"\" (Reason: \""+usEx.getReason()+"\").");
				logger.error("Failed to set scanning service broker URI for '"+queueBean.getName()+"': \""+usEx.getMessage()+"\" (Reason: \""+usEx.getReason()+"\").");
				throw new EventException("Failed to set broker URI", usEx);
			}

		} else {
			scanBrokerURI = QueueServicesHolder.getQueueService().getURI();
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
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.1,
				"Creating scan request from configured values.");
		ScanRequest<?> scanReq = new ScanRequest<>();
		scanReq.setModels(queueBean.getPathModels());
		scanReq.setDetectors(queueBean.getDetectorModels());
		scanReq.setMonitorNames(queueBean.getMonitors());

		//Configure the ScanBean & set the ScanRequest
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.3);
		if (scanBean.getUniqueId() == null) scanBean.setUniqueId(UUID.randomUUID().toString());
		String scanUID = scanBean.getUniqueId();
		scanBean.setBeamline(queueBean.getBeamline());
		scanBean.setName(queueBean.getName());
		scanBean.setHostName(queueBean.getHostName());
		scanBean.setUserName(queueBean.getUserName());
		scanBean.setScanRequest(scanReq);
		
		//Create the scanning service infrastructure & submit ScanBean
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.3,
				"Submitting bean to scanning service.");
		scanPublisher = eventService.createPublisher(scanBrokerURI, scanStatusTopicName);
		scanSubscriber = eventService.createSubscriber(scanBrokerURI, scanStatusTopicName);
//		try {
//		scanSubscriber.addListener(new QueueListener<ScanBean, T>(bean, this, beanID, configPercent)); //TODO
//		} catch (EventException evEx) {
//			broadcaster.broadcast(Status.FAILED, "Failed to add QueueListener to scan subscriber; unable to monitor queue. Cannot continue: \""+evEx.getMessage()+"\".");
//			logger.error("Failed to add QueueListener to scan subscriber for '"+queueBean.getName()+"'; unable to monitor queue. Cannot continue: \""+evEx.getMessage()+"\".");
//			throw new EventException("Failed to add QueueListener to scan subscriber", evEx);
//		}
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
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.1,
				"Waiting for scan to complete.");
		scanLatch.await();

		//Post-match analysis
		if (isTerminated()) {
			commandScanBean(Status.REQUEST_TERMINATE);
			//TODO Await confirmation of termination from QueueListener?
			return;
		}

		//Clean finish
		if (isComplete()) {
			broadcaster.broadcast(Status.COMPLETE, 100d, "Scan completed.");
		} else {
			broadcaster.broadcast(Status.FAILED, "Scan ended unexpectedly.");
			logger.warn("Processing of ScanAtom '"+queueBean.getName()+"' ended unexpectedly.");
		}

	}

	@Override
	public void pause() throws EventException {
		commandScanBean(Status.REQUEST_PAUSE);
		//TODO More?
	}

	@Override
	public void resume() throws EventException {
		commandScanBean(Status.REQUEST_RESUME);
		//TODO More?
	}

	@Override
	public void terminate() throws EventException {
		setTerminated();
		scanLatch.countDown();
	}

	@Override
	public Class<ScanAtom> getBeanClass() {
		return ScanAtom.class;
	}
	
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

}
