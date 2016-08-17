package org.eclipse.scanning.api.points.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * This class is designed to encapsulate the information
 * to required to provide all the locations, with regions
 * of an n-Dimensional scan.
 * 
 * <pre>
 * CompoundModel {
 *     models:  [ list of models]
 *     regions: [ list of regions]
 * }
 * Region {
 *     roi: geometric roi
 *     scannables: [ list of scannable names]   
 * }
 * </pre>
 * <b>Example:</b><p>
 * <pre>
 * CompoundModel {
 *     models : [
 *         {type: SpiralModel
 *          fastAxisName: x
 *          slowAxisName: y
 *          ...
 *         }
 *     ]
 *     regions : [
 *        {
 *          roi : {type: CircularROI
 *           centre: [0,1]
 *           radius: 2
 *          }
 *          scannables: ["x", "y"]
 *        }
 *     ]
 * }   
 *    
 * 
 * </pre>
 * 
 * @author Matthew Gerring
 *
 */
public class CompoundModel {

	private List<Object>               models;
	private Collection<ScanRegion<?>>  regions;
	
	public CompoundModel() {
		// Must have no-arg constructor
	}

	public CompoundModel(Object... ms) {
		if (ms!=null && ms.length==1 && ms[0] instanceof List) {
			models = (List<Object>)ms[0];
		} else {
		    models = Arrays.asList(ms);
		}
	}
	public CompoundModel(List<Object> ms) {
		models = ms;
	}
	public <T> CompoundModel(IScanPathModel model, T region) {
		if (region instanceof IScanPathModel) { // It's not a region
			models = Arrays.asList(new IScanPathModel[]{model, (IScanPathModel)region});
		} else {
		    setData(model, region, model.getScannableNames());
		}
	}
	
	public <T> void setData(IScanPathModel model, T region) {
		if (region instanceof IScanPathModel) { // It's not a region
			models = Arrays.asList(new IScanPathModel[]{model, (IScanPathModel)region});
		} else {
			setData(model, region, model.getScannableNames());
		}
	}
	
	public <T> void setData(IScanPathModel model, T region, List<String> names) {
		if (region instanceof IScanPathModel) throw new IllegalArgumentException("The region must not be a generator model!");
		
		// We do it this way to make setData(...) fast. This means addData(...) has to deal with unmodifiable lists.
		this.models  = Arrays.asList(model);
	    this.regions = Arrays.asList(new ScanRegion<T>(region, names)); 
	}

	/**
	 * Method to add a model and regions which are assumed to act on the
	 * model provided and are assigned to it using its scannable names.
	 * 
	 * @param model
	 * @param rois
	 */
	public void addData(Object model, Collection<?> rois) {
		
		if (models==null) models = new ArrayList<Object>(7);
		try {
			models.add(model);
		} catch(Exception ne) {
			// Models is allowed to be non-null and unmodifiable	
			// If it is, we make it modifiable and add the model.
			List<Object> tmp = new ArrayList<Object>(7);
			tmp.addAll(models);
			tmp.add(model);
			models = tmp;
		}
		
		// They are not really ordered but for now we maintain order.
		if (regions==null) regions = new LinkedHashSet<ScanRegion<?>>(7);
		if (rois!=null) for (Object roi : rois) {
			ScanRegion<?> region = new ScanRegion<>(roi, AbstractPointsModel.getScannableNames(model));
			try {
				this.regions.add(region);
			} catch(Exception ne) {
				// It might be unmodifiable
				Collection<ScanRegion<?>> tmp = new LinkedHashSet<ScanRegion<?>>(7);
				tmp.addAll(this.regions);
				tmp.add(region);
				regions = tmp;
			}
		}
	}
	
	public List<Object> getModels() {
		return models;
	}
	public void setModels(List<Object> models) {
		this.models = models;
	}
	public void setModelsVarArgs(Object... models) {
		this.models = Arrays.asList(models);
	}
	public Collection<ScanRegion<?>> getRegions() {
		return regions;
	}
	public void setRegions(Collection<ScanRegion<?>> regions) {
		this.regions = regions;
	}
	public void setRegionsVarArgs(ScanRegion<?>... regions) {
		this.regions = Arrays.asList(regions);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((models == null) ? 0 : models.hashCode());
		result = prime * result + ((regions == null) ? 0 : regions.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompoundModel other = (CompoundModel) obj;
		if (models == null) {
			if (other.models != null)
				return false;
		} else if (!equals(models, other.models))
			return false;
		if (regions == null) {
			if (other.regions != null)
				return false;
		} else if (!equals(regions, other.regions))
			return false;
		return true;
	}
	
	/**
	 * This equals does an equals on two collections
	 * as if they were two lists because order matters with the names.
	 * @param o
	 * @param t
	 * @return
	 */
    private boolean equals(Collection<?> o, Collection<?> t) {
        
    	if (o == t)
            return true;
    	if (o == null && t == null)
            return true;
    	if (o == null || t == null)
            return false;
 
        Iterator<?> e1 = o.iterator();
        Iterator<?> e2 = t.iterator();
        while (e1.hasNext() && e2.hasNext()) {
            Object o1 = e1.next();
            Object o2 = e2.next();
            
            // Collections go down to the same equals.
            if (o1 instanceof Collection && o2 instanceof Collection) {
            	boolean collectionsEqual = equals((Collection<?>)o1,(Collection<?>)o2);
            	if (!collectionsEqual) {
            		return false;
            	} else {
            		continue;
            	}
            }
            
            // Otherwise we use object equals.
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

}
