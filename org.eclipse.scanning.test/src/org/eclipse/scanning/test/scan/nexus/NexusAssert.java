package org.eclipse.scanning.test.scan.nexus;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;

/**
 * 
 * Avoid dependency on org.eclipse.dawnsci.nexus.test which is not on the dawnsci p2
 * 
 * @author Matthew Gerring
 *
 */
public class NexusAssert {

	

	public static void assertAxes(NXdata nxData, String... expectedValues) {
		Attribute axesAttr = nxData.getAttribute("axes");
		assertThat(axesAttr, is(notNullValue()));
		assertThat(axesAttr.getRank(), is(1));
		assertThat(axesAttr.getShape()[0], is(expectedValues.length));
		IDataset value = axesAttr.getValue();
		for (int i = 0; i < expectedValues.length; i++) {
			assertThat(value.getString(i), is(equalTo(expectedValues[i])));
		}
	}

	public static void assertIndices(NXdata nxData, String axisName, int... indices) {
		Attribute indicesAttr = nxData.getAttribute(axisName + "_indices");
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
		Attribute targetAttr = dataNode.getAttribute(NexusDataBuilder.ATTR_NAME_TARGET);
		assertThat(targetAttr, is(notNullValue()));
		assertThat(targetAttr.getSize(), is(1));
		assertThat(targetAttr.getFirstElement(), is(equalTo(targetPath)));
		
		NodeLink nodeLink = nxRoot.findNodeLink(targetPath);
		assertTrue(nodeLink.isDestinationData());
		assertThat(nodeLink.getDestination(), is(sameInstance(dataNode)));
	}
	
	public static void assertSignal(NXdata nxData, String expectedSignalFieldName) {
		Attribute signalAttr = nxData.getAttribute("signal");
		assertThat(signalAttr, is(notNullValue()));
		assertThat(signalAttr.getRank(), is(1));
		assertThat(signalAttr.getFirstElement(), is(equalTo(expectedSignalFieldName)));
		assertThat(nxData.getDataNode(expectedSignalFieldName), is(notNullValue()));
		
	}

}
