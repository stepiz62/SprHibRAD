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

package com.sprhibrad.framework.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.sprhibrad.framework.common.Utils;
/**
 * The parent class for binary object management, it renders the commands for storing and viewing the object.
 * It lets the actual rendering be implemented in its children concrete classes.
 */
public abstract class ShrBinaryTag extends FormAccessor {
	String path;
	Boolean editable;
	boolean objectGot;


	@Override
	public int doStartTag() throws JspException {
		DataItem editableBox = (DataItem) findAncestorWithClass(this, TermTag.class);
		if (!Utils.isEditing(request()) || editableBox != null && ! editableBox.getViewOnly()) {
			if (editableBox != null) {
				String tagPath = getPath();
				if (tagPath == null || tagPath.isEmpty())  
					setPath(editableBox.getPath());
				checkFurtherInheritance(editableBox);
			}
			try {
				out().println("<table class='binary'><tr>" + rendering());
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (editable != null && editable)
				renderEditingCommands();
		}
		return super.doStartTag();
	}
		
		
	protected void checkFurtherInheritance(TermTag editableBox) {}


	byte[] getBytes() {
		byte[] retVal = (byte[]) Utils.extractValue(getObjectPath(), getEntityObj());
		objectGot = retVal != null;
		return retVal;
	}
	
	protected String getObjectPath() {
		return path;
	}

	abstract String rendering();
	
	void reset() {
		path = null;
		editable = null;
	}
	
	@Override
	public int doEndTag() throws JspException {
		int retVal = super.doEndTag();
		try {
			out().println("</tr></table>");
		} catch (IOException e) {
			e.printStackTrace();
		}
		reset();
		return retVal;
	}
	
 
	protected void renderEditingCommands() {
		try {
			outButton(msgMgr().msgOrKey(objectGot ? "command.change" : "command.add"), contextPath() + "/", 
								getEntityName() + "/uploadBinary?" + getArguments(),
								null, null);
			if (objectGot)
				outButton(msgMgr().msgOrKey("command.delete"), contextPath() + "/", 
						getEntityName() + "/deleteBinary?" + getArguments(), wantToDelQuestion("binary." + getVerbose()), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	abstract String getVerbose();

	String getArguments() {
		return  "op=" + path + "&pp=";
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}


}
