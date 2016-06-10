package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset.getDType;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_POINTS;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_SCAN_FINISHED;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_UNIQUE_KEYS;
import static org.eclipse.scanning.test.scan.nexus.ScanPointsWriterTest.ExternalFileWritingDetector.EXTERNAL_FILE_NAME;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.io.ILazySaver;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
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
		public IDataset getDataset(IMonitor mon, SliceND slice) throws IOException {
			return null;
		}

		@Override
		public void initialize() throws IOException {
			// do nothing
		}

		@Override
		public boolean isFileWriteable() {
			return true;
		}

		@Override
		public void setSlice(IMonitor mon, IDataset data, SliceND slice) throws IOException {
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
		protected NXpositioner createNexusObject() {
			final NXpositioner positioner = NexusNodeFactory.createNXpositioner();
			addExternalLink(positioner, NXpositioner.NX_VALUE, "/entry/data", 2);
			
			return positioner;
		}
	}
	
	public static class ExternalFileWritingDetector extends AbstractNexusObjectProvider<NXdetector> {
		
		public static final String EXTERNAL_FILE_NAME = "detector.nxs";
		
		public ExternalFileWritingDetector() {
			super("detector", NexusBaseClass.NX_DETECTOR);
			setExternalFileName(EXTERNAL_FILE_NAME);
		}

		@Override
		protected NXdetector createNexusObject() {
			final NXdetector detector = NexusNodeFactory.createNXdetector();
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
		int[] expectedChunking = new int[scanInfo.getRank()];
		Arrays.fill(expectedChunking, 1);

		// Act
		NXcollection scanPointsCollection = scanPointsWriter.createNexusObject(scanInfo);
		
		// Assert
		assertTrue(scanPointsCollection!=null);

		// assert unique keys dataset created correctly
		DataNode uniqueKeysDataNode = scanPointsCollection.getDataNode(FIELD_NAME_UNIQUE_KEYS);
		assertTrue(uniqueKeysDataNode!=null);
		assertTrue(uniqueKeysDataNode.getDataset()!=null && uniqueKeysDataNode.getDataset() instanceof ILazyWriteableDataset);
		ILazyWriteableDataset uniqueKeysDataset = (ILazyWriteableDataset) uniqueKeysDataNode.getDataset();
		assertTrue(uniqueKeysDataset.getRank()==scanRank);
		assertTrue(((LazyDataset) uniqueKeysDataset).getDtype()==Dataset.INT32);
		assertTrue(Arrays.equals(uniqueKeysDataset.getChunking(), expectedChunking));
		MockLazySaver uniqueKeysSaver = new MockLazySaver();
		uniqueKeysDataset.setSaver(uniqueKeysSaver);
		
		// assert scan points dataset created correctly
		DataNode pointsDataNode = scanPointsCollection.getDataNode(FIELD_NAME_POINTS);
		assertTrue(pointsDataNode!=null);
		assertTrue(pointsDataNode.getDataset()!=null && pointsDataNode.getDataset() instanceof ILazyWriteableDataset);
		ILazyWriteableDataset pointsDataset = (ILazyWriteableDataset) pointsDataNode.getDataset();
		assertTrue(pointsDataset.getRank()==scanRank);
		assertTrue(((LazyDataset) pointsDataset).getDtype()==Dataset.STRING);
		assertTrue(Arrays.equals(pointsDataset.getChunking(), expectedChunking));
		MockLazySaver pointsSaver = new MockLazySaver();
		pointsDataset.setSaver(pointsSaver);
		
		// assert scan finished dataset created correctly - value must be false
		DataNode scanFinishedDataNode = scanPointsCollection.getDataNode(FIELD_NAME_SCAN_FINISHED);
		assertTrue(scanFinishedDataNode!=null);
		assertTrue(scanFinishedDataNode.getDataset()!=null && scanFinishedDataNode.getDataset() instanceof ILazyWriteableDataset);
		ILazyWriteableDataset scanFinishedDataset = (ILazyWriteableDataset) scanFinishedDataNode.getDataset();
		assertTrue(scanFinishedDataset.getRank()==1);
		assertTrue(Arrays.equals(scanFinishedDataset.getShape(), new int[] { 1 }));
		MockLazySaver scanFinishedSaver = new MockLazySaver();
		scanFinishedDataset.setSaver(scanFinishedSaver);
		
		// assert links to external nodes
		scanPointsCollection.getNumberOfNodelinks();
		assertTrue(scanPointsCollection.getSymbolicNode(EXTERNAL_FILE_NAME)!=null);
		for (String positionerName : positionerNames) {
			assertTrue(scanPointsCollection.getSymbolicNode(positionerName + ".nxs")!=null);
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
		int[] expectedChunking = new int[scanInfo.getRank()];
		Arrays.fill(expectedChunking, 1);

		NXcollection scanPointsCollection = scanPointsWriter.createNexusObject(scanInfo);
		
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
		assertTrue(scanPointsCollection.getSymbolicNode(EXTERNAL_FILE_NAME)!=null);
		for (String positionerName : positionerNames) {
			assertTrue(scanPointsCollection.getSymbolicNode(positionerName + ".nxs")!=null);
		}
		
		// test calling positionPerformed
		// arrange
		double[] pos = new double[] { 172.5, 56.3 };
		int[] indices = new int[] { 8, 3 };
		int stepIndex = 23;
		MapPosition position = new MapPosition();
		position.setStepIndex(stepIndex);
		List<Collection<String>> names = new ArrayList<>( positionerNames.length);
		for (int i = 0; i < positionerNames.length; i++) {
			position.put(positionerNames[i], pos[i]);
			position.putIndex(positionerNames[i], indices[i]);
			names.add(Arrays.asList(positionerNames[i]));
		}
		position.setDimensionNames(names);
		
		// act
		scanPointsWriter.positionPerformed(new PositionEvent(position));
		scanPointsWriter.scanFinished();

		// assert
		assertTrue(pointsSaver.getNumberOfWrites()==1);

		IDataset writtenToUniqueKeysData = uniqueKeysSaver.getLastWrittenData();
		assertTrue(writtenToUniqueKeysData!=null);
		int[] expectedShape = new int[scanInfo.getRank()];
		Arrays.fill(expectedShape, 1);
		assertTrue(Arrays.equals(writtenToUniqueKeysData.getShape(), expectedShape));
		assertTrue(getDType(writtenToUniqueKeysData)==Dataset.INT);
		int[] valuePos = new int[scanRank]; // all zeros
		assertTrue(writtenToUniqueKeysData.getInt(valuePos)==(stepIndex+1));

		SliceND uniqueKeysSlice = uniqueKeysSaver.getLastSlice();
		assertTrue(uniqueKeysSlice!=null);
		assertTrue(Arrays.equals(uniqueKeysSlice.getShape(), expectedShape));
		assertTrue(Arrays.equals(uniqueKeysSlice.getStart(), indices));
		assertTrue(Arrays.equals(uniqueKeysSlice.getStep(), expectedShape)); // all ones
		int[] stopIndices = Arrays.stream(indices).map(x -> x + 1).toArray(); 
		assertTrue(Arrays.equals(uniqueKeysSlice.getStop(), stopIndices));
		
		IDataset writtenToPointsData = pointsSaver.getLastWrittenData();
		assertTrue(writtenToPointsData!=null);
		assertTrue(Arrays.equals(writtenToPointsData.getShape(), expectedShape));
		assertTrue(getDType(writtenToPointsData)==Dataset.STRING);
		assertTrue(writtenToPointsData.getString(valuePos).equals(position.toString()));
		
		SliceND pointsSlice = pointsSaver.getLastSlice();
		assertTrue(pointsSlice!=null);
		assertTrue(Arrays.equals(pointsSlice.getShape(), expectedShape));
		assertTrue(Arrays.equals(pointsSlice.getStart(), indices));
		assertTrue(Arrays.equals(pointsSlice.getStep(), expectedShape));
		assertTrue(Arrays.equals(pointsSlice.getStop(), stopIndices));
	}
	
}
