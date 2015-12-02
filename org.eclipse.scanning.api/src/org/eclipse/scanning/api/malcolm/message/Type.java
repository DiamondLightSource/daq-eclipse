package org.eclipse.scanning.api.malcolm.message;


public enum Type {
	
	
	CALL, GET, READY, VALUE, RETURN, ERROR, SUBSCRIBE, UNSUBSCRIBE;
	
	public boolean isError() {
		return this==Type.ERROR;
	}

}


