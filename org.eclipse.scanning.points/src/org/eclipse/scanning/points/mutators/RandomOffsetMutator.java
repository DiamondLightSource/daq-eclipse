package org.eclipse.scanning.points.mutators;

import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.eclipse.scanning.points.ScanPointGeneratorFactory.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyList;

public class RandomOffsetMutator implements IMutator {

	private int seed;
	private List<String> axes;
	private Map<String, Double> maxOffsets;

	public RandomOffsetMutator(int seed, List<String> axes, Map<String, Double> maxOffsets) {
		this.seed = seed;
		this.axes = axes;
		this.maxOffsets = maxOffsets;
	}
	
	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public List<String> getAxes() {
		return axes;
	}

	public void setAxes(List<String> axes) {
		this.axes = axes;
	}

	public Map<String, Double> getMaxOffsets() {
		return maxOffsets;
	}

	public void setMaxOffsets(Map<String, Double> maxOffsets) {
		this.maxOffsets = maxOffsets;
	}
	
	@Override
	public Object getMutatorAsJythonObject() {
		JythonObjectFactory randomOffsetMutatorFactory = ScanPointGeneratorFactory.JRandomOffsetMutatorFactory();
        
        PyList pyAxes = new PyList(axes);
        
        PyDictionary maxOffset = new PyDictionary();
        for (String axis : maxOffsets.keySet()) {
        	maxOffset.put(axis, maxOffsets.get(axis));
        }
        
		return randomOffsetMutatorFactory.createObject(seed, pyAxes, maxOffset);
	}
}