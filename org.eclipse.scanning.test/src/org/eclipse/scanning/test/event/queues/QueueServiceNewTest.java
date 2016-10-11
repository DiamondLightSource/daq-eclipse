package org.eclipse.scanning.test.event.queues;

public class QueueServiceNewTest {
	
	/**
	 * Test initialisation & starting of the service
	 */
	public void testServiceInit() {
		/*
		 * Should:
		 * - check queueRoot & URI are set
		 * - should set heartbeatTopicName, commandSetName, commandTopicName
		 * - create job-queue
		 * 
		 * - start job-queue
		 * - mark service active
		 */
	}
	
	/**
	 * Test clean-up of service
	 */
	public void testServiceDisposal() {
		/*
		 * Should:
		 * - call stop
		 * - dispose job-queue
		 */
	}
	
	/**
	 * Test starting & stopping of service
	 */
	public void testServiceStop() {
		/*
		 * Should:
		 * - stop active-queue(s)
		 * - deregister active-queue(s)
		 * - stop job-queue
		 * - mark service inactive
		 */
	}
	
	/**
	 * Test starting & stopping of a queue
	 */
	public void testQueueStartStop() {
		/*
		 * Should:
		 * - start queue (not possible without queue service start)
		 * - stop queue nicely
		 * - start queue
		 * - stop queue forcefully
		 */
	}
	
	/**
	 * Test registration & deregistration of active-queues
	 */
	public void testRegistration() {
		/*
		 * Should:
		 * - register active-queue (not possible without queue service start)
		 * - register 5 active-queues (test names all different)
		 * - start 1 queue, deregister all
		 * - force deregister remaining queue
		 */
	}

}
