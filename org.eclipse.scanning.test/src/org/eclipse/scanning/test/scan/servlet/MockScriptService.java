package org.eclipse.scanning.test.scan.servlet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.scanning.api.script.ScriptExecutionException;
import org.eclipse.scanning.api.script.ScriptLanguage;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.api.script.ScriptResponse;
import org.eclipse.scanning.api.script.UnsupportedLanguageException;

final class MockScriptService implements IScriptService {

	private List<ScriptRequest> scriptRequests = new ArrayList<>();
	
	@Override
	public ScriptLanguage[] supported() {
		return ScriptLanguage.values();
	}

	@Override
	public ScriptResponse<?> execute(ScriptRequest req)
			throws UnsupportedLanguageException, ScriptExecutionException {
		scriptRequests.add(req);
		return new ScriptResponse<>();
	}
	
	public List<ScriptRequest> getScriptRequests() {
		return scriptRequests;
	}

}
