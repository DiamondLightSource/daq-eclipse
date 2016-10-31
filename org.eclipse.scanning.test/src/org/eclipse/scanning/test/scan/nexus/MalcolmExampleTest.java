package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.example.malcolm.TestMalcolmModel;
import org.junit.Test;

/**
 * This test uses the RunnableDeviceService to create an Example Malcolm Device and run it, 
 * which just writes two test Nexus files with Random Image data
 * 
 * @author Matt Taylor
 *
 */
public class MalcolmExampleTest extends NexusTest {
	
	private IPointGenerator<?> getGenerator(int... size) throws GeneratorException {
		GridModel gmodel = new GridModel();
		gmodel.setFastAxisName("xNex");
		gmodel.setFastAxisPoints(size[size.length-1]);
		gmodel.setSlowAxisName("yNex");
		gmodel.setSlowAxisPoints(size[size.length-2]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		
		IPointGenerator<?> gen = gservice.createGenerator(gmodel);
		
		IPointGenerator<?>[] gens = new IPointGenerator<?>[size.length - 1];
		// We add the outer scans, if any
		if (size.length > 2) { 
			for (int dim = size.length-3; dim>-1; dim--) {
				final StepModel model;
				if (size[dim]-1>0) {
				    model = new StepModel("neXusScannable"+(dim+1), 10,20,9.99d/(size[dim]-1));
				} else {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,30); // Will generate one value at 10
				}
				final IPointGenerator<?> step = gservice.createGenerator(model);
				gens[dim] = step;
			}
		}
		gens[size.length - 2] = gen;

		gen = gservice.createCompoundGenerator(gens);
		
		return gen;
	}
	
	@Test
	public void testMalcolmExampleRun() throws Exception {
		
		TestMalcolmModel model = new TestMalcolmModel();
		model.setFilePath("/tmp/");
		model.setGenerator(getGenerator(2, 2)); // Generator isn't actually used by the test malcolm device
		IRunnableDevice<TestMalcolmModel> malcolmDevice = dservice.createRunnableDevice(model);
		assertNotNull(malcolmDevice);	
		
		malcolmDevice.run(null);
		
		// Check file has been written with some data
		IMalcolmDevice<?> imd = (IMalcolmDevice<?>)malcolmDevice;
		Object datasetsValue = imd.getAttributeValue("datasets");
		MalcolmTable table = (MalcolmTable)datasetsValue;
		List<Object> fileNames = table.getColumn("filename");
		assertEquals(4, fileNames.size());
		List<Object> paths = table.getColumn("path");
		assertEquals(4, paths.size());
		
		assertTrue(fileNames.get(0).toString().contains(model.getFilePath()));
		
		NXroot rootNodeFile1 = getNexusRoot(fileNames.get(0).toString());
		NXentry rootEntryFile1 = rootNodeFile1.getEntry();
		String entry0node = paths.get(0).toString().split("/")[2];
		String entry1node = paths.get(1).toString().split("/")[2];
		Node entry0 = rootEntryFile1.getNode(entry0node);
		Node entry1 = rootEntryFile1.getNode(entry1node);
		
		NXroot rootNodeFile2 = getNexusRoot(fileNames.get(2).toString());
		NXentry rootEntryFile2 = rootNodeFile2.getEntry();
		String entry2node = paths.get(2).toString().split("/")[2];
		String entry3node = paths.get(3).toString().split("/")[2];
		Node entry2 = rootEntryFile2.getNode(entry2node);
		Node entry3 = rootEntryFile2.getNode(entry3node);
		
		assertNotNull(entry0);
		assertNotNull(entry1);
		assertNotNull(entry2);
		assertNotNull(entry3);
	}
	
	private NXroot getNexusRoot(String filePath) throws Exception {
		INexusFileFactory fileFactory = org.eclipse.dawnsci.nexus.ServiceHolder.getNexusFileFactory();
		NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();
		
		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		return (NXroot) nexusTree.getGroupNode();
	}

}
