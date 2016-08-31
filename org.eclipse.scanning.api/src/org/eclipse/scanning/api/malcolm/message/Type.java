package org.eclipse.scanning.api.malcolm.message;


public enum Type {
	
	
	CALL, GET, READY, PUT, RETURN, ERROR, SUBSCRIBE, UNSUBSCRIBE, UPDATE, DELTA;
	
	public boolean isError() {
		return this==Type.ERROR;
	}

}


