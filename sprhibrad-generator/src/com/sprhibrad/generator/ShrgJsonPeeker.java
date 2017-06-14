/*
	Copyright (c) 2017, Stefano Pizzocaro. All rights reserved. Use is subject to license terms.

	This file is part of SprHibRAD 1.0.

	SprHibRAD 1.0 is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	SprHibRAD 1.0 is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public License
	along with SprHibRAD 1.0.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.sprhibrad.generator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A class to peek an object submitting the identifying path.
 */
public class ShrgJsonPeeker {

	public class RetVal {
		String value;
		Object hostingObj;
		ShrgJSONArray array;
		public RetVal(Object hostingObj) {
			this.value = null;
			this.hostingObj = hostingObj;
			array = new ShrgJSONArray();
		}
	}
	
	public  RetVal peek(Object root, String[] path) {
		RetVal retVal = new RetVal(root);
		for (String key : path) {
			if (retVal.hostingObj instanceof JSONObject)
				retVal.hostingObj = ((JSONObject) retVal.hostingObj).get(key);
			else if (retVal.hostingObj instanceof JSONArray){
				retVal.array.clone((JSONArray) retVal.hostingObj);
				retVal.hostingObj = retVal.array.get(key);				
			} 
		}
		if (retVal.hostingObj == null)
			retVal.array.clear();
		else if (retVal.hostingObj instanceof String)
			retVal.value = (String) retVal.hostingObj;
		else if (retVal.hostingObj instanceof JSONArray)
			retVal.array.clone((JSONArray) retVal.hostingObj);
		return retVal;
	}
	
	public boolean isAttributeSet(JSONObject root, String[] path) {
		boolean retVal = false;
		RetVal peekRetVal = peek(root, path);
		if (peekRetVal.value != null)
			retVal = peekRetVal.value.compareTo("true") == 0;
		return retVal;
	}
}
