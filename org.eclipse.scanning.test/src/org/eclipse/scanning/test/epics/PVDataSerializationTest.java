package org.eclipse.scanning.test.epics;

import static org.junit.Assert.assertEquals;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.test.epics.custommarshallers.TestBoundingBoxDeserialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestBoundingBoxSerialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestCircularROIDeserialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestCircularROISerialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestGridModelDeserialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestGridModelSerialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestRectangularROIDeserialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestRectangularROISerialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestSpiralModelDeserialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestSpiralModelSerialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestStepModelDeserialiser;
import org.eclipse.scanning.test.epics.custommarshallers.TestStepModelSerialiser;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Union;
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
		
		/**
		 * Test Compound Model is serialised into correct format expected by Malcom
		 * @throws Exception
		 */
		@Test
		public void TestCompoundModelSerialisation() throws Exception {

			// Create the expected PVStructure
			FieldCreate fieldCreate = FieldFactory.getFieldCreate();

			PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
			
			Structure boundingBoxStructure = fieldCreate.createFieldBuilder().
					add("fastAxisStart", ScalarType.pvDouble).
					add("slowAxisStart", ScalarType.pvDouble).
					add("fastAxisLength", ScalarType.pvDouble).
					add("slowaxisLength", ScalarType.pvDouble).
					createStructure();
			
			Structure spiralModelStructure = fieldCreate.createFieldBuilder().
					add("name", ScalarType.pvString).
					add("boundingBox", boundingBoxStructure).
					add("fastAxisName", ScalarType.pvString).
					add("slowAxisName", ScalarType.pvString).
					add("scale", ScalarType.pvDouble).
					setId("SpiralModel").
					createStructure();
			
			Structure circularRoistructure = fieldCreate.createFieldBuilder().
					add("radius", ScalarType.pvDouble).
					add("angle", ScalarType.pvDouble).
					addArray("point", ScalarType.pvDouble).
					setId("CircularROI").
					createStructure();
			
			Structure regionStructure = fieldCreate.createFieldBuilder().
					add("roi", circularRoistructure).
					addArray("scannables", ScalarType.pvString).
					createStructure();
			
			Union union = fieldCreate.createVariantUnion();
			
			Structure compoundModelstructure = fieldCreate.createFieldBuilder().
					addArray("models", union).
					addArray("regions", union).
					createStructure();
			
			PVStructure expectedPVStructure = pvDataCreate.createPVStructure(compoundModelstructure);
			
			// Create the models section
			PVUnionArray pvModelsUnionArrayValue = expectedPVStructure.getSubField(PVUnionArray.class, "models");
			
			PVStructure modelPVStructure = pvDataCreate.createPVStructure(spiralModelStructure);

			PVString nameValue = modelPVStructure.getSubField(PVString.class, "name");
			nameValue.put("Fermat Spiral");
			
			PVString fastAxisNameValue = modelPVStructure.getSubField(PVString.class, "fastAxisName");
			fastAxisNameValue.put("x");
			
			PVString slowAxisNameValue = modelPVStructure.getSubField(PVString.class, "slowAxisName");
			slowAxisNameValue.put("y");
			
			PVDouble scaleValue = modelPVStructure.getSubField(PVDouble.class, "scale");
			scaleValue.put(1);
			
			PVStructure bbPVStructure = modelPVStructure.getSubField(PVStructure.class, "boundingBox");

			PVDouble fastAxisStartValue = bbPVStructure.getSubField(PVDouble.class, "fastAxisStart");
			fastAxisStartValue.put(0);
			PVDouble slowAxisStartValue = bbPVStructure.getSubField(PVDouble.class, "slowAxisStart");
			slowAxisStartValue.put(-5);
			PVDouble fastAxisLengthValue = bbPVStructure.getSubField(PVDouble.class, "fastAxisLength");
			fastAxisLengthValue.put(10);
			PVDouble slowaxisLengthValue = bbPVStructure.getSubField(PVDouble.class, "slowaxisLength");
			slowaxisLengthValue.put(5);
			
			PVUnion modelArray[] = new PVUnion[1];
			
			modelArray[0] = pvDataCreate.createPVUnion(union);
			modelArray[0].set(modelPVStructure);
			
			pvModelsUnionArrayValue.put(0, 1, modelArray, 0);
			
			// Create the regions section
			PVUnionArray pvRegionsUnionArrayValue = expectedPVStructure.getSubField(PVUnionArray.class, "regions");

			PVStructure regionPVStructure = pvDataCreate.createPVStructure(regionStructure);
			
			PVStructure crPVStructure = regionPVStructure.getSubField(PVStructure.class, "roi");
			PVDouble crrValue = crPVStructure.getSubField(PVDouble.class, "radius");
			crrValue.put(2);
			PVDouble craValue = crPVStructure.getSubField(PVDouble.class, "angle");
			craValue.put(0);
			PVDoubleArray crpValue = crPVStructure.getSubField(PVDoubleArray.class, "point");
			double[] crPoint = {0, 0};
			crpValue.put(0, 2, crPoint, 0);

			PVStringArray scannablesValue = regionPVStructure.getSubField(PVStringArray.class, "scannables");
			String[] scannablesPoint = {"x", "y"};
			scannablesValue.put(0, 2, scannablesPoint, 0);
			
			PVUnion regionArray[] = new PVUnion[1];
			
			regionArray[0] = pvDataCreate.createPVUnion(union);
			regionArray[0].set(regionPVStructure);
			
			pvRegionsUnionArrayValue.put(0, 1, regionArray, 0);
			
			// Create the test model
			CompoundModel model = new CompoundModel();
			model.setData(new SpiralModel("x", "y", 1, new BoundingBox(0, -5, 10, 5)), new CircularROI(2, 0, 0));
			
			// Set up the custom serialisers and deserialisers
			marshaller.registerSerialiser(SpiralModel.class, new TestSpiralModelSerialiser());
			marshaller.registerDeserialiser("SpiralModel", new TestSpiralModelDeserialiser());
			marshaller.registerSerialiser(CircularROI.class, new TestCircularROISerialiser());
			marshaller.registerDeserialiser("CircularROI", new TestCircularROIDeserialiser());
			marshaller.registerSerialiser(BoundingBox.class, new TestBoundingBoxSerialiser());
			marshaller.registerDeserialiser("BoundingBox", new TestBoundingBoxDeserialiser());
			
			// Serialise the model
			PVStructure pvStructure = marshaller.toPVStructure(model);
			
			// Compare
			assertEquals(expectedPVStructure.getStructure(), pvStructure.getStructure());
			assertEquals(expectedPVStructure, pvStructure);
		}
		
		@Test
		public void TestCompoundModel1() throws Exception {

			CompoundModel model = new CompoundModel();
			model.setData(new SpiralModel("x", "y", 1, new BoundingBox(0, -5, 10, 5)), new CircularROI(2, 0, 0));
			
			marshaller.registerSerialiser(SpiralModel.class, new TestSpiralModelSerialiser());
			marshaller.registerDeserialiser("SpiralModel", new TestSpiralModelDeserialiser());
			marshaller.registerSerialiser(CircularROI.class, new TestCircularROISerialiser());
			marshaller.registerDeserialiser("CircularROI", new TestCircularROIDeserialiser());
			marshaller.registerSerialiser(BoundingBox.class, new TestBoundingBoxSerialiser());
			marshaller.registerDeserialiser("BoundingBox", new TestBoundingBoxDeserialiser());
			
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
			marshaller.registerSerialiser(BoundingBox.class, new TestBoundingBoxSerialiser());
			marshaller.registerDeserialiser("BoundingBox", new TestBoundingBoxDeserialiser());
			
			PVStructure pvStructure = marshaller.toPVStructure(model);
			
			CompoundModel ledom = marshaller.fromPVStructure(pvStructure, CompoundModel.class);
			
			assertEquals(model, ledom);
		}
		
	}

