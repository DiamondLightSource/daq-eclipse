package org.eclipse.scanning.server.servlet;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.process.IPreprocessingService;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.api.scan.process.ProcessingException;

/**
 * A servlet to do any scan type based on the information provided
 * in a ScanBean.
 * 
     Spring config started, for instance:
    <pre>
    
    {@literal <bean id="scanServlet" class="org.eclipse.scanning.server.servlet.ScanServlet" init-method="connect">}
    {@literal    <property name="broker"      value="tcp://p45-control:61616" />}
    {@literal    <property name="submitQueue" value="uk.ac.diamond.p45.submitQueue" />}
    {@literal    <property name="statusSet"   value="uk.ac.diamond.p45.statusSet"   />}
    {@literal    <property name="statusTopic" value="uk.ac.diamond.p45.statusTopic" />}
    {@literal    <property name="durable"     value="true" />}
    {@literal </bean>}
     
    </pre>

 * 
 * @author Matthew Gerring
 *
 */
public class ScanServlet extends AbstractConsumerServlet<ScanBean> {
	

	@Override
	public ScanProcess createProcess(ScanBean scanBean, IPublisher<ScanBean> response) throws EventException {
		
		if (scanBean.getScanRequest()==null) throw new EventException("The scan must include a request to run something!");
		preprocess(scanBean);
        return new ScanProcess(scanBean, response, isBlocking());
	}

	private void preprocess(ScanBean scanBean) throws ProcessingException {
		
		IPreprocessingService service = Services.getPreprocessingService();
		if (service==null) return;

		// General name available to all
		String preprocessorName = System.getProperty("org.eclipse.scanning.api.preprocessor.name");
		
		// DLS specific name, not recommended to use for new products.
		if (preprocessorName==null) preprocessorName = System.getenv("BEAMLINE");               
		// DLS specific name, not recommended to use for new products.
		if (preprocessorName==null) preprocessorName = System.getProperty("gda.beamline.name"); 
		
		if (preprocessorName==null) return;
		
		IPreprocessor processor = service.getPreprocessor(preprocessorName);
		if (processor == null) return;
		
		@SuppressWarnings("unchecked")
		ScanRequest<?> req = processor.preprocess(scanBean.getScanRequest());
		scanBean.setScanRequest(req);
	}
}
