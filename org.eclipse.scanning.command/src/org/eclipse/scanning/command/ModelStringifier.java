package org.eclipse.scanning.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.StepModel;


public class ModelStringifier {
	// This class uses introspection because that seems to be the only way to access
	// model fields in a systematic way.

	// The schema below must be manually kept up to date with the Python syntax.
	//
	// TODO: What would be really cool is if we imported this schema from Python
	// and, according to the schema, generated the required Python functions.
	//
	// As it stands at the moment, we have tightly coupled information spread across
	// three sources:
	// - the IScanPathModels,
	// - scan_syntax.py,
	// - this class (ModelStringifier).
	// If scan_syntax.py imported this schema then we would have reduced the number
	// of authoritative sources from three to two. Theoretically that should be easy,
	// but it's made difficult by various inconsistencies in the models, such as:
	// - the setter for snaking is setSnake, but the getter is isSnake (not getSnake),
	// - the setter xStart is setxStart, not setXStart, etc.,
	// - ArrayModel.setPositions takes a varargs argument (unlike other model setters),
	// - GridModel doesn't encapsulate BoundingBox,
	// - IScanPathModel doesn't encapsulate IROIs (or even have a notion of them).
	// These inconsistencies in the models mean that scan_syntax.py and this class
	// have to handle several edge cases which increase complexity and information
	// coupling between the three sources.
	//
	// What would be /even cooler/ than importing the ModelStringifier schema into
	// scan_syntax.py is if the models themselves contained all the information that
	// we're putting here in the schema, so we had just one source of truth.
	// </contemplation>

	// The complexity goes in the data structure so that the algorithm can be simple:
	// Pseudo-structure of the schema:
	// HashMap<Class modelType,           # Map from model class to...
	//   Pair<                            # ...information about syntax for the model.
	//     String friendlyName,           # Friendly name for the model, e.g. "grid".
	//     List<Pair<                     # List of keywords for the model syntax.
	//       Pair<                        # Information about the keyword string.
	//         String keyword,            # The keyword itself, e.g. "axes".
	//         Boolean keywordRequired>,  # Must this keyword string be present?
	//       List<String getter>>>>>      # Getters for the kwarg tuple elements.

	private static final

	HashMap<Class<?>,                     // HashMap<Class modelType,
	    AbstractMap.SimpleEntry<          //     Pair<
	        String,                       //       String friendlyName,
	        LinkedHashMap<                //       List<Pair<
	            AbstractMap.SimpleEntry<  //           Pair<
	                String,               //               String keyword,
	                Boolean>,             //               Boolean keywordRequired>,
	            List<String>>>>           //           List<String getter>>>>>

	schema = new HashMap<Class<?>, AbstractMap.SimpleEntry<String,
	         LinkedHashMap<AbstractMap.SimpleEntry<String, Boolean>,
	         List<String>>>>();

	static {

		schema.put(
				GridModel.class,
				new AbstractMap.SimpleEntry<String, LinkedHashMap<AbstractMap.SimpleEntry<String,
				Boolean>, List<String>>>("grid", new LinkedHashMap<AbstractMap.SimpleEntry<String,
				Boolean>, List<String>>()));

		schema.get(GridModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"axes", false), Arrays.asList("getFastAxisName", "getSlowAxisName"));

