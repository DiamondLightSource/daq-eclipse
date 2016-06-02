package org.eclipse.scanning.event.queues.processors;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.ScanAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanAtomProcessor extends AbstractQueueProcessor<ScanAtom> {
	
	private static Logger logger = LoggerFactory.getLogger(ScanAtomProcessor.class);
	
	//Scanning infrastructure
	private final IEventService eventService;
	private ISubmitter<ScanBean> scanSubmitter;
	
	//Processor operation
	private CountDownLatch scanLatch = new CountDownLatch(1);
	private final ScanBean scanBean;
	
	public ScanAtomProcessor() {
		eventService = QueueServicesHolder.getEventService();
		scanBean = new ScanBean();
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		setExecuted();
		if (!(queueBean.equals(broadcaster.getBean()))) throw new EventException("Beans on broadcaster and processor differ");
		
		//Percentage alloted to scan configuration
		final Double configPercent = 5d;
		
		//Get scanning service configuration
		broadcaster.broadcast(Status.RUNNING, "Reading scanning service configuration");
		final URI scanBrokerURI;
		final String scanSubmitQueueName, scanStatusTopicName; 
		if (queueBean.getScanBrokerURI() != null) {
			try {
				scanBrokerURI = new URI(queueBean.getScanBrokerURI());
			} catch (URISyntaxException usEx) {
				broadcaster.broadcast(Status.FAILED, "Failed to set broker URI: "+usEx.getMessage()+" - "+usEx.getReason());
				logger.error("Failed to set scanning service broker URI for "+queueBean.getName()+": "+usEx.getMessage()+" - "+usEx.getReason());
				throw new EventException("Failed to set broker URI: "+usEx.getMessage()+" - "+usEx.getReason());
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
				"Creating scan request from configured values");

		//Configure the ScanBean & set the ScanRequest
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.3);
		
		//Create the scanning service infrastructure & submit ScanBean
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.3,
				"Submitting bean to scanning service");
		
		//Allow scan to run
		broadcaster.broadcast(Status.RUNNING, queueBean.getPercentComplete()+configPercent*0.1,
				"Waiting for scan to complete");
		

	}

	@Override
	public void pause() throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() throws EventException {
		// TODO Auto-generated method stub

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

}
