package org.eclipse.scanning.test.nexus;

import static org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset.getDType;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_POINTS;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_SCAN_FINISHED;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_UNIQUE_KEYS;
import static org.eclipse.scanning.test.nexus.ScanPointsWriterTest.ExternalFileWritingDetector.EXTERNAL_FILE_NAME;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.io.ILazySaver;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.impl.BooleanDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.AbstractNexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.sequencer.nexus.ScanPointsWriter;
import org.junit.Test;


public class ScanPointsWriterTest {
	
	public static class MockLazySaver implements ILazySaver {

		private static final long serialVersionUID = 1L;

		private IDataset lastWrittenData = null;
		
		private SliceND lastSlice = null;
		
		private int numWrites = 0;
		
		@Override
		public boolean isFileReadable() {
			return true;
		}

		@Override
		public IDataset getDataset(IMonitor mon, SliceND slice) throws Exception {
			return null;
		}

		@Override
		public void initialize() throws Exception {
			// do nothing
		}

		@Override
		public boolean isFileWriteable() {
			return true;
		}

		@Override
		public void setSlice(IMonitor mon, IDataset data, SliceND slice) throws Exception {
			// TODO could write to a dataset? SliceIterator may be useful here
			lastWrittenData = data;
			lastSlice = slice;
			numWrites++;
		}
		
		public IDataset getLastWrittenData() {
			return lastWrittenData;
		}
		
		public SliceND getLastSlice() {
			return lastSlice;
		}
		
		public int getNumberOfWrites() {
			return numWrites;
		}
		
	}
	
	public static class ExternalFileWritingPositioner extends AbstractNexusObjectProvider<NXpositioner> {
		
		public ExternalFileWritingPositioner(String name) {
			super(name, NexusBaseClass.NX_POSITIONER, NXpositioner.NX_VALUE);
			setExternalFileName(name + ".nxs");
		}
		
		@Override
		protected NXpositioner doCreateNexusObject(NexusNodeFactory nodeFactory) {
			final NXpositioner positioner = nodeFactory.createNXpositioner();
			addExternalLink(positioner, NXpositioner.NX_VALUE, "/entry/data", 2);
			
			return positioner;
		}
	}
	
	public static class ExternalFileWritingDetector extends AbstractNexusObjectProvider<NXdetector> {
		
		public static final String EXTERNAL_FILE_NAME = "detector.nxs";
		
		public ExternalFileWritingDetector() {
			super(NexusBaseClass.NX_DETECTOR);
			setExternalFileName(EXTERNAL_FILE_NAME);
		}

		@Override
		protected NXdetector doCreateNexusObject(NexusNodeFactory nodeFactory) {
			final NXdetector detector = nodeFactory.createNXdetector();
			addExternalLink(detector, NXdetector.NX_DATA, "/entry/data", 4);
			
			return detector;
		}
		
	}
	
