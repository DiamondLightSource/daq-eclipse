package org.eclipse.scanning.command.factory;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.RepeatedPointModel;

class RepeatedPointExpresser extends PyModelExpresser<RepeatedPointModel> {

	@Override
	public String pyExpress(RepeatedPointModel model, Collection<IROI> rois, boolean verbose) {
		
		if (rois != null && rois.size() > 0) throw new IllegalStateException("RepeatedPointModel cannot be associated with ROIs.");
		
		// TODO Use StringBuilder
		return "repeat("
			+(verbose?"axis=":"")
			+"'"+model.getName()+"'"+", "
			+(verbose?"count=":"")
			+model.getCount()+", "
			+(verbose?"value=":"")
			+model.getValue()+", "
			+(verbose?"sleep=":"")
			+model.getSleep()
		+")";
	}

}
