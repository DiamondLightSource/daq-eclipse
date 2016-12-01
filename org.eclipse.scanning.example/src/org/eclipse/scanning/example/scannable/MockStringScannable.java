package org.eclipse.scanning.example.scannable;

import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;

public class MockStringScannable extends AbstractScannable<String> implements INameable {

	private String value;
	
	public MockStringScannable(String name, String pos) {
		setName(name);
		this.value = pos;
	}

	@Override
	public String getPosition() throws Exception {
		return value;
	}

	@Override
	public void setPosition(String value, IPosition position) throws Exception {
		this.value = value;
		delegate.firePositionChanged(getLevel(), new Scalar<String>(getName(), -1, value));
	}


}
