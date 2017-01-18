package org.eclipse.scanning.command.factory;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.StepModel;

class StepModelExpresser extends PyModelExpresser<StepModel> {

	@Override
	public String pyExpress(StepModel model, Collection<IROI> rois, boolean verbose) {
		
		if (rois != null && rois.size() > 0) throw new IllegalStateException("StepModels cannot be associated with ROIs.");
		
		// TODO Use StringBuilder
		return "step("
			+(verbose?"axis=":"")
			+"'"+model.getName()+"'"+", "
			+(verbose?"start=":"")
			+model.getStart()+", "
			+(verbose?"stop=":"")
			+model.getStop()+", "
			+(verbose?"step=":"")
			+model.getStep()
		+")";
	}

}
