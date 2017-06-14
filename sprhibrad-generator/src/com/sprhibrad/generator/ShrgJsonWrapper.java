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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
/**
 * A wrapper for an entire json tree equipped with methods to store and load the tree object.
 * A method for saving an indented form of the json tree code is provided to make easier debugging of the json structure.
 */
public class ShrgJsonWrapper {
	public JSONObject obj;
	SprHibRadGen	app;
	private boolean readableFlavorToo;
	
	public ShrgJsonWrapper(SprHibRadGen app, boolean readableFlavorToo) {
		this.app = app;
		this.readableFlavorToo = readableFlavorToo;
		obj = new JSONObject();
	}

	public JSONObject getObj() {
		return obj;
	}

	public void setObj(JSONObject obj) {
		this.obj = obj == null ? new JSONObject() : obj;
	}

	public String getPrpty(String key) {
		return (String) obj.get(key);
	}

	public void setPrpty(String key, String value) {
		if (obj == null)
			setObj(null);
		obj.put(key, value);
	}

	public boolean load(String filePathName) {
		boolean retVal = false;
		try {
			FileReader fileReader = new FileReader(filePathName);
			setObj((JSONObject) app.jsonParser.parse(fileReader));
			fileReader.close();
			retVal = true;
		} catch (ParseException | IOException e) {
		}
		return retVal;
	}

	public void store(String filePathName) {
		try {
			FileWriter fileWriter = new FileWriter(filePathName);
			fileWriter.write(obj.toJSONString());
			fileWriter.close();
			if(readableFlavorToo)
				storeReadableFlavor(filePathName);
		} catch (IOException e) {
			app.outToConsole(e);
		}
	}
	
	private void storeReadableFlavor(String filePathName) throws IOException {
		FileWriter fileWriter = new FileWriter(filePathName + "_indented");
		String row = obj.toJSONString();
		int index = 0;
		char character;
		int openCount = 0;
		while (index < row.length() - 1) {
			character = row.charAt(index);
			if ((character == '{' && row.charAt(index + 1) != '}' || character == '[' && row.charAt(index + 1) != ']') ) {
				fileWriter.write(character);
				fileWriter.write("\n");
				openCount++;
				indent(fileWriter, openCount);
			} else if ((character == '}' || character == ']') ) {
				fileWriter.write("\n");
				openCount--;
				indent(fileWriter, openCount);
				fileWriter.write(character);
			} else {
				fileWriter.write(character); // and next is the closing match, indeed
				if (character == '{' || character == '[') {
					index++;
					fileWriter.write(row.charAt(index));
				}
			}
			if (character == ',') {
				fileWriter.write("\n");				
				indent(fileWriter, openCount);
			}
			index ++ ;
		}	
		if (index < row.length()) {
			fileWriter.write("\n");				
			indent(fileWriter, --openCount);
			fileWriter.write(row.charAt(index));
		}
		fileWriter.close();
	}

	private void indent(FileWriter fileWriter, int openCount) throws IOException {
		for (int i = 0 ; i < openCount ; i++)
			fileWriter.write("\t");						
	}
}

