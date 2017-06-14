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
/**
 * Just for rendering an error identified by {@code parma} and peeked up through a message key when, from the request, it is stated to 
 */
public class ParamError extends ShrTagSupport {
	private String param;

	@Override
	public int doStartTag() throws JspException {
		String param = request().getParameter(getParam());
		if (param != null)
			try {
				out().append("<tr class='perror'><td colspan='2'>" + getText() + "</td></tr>");
			} catch (IOException e) {
				e.printStackTrace();
			}
		return super.doStartTag();
	}

	public String getParam() {
		return param;																															
	}

	public void setParam(String param) {
		this.param = param;
	}
	
}
