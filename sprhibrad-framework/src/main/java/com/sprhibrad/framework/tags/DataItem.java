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

import javax.servlet.jsp.JspException;

import com.sprhibrad.framework.common.Utils;

/** 
 * The handler class of the generic tag that, one for each entity's attribute, populates the body of a {@code dataForm} tag.
 */
public class DataItem extends TermTag {
	
	Boolean viewOnly = false;
	Boolean readOnly = false;
	
	protected void setViewOnly(Boolean viewOnly) {
		this.viewOnly = viewOnly;
	}

	public Boolean getViewOnly() {
		return viewOnly;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}


	@Override
	public int doStartTag() throws JspException {
		return viewOnly && Utils.isEditing(request()) ? SKIP_BODY : termStartTag();
	}

	@Override
	public int doEndTag() throws JspException {
		termEndTag();
		return super.doEndTag();
	}

	@Override
	public boolean isEditable() {
		return Utils.isEditing(request()) || ! (getFormTag() instanceof DataFormTag);
	}

	
}
