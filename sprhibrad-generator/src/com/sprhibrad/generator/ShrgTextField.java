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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import org.json.simple.JSONObject;

/**
 * One of the gui components that takes a seat in Json tree project. 
 */
public class ShrgTextField extends JTextField implements FocusListener {
	private ShrgObject shrgObject;
	private boolean changing;
	private ActionPerformer changeInterface;

	public ShrgTextField() {
		super();
		addFocusListener(this);
	}

	public ShrgObject getShrgObject() {
		return shrgObject;
	}

	public void setShrgObject(ShrgObject shrgObject) {
		this.shrgObject = shrgObject;
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		String selection = getText();
		changing = true;
		if (changeInterface != null)
			changeInterface.handle(selection);
		Object obj = shrgObject.getGuiObject().getObject();
		if (shrgObject != null &&  (! (obj instanceof ShrgList) || ! ((ShrgList) obj).changing))
			shrgObject.set(false);		
		changing = false;
	}

	public void clear() {
		setText("");
	}

	public void set(JSONObject jsonObj, Boolean toGui) {
		if (toGui) {
			String text = (String) jsonObj.get("value");
			setText(text == null ? "" : text);
		} else
			jsonObj.put("value", getText());
	}
	
	public void enable(Boolean truth) {
		setEnabled(truth);
	}
}
