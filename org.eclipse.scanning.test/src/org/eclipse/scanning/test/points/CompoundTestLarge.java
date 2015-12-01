package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.points.GeneratorServiceImpl;
import org.junit.Before;
import org.junit.Test;

public class CompoundTestLarge {
	
	private IGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new GeneratorServiceImpl();
	}

	
	@Test
	public void test2Pow24() throws Exception {
		
		List<IGenerator> gens = new ArrayList<IGenerator>(20);
		for (int i = 0; i < 24; i++) {
			IGenerator<StepModel, IPosition> two = service.createGenerator(new StepModel("Temperature"+i, 290,291,1));
			assertEquals(2, two.size());
			gens.add(two);
		}

		long start = System.currentTimeMillis();
		IGenerator<?,IPosition> scan = service.createCompoundGenerator(gens.toArray(new IGenerator[gens.size()]));
		int size = scan.size();
		assertTrue(Math.pow(2, 24)==size);
		
		long stage1 = System.currentTimeMillis();
		System.out.println("Size of "+size+" returned in "+(stage1-start)+" ms");
		
		Iterator<IPosition> it = scan.iterator();
		long stage2 = System.currentTimeMillis();
		System.out.println("Iterator returned in "+(stage2-stage1)+" ms");
		
		int sz=0;
		while(it.hasNext()) {
			it.next();
			sz++;
			if (sz>size) throw new Exception("Iterator grew too large!");
		}
		long stage3 = System.currentTimeMillis();
		System.out.println("Iterator size "+sz+" ran over in "+(stage3-stage2)+" ms");
	
	}

}
