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
import javax.servlet.jsp.JspWriter;

/**
 *  The buttons to go forward and backward with pagination of the data listing.
 */
public class IteratorButtons extends ListManagerAccessor {

	@Override
	public int doStartTag() throws JspException {
		try {
			JspWriter out = out();
			out.println("<table class='formButtons'><tr>");
			if (getIteration() > -1) {
				out.println("<td  width='50%'>");
			} else
				out.println("<td>");
			if (getIteration() >= 1)
				out.println(navigCommand(msgMgr().msgOrKey("command.previous"), getIteration() - 1, "button"));
			out.println("</td><td>");
			if (getIteration() == -1)
				out.println(navigCommand(msgMgr().msgOrKey("command.search"), 0, "button"));
			else {
				if (request().getAttribute("more") != null
						&& request().getAttribute("more").toString()
								.compareTo("true") == 0) {
					out.println(navigCommand(msgMgr().msgOrKey("command.next"), getIteration() + 1, "button"));
				}
			}
			out.println("</td>");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return getIteration() == -1 ? EVAL_BODY_INCLUDE : super.doStartTag();
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			out().println("</tr></table>");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.doEndTag();
	}
	
}
