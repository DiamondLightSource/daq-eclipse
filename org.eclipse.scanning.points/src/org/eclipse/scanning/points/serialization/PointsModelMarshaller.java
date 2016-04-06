package org.eclipse.scanning.points.serialization;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshaller;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPosition;

@SuppressWarnings("unchecked")
public class PointsModelMarshaller implements IMarshaller {

	@Override
	public Class<IPosition> getObjectClass() {
		return IPosition.class;
	}

	@Override
	public Class<PositionSerializer> getSerializerClass() {
		return PositionSerializer.class;
	}

	@Override
	public Class<PositionDeserializer> getDeserializerClass() {
		return PositionDeserializer.class;
	}
	
	@Override
	public Class<ScanRequest> getMixinAnnotationType() {
		return ScanRequest.class;
	}
	
	@Override
	public Class<ScanRequestMixIn> getMixinAnnotationClass() {
		return ScanRequestMixIn.class;
	}

}
