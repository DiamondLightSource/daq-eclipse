package org.eclipse.scanning.sequencer;

import java.util.Comparator;

import org.eclipse.scanning.api.ILevel;

public class LevelComparitor implements Comparator<Object> {

	@Override
	public int compare(Object o1, Object o2) {
		int l1 = getLevel(o1);
		int l2 = getLevel(o2);
		return l1-l2;
	}

	private int getLevel(Object o) {
		if (!(o instanceof ILevel)) return ILevel.MAXIMUM;
		return ((ILevel)o).getLevel();
	}

}