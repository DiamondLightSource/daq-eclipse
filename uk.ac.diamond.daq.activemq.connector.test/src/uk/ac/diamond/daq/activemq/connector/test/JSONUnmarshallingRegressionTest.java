package uk.ac.diamond.daq.activemq.connector.test;

import org.dawnsci.commandserver.mx.beans.ProjectBean;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Regression tests to ensure beans serialized by old versions of the connector can still be deserialized correctly.
 *
 * @author Colin Palmer
 *
 */
public class JSONUnmarshallingRegressionTest {

	// An example of a bean used by Xia2 which could be sent by another process and must deserialize correctly in current version
	private String jsonForProjectBean = "{\"status\":\"COMPLETE\",\"name\":\"X1_weak_M1S1_1 - X1_weak_M1S1_1\",\"message\":\"Xia2 run completed normally\",\"percentComplete\":100.0,\"userName\":\"awa25\",\"hostName\":\"cs04r-sc-vserv-45.diamond.ac.uk\",\"runDirectory\":\"/dls/i03/data/2016/cm14451-1/processed/tmp/2016-01-27/fake085224/MultiCrystal_1\",\"uniqueId\":\"1453910139320_94ed2a2b-997e-4dbc-ad6e-0c3c04bb2c82\",\"submissionTime\":1453910139340,\"properties\":null,\"projectName\":\"MultiCrystalRerun\",\"cystalName\":\"fake085224\",\"sweeps\":[{\"name\":\"X1_weak_M1S1_1\",\"sessionId\":\"55167\",\"dataCollectionId\":\"1007379\",\"imageDirectory\":\"/dls/i03/data/2016/cm14451-1/tmp/2016-01-27/fake085224/\",\"firstImageName\":\"X1_weak_M1S1_1_0001.cbf\",\"start\":1,\"end\":900,\"wavelength\":0.979493,\"xBeam\":212.51,\"yBeam\":219.98,\"resolution\":null}],\"wavelength\":\"NaN\",\"commandLineSwitches\":\"\",\"anomalous\":true,\"spaceGroup\":null,\"unitCell\":null,\"resolution\":null}";

	private ActivemqConnectorService marshaller;

	@Before
	public void setUp() throws Exception {

		marshaller = new ActivemqConnectorService();
	}

	@After
	public void tearDown() throws Exception {
		marshaller = null;
	}

	@Test
	public void testProjectBeanDeserialization() throws Exception {
		marshaller.unmarshal(jsonForProjectBean, ProjectBean.class);
	}

	@Test(expected = JsonMappingException.class)
	public void testProjectBeanDeserializationWithWrongType() throws Exception {
		marshaller.unmarshal(jsonForProjectBean, StatusBean.class);
	}

	// TODO	remove this - just for experimenting!
	@Test
	public void testProjectBeanSerialization() throws Exception {
		ProjectBean bean = marshaller.unmarshal(jsonForProjectBean, ProjectBean.class);
		String json = marshaller.marshal(bean);
		System.out.println(json);
	}
}