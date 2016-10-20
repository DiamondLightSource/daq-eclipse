package org.eclipse.scanning.points.classregistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;
import org.eclipse.scanning.api.device.models.ClusterProcessingModel;
import org.eclipse.scanning.api.device.models.ProcessingModel;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.KillBean;
import org.eclipse.scanning.api.event.alive.PauseBean;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.scan.DeviceAction;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.PositionRequestType;
import org.eclipse.scanning.api.event.scan.PositionerRequest;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.AdministratorMessage;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.points.EmptyPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CollatedStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.EmptyModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.RandomOffsetGridModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.SinglePointModel;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.AxisConfiguration;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.event.Location;
import org.eclipse.scanning.api.scan.ui.ControlEnumNode;
import org.eclipse.scanning.api.scan.ui.ControlFileNode;
import org.eclipse.scanning.api.scan.ui.ControlGroup;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.api.script.ScriptLanguage;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.api.script.ScriptResponse;

/**
 * 
 * The registry is here because it makes dependencies on DAWNSCI
 * in order to link in beans to the marshaller. 
 * 
 * @author Martin Gaughran
 * @author Matthew Gerring
 *
 */
public class ScanningAPIClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> idToClassMap;
	static {
		Map<String, Class<?>> tmp = new HashMap<String, Class<?>>();
		
		// event.scan
		registerClass(tmp, DeviceAction.class);
		registerClass(tmp, DeviceInformation.class);
		registerClass(tmp, DeviceRequest.class);
		registerClass(tmp, DeviceState.class);
		registerClass(tmp, PositionerRequest.class);
		registerClass(tmp, PositionRequestType.class);
		registerClass(tmp, ScanBean.class);
		registerClass(tmp, ScanEvent.class);
		registerClass(tmp, ScanRequest.class);
		
		// points
		registerClass(tmp, EmptyPosition.class);
		registerClass(tmp, MapPosition.class);
		registerClass(tmp, Point.class);
		registerClass(tmp, Scalar.class);
		
		// points.models
		registerClass(tmp, ArrayModel.class);
		registerClass(tmp, BoundingBox.class);
		registerClass(tmp, BoundingLine.class);
		registerClass(tmp, CollatedStepModel.class);
		registerClass(tmp, CompoundModel.class);
		registerClass(tmp, EmptyModel.class);
		registerClass(tmp, GridModel.class);
		registerClass(tmp, LissajousModel.class);
		registerClass(tmp, OneDEqualSpacingModel.class);
		registerClass(tmp, OneDStepModel.class);
		registerClass(tmp, RandomOffsetGridModel.class);
		registerClass(tmp, RasterModel.class);
		registerClass(tmp, ScanRegion.class);
		registerClass(tmp, SinglePointModel.class);
		registerClass(tmp, SpiralModel.class);
		registerClass(tmp, StepModel.class);
		
		// scan.ui
		registerClass(tmp, ControlEnumNode.class);
		registerClass(tmp, ControlFileNode.class);
		registerClass(tmp, ControlGroup.class);
		registerClass(tmp, ControlNode.class);
		registerClass(tmp, ControlTree.class);
		registerClass(tmp, AxisConfiguration.class);
		
		// event.alive
		registerClass(tmp, HeartbeatBean.class);
		registerClass(tmp, KillBean.class);
		registerClass(tmp, PauseBean.class);
		
		// event.status
		registerClass(tmp, Status.class);
		registerClass(tmp, AdministratorMessage.class);
		registerClass(tmp, StatusBean.class);
		
		// event.queues.beans
		registerClass(tmp, Queueable.class);
		registerClass(tmp, QueueAtom.class);
		registerClass(tmp, QueueBean.class);
		
		// malcolm.event
		registerClass(tmp, MalcolmEventBean.class);

		// api.scan
		registerClass(tmp, PositionEvent.class);

		// scan.event
		registerClass(tmp, Location.class);
		
		// device.models
		registerClass(tmp, ProcessingModel.class);
		registerClass(tmp, ClusterProcessingModel.class);

		// script
		registerClass(tmp, ScriptLanguage.class);
		registerClass(tmp, ScriptRequest.class);
		registerClass(tmp, ScriptResponse.class);
		

		idToClassMap = Collections.unmodifiableMap(tmp);
	}
	
	private static void registerClass(Map<String, Class<?>> map, Class<?> clazz) {
		map.put(clazz.getSimpleName(), clazz);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return idToClassMap;
	}
}
