package org.eclipse.scanning.test.event.queues.beans.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.event.queues.beans.MonitorAtom;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.eclipse.scanning.event.queues.beans.ScanAtom;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;

/**
 * Factory class to generate QueueAtoms which can be acted in active-queues.
 * Used within the testing framework.
 * 
 * @author Michael Wharmby
 *
 */
public class TestAtomMaker {
	
	/*
	 * List of Objects Available
	 * -------------------------
	 *
	 * MoveAtomA - move sample out of beam
	 * MoveAtomB - move sample in to beam
	 * MoveAtomC - open fast shutter
	 * MoveAtomD - close fast shutter
	 * MoveAtomE - move to sample X position
	 * MoveAtomF - move to sample Y position
	 * 
	 * MonitorAtomA - read bpm3
	 * MonitorAtomB - read d2
	 * 
	 * ScanAtomA - SubAtomSetA (VT Scan)
	 * ScanAtomB - SubAtomSetB (m-Dac Scan)
	 */
	
	
	/*
	 * MoveAtoms
	 */
	public static MoveAtom makeTestMoveAtomA() {
		//move sample out of beam
		return new MoveAtom("Sample In MoveAtomA", "SampleY", 0., 1500);
	}
	
	public static MoveAtom makeTestMoveAtomB() {
		//move sample in to beam
		return new MoveAtom("Sample Out MoveAtomB", "SampleY", 1., 1500);
	}
	
	public static MoveAtom makeTestMoveAtomC() {
		//- open fast shutter
		return new MoveAtom("Open Fast Shutter MoveAtomC", "FastShutter", 1., 50);
	}
	
	public static MoveAtom makeTestMoveAtomD() {
		//- close fast shutter
		return new MoveAtom("Close Fast Shutter MoveAtomD", "FastShutter", 0., 50);
	}
	
	public static MoveAtom makeTestMoveAtomE() {
		//move to sample position in X
		return new MoveAtom("Move to sample X MoveAtomE", "SampleX", 10.53, 5020);
	}
	
	public static MoveAtom makeTestMoveAtomF() {
		//move to sample position in Y
		return new MoveAtom("Move to sample Y MoveAtomF", "SampleY", 7.85, 7061);
	}
	
	/*
	 * MonitorAtoms
	 */
	public static MonitorAtom makeTestMonitorAtomA() {
		//- read bpm3
		return new MonitorAtom("bpm3 MonitorAtomA", "bpm3", 10000);
	}
	
	public static MonitorAtom makeTestMonitorAtomB() {
		//- read d2
		return new MonitorAtom("d2 MonitorAtomB", "d2", 10000);
	}

	/*
	 * ScanAtoms
	 */
	public static ScanAtom makeTestScanAtomA() {
		List<IScanPathModel> scanAxes = new ArrayList<>();
		scanAxes.add(new StepModel("ocs", 290, 80, 10));
		scanAxes.add(new StepModel("xMotor", 150, 100, 5));
		
		Map<String, Object> detectors = new HashMap<>();
		detectors.put("pe", new MockDetectorModel(30d));
		
		List<String> monitors = new ArrayList<>();
		monitors.add("bpm3");
		monitors.add("i0");
		
		return new ScanAtom("TestScanA", scanAxes, detectors);
		
	}
	
	public static ScanAtom makeTestScanAtomB() {
		List<IScanPathModel> scanAxes = new ArrayList<>();
		scanAxes.add(new StepModel("mDac", 0, 50, 5));
		
		Map<String, Object> detectors = new HashMap<>();
		detectors.put("pe", new MockDetectorModel(30d));
		
		return new ScanAtom("TestScanB", scanAxes, detectors);
	}
	
	/*
	 * Lists of Atoms for building SubTaskBeans
	 */
	public static List<QueueAtom> makeTestAtomSetA() {
		//- position the sample
		List<QueueAtom> atomList = new ArrayList<>();
		atomList.add(makeTestMoveAtomE());
		atomList.add(makeTestMoveAtomF());
		return atomList;
	}
	
	public static List<QueueAtom> makeTestAtomSetB() {
		//- a measure transmission SubTask
		List<QueueAtom> atomList = new ArrayList<>();
		atomList.add(makeTestMoveAtomA());
		atomList.add(makeTestMonitorAtomA());
		atomList.add(makeTestMonitorAtomB());
		atomList.add(makeTestMoveAtomB());
		atomList.add(makeTestMonitorAtomA());
		atomList.add(makeTestMonitorAtomB());
		
		return atomList;
	}
	
//	FIXME!
//	public static List<AbstractQueueAtom> makeTestAtomSetC() {
//		//- take an image of the sample
//		List<AbstractQueueAtom> atomList = new ArrayList<AbstractQueueAtom>();
//		atomList.add(makeTestScanAtomC());
//		atomList.add(makeTestScanAtomD());
//		return atomList;
//	}
	
	public static List<QueueAtom> makeTestAtomSetD() {
		//- a VT data collection SubTask
		List<QueueAtom> atomList = new ArrayList<QueueAtom>();
		atomList.add(makeTestMoveAtomA());
		atomList.add(makeTestMoveAtomC());
//		FIXME!
//		atomList.add(makeTestScanAtomA());
		atomList.add(makeTestMoveAtomD());
		
		return atomList;
	}

}
