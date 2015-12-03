package org.eclipse.scanning.sequencer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.scan.IScannableConnectorService;
import org.eclipse.scanning.api.scan.IScanner;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.osgi.service.component.ComponentContext;

public class ScannerServiceImpl implements IScanningService {
	
	private static IScannableConnectorService scanningConnectorService;

	private static final Map<Class<?>, Class<? extends IScanner<?>>> scanners;
	
	// Use a factory pattern to register the types.
	// This pattern can always be extended by extension points
	// to allow point generators to be dynamically registered. 
	static {
		System.out.println("Starting scanning service");
		Map<Class<?>, Class<? extends IScanner<?>>> tmp = new HashMap<>(7);
		tmp.put(ScanModel.class,         DescretePointScanner.class);
		
		scanners = Collections.unmodifiableMap(tmp);
	}
	
	@Override
	public <T> IScanner<T> createScanner(T model) throws ScanningException {
        return createScanner(model, null);
	}

	@Override
	public <T> IScanner<T> createScanner(T model, IScannableConnectorService sservice) throws ScanningException {
		
		if (sservice==null) sservice = ScannerServiceImpl.scanningConnectorService;
		
		try {
			final IScanner<T> scanner = (IScanner<T>)scanners.get(model.getClass()).newInstance();
			scanner.configure(model);
			return scanner;
			
		} catch (ScanningException s) {
			throw s;
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

	public static IScannableConnectorService getScanningConnectorService() {
		return scanningConnectorService;
	}

	public static void setScanningConnectorService(IScannableConnectorService connectorService) {
		ScannerServiceImpl.scanningConnectorService = connectorService;
	}
	

	private static ComponentContext context;

	public void start(ComponentContext context) {
		ScannerServiceImpl.context = context;
	}
	
	public void stop() {
		context = null;
	}

}
