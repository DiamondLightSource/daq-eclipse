package org.eclipse.scanning.api.script;

/**
 * 
 * A service to run scripts, python, javascript, r etc.
 * 
 * This service may be backed by a custom engine which supports a subset
 * of the scripting languages. It might be implemented by an OSGi Jython
 * or by an eclipse EASE environment.
 * 
 * @author Matthew Gerring
 *
 */
public interface IScriptService {
	
	/**
	 * For DAQ server version 8 and 9 this will probably be {JYTHON, SPEC_PASTICHE}
	 * @return the available supported scripting languages that this service can execute.
	 */
	ScriptLanguage[] supported();

	/**
	 * Execute a script on the server. This can be used for instance inside scanning to run
	 * a script before and after a scan.
	 * 
	 * @param req
	 */
	ScriptResponse execute(ScriptRequest req) throws UnsupportedLanguageException, ScriptExecutionException;
}
