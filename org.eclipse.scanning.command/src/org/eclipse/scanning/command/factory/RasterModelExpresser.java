package org.eclipse.scanning.command.factory;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.command.PyExpressionNotImplementedException;

class RasterModelExpresser extends PyModelExpresser<RasterModel> {

	String pyExpress(RasterModel model, Collection<IROI> rois, boolean verbose) throws Exception {
		return "grid("
				+(verbose?"axes=":"")+"("
					+"'"+model.getFastAxisName()+"'"+", "
					+"'"+model.getSlowAxisName()+"'"+"), "
				+(verbose?"start=":"")+"("
					+model.getBoundingBox().getFastAxisStart()+", "
					+model.getBoundingBox().getSlowAxisStart()+"), "
				+(verbose?"stop=":"")+"("
					+(model.getBoundingBox().getFastAxisStart()
						+model.getBoundingBox().getFastAxisLength())+", "
					+(model.getBoundingBox().getSlowAxisStart()
						+model.getBoundingBox().getSlowAxisLength())+"), "
				+(verbose?"step=":"")+"("
					+model.getFastAxisStep()+", "
					+model.getSlowAxisStep()+")"
				+(verbose
					? (", snake="+(model.isSnake()?"True":"False"))
					: (model.isSnake()?"":", snake=False"))
				+((rois == null)
					? ""
					: ((rois.size() == 0)
						? ""
						: ", roi="+factory.pyExpress(rois, verbose)))
			+")";
	}

}
