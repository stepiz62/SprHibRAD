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
 * renders the place-holder for accessing to a whatever digitized object (for work)
 */
public class Handout extends ShrBinaryTag {

	@Override
	public int doStartTag() throws JspException {
		return super.doStartTag();
	}

	@Override
	String rendering() {
		return getBytes() == null ? "" : Utils.cellWrap(renderTag(getEntityName(), path));
	}
	
	String renderTag(String entityName, String fieldName) {
		StringBuilder labString = new StringBuilder("");
		String prefix = contextPath() + "/" +  entityName + "/";
		labString.append("<a href='" + prefix + "viewHandout?target=" + fieldName);
		labString.append("&" + "id=");
		labString.append("' target='_blank'>");
		labString.append("<img src='" + contextPath()  + "/handout.jpg' />");
		labString.append("</a>");
		return labString.toString();
	}

	@Override
	String getVerbose() {
		return "handout";
	}

}