		schema.get(GridModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"origin", false), Arrays.asList("getBoundingBox.getFastAxisStart", "getBoundingBox.getSlowAxisStart"));

		schema.get(GridModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"size", false), Arrays.asList("getBoundingBox.getFastAxisLength", "getBoundingBox.getSlowAxisLength"));

		schema.get(GridModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"count", true), Arrays.asList("getFastAxisPoints", "getSlowAxisPoints"));

		schema.get(GridModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"snake", true), Arrays.asList("isSnake"));

		schema.put(
				RasterModel.class,
				new AbstractMap.SimpleEntry<String, LinkedHashMap<AbstractMap.SimpleEntry<String,
				Boolean>, List<String>>>("grid", new LinkedHashMap<AbstractMap.SimpleEntry<String,
				Boolean>, List<String>>()));

		schema.get(RasterModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"axes", false), Arrays.asList("getFastAxisName", "getSlowAxisName"));

		schema.get(RasterModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"origin", false), Arrays.asList("getBoundingBox.getFastAxisStart", "getBoundingBox.getSlowAxisStart"));

		schema.get(RasterModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"size", false), Arrays.asList("getBoundingBox.getFastAxisLength", "getBoundingBox.getSlowAxisLength"));

		schema.get(RasterModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"step", true), Arrays.asList("getFastAxisStep", "getSlowAxisStep"));

		schema.get(RasterModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"snake", true), Arrays.asList("isSnake"));


		schema.put(
				StepModel.class,
				new AbstractMap.SimpleEntry<String, LinkedHashMap<AbstractMap.SimpleEntry<String,
				Boolean>, List<String>>>("step",new LinkedHashMap<AbstractMap.SimpleEntry<String,
				Boolean>, List<String>>()));

		schema.get(StepModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"axis", false), Arrays.asList("getName"));

		schema.get(StepModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"start", false), Arrays.asList("getStart"));

		schema.get(StepModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"stop", false), Arrays.asList("getStop"));

		schema.get(StepModel.class).getValue().put(new AbstractMap.SimpleEntry<String, Boolean>(
				"step", false), Arrays.asList("getStep"));

	}

	/**
	 * Return a string of valid Python which, in conjunction with
	 * scan_syntax.py, would generate the given IScanPathModel.
	 */
	final public static String stringify(IScanPathModel model, Boolean verbose)
			throws StringificationNotImplementedException {

		// TODO: Take also an optional list of ROIs.

		SimpleEntry<String, LinkedHashMap<SimpleEntry<String, Boolean>, List<String>>> modelSchema =
				schema.get(model.getClass());
		if (modelSchema == null) {
			throw new StringificationNotImplementedException(
					"Stringification not implemented for this model type.");
		}
		String friendlyName = modelSchema.getKey();
		LinkedHashMap<SimpleEntry<String, Boolean>, List<String>> keywordInfos = modelSchema.getValue();

		String fragment = friendlyName+"(";
		Boolean listPartiallyWritten = false;

		for (SimpleEntry<String, Boolean> keywordInfo : keywordInfos.keySet()) {

			String keyword = keywordInfo.getKey();
			Boolean keywordRequired = keywordInfo.getValue();
			List<String> getters = keywordInfos.get(keywordInfo);

			if (listPartiallyWritten) { fragment += ", "; }
			if (verbose || keywordRequired) { fragment += keyword+"="; }

			if (getters.size() > 1) {  // Tuple.

				fragment += "(";
				Boolean tuplePartiallyWritten = false;

				for (String getterString : getters) {
					if (tuplePartiallyWritten) { fragment += ", "; }
					fragment += getPrintableParam(model, getterString);
					tuplePartiallyWritten = true;
				}

				fragment += ")";

			} else {  // Single thing.
				String getterString = getters.get(0);
				fragment += getPrintableParam(model, getterString);
			}

			listPartiallyWritten = true;
		}

		fragment += ")";

		return fragment;
	}

	final private static Object getPrintableParam(IScanPathModel model, String getterString) {
		return handleSpecialCases(getParam(model, getterString));
	}

	final private static Object getParam(IScanPathModel model, String getterString) {
		try {
			if (!getterString.contains(".")) {
				Method getter = model.getClass().getMethod(getterString);
				return getter.invoke(model);  // Equivalent to `return model.getThing()`.
			} else {
				// E.g. "getBoundingBox.getxStart" -> return model.getBoundingBox().getxStart().
				String[] nestedGetterStrings = getterString.split("\\.");
				Method outerGetter = model.getClass().getMethod(nestedGetterStrings[0]);
				Object outerGetterResult = outerGetter.invoke(model);
				Method innerGetter = outerGetterResult.getClass().getMethod(nestedGetterStrings[1]);
				return innerGetter.invoke(outerGetterResult);
			}
		} catch (NoSuchMethodException|SecurityException|IllegalAccessException
				|IllegalArgumentException|InvocationTargetException ex) {
			// The reason we have all these exceptions to handle is because we're doing
			// introspection; the compiler doesn't know that these exceptions won't arise.
			// The introspection we're doing is predictable w.r.t. user input, so if we
			// reach this catch block it's because this class is written incorrectly
			// (probably the schema is out of date), not because of bad user input.
			// TL;DR: We should never reach this catch block, but if we do, fail gracefully
			// because it's our fault.
			// TODO: Put a warning to stderr or somewhere?
			return "???";
		}
	}

	final private static Object handleSpecialCases(Object getterResult) {
		if (getterResult.getClass() == Boolean.class) {
			if ((Boolean) getterResult) {
				return "True";
			} else {
				return "False";
			}
		} else if(getterResult.getClass() == String.class) {
			return "'"+getterResult+"'";
		} else {
			return getterResult;
		}
	}

}
