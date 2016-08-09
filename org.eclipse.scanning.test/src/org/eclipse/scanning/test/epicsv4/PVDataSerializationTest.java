package org.eclipse.scanning.test.epicsv4;

import static org.junit.Assert.assertEquals;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.test.epicsv4.custommarshallers.TestCircularROIDeserialiser;
import org.eclipse.scanning.test.epicsv4.custommarshallers.TestCircularROISerialiser;
import org.eclipse.scanning.test.epicsv4.custommarshallers.TestGridModelDeserialiser;
import org.eclipse.scanning.test.epicsv4.custommarshallers.TestGridModelSerialiser;
import org.eclipse.scanning.test.epicsv4.custommarshallers.TestRectangularROIDeserialiser;
import org.eclipse.scanning.test.epicsv4.custommarshallers.TestRectangularROISerialiser;
import org.eclipse.scanning.test.epicsv4.custommarshallers.TestSpiralModelDeserialiser;
import org.eclipse.scanning.test.epicsv4.custommarshallers.TestSpiralModelSerialiser;
import org.eclipse.scanning.test.epicsv4.custommarshallers.TestStepModelDeserialiser;
import org.eclipse.scanning.test.epicsv4.custommarshallers.TestStepModelSerialiser;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvmarshaller.marshaller.PVMarshaller;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for serialisation into EPICS V4 structures for transmission over PVAccess
 * @author Matt Taylor
 *
 */
public class PVDataSerializationTest {

		private PVMarshaller marshaller;

		@Before
		public void create() throws Exception {
			marshaller = new PVMarshaller();
		}
		
		@Test
		public void TestCompoundModel1() throws Exception {

			CompoundModel model = new CompoundModel();
			model.setData(new SpiralModel("x", "y", 1, new BoundingBox(0, -5, 10, 5)), new CircularROI(2, 0, 0));
			
			marshaller.registerSerialiser(SpiralModel.class, new TestSpiralModelSerialiser());
			marshaller.registerDeserialiser("SpiralModel", new TestSpiralModelDeserialiser());
			marshaller.registerSerialiser(CircularROI.class, new TestCircularROISerialiser());
			marshaller.registerDeserialiser("CircularROI", new TestCircularROIDeserialiser());
			
			PVStructure pvStructure = marshaller.toPVStructure(model);
			
			CompoundModel ledom = marshaller.fromPVStructure(pvStructure, CompoundModel.class);
			
			assertEquals(model, ledom);
		}
		
		@Test
		public void testCompoundModel2() throws Exception {

			CompoundModel model = new CompoundModel();
			
			model.setModelsVarArgs(new StepModel("T", 290, 300, 1), new SpiralModel("x", "y", 1, new BoundingBox(0, -5, 10, 5)), new GridModel("fast", "slow"));
			model.setRegionsVarArgs(new ScanRegion(new CircularROI(2, 0, 0), "x", "y"), new ScanRegion(new RectangularROI(1,2,0), "fast", "slow"));

			marshaller.registerSerialiser(SpiralModel.class, new TestSpiralModelSerialiser());
			marshaller.registerDeserialiser("SpiralModel", new TestSpiralModelDeserialiser());
			marshaller.registerSerialiser(StepModel.class, new TestStepModelSerialiser());
			marshaller.registerDeserialiser("StepModel", new TestStepModelDeserialiser());
			marshaller.registerSerialiser(GridModel.class, new TestGridModelSerialiser());
			marshaller.registerDeserialiser("GridModel", new TestGridModelDeserialiser());
			marshaller.registerSerialiser(CircularROI.class, new TestCircularROISerialiser());
			marshaller.registerDeserialiser("CircularROI", new TestCircularROIDeserialiser());
			marshaller.registerSerialiser(RectangularROI.class, new TestRectangularROISerialiser());
			marshaller.registerDeserialiser("RectangularROI", new TestRectangularROIDeserialiser());
			
			PVStructure pvStructure = marshaller.toPVStructure(model);
			
			CompoundModel ledom = marshaller.fromPVStructure(pvStructure, CompoundModel.class);
			
			assertEquals(model, ledom);
		}
		
	}

