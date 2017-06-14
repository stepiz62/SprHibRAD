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
 * A class that renders directly different types of input tag and that doesn't participate in the double behavior (idle/edit).
 */
public class SimpleInputTag extends ShrTagSupport {

	private String type;

	@Override
	public int doStartTag() throws JspException {
		String gotType = getType();
		String value = null;
		TermTag box = null;
		if (gotType.compareTo("submit") == 0)
			value = getText();
		else
			box = ((TermTag) findAncestorWithClass(this, TermTag.class));
		try {
			boolean editable = true;
			if (box != null)
				editable = box.isEditable() && Utils.isContainerEditable(box);
			if (editable) {
			out().append("<input" + Utils.writeAttr("type", getType()));
			if (box != null)
				out().append(Utils.idAndNameAttrs(box.getPath()));
			if (value != null)
				out().append(Utils.writeAttr("value", value));
			out().append(" />");
			} else if (gotType.compareTo("password") == 0)
				out().append("* * * * *");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.doStartTag();
	}

	public String getType() {
		return type == null ? "text" : type;
	}

	public void setType(String type) {
		this.type = type;
	}


}
