package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.AbstractPosition;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.python.core.PyDictionary;

/**
 * 
 * The compound generator must only compound positions
 * which implement AbstractPosition or  
 * 
 * @author Matthew Gerring
 *
 */
class CompoundGenerator extends AbstractGenerator<CompoundModel> implements PySerializable {
	
	private IPointGenerator<?>[]     generators;
	private List<Collection<String>> dimensionNames;

	public CompoundGenerator(IPointGenerator<?>[] generators) throws GeneratorException {
		super(createId(generators));
        if (generators == null || generators.length<1) throw new GeneratorException("Cannot make a compound generator from a list of less than one generators!");
        
        // We create a model with no regions from the generators.
        this.model = new CompoundModel();
        for (IPointGenerator<?> g : generators) model.addData(g.getModel(), null);
        // This model is not designed to hold all the data because we have the actual generators!
        
        this.generators = generators;
	    this.dimensionNames = createDimensionNames(generators);
		setLabel("Compound");
		setDescription("Compound generator used when wrapping scans.");
		setVisible(false);
	}

	private List<Collection<String>> createDimensionNames(IPointGenerator<?>[] generators) {
		List<Collection<String>> names = new ArrayList<>(generators.length+2); // Roughly
		for (IPointGenerator<?> gen : generators) {
			IPosition pos = gen.iterator().next();
			names.addAll(((AbstractPosition)pos).getDimensionNames());
		}
		return names;
	}

	private static String createId(IPointGenerator<?>[] gens) throws GeneratorException {
        if (gens == null || gens.length<1) throw new GeneratorException("Cannot make a compound generator from a list of less than one generators!");
	
        final StringBuilder buf = new StringBuilder();
        for (IPointGenerator<?> gen : gens) buf.append("+"+gen);
        return buf.toString();
	}

	@Override
	protected void validateModel() {
		// CompoundGenerator is a bit of a special case. this.iterator() calls
		// the .iterator() methods of the component IPointGenerators, which in
		// turn each calls .validateModel(). Therefore we don't need to do any
		// explicit validation here.
	}
	
	@Override
	public int sizeOfValidModel() throws GeneratorException {
		CompoundIterator it = (CompoundIterator) iteratorFromValidModel();
		return it.size();
	}
	

    public PyDictionary toDict() {
		Iterator<?> it = iteratorFromValidModel();
		if (it instanceof PySerializable) return ((PySerializable)it).toDict();
		return null;
    }
	
	/**
	 * The description is run on the fly for compound generator
	 * and it provides the scan point summary.
	 */
	@Override
	public String getDescription() {
		if (model==null) return super.getDescription();
		try {
			validate(model); // Probably does nothing depending on what validation is chosen for compound models.

			final StringBuilder buf = new StringBuilder();
			buf.append("A scan of "+size()+" points, ");
			IPosition first = iterator().next();
			buf.append("scanning motors: ");
			for (Iterator<String> it = first.getNames().iterator(); it.hasNext();) {
				String name = it.next();
				buf.append(name);
				if(it.hasNext()) buf.append(",");
				buf.append(" ");
			}
			buf.append('\n');
			
			return buf.toString();
			
		} catch (Exception ne) {
			return ne.getMessage() != null ? ne.getMessage() : ne.toString();
		}
		
	}
	
	@Override
	protected Iterator<IPosition> iteratorFromValidModel() {
		try {
			return new CompoundIterator(this);
		} catch (GeneratorException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public List<IPosition> createPoints() throws GeneratorException {
		
		List<IPosition> points = new ArrayList<>(size());
		createPoints(0, points, null);
		return points;
	}

	/**
	 * This simple recursive method is what nested scans reduce to.
	 * 
	 * @param igen
	 * @param points
	 * @param parent
	 */
	private void createPoints(int igen, List<IPosition> points, IPosition parent) {
		
		IPointGenerator<?> gen = generators[igen];
		Iterator<? extends IPosition>     it  = gen.iterator();
		while(it.hasNext()) {
			IPosition next = it.next();
			IPosition pos = next.compound(parent);
			((AbstractPosition)pos).setDimensionNames(dimensionNames);
			int nextGen = igen+1;
			if (nextGen<generators.length) {
				createPoints(nextGen, points, pos);
			} else {
				points.add(pos);
			}
		}
	
	}

	public IPointGenerator<?>[] getGenerators() {
		return generators;
	}

	public List<Collection<String>> getDimensionNames() {
		return dimensionNames;
	}

	public void setDimensionNames(List<Collection<String>> dimensionNames) {
		this.dimensionNames = dimensionNames;
	}

}
