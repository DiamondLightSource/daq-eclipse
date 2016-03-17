package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset.getDType;
import static org.eclipse.dawnsci.nexus.builder.NexusDataBuilder.ATTR_NAME_AXES;
import static org.eclipse.dawnsci.nexus.builder.NexusDataBuilder.ATTR_NAME_SIGNAL;
import static org.eclipse.dawnsci.nexus.builder.NexusDataBuilder.ATTR_NAME_TARGET;
import static org.eclipse.dawnsci.nexus.builder.NexusDataBuilder.ATTR_SUFFIX_INDICES;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_POINTS;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_UNIQUE_KEYS;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.GROUP_NAME_SOLSTICE_SCAN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
		assertThat(axesAttr, is(notNullValue()));
		assertThat(axesAttr.getRank(), is(1));
		assertThat(axesAttr.getShape()[0], is(expectedValues.length));
		IDataset value = axesAttr.getValue();
		for (int i = 0; i < expectedValues.length; i++) {
			assertThat(value.getString(i), is(equalTo(expectedValues[i])));
		}
	}

	public static void assertIndices(NXdata nxData, String axisName, int... indices) {
		Attribute indicesAttr = nxData.getAttribute(axisName + ATTR_SUFFIX_INDICES);
		assertThat(indicesAttr, is(notNullValue()));
		assertThat(indicesAttr.getRank(), is(1));
		assertThat(indicesAttr.getShape()[0], is(indices.length));
		IDataset value = indicesAttr.getValue();
		for (int i = 0; i < indices.length; i++) {
			assertThat(value.getInt(i), is(equalTo(indices[i])));
		}
	}
	
	public static void assertTarget(NXdata nxData, String destName, NXroot nxRoot, String targetPath) {
		DataNode dataNode = nxData.getDataNode(destName);
		assertThat(dataNode, is(notNullValue()));
		Attribute targetAttr = dataNode.getAttribute(ATTR_NAME_TARGET);
		assertThat(targetAttr, is(notNullValue()));
		assertThat(targetAttr.getSize(), is(1));
		assertThat(targetAttr.getFirstElement(), is(equalTo(targetPath)));
		
		NodeLink nodeLink = nxRoot.findNodeLink(targetPath);
		assertTrue(nodeLink.isDestinationData());
		assertThat(nodeLink.getDestination(), is(sameInstance(dataNode)));
	}
	
	public static void assertSignal(NXdata nxData, String expectedSignalFieldName) {
		Attribute signalAttr = nxData.getAttribute(ATTR_NAME_SIGNAL);
		assertThat(signalAttr, is(notNullValue()));
		assertThat(signalAttr.getRank(), is(1));
		assertThat(signalAttr.getFirstElement(), is(equalTo(expectedSignalFieldName)));
		assertThat(nxData.getDataNode(expectedSignalFieldName), is(notNullValue()));
	}
	
	public static void assertScanPointsGroup(NXentry entry, int... sizes) {
		NXcollection scanPointsCollection = entry.getCollection(GROUP_NAME_SOLSTICE_SCAN);
		assertNotNull(scanPointsCollection);
		 
		assertScanPoints("", scanPointsCollection, sizes);
		// TODO assert links to unique keys datasets in external HDF5 files
	}

	public static void assertScanPoints(NXdata nxData, int... sizes) {
		assertScanPoints(GROUP_NAME_SOLSTICE_SCAN + "_", nxData, sizes);
	}
		
	private static void assertScanPoints(String fieldNamePrefix,
			NXobject parentGroup, int... sizes) {
		// check the unique keys field - contains the step number for each scan points
		DataNode dataNode = parentGroup.getDataNode(fieldNamePrefix + FIELD_NAME_UNIQUE_KEYS);
		assertNotNull(dataNode);
		IDataset dataset = dataNode.getDataset().getSlice();
		assertThat(getDType(dataset), is(Dataset.INT32));
		assertThat(dataset.getRank(), is(sizes.length));
		assertArrayEquals(sizes, dataset.getShape());
		PositionIterator iter = new PositionIterator(dataset.getShape());
		
		int expectedPos = 0;
		while (iter.hasNext()) { // hasNext also increments the position iterator (ugh!)
			assertThat(dataset.getInt(iter.getPos()), is(expectedPos));
			expectedPos++;
		}
		
		// check the scan points field - contains the scan points as strings
		dataNode = parentGroup.getDataNode(fieldNamePrefix + FIELD_NAME_POINTS);
		assertNotNull(dataNode);
		dataset = dataNode.getDataset().getSlice();
		assertThat(getDType(dataset), is(Dataset.STRING));
		assertThat(dataset.getRank(), is(sizes.length));
		assertArrayEquals(sizes, dataset.getShape());
	}

}
