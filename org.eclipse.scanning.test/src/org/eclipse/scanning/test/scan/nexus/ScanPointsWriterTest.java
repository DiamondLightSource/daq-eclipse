package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_SCAN_FINISHED;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_SCAN_RANK;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.FIELD_NAME_UNIQUE_KEYS;
import static org.eclipse.scanning.sequencer.nexus.ScanPointsWriter.GROUP_NAME_KEYS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.AbstractNexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.LazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.io.ILazySaver;
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
			addExternalFileName(name + ".nxs");
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
			addExternalFileName(EXTERNAL_FILE_NAME);
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
		expectedChunking[expectedChunking.length-1] = 8;

		// Act
		NXcollection solsticeScanCollection = scanPointsWriter.createNexusObject(scanInfo);
		
		// Assert
		assertTrue(solsticeScanCollection!=null);

		// assert scan finished dataset created correctly - value must be false
		DataNode scanFinishedDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_FINISHED);
		assertTrue(scanFinishedDataNode!=null);
		assertTrue(scanFinishedDataNode.getDataset()!=null && scanFinishedDataNode.getDataset() instanceof ILazyWriteableDataset);
		ILazyWriteableDataset scanFinishedDataset = (ILazyWriteableDataset) scanFinishedDataNode.getDataset();
		assertTrue(scanFinishedDataset.getRank()==1);
		assertTrue(Arrays.equals(scanFinishedDataset.getShape(), new int[] { 1 }));
		MockLazySaver scanFinishedSaver = new MockLazySaver();
		scanFinishedDataset.setSaver(scanFinishedSaver);
		
		// assert scan rank set correctly
		DataNode scanRankDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_RANK);
		assertEquals(scanRank, scanRankDataNode.getDataset().getSlice().getInt());
		
		// assert unique keys dataset created correctly
		NXcollection keysCollection = (NXcollection) solsticeScanCollection.getGroupNode(GROUP_NAME_KEYS);
		assertNotNull(keysCollection);
		
		DataNode uniqueKeysDataNode = keysCollection.getDataNode(FIELD_NAME_UNIQUE_KEYS);
		assertTrue(uniqueKeysDataNode!=null);
		assertTrue(uniqueKeysDataNode.getDataset()!=null && uniqueKeysDataNode.getDataset() instanceof ILazyWriteableDataset);
		ILazyWriteableDataset uniqueKeysDataset = (ILazyWriteableDataset) uniqueKeysDataNode.getDataset();
		assertTrue(uniqueKeysDataset.getRank()==scanRank);
		assertTrue(((LazyDataset) uniqueKeysDataset).getDType()==Dataset.INT32);
		assertTrue(Arrays.equals(uniqueKeysDataset.getChunking(), expectedChunking));
		MockLazySaver uniqueKeysSaver = new MockLazySaver();
		uniqueKeysDataset.setSaver(uniqueKeysSaver);
		
		// assert links to external nodes
		// TODO reinstate assertions about external links
//		keysCollection.getNumberOfNodelinks();
//		assertTrue(keysCollection.getSymbolicNode(EXTERNAL_FILE_NAME)!=null);
//		for (String positionerName : positionerNames) {
//			assertTrue(keysCollection.getSymbolicNode(positionerName + ".nxs")!=null);
//		}
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

		// Act
		NXcollection solsticeScanCollection = scanPointsWriter.createNexusObject(scanInfo);
		
		// Assert
		DataNode scanFinishedDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_FINISHED);
		ILazyWriteableDataset scanFinishedDataset = (ILazyWriteableDataset) scanFinishedDataNode.getDataset();
		MockLazySaver scanFinishedSaver = new MockLazySaver();
		scanFinishedDataset.setSaver(scanFinishedSaver);
		
		// assert unique keys dataset created correctly
		NXcollection keysCollection = (NXcollection) solsticeScanCollection.getGroupNode(GROUP_NAME_KEYS);
		assertNotNull(keysCollection);
		
		DataNode uniqueKeysDataNode = keysCollection.getDataNode(FIELD_NAME_UNIQUE_KEYS);
		ILazyWriteableDataset uniqueKeysDataset = (ILazyWriteableDataset) uniqueKeysDataNode.getDataset();
		MockLazySaver uniqueKeysSaver = new MockLazySaver();
		uniqueKeysDataset.setSaver(uniqueKeysSaver);
		
		// assert links to external nodes
		// TODO reinstate assertions about external links
//		keysCollection.getNumberOfNodelinks();
//		assertTrue(keysCollection.getSymbolicNode(EXTERNAL_FILE_NAME)!=null);
//		for (String positionerName : positionerNames) {
//			assertTrue(keysCollection.getSymbolicNode(positionerName + ".nxs")!=null);
//		}
		
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
		IDataset writtenToUniqueKeysData = uniqueKeysSaver.getLastWrittenData();
		assertTrue(writtenToUniqueKeysData!=null);
		int[] expectedShape = new int[scanInfo.getRank()];
		Arrays.fill(expectedShape, 1);
		assertTrue(Arrays.equals(writtenToUniqueKeysData.getShape(), expectedShape));
		assertTrue(DTypeUtils.getDType(writtenToUniqueKeysData)==Dataset.INT);
		int[] valuePos = new int[scanRank]; // all zeros
		assertTrue(writtenToUniqueKeysData.getInt(valuePos)==(stepIndex+1));

		SliceND uniqueKeysSlice = uniqueKeysSaver.getLastSlice();
		assertTrue(uniqueKeysSlice!=null);
		assertTrue(Arrays.equals(uniqueKeysSlice.getShape(), expectedShape));
		assertTrue(Arrays.equals(uniqueKeysSlice.getStart(), indices));
		assertTrue(Arrays.equals(uniqueKeysSlice.getStep(), expectedShape)); // all ones
		int[] stopIndices = Arrays.stream(indices).map(x -> x + 1).toArray(); 
		assertTrue(Arrays.equals(uniqueKeysSlice.getStop(), stopIndices));
	}
	
}
