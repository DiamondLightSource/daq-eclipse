/*-
 *******************************************************************************
 * Copyright (c) 2011, 2014, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Colin Palmer - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.points.serialization;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class PositionDeserializer extends JsonDeserializer<IPosition> {

	@Override
	public IPosition deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
		TypeReference<LinkedHashMap<String, Object>> linkedHashMap = new TypeReference<LinkedHashMap<String, Object>>() {};
		Map<String, Object> map = parser.readValueAs(linkedHashMap);
		return new MapPosition(map);
	}

	@Override
	public Object deserializeWithType(JsonParser parser, DeserializationContext context, TypeDeserializer typeDeserializer)
			throws IOException, JsonProcessingException {
		return deserialize(parser, context);
	}
}
