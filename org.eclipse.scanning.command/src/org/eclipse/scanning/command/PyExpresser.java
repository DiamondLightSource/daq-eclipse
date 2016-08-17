package org.eclipse.scanning.command;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.StepModel;


/**
 * This class contains functions which express (i.e.
 * generate Python expressions for) ScanRequests and
 * other associated Java objects.
 *
 * The returned Python expressions are for use with
 * mapping_scan_commands.py (located in the scripts
 * directory of this package).
 */
public class PyExpresser {
	
	private static IPointGeneratorService pointGeneratorService;

	final public static String pyExpress(
			ScanRequest<IROI> request,
			boolean verbose)
					throws Exception {

		String fragment = "scan_request(";
		boolean scanRequestPartiallyWritten = false;

		if (request.getCompoundModel().getModels() != null
				&& request.getCompoundModel().getModels().size() > 0) {

			if (verbose) { fragment += "path="; }

			if (verbose || request.getCompoundModel().getModels().size() > 1) fragment += "[";
			boolean listPartiallyWritten = false;

			for (Object model : request.getCompoundModel().getModels()) {  // Order is important.
				if (listPartiallyWritten) fragment += ", ";
				Collection<IROI> rois = pointGeneratorService.findRegions(request.getCompoundModel(), model);
				fragment += pyExpress(model, rois, verbose);
				listPartiallyWritten |= true;
			}

			if (verbose || request.getCompoundModel().getModels().size() > 1) fragment += "]";
			scanRequestPartiallyWritten |= true;
		}

		if (request.getMonitorNames() != null
				&& request.getMonitorNames().size() > 0) {

			if (scanRequestPartiallyWritten) fragment += ", ";
			if (verbose || !scanRequestPartiallyWritten) { fragment += "mon="; }

			if (verbose || request.getMonitorNames().size() > 1) fragment += "[";
			boolean listPartiallyWritten = false;

			for (String monitorName : request.getMonitorNames()) {
				if (listPartiallyWritten) fragment += ", ";
				fragment += "'"+monitorName+"'";
				listPartiallyWritten |= true;
			}

			if (verbose || request.getMonitorNames().size() > 1) fragment += "]";
			scanRequestPartiallyWritten |= true;
		}

		fragment += ")";
		return fragment;
	}

	final private static String pyExpress(
			Object model,
			Collection<IROI> rois,
			boolean verbose)
					throws PyExpressionNotImplementedException {

		if (model instanceof StepModel) {
			if (rois != null && rois.size() > 0)
				throw new IllegalStateException(
						"StepModels cannot be associated with ROIs.");
			return pyExpress((StepModel) model, verbose);
		}

		else if (model instanceof GridModel)
			return pyExpress((GridModel) model, rois, verbose);

		else if (model instanceof RasterModel)
			return pyExpress((RasterModel) model, rois, verbose);

		else if (model instanceof ArrayModel) {
			if (rois != null && rois.size() > 0)
				throw new IllegalStateException(
						"ArrayModels cannot be associated with ROIs.");
			return pyExpress((ArrayModel) model, verbose);
		}

		else throw new PyExpressionNotImplementedException();
	}

	final public static String pyExpress(StepModel model, boolean verbose) {
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

	final public static String pyExpress(
			GridModel model,
			Collection<IROI> rois,
			boolean verbose)
					throws PyExpressionNotImplementedException {
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
					+"count=("
						+model.getFastAxisPoints()+", "
						+model.getSlowAxisPoints()+")"
					+(verbose
						? (", snake="+(model.isSnake()?"True":"False"))
						: (model.isSnake()?"":", snake=False"))
					+((rois == null)
						? ""
						: ((rois.size() == 0)
							? ""
							: (", roi="+pyExpress(rois, verbose))))
				+")";
	}

	final public static String pyExpress(
			RasterModel model,
			Collection<IROI> rois,
			boolean verbose)
					throws PyExpressionNotImplementedException {
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
							: ", roi="+pyExpress(rois, verbose)))
				+")";
	}

	final public static String pyExpress(
			ArrayModel model,
			boolean verbose) {

		if (model.getPositions().length == 1 && !verbose)
			return "val('"+model.getName()+"', "+model.getPositions()[0]+")";

		String fragment =
				"array("
					+(verbose?"axis=":"")+"'"+model.getName()+"'"+", "
					+(verbose?"values=":"")+"[";
		boolean listPartiallyWritten = false;

		for (double position : model.getPositions()) {
			if (listPartiallyWritten) fragment += ", ";
			fragment += position;
			listPartiallyWritten |= true;
		}

		fragment += "])";
		return fragment;
	}

	final private static String pyExpress(Collection<IROI> rois, boolean verbose)
			throws PyExpressionNotImplementedException {

		if (rois.size() == 0)
			throw new PyExpressionNotImplementedException();

		else if (rois.size() == 1 && !verbose)
			return pyExpress(rois.iterator().next(), verbose);

		else {
			String fragment = "[";
			boolean listPartiallyWritten = false;

			for (IROI roi : rois) {
				if (listPartiallyWritten) fragment += ", ";
				fragment += pyExpress(roi, verbose);
				listPartiallyWritten |= true;
			}

			fragment += "]";
			return fragment;
		}
	}

	final private static String pyExpress(IROI roi, boolean verbose)
			throws PyExpressionNotImplementedException {

		if (roi instanceof CircularROI)
			return pyExpress((CircularROI) roi, verbose);

		else if (roi instanceof RectangularROI)
			return pyExpress((RectangularROI) roi, verbose);

		else if (roi instanceof PolygonalROI)
			return pyExpress((PolygonalROI) roi, verbose);

		else throw new PyExpressionNotImplementedException();
	}

	final private static String pyExpress(CircularROI croi, boolean verbose) {
		return "circ("
					+(verbose?"origin=":"")
						+"("+croi.getCentre()[0]+", "+croi.getCentre()[1]+"), "
					+(verbose?"radius=":"")+croi.getRadius()
				+")";
	}

	final private static String pyExpress(RectangularROI rroi, boolean verbose) {
		return "rect("
					+(verbose?"origin=":"")+"("
						+rroi.getPointX()+", "+rroi.getPointY()
					+")"
					+(verbose?"size=":"")+"("
						+rroi.getLengths()[0]+", "+rroi.getLengths()[1]
					+")"
				+")";
	}

	final private static String pyExpress(PolygonalROI proi, boolean verbose) {
		String fragment = "poly(";

		boolean pointListPartiallyWritten = false;
		for (IROI p : proi.getPoints()) {
			if (pointListPartiallyWritten) fragment += ", ";
			fragment += "("+((PointROI) p).getPointX()+", "+((PointROI) p).getPointY()+")";
		}

		fragment += ")";
		return fragment;
	}

	public static IPointGeneratorService getPointGeneratorService() {
		return pointGeneratorService;
	}

	public static void setPointGeneratorService(IPointGeneratorService pointGeneratorService) {
		PyExpresser.pointGeneratorService = pointGeneratorService;
	}

}
