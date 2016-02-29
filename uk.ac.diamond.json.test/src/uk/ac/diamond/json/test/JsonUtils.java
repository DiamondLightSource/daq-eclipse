/*-
 * Copyright © 2016 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.json.test;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class JsonUtils {

	/**
	 * Compare two JSON strings for equality, ignoring whitespace between entries and the order of an object's
	 * properties.
	 *
	 * @param expected JSON string for the expected object
	 * @param actual JSON string for the actual object
	 * @throws Exception if there is a problem reading the strings as JSON
	 */
	public static void assertJsonEquals(String expected, String actual) throws Exception {
		ObjectMapper jsonMapper = new ObjectMapper();
		JsonNode actualTree = jsonMapper.readTree(actual);
		JsonNode expectedTree = jsonMapper.readTree(expected);
		assertEquals("JSON trees are different.", expectedTree, actualTree);
	}
}