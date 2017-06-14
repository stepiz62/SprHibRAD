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

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Each element of the array is a JSONObject having an only "key : value" pair.
 * Furthermore The class extends its parent by adding the direct access capability with the {@link #clone(JSONArray)} method.
 */

public class ShrgJSONArray extends JSONArray {
	
	public ShrgJSONArray() {
	}

	public ShrgJSONArray(JSONArray src) {
		clone(src);
	}

	HashMap<String, Integer> map = new HashMap<String, Integer>();
	
	public int indexOf(String key) {
		int retVal = -1;
		Integer index =  map.get(key);
		if (index != null)
			retVal = index;
		return retVal;
	}

	protected JSONObject get(String key) {
		int index = indexOf(key);
		return index == -1 ? null : (JSONObject) ((JSONObject) get(index)).get(key);
	}
	
	static public  String getKey(JSONObject element) {
		String retVal = null;
		if (element.entrySet().iterator().hasNext())
			retVal = theOnlyKeyValuePair(element).getKey();
		return retVal;
	}

	private static Entry<String, Object> theOnlyKeyValuePair(JSONObject element) {
		return (Entry<String, Object>) element.entrySet().iterator().next();
	}
	
	static public  JSONObject getValue(JSONObject element) {
		JSONObject retVal = null;
		if (element.entrySet().iterator().hasNext())
			retVal = (JSONObject) theOnlyKeyValuePair(element).getValue();
		return retVal;
	}

	public boolean remove(String key) {
		boolean retVal = false;
		int index = indexOf(key);
		Vector<String> toDecrementKeys = new Vector<String>();
		for (int i = index + 1; i < size(); i++)
			toDecrementKeys.add(getKey((JSONObject) get(i)));
		for (String toDecrementKey : toDecrementKeys)
			map.put(toDecrementKey, map.get(toDecrementKey) - 1);
		if (index >= 0) {
			retVal = remove(index) != null;
			map.remove(key);
		}
		return retVal;
	}

	public boolean add(String key) {
		JSONObject obj = new JSONObject();
		obj.put(key, new JSONObject());
		return super.add(obj);
	}

	@Override
	public boolean add(Object obj) {
		map.put(getKey((JSONObject) obj), size());
		return super.add(obj);
	}

	@Override
	public void clear() {
		map.clear();
		super.clear();
	}

	public void clone(JSONArray srcArray) {
		clear();
		for (Object obj : srcArray)
			add(obj);		
	}

	
	
}
