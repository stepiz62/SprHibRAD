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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.json.simple.JSONObject;

/**
 * A reflection provider for ShrgObject and Selector : several instances can wrap
 * the same "wrapped" object
 */

public class ShrgGuiObject {
	Object object;
	EnablerOnField enableOnFieldInterface;
	
	public void setEnableOnFieldInterface(EnablerOnField enableOnFieldInterface) {
		this.enableOnFieldInterface = enableOnFieldInterface;
	}

	public interface EnablerOnField {
		public boolean isToEnable(String fieldName);
	}
	
	public ShrgGuiObject(Object guiObject) {
		this.object = guiObject;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public void setShrgObject(ShrgObject shrgObject) {
		invokeProcMethod("setShrgObject", object, new Object[] {shrgObject});
	}

	public void setShrgObject(ShrgObject shrgObject, Boolean forFeeding) {
		if (forFeeding)
			invokeProcMethod("setShrgObject", object, new Object[] {shrgObject, forFeeding});
		else
			setShrgObject(shrgObject);
	}

	public ShrgObject getShrgObject() {
		return (ShrgObject) invokeFunMethod("getShrgObject", object, null);
	}

	public String getProperty() {
		return (String) invokeFunMethod("getProperty", object, null);
	}
	
	public ShrgObject getShrgObject(Boolean forFeeding) {
		return forFeeding ? (ShrgObject) invokeFunMethod("getShrgObject", object, new Object[] {forFeeding}) :
							getShrgObject();
	}

	public void addSelector(Selector selector) {
		invokeProcMethod("addSelector", object, new Object[] {selector});
	}

	public void set(JSONObject jsonObj, Boolean toGui) {
		if (! toGui || jsonObj != null)
			invokeProcMethod("set", object, new Object[] {jsonObj, toGui});
	}

	public void set(ShrgJSONArray jsonArray, Boolean toGui) {
		if (! toGui || jsonArray != null)
			invokeProcMethod("set", object, new Object[] {jsonArray, toGui});
	}

	
	public void project(ShrgJSONArray array) {
		invokeProcMethod("project", object, new Object[] {array});
	}

	public void project(JSONObject jsonObj) {
		invokeProcMethod("project", object, new Object[] {jsonObj});
	}
	
	public void clear() {
		invokeProcMethod("clear", object, null);
	}

	public void clear(Boolean forFeeding) {
		if (forFeeding)
			invokeProcMethod("clear", object, new Object[] {forFeeding});
		else
			clear();
	}


	public void enable(Boolean truth) {
		invokeProcMethod("enable", object, new Object[] {truth});
	}

	public void setObjectProperty(String property) {
		invokeProcMethod("setProperty", object, new Object[] {property});
	}

	protected  Object invokeFunMethod(String methodName, Object object, Object[] args) {
		Method method = null;
		try {			
			method = object.getClass().getDeclaredMethod(methodName, getClasses(args));
		} catch (NoSuchMethodException | SecurityException e) {
			SprHibRadGen.app.outToConsole(e);
		}
		Object retVal = null;
		try {
			retVal = method.invoke(object, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			SprHibRadGen.app.outToConsole(e);
		}
		return retVal;
	}

	private Class[] getClasses(Object[] objects) {
		Class[] retVal = null;
		if (objects != null) {
			retVal = new Class[objects.length];
			for (int i = 0; i < objects.length; i++)
				retVal[i] = objects[i].getClass();
		}
		return retVal;
	}
	
	protected void invokeProcMethod(String methodName, Object object, Object[] args) {
		Method method = null;
		try {
			method = object.getClass().getDeclaredMethod(methodName, getClasses(args));
		} catch (NoSuchMethodException | SecurityException e) {
			SprHibRadGen.app.outToConsole(e);
		}
		try {
			method.invoke(object, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			SprHibRadGen.app.outToConsole(e);
		}
	}



}
