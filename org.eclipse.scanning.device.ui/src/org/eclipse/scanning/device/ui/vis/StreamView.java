package org.eclipse.scanning.device.ui.vis;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A view which displays a view of a stream, either MJpeg or Epics array.
 * 
 * @author Matthew Taylor
 *
 */
public class StreamView extends ViewPart implements IAdaptable {
	
	public static final String ID = "org.eclipse.scanning.device.ui.vis.StreamView";
	
	private static final long DEFAULT_SLEEP_TIME = 50; // ms i.e. 20 fps
	
	private static final int DEFAULT_CACHE_SIZE = 3; // frames
	
	private static final Logger logger = LoggerFactory.getLogger(StreamView.class);
			
	protected IPlottingSystem<Composite> system;
	
	protected IRemoteDatasetService service;
	
	protected IDatasetConnector datasetConenctor;
	
	public StreamView() {
		try {
			service = ServiceHolder.getRemoteDatasetService();
			
			IPlottingService plottingService = ServiceHolder.getPlottingService();
			system = plottingService.createPlottingSystem();
			
		} catch (Exception ne) {
			logger.error("Unable to make plotting system", ne);
			system = null; // It creates the view but there will be no plotting system 
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		system.createPlotPart(parent, getPartName(), getViewSite().getActionBars(), PlotType.IMAGE, this); 
		
		for (IAxis axis : system.getAxes()) {
			axis.setVisible(false);
		}
				
		try {
			
			URL url = new URL("http://ws137.diamond.ac.uk:8080/ADSIM.mjpg.mjpg"); // TODO hard coded for now. Replace with user setting.
			
			// TODO determine if colour or grayscale somehow, and create different dataset type depending on that
			datasetConenctor = service.createGrayScaleMJPGDataset(url, DEFAULT_SLEEP_TIME, DEFAULT_CACHE_SIZE);

			datasetConenctor.connect();
			
			IDataset image = datasetConenctor.getSlice();
			if (image.getShape()==null || image.getShape().length==0) {
				throw new IllegalArgumentException("There is no data to prepare, is the device turned on?");
			}
			
			IImageTrace trace = (IImageTrace)system.createPlot2D(image, null, null);
			
			// Settings to increase the render speed
			trace.setDownsampleType(DownsampleType.POINT);
			trace.setRescaleHistogram(false);
			
			// Fix the aspect ratio as is typically required for visible cameras
			system.setKeepAspect(true);
			
			// Disable auto rescale as the live stream is constantly refreshing
			system.setRescale(false);
			
			datasetConenctor.addDataListener(new IDataListener() {	
				int[] oldShape;
				@Override
				public void dataChangePerformed(DataEvent evt) {
					if (!Arrays.equals(evt.getShape(), oldShape)) {
						oldShape = evt.getShape();
						// Need to be in the UI thread to do rescaling
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								system.autoscaleAxes();
								trace.rehistogram();
							}
						});
					}
				}
			});

		} catch (MalformedURLException e) {
			// TODO Handle URL exception
			//e.printStackTrace();
		} catch (Exception e) {
			// TODO handle exception
			//e.printStackTrace();
		}
		 
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public  <T> T getAdapter(Class<T> adapter) {
		if (IPlottingSystem.class == adapter) return (T)system;
		if (IToolPageSystem.class == adapter) return system.getAdapter(adapter);
		return super.getAdapter(adapter);
	}

	@Override
	public void setFocus() {
		if (system!=null) system.setFocus();
	}

	@Override
	public void dispose() {
		if (system!=null) system.dispose();
		if (datasetConenctor != null) {
			try {
				datasetConenctor.disconnect();
				datasetConenctor = null;
			} catch (Exception e) {
				logger.error("Error disconnecting remote data stream", e);
			}
		}
		super.dispose();
	}

}