	@Test
	public void testCreateNexusObject() throws Exception {
		// Arrange
		List<NexusObjectProvider<?>> nexusObjectProviders = new ArrayList<>();
		nexusObjectProviders.add(new ExternalFileWritingDetector());
		String[] positionerNames = new String[] { "xPos", "yPos" };
		for (String positionerName : positionerNames) {
			nexusObjectProviders.add(new ExternalFileWritingPositioner(positionerName));
		}

		ScanPointsWriter scanPointsWriter = new ScanPointsWriter();
		scanPointsWriter.setNexusObjectProviders(nexusObjectProviders);
		
		final int scanRank = 2;
		NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setRank(scanRank);
		NexusNodeFactory nodeFactory = new NexusNodeFactory();
		int[] expectedChunking = new int[scanInfo.getRank()];
		Arrays.fill(expectedChunking, 1);

		// Act
		NXcollection scanPointsCollection = scanPointsWriter.createNexusObject(nodeFactory, scanInfo);
		
		// Assert
		assertThat(scanPointsCollection, is(notNullValue()));

		// assert unique keys dataset created correctly
		DataNode uniqueKeysDataNode = scanPointsCollection.getDataNode(FIELD_NAME_UNIQUE_KEYS);
		assertThat(uniqueKeysDataNode, is(notNullValue()));
		assertThat(uniqueKeysDataNode.getDataset(), both(is(notNullValue())).and(
				instanceOf(ILazyWriteableDataset.class)));
		ILazyWriteableDataset uniqueKeysDataset = (ILazyWriteableDataset) uniqueKeysDataNode.getDataset();
		assertThat(uniqueKeysDataset.getRank(), is(equalTo(scanRank)));
		assertThat(((LazyDataset) uniqueKeysDataset).getDtype(), is(Dataset.INT32));
		assertThat(uniqueKeysDataset.getChunking(), is(equalTo(expectedChunking)));
		MockLazySaver uniqueKeysSaver = new MockLazySaver(); // TODO could use mockito instead?
		uniqueKeysDataset.setSaver(uniqueKeysSaver);
		
		// assert scan points dataset created correctly
		DataNode pointsDataNode = scanPointsCollection.getDataNode(FIELD_NAME_POINTS);
		assertThat(pointsDataNode, is(notNullValue()));
		assertThat(pointsDataNode.getDataset(), both(is(notNullValue())).and(
				instanceOf(ILazyWriteableDataset.class)));
		ILazyWriteableDataset pointsDataset = (ILazyWriteableDataset) pointsDataNode.getDataset();
		assertThat(pointsDataset.getRank(), is(equalTo(scanRank)));
		assertThat(((LazyDataset) pointsDataset).getDtype(), is(Dataset.STRING));
		assertThat(pointsDataset.getChunking(), is(equalTo(expectedChunking)));
		MockLazySaver pointsSaver = new MockLazySaver();
		pointsDataset.setSaver(pointsSaver);
		
		// assert scan finished dataset created correctly - value must be false
		DataNode scanFinishedDataNode = scanPointsCollection.getDataNode(FIELD_NAME_SCAN_FINISHED);
		assertThat(scanFinishedDataNode, is(notNullValue()));
		assertThat(scanFinishedDataNode.getDataset(), both(is(notNullValue())).and(
				instanceOf(ILazyWriteableDataset.class)));
		ILazyWriteableDataset scanFinishedDataset = (ILazyWriteableDataset) scanFinishedDataNode.getDataset();
		assertThat(scanFinishedDataset.getRank(), is(1));
		assertThat(scanFinishedDataset.getShape(), is(equalTo(new int[] { 1 })));
		MockLazySaver scanFinishedSaver = new MockLazySaver();
		scanFinishedDataset.setSaver(scanFinishedSaver);
		
		// assert links to external nodes
		scanPointsCollection.getNumberOfNodelinks();
		assertThat(scanPointsCollection.getSymbolicNode(EXTERNAL_FILE_NAME), is(notNullValue()));
		for (String positionerName : positionerNames) {
			assertThat(scanPointsCollection.getSymbolicNode(positionerName + ".nxs"), is(notNullValue()));
		}
	}
	
