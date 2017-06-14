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

import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Based on the sub-domain resulting from the hierarchy of selection identified
 * by the parent selectors status, performs the data exchange between the json
 * tree and gui object which it is associated to. Its constructor associates a
 * gui object with an attribute inside the Json tree identified by the
 * {@code Selector} "chain" that is built by invoking
 * {@link #addParentSelector(Selector, boolean)}
 * 
 * @see Selector
 * @see ShrgGuiObject
 */

public class ShrgObject {
	ShrgJsonWrapper jsonContainer;
	ShrgGuiObject guiObject;	
	String property;
	ShrgComboBox targetSelector;

	Vector<Selector> selHierachy = new Vector<Selector>();
	private JSONObject associatedObject;
	private ShrgJSONArray associatedArray;
	boolean setting;
	boolean settingForProjection;
	boolean keepOrder;
	boolean toGuiOnly;
	SprHibRadGen app = SprHibRadGen.app;

	boolean isSetting(boolean projection) {
		return projection ? settingForProjection : setting;
	}

	void setSettingTo(boolean projection, boolean truth) {
		if (projection)
			settingForProjection = truth;
		else
			setting = truth;
	}
	
	public ShrgObject(ShrgJsonWrapper jsonContainer, String property) {
		this.jsonContainer = jsonContainer;
		this.property = property;
	}

	public ShrgObject(ShrgJsonWrapper jsonContainer, Object object, String property) {
		this(jsonContainer, object, property, false);
	}

	public ShrgObject(ShrgJsonWrapper jsonContainer, Object object, String property, boolean keepOrder) {
		this(jsonContainer, object, property, keepOrder, false);
	}
	
	public ShrgObject(ShrgJsonWrapper jsonContainer, Object object, String property, boolean keepOrder, boolean toGuiOnly) {
		this(jsonContainer, property);
		guiObject = new ShrgGuiObject(object);
		guiObject.setShrgObject(this, toGuiOnly);
		guiObject.setObjectProperty(property);
		this.keepOrder = keepOrder;
		this.toGuiOnly = toGuiOnly;
	}
	
	
	public ShrgObject(ShrgJsonWrapper jsonContainer, Object object, String rootProperty, ShrgGuiObject.EnablerOnField enableOnFieldInterface) {
		this(jsonContainer, object, rootProperty, false, enableOnFieldInterface);
	}
	
	public ShrgObject(ShrgJsonWrapper jsonContainer, Object object, String rootProperty, boolean keepOrder, ShrgGuiObject.EnablerOnField enableOnFieldInterface) {
		this(jsonContainer, object, rootProperty, keepOrder);
		guiObject.setEnableOnFieldInterface(enableOnFieldInterface);
	}

	public void addParentSelector(Selector selector, boolean leaf) {
		selHierachy.add(selector);
		if (leaf)
			selector.addDependent(guiObject, toGuiOnly);
	}
	
	public ShrgGuiObject getGuiObject() {
		return guiObject;
	}

	public ShrgComboBox getTargetSelector() {
		return targetSelector;
	}
	
	public void setTargetSelector(ShrgComboBox targetSelector) {
		this.targetSelector = targetSelector;
	}

	public void applySelectionDomain(ShrgComboBox selector) {
		this.targetSelector = selector;
		this.targetSelector.setShrgObject(this);
	}

	public void set(Boolean toGui) {
		set(toGui, false);
	}
	
	public void set(Boolean toGui, boolean projection) {
		if (isSetting(projection))
			return;
		else
			setSettingTo(projection, true);
		JSONObject jsonNode = jsonContainer.obj;
		JSONObject parentObject = null;
		ShrgJSONArray parentArray = new ShrgJSONArray();
		Object gotElem = null;
		String parentDomain = null;
		String gotKey = null;
		
		boolean identified = true;
		for (Selector selector : selHierachy) {
			try {
				parentDomain = selector.getDomain();
				gotKey = selector.getAttr(); 
				if (gotKey==null) {
					identified = false;		
					break;
				}
				boolean selectorIsOrdered = jsonNode.get(parentDomain) instanceof JSONArray;
				if (selectorIsOrdered) {
					parentArray.clone((JSONArray) jsonNode.get(parentDomain));
					gotElem = parentArray.get(gotKey);
				} else {
					parentObject = (JSONObject) jsonNode.get(parentDomain);
					gotElem = parentObject.get(gotKey);
				}
				if (gotElem instanceof JSONObject) {
					jsonNode = (JSONObject) gotElem;
				}
				else {
					identified = false;
					break;
				}
			} catch (Exception e) {
			}
		}
		if (identified) {
			if (keepOrder) {
				associatedArray = new ShrgJSONArray();
				try {
					JSONArray gotArray = (JSONArray) jsonNode.get(property);
					if (gotArray != null)
						associatedArray.clone(gotArray);		/* to deal with JSONSimple and inheriting too from it */				
					jsonNode.put(property, associatedArray);
				} catch (Exception e) {
					jsonTypeMismatchMsg(e, "Object");
				}
			} else {
				associatedObject = null;
				try {
					associatedObject = (JSONObject) jsonNode.get(property);
					if (associatedObject == null) {
						associatedObject = new JSONObject();
						jsonNode.put(property, associatedObject);
					}
				} catch (Exception e) {
					jsonTypeMismatchMsg(e, "Array");
				}	
			}
			if (toGui && ! projection)
				clear(toGuiOnly);
			if (keepOrder) {
				if (projection)
					guiObject.project(associatedArray);
				else if (toGui || ! toGuiOnly)
					guiObject.set(associatedArray, toGui);
				}
			else {
				if (projection)
					guiObject.project(associatedObject);
				else if (toGui || ! toGuiOnly)
					guiObject.set(associatedObject, toGui);
			}
		}
		setSettingTo(projection, false);
	}


	void jsonTypeMismatchMsg(Exception e, String type) {
		app.outToConsole(property + " is an JSON " + type
				+ " - check ShrgObject constructor 'keepOrder' param ");
	}
	
	public void clear(boolean forFeeding) {
		guiObject.clear(forFeeding);
		if (targetSelector != null)
			targetSelector.removeAllItems();
	}

}
