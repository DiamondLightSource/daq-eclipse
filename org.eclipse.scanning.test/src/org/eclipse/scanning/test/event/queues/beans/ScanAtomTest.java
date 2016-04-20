package org.eclipse.scanning.test.event.queues.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.models.EmptyModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.event.queues.beans.ScanAtom;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.Before;

/**
 * Test for the {@link ScanAtom} class. This class only create the POJO.
 * Actual tests in {@link AbstractBeanTest}.
 * 
 * @author Michael Wharmby
 *
 */
public class ScanAtomTest extends AbstractBeanTest<ScanAtom> {
	
	private String nameA = "Test scan 1", nameB = "Test scan 2";
	private List<IScanPathModel> modelsA, modelsB;
	private Map<String, IDetectorModel> detectorsA, detectorsB;
	private List<String> monitors;
	
	@Before
	public void buildBeans() {
		detectorsA = new HashMap<>();
		detectorsA.put("Test Detector A", new MockDetectorModel(30.0));
		detectorsA.put("Test Detector B", new MockDetectorModel(30.0));
		detectorsB = new HashMap<>();
		detectorsB.put("Test Detector C", new MockDetectorModel(50.0));
		
		modelsA = new ArrayList<>();
		modelsA.add(new EmptyModel());
		modelsB = new ArrayList<>();
		modelsB.add(new EmptyModel());
		modelsB.add(new EmptyModel());
		
		monitors = new ArrayList<>();
		monitors.add("Fake monitor");
		
		beanA = new ScanAtom(nameA, modelsA, detectorsA);
		beanB = new ScanAtom(nameB, modelsB, detectorsB, monitors);
	}

}