	@Test
	public void testWriteScanPoints() throws Exception {
		// Arrange - we have to create the nexus object first 
		List<NexusObjectProvider<?>> nexusObjectProviders = new ArrayList<>();
		nexusObjectProviders.add(new ExternalFileWritingDetector());
		String[] positionerNames = new String[] { "xPos", "yPos" };
		for (String positionerName : positionerNames) {
			nexusObjectProviders.add(new ExternalFileWritingPositioner(positionerName));
		}

		ScanPointsWriter scanPointsWriter = new ScanPointsWriter();
		scanPointsWriter.setNexusObjectProviders(nexusObjectProviders);
		
		final int scanRank = 2;
		NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setRank(scanRank);
		NexusNodeFactory nodeFactory = new NexusNodeFactory();
		int[] expectedChunking = new int[scanInfo.getRank()];
		Arrays.fill(expectedChunking, 1);

		NXcollection scanPointsCollection = scanPointsWriter.createNexusObject(nodeFactory, scanInfo);
		
		DataNode uniqueKeysDataNode = scanPointsCollection.getDataNode(FIELD_NAME_UNIQUE_KEYS);
		ILazyWriteableDataset uniqueKeysDataset = (ILazyWriteableDataset) uniqueKeysDataNode.getDataset();
		MockLazySaver uniqueKeysSaver = new MockLazySaver(); // TODO could use mockito instead?
		uniqueKeysDataset.setSaver(uniqueKeysSaver);
		
		DataNode pointsDataNode = scanPointsCollection.getDataNode(FIELD_NAME_POINTS);
		ILazyWriteableDataset pointsDataset = (ILazyWriteableDataset) pointsDataNode.getDataset();
		MockLazySaver pointsSaver = new MockLazySaver();
		pointsDataset.setSaver(pointsSaver);
		
		DataNode scanFinishedDataNode = scanPointsCollection.getDataNode(FIELD_NAME_SCAN_FINISHED);
		ILazyWriteableDataset scanFinishedDataset = (ILazyWriteableDataset) scanFinishedDataNode.getDataset();
		MockLazySaver scanFinishedSaver = new MockLazySaver();
		scanFinishedDataset.setSaver(scanFinishedSaver);
		
		// assert links to external nodes
		scanPointsCollection.getNumberOfNodelinks();
		assertThat(scanPointsCollection.getSymbolicNode(EXTERNAL_FILE_NAME), is(notNullValue()));
		for (String positionerName : positionerNames) {
			assertThat(scanPointsCollection.getSymbolicNode(positionerName + ".nxs"), is(notNullValue()));
		}
		
		// test calling positionPerformed
		// arrange
		double[] pos = new double[] { 172.5, 56.3 };
		int[] indices = new int[] { 8, 3 };
		int stepIndex = 23;
		MapPosition position = new MapPosition();
		position.setStepIndex(stepIndex);
		for (int i = 0; i < positionerNames.length; i++) {
			position.put(positionerNames[i], pos[i]);
			position.putIndex(positionerNames[i], indices[i]);
		}
		
		// act
		scanPointsWriter.positionPerformed(new PositionEvent(position));
		scanPointsWriter.runPerformed(null);

		// assert
		assertThat(pointsSaver.getNumberOfWrites(), is(1));

		IDataset writtenToUniqueKeysData = uniqueKeysSaver.getLastWrittenData();
		assertThat(writtenToUniqueKeysData, is(notNullValue()));
		int[] expectedShape = new int[scanInfo.getRank()];
		Arrays.fill(expectedShape, 1);
		assertThat(writtenToUniqueKeysData.getShape(), is(expectedShape));
		assertThat(getDType(writtenToUniqueKeysData), is(Dataset.INT));
		int[] valuePos = new int[scanRank]; // all zeros
		assertThat(writtenToUniqueKeysData.getInt(valuePos), is(stepIndex+1));

		SliceND uniqueKeysSlice = uniqueKeysSaver.getLastSlice();
		assertThat(uniqueKeysSlice, is(notNullValue()));
		assertThat(uniqueKeysSlice.getShape(), is(expectedShape));
		assertThat(uniqueKeysSlice.getStart(), is(indices));
		assertThat(uniqueKeysSlice.getStep(), is(expectedShape)); // all ones
		int[] stopIndices = Arrays.stream(indices).map(x -> x + 1).toArray(); 
		assertThat(uniqueKeysSlice.getStop(), is(stopIndices));
		
		IDataset writtenToPointsData = pointsSaver.getLastWrittenData();
		assertThat(writtenToPointsData, is(notNullValue()));
		assertThat(writtenToPointsData.getShape(), is(expectedShape));
		assertThat(getDType(writtenToPointsData), is(Dataset.STRING));
		assertThat(writtenToPointsData.getString(valuePos), is(equalTo(position.toString())));
		
		SliceND pointsSlice = pointsSaver.getLastSlice();
		assertThat(pointsSlice, is(notNullValue()));
		assertThat(pointsSlice.getShape(), is(expectedShape));
		assertThat(pointsSlice.getStart(), is(indices));
		assertThat(pointsSlice.getStep(), is(expectedShape));
		assertThat(pointsSlice.getStop(), is(stopIndices));
	}
	
}
