package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.AbstractPosition;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Test different scan ranks after compounds are created.
 * 
 * @author Matthew Gerring
 *
 */
public class ScanRankTest {
	
	private IPointGeneratorService service;
	
	@Before
	public void before() throws Exception {
		service = new PointGeneratorFactory();
	}

	@Test
	public void testRankLine1D() throws Exception {
		lineTest(0);
	}

	@Test
	public void testRankLine2D() throws Exception {
		lineTest(1);
	}
	
	@Test
	public void testRankLine3D() throws Exception {
		lineTest(2);
	}
	
	@Test
	public void testRankLine4D() throws Exception {
		lineTest(3);
	}
	@Test
	public void testRankLine5D() throws Exception {
		lineTest(4);
	}
	@Test
	public void testRankLine6D() throws Exception {
		lineTest(5);
	}
	@Test
	public void testRankLine7D() throws Exception {
		lineTest(6);
	}
	@Test
	public void testRankLine8D() throws Exception {
		lineTest(7);
	}
	@Test
	public void testRankLine9D() throws Exception {
		lineTest(8);
	}

	private void lineTest(int nestCount) throws Exception {
		
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(10);
		
		// Get the point list
		IPointGenerator<?> gen = service.createGenerator(model, roi);
		
		for (int i = 0; i < nestCount; i++) {
			IPointGenerator<StepModel>             temp = service.createGenerator(new StepModel("T"+i, 290, 300, 1)); 
			gen = service.createCompoundGenerator(temp, gen);
		}
        
        
		System.out.println("The number of points will be: "+gen.size());
		
		int scanRank = nestCount+1;
		
		int count=0;
        for (IPosition pos : gen) {
		    assertTrue("The ranks should be "+scanRank+" but was "+pos.getScanRank()+" for "+pos, pos.getScanRank()==scanRank); 
		    for (int i = 0; i < nestCount; i++) {
		    	final Collection<String> names = ((AbstractPosition)pos).getDimensionNames(i);
		    	final Collection<String> expected = Arrays.asList("T"+(nestCount-1-i));
				assertTrue("Names are: "+names+" expected was: "+expected, expected.containsAll(names));
			}
		    if (nestCount>0) {
			    assertTrue(Arrays.asList("x", "y").containsAll(((AbstractPosition)pos).getDimensionNames(scanRank-1)));
		    }
		    
		    ++count;
		    if (count>100) break; // We just check the first few.
        }

	}

	
	@Test
	public void testRankGrid1D() throws Exception {
		gridTest(0);
	}
	@Test
	public void testRankGrid2D() throws Exception {
		gridTest(1);
	}
	@Test
	public void testRankGrid3D() throws Exception {
		gridTest(2);
	}
	@Test
	public void testRankGrid4D() throws Exception {
		gridTest(3);
	}
	@Test
	public void testRankGrid5D() throws Exception {
		gridTest(4);
	}
	@Test
	public void testRankGrid6D() throws Exception {
		gridTest(5);
	}
	@Test
	public void testRankGrid7D() throws Exception {
		gridTest(6);
	}

	private void gridTest(int nestCount) throws Exception {
		
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(0);
		box.setSlowAxisStart(0);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(3);

		GridModel model = new GridModel();
		model.setSlowAxisPoints(20);
		model.setFastAxisPoints(20);
		model.setBoundingBox(box);
		
		// Get the point list
		IPointGenerator<?> gen = service.createGenerator(model);
		
		for (int i = 0; i < nestCount; i++) {
			IPointGenerator<StepModel>             temp = service.createGenerator(new StepModel("T"+i, 290, 300, 1)); 
			gen = service.createCompoundGenerator(temp, gen);
		}
		
		System.out.println("The number of points will be: "+gen.size());
		
		int scanRank = nestCount+2;
		
		int count=0;
        for (IPosition pos : gen) {
		    assertTrue("The ranks should be "+scanRank+" but was "+pos.getScanRank()+" for "+pos, pos.getScanRank()==scanRank); 
		    for (int i = 0; i < nestCount; i++) {
		    	final Collection<String> names = ((AbstractPosition)pos).getDimensionNames(i);
		    	final Collection<String> expected = Arrays.asList("T"+(nestCount-1-i));
				assertTrue("Names are: "+names+" expected was: "+expected, expected.containsAll(names));
			}
		    if (nestCount>0) {
			    assertTrue(Arrays.asList("y").containsAll(((AbstractPosition)pos).getDimensionNames(scanRank-2)));
			    assertTrue(Arrays.asList("x").containsAll(((AbstractPosition)pos).getDimensionNames(scanRank-1)));
		    }
		    
		    ++count;
		    if (count>100) break; // We just check the first few.
        }
	}

}
