package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.nexus.builder.data.NexusDataBuilder.ATTR_NAME_AXES;
import static org.eclipse.dawnsci.nexus.builder.data.NexusDataBuilder.ATTR_NAME_SIGNAL;
import static org.eclipse.dawnsci.nexus.builder.data.NexusDataBuilder.ATTR_NAME_TARGET;
import static org.eclipse.dawnsci.nexus.builder.data.NexusDataBuilder.ATTR_SUFFIX_INDICES;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_POINTS;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_SCAN_FINISHED;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_UNIQUE_KEYS;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.GROUP_NAME_SOLSTICE_SCAN;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;

/**
 * 
 * Copied to avoid dependency on org.eclipse.dawnsci.nexus.test which is not on the dawnsci p2
 * 
 * @author Matthew Gerring
 *
 */
public class NexusAssert {

	public static void assertAxes(NXdata nxData, String... expectedValues) {
		if (expectedValues.length == 0) return; // axes not written if no axes to write (a scalar signal field)
		Attribute axesAttr = nxData.getAttribute(ATTR_NAME_AXES);
		assertNotNull(axesAttr);
		assertEquals(1, axesAttr.getRank());
		assertEquals(expectedValues.length, axesAttr.getShape()[0]);
		IDataset value = axesAttr.getValue();
		for (int i = 0; i < expectedValues.length; i++) {
			assertTrue(value.getString(i).equals(expectedValues[i]));
		}
	}

	public static void assertIndices(NXdata nxData, String axisName, int... indices) {
		Attribute indicesAttr = nxData.getAttribute(axisName + ATTR_SUFFIX_INDICES);
		assertNotNull(indicesAttr);
		assertEquals(1, indicesAttr.getRank());
		assertEquals(indices.length, indicesAttr.getShape()[0]);
		IDataset value = indicesAttr.getValue();
		for (int i = 0; i < indices.length; i++) {
			assertEquals(indices[i], value.getInt(i));
		}
	}
	
	public static void assertTarget(NXdata nxData, String destName, NXroot nxRoot, String targetPath) {
		DataNode dataNode = nxData.getDataNode(destName);
		assertNotNull(dataNode);
		Attribute targetAttr = dataNode.getAttribute(ATTR_NAME_TARGET);
		assertNotNull(targetAttr);
		assertEquals(1, targetAttr.getSize());
		assertEquals(targetPath, targetAttr.getFirstElement());
		
		NodeLink nodeLink = nxRoot.findNodeLink(targetPath);
		assertTrue(nodeLink.isDestinationData());
		assertTrue(nodeLink.getDestination()==dataNode);
	}
	
	public static void assertSignal(NXdata nxData, String expectedSignalFieldName) {
		Attribute signalAttr = nxData.getAttribute(ATTR_NAME_SIGNAL);
		assertNotNull(signalAttr);
		assertEquals(1, signalAttr.getSize());
		assertEquals(expectedSignalFieldName, signalAttr.getFirstElement());
		assertNotNull(nxData.getDataNode(expectedSignalFieldName));
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
		assertEquals(Dataset.INT32, DTypeUtils.getDType(dataset));
		assertEquals(sizes.length, dataset.getRank());
		assertArrayEquals(sizes, dataset.getShape());
		PositionIterator iter = new PositionIterator(dataset.getShape());
		
		int expectedPos = 1;
		while (iter.hasNext()) { // hasNext also increments the position iterator (ugh!)
			assertEquals(expectedPos, dataset.getInt(iter.getPos()));
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
		assertEquals(Dataset.STRING, DTypeUtils.getDType(dataset));
		assertEquals(sizes.length, dataset.getRank());
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
		assertEquals(Dataset.INT32, DTypeUtils.getDType(dataset)); // HDF5 doesn't support boolean datasets
		assertEquals(1, dataset.getRank());
		assertArrayEquals(new int[] {1}, dataset.getShape());
		assertEquals(finished, dataset.getBoolean(0));
	}

}
