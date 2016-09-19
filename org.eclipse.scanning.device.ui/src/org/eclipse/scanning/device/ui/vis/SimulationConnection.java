package org.eclipse.scanning.device.ui.vis;

import java.net.URL;

import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.streams.AbstractStreamConnection;
import org.eclipse.scanning.api.streams.StreamConnectionException;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.widgets.Display;

public class SimulationConnection extends AbstractStreamConnection<IDataset> {

	// Data for connecting.
	private long DEFAULT_SLEEP_TIME = 50; // ms i.e. 20 fps
	private int DEFAULT_CACHE_SIZE = 3; // frames
	private String currentURI = "http://ws137.diamond.ac.uk:8080/ADSIM.mjpg.mjpg";

	// Services
	protected IRemoteDatasetService service;

	// Data
	protected IDatasetConnector     datasetConenctor;

	public SimulationConnection() {
		service = ServiceHolder.getRemoteDatasetService();
	}
	
	@Override
	public IDataset connect() throws StreamConnectionException {
		
		URL url = null;
		try {
			url = new URL(currentURI); // TODO hard coded for now. Replace with user setting.
						
			// TODO determine if colour or grayscale somehow, and create different dataset type depending on that
			datasetConenctor = service.createGrayScaleMJPGDataset(url, DEFAULT_SLEEP_TIME, DEFAULT_CACHE_SIZE);
			datasetConenctor.connect();
			
			IDataset image = datasetConenctor.getSlice();
			if (image.getShape()==null || image.getShape().length==0) {
				throw new IllegalArgumentException("There is no data to prepare, is the device turned on?");
			}
			
			return image;

		} catch (Exception ne) {
			throw new StreamConnectionException(ne);
		}
	}
		
	@Override
	public void disconnect()  throws StreamConnectionException {
		if (datasetConenctor!=null) {
			try {
				datasetConenctor.disconnect();
			} catch (DatasetException e) {
				throw new StreamConnectionException(e);
			}
		}
	}
	
	@Override
	public void configure() throws StreamConnectionException {
		MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Matthew Taylor TODO", "Please write a form to set the three things for simulation: URI, sleep time, cache size");
		// TODO Show a form setting URI, sleep time, cache size.
	}
}
