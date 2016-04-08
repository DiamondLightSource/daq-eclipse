package org.eclipse.scanning.example.xcen.ui;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.scanning.api.event.IEventService;
import org.osgi.service.component.ComponentContext;

public class XcenServices {

	private ILoaderService       loaderService;
	private IEventService        eventService;

	public ILoaderService getLoaderService() {
		return loaderService;
	}

	public void setLoaderService(ILoaderService loaderService) {
		this.loaderService = loaderService;
	}
	
	private static XcenServices current;

	public void start(ComponentContext context) {
		current = this;
	}
	
	public void stop() {
		current = null;
	}

	public static XcenServices getCurrent() {
		return current;
	}

	public int hashCode() {
		return eventService.hashCode();
	}

	public boolean equals(Object obj) {
		return eventService.equals(obj);
	}

	public String toString() {
		return eventService.toString();
	}

	public IEventService getEventService() {
		return eventService;
	}

	public void setEventService(IEventService eventService) {
		this.eventService = eventService;
	}

}
