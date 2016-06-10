package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset.getDType;
import static org.eclipse.dawnsci.nexus.builder.data.NexusDataBuilder.ATTR_NAME_AXES;
import static org.eclipse.dawnsci.nexus.builder.data.NexusDataBuilder.ATTR_NAME_SIGNAL;
import static org.eclipse.dawnsci.nexus.builder.data.NexusDataBuilder.ATTR_NAME_TARGET;
import static org.eclipse.dawnsci.nexus.builder.data.NexusDataBuilder.ATTR_SUFFIX_INDICES;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_POINTS;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_SCAN_FINISHED;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_UNIQUE_KEYS;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.GROUP_NAME_SOLSTICE_SCAN;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.dawnsci.analysis.api.dataset.DatasetException;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXroot;

/**
 * 
 * Copied to avoid dependency on org.eclipse.dawnsci.nexus.test which is not on the dawnsci p2
 * 
 * @author Matthew Gerring
 *
 */
public class NexusAssert {

	public static void assertAxes(NXdata nxData, String... expectedValues) {
		Attribute axesAttr = nxData.getAttribute(ATTR_NAME_AXES);
		assertTrue(axesAttr!=null);
		assertTrue(axesAttr.getRank()==1);
		assertTrue(axesAttr.getShape()[0]==expectedValues.length);
		IDataset value = axesAttr.getValue();
		for (int i = 0; i < expectedValues.length; i++) {
			assertTrue(value.getString(i).equals(expectedValues[i]));
		}
	}

	public static void assertIndices(NXdata nxData, String axisName, int... indices) {
		Attribute indicesAttr = nxData.getAttribute(axisName + ATTR_SUFFIX_INDICES);
		assertTrue(indicesAttr!=null);
		assertTrue(indicesAttr.getRank()==1);
		assertTrue(indicesAttr.getShape()[0]==indices.length);
		IDataset value = indicesAttr.getValue();
		for (int i = 0; i < indices.length; i++) {
			assertTrue(value.getInt(i)==indices[i]);
		}
	}
	
	public static void assertTarget(NXdata nxData, String destName, NXroot nxRoot, String targetPath) {
		DataNode dataNode = nxData.getDataNode(destName);
		assertTrue(dataNode!=null);
		Attribute targetAttr = dataNode.getAttribute(ATTR_NAME_TARGET);
		assertTrue(targetAttr!=null);
		assertTrue(targetAttr.getSize()==1);
		assertTrue(targetAttr.getFirstElement().equals(targetPath));
		
		NodeLink nodeLink = nxRoot.findNodeLink(targetPath);
		assertTrue(nodeLink.isDestinationData());
		assertTrue(nodeLink.getDestination()==dataNode);
	}
	
	public static void assertSignal(NXdata nxData, String expectedSignalFieldName) {
		Attribute signalAttr = nxData.getAttribute(ATTR_NAME_SIGNAL);
		assertTrue(signalAttr!=null);
		assertTrue(signalAttr.getRank()==1);
		assertTrue(signalAttr.getFirstElement().equals(expectedSignalFieldName));
		assertTrue(nxData.getDataNode(expectedSignalFieldName)!=null);
	}
	
	public static void assertScanPointsGroup(NXentry entry, int... sizes) {
		NXcollection scanPointsCollection = entry.getCollection(GROUP_NAME_SOLSTICE_SCAN);
		assertNotNull(scanPointsCollection);
		 
		assertScanPoints("", scanPointsCollection, sizes);
		// TODO assert links to unique keys datasets in external HDF5 files
		assertScanFinished(entry);
	}

	public static void assertScanPoints(NXdata nxData, int... sizes) {
		assertScanPoints(GROUP_NAME_SOLSTICE_SCAN + "_", nxData, sizes);
	}
		
	private static void assertScanPoints(String fieldNamePrefix,
			NXobject parentGroup, int... sizes) {
		// check the unique keys field - contains the step number for each scan points
		DataNode dataNode = parentGroup.getDataNode(fieldNamePrefix + FIELD_NAME_UNIQUE_KEYS);
		assertNotNull(dataNode);
		IDataset dataset;
		try {
			dataset = dataNode.getDataset().getSlice();
		} catch (DatasetException e) {
			throw new AssertionError("Could not get data from lazy dataset", e);
		}
		assertTrue(getDType(dataset)==Dataset.INT32);
		assertTrue(dataset.getRank()==sizes.length);
		assertArrayEquals(sizes, dataset.getShape());
		PositionIterator iter = new PositionIterator(dataset.getShape());
		
		int expectedPos = 1;
		while (iter.hasNext()) { // hasNext also increments the position iterator (ugh!)
			assertTrue(dataset.getInt(iter.getPos())==expectedPos);
			expectedPos++;
		}
		
		// check the scan points field - contains the scan points as strings
		dataNode = parentGroup.getDataNode(fieldNamePrefix + FIELD_NAME_POINTS);
		assertNotNull(dataNode);
		try {
			dataset = dataNode.getDataset().getSlice();
		} catch (DatasetException e) {
			throw new AssertionError("Could not get data from lazy dataset", e);
		}
		assertTrue(getDType(dataset)==Dataset.STRING);
		assertTrue(dataset.getRank()==sizes.length);
		assertArrayEquals(sizes, dataset.getShape());
	}
	
	public static void assertScanFinished(NXentry entry) {
		assertScanFinished(entry, true);
	}

	public static void assertScanNotFinished(NXentry entry) {
		assertScanFinished(entry, false);
	}
	
	private static void assertScanFinished(NXentry entry, boolean finished) {
		NXcollection scanPointsCollection = entry.getCollection(GROUP_NAME_SOLSTICE_SCAN);
		assertNotNull(scanPointsCollection);
		
		// check the scan finished boolean is set to true
		DataNode dataNode = scanPointsCollection.getDataNode(FIELD_NAME_SCAN_FINISHED);
		assertNotNull(dataNode);
		IDataset dataset;
		try {
			dataset = dataNode.getDataset().getSlice();
		} catch (DatasetException e) {
			throw new AssertionError("Could not get data from lazy dataset", e);
		}
		assertTrue(getDType(dataset)==Dataset.INT32); // HDF5 doesn't support boolean datasets
		assertTrue(dataset.getRank()==1);
		assertArrayEquals(dataset.getShape(), new int[] { 1 });
		assertTrue(dataset.getBoolean(0)==finished);
	}

}
