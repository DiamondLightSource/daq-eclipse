package org.eclipse.scanning.server.servlet;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;

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
        return new ScanProcess(scanBean, response, isBlocking());
	}
}
