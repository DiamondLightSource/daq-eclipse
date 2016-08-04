package org.eclipse.scanning.test.event.queues.util;

import org.eclipse.scanning.api.event.queues.beans.IAtomQueue;
import org.eclipse.scanning.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.event.queues.beans.TaskBean;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;

/**
 * Factory class to generate classes implementing the {@link IAtomQueue} class.
 * Used within the testing framework.
 * 
 * @author Michael Wharmby
 *
 */
public class TestAtomQueueBeanMaker {
	
	/*
	 * List of Objects Available
	 * -------------------------
	 * DummySubTaskBeanA - SubTaskBean containing four DummyAtomBeans (ABCD)
	 * DummySubTaskBeanB - SubTaskBean containing two DummyAtomBeans (EF)
	 * DummySubTaskBeanC - SubTaskBean containing three DummyAtomBeans (HIJ)
	 * DummySubTaskBeanD - SubTaskBean containing five DummyAtomBeans (LMNOP)
	 * DummySubTaskBeanE - SubTaskBean containing two DummyAtomBeans (QR)
	 * 
	 * DummyTaskBeanA - TaskBean containing DummySTBA & DummySTBB
	 */
	
	public static SubTaskAtom makeDummySubTaskBeanA() {
		SubTaskAtom sta = new SubTaskAtom("Dummy SubTask A");
		sta.queue().add(new DummyAtom("Arnold", 10));
		sta.queue().add(new DummyAtom("Beatrice", 465));
		sta.queue().add(new DummyAtom("Carlos", 1245));
		sta.queue().add(new DummyAtom("Deirdre", 675));
		
		return sta;
	}
	
	public static SubTaskAtom makeDummySubTaskBeanB() {
		SubTaskAtom sta = new SubTaskAtom("Dummy SubTask B");
		sta.queue().add(new DummyAtom("Edmund", 10));
		sta.queue().add(new DummyAtom("Francis", 20));
		return sta;
	}
	
	public static SubTaskAtom makeDummySubTaskBeanC() {
		SubTaskAtom sta = new SubTaskAtom("Dummy SubTask C");
		sta.queue().add(new DummyAtom("Heinrich", 479));
		sta.queue().add(new DummyAtom("Imhotep", 236));
		sta.queue().add(new DummyAtom("Jane", 387));
		return sta;
	}
	
	public static SubTaskAtom makeDummySubTaskBeanD() {
		SubTaskAtom sta = new SubTaskAtom("Dummy SubTask D");
		sta.queue().add(new DummyAtom("Linda", 7444));
		sta.queue().add(new DummyAtom("Manuel", 125));
		sta.queue().add(new DummyAtom("Nicola", 4756));
		sta.queue().add(new DummyAtom("Olive", 5887));
		sta.queue().add(new DummyAtom("Pablo", 58));
		return sta;
	}
	
	public static SubTaskAtom makeDummySubTaskBeanE() {
		SubTaskAtom sta = new SubTaskAtom("Dummy SubTask E");
		sta.queue().add(new DummyAtom("Quentin", 986));
		sta.queue().add(new DummyAtom("Reginald", 5886));
		return sta;
	}
	
	public static TaskBean makeDummyTaskBeanA() {
		TaskBean tb = new TaskBean("Dummy Task A");
		tb.queue().add(makeDummySubTaskBeanA());
		tb.queue().add(makeDummySubTaskBeanB());
		return tb;
	}

}
