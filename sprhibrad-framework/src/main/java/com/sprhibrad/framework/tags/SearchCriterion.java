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

import org.springframework.web.util.HtmlUtils;

import com.sprhibrad.framework.common.Utils;

/**
 *  The component of the {@link SearchBox}. Depending on the data type of the attribute involved in the filter expression, the rendering selects the correct html tag to be rendered.
 *  Depending on whether the iteration index of the page is equal -1 the {@code TermTag} will be editable or not. An optional logical operator is rendered too (the default one is '=').
 */
public class SearchCriterion extends TermTag {
	 
	private String withOp;
	private ListIteratorAccessor iteratorAccessor = new ListIteratorAccessor();
		
	@Override
	public int doStartTag() throws JspException {
		return termStartTag();
	}

	@Override
	public int doEndTag() throws JspException {
		termEndTag();
		return super.doEndTag();
	}

	@Override
	public void tagMiddle(StringBuilder output) {
		if (withOp != null && withOp.compareTo("true") == 0) {
			String opTagId = getPath() + Utils.operatorIdSuffix;
			output.append("<td>");
			String opValue = (String) request().getParameter(opTagId);
			if (opValue==null) /* "back to list" management */
				opValue = (String) request().getAttribute(opTagId); 
			output.append(isEditable() ? ("<select " + Utils.idAndNameAttrs(opTagId) + ">"
										+ optionTag(opValue, "", "")
										+ optionTag(opValue, ">", ">")
										+ optionTag(opValue, "<", "<")
										+ optionTag(opValue, "N", "N")
										+ optionTag(opValue, "NN", "NN")
										+ "</select>")
									: ( HtmlUtils.htmlEscape(opValue == null ? "" : opValue) + Utils.hidden(opTagId, opValue))) ;
			output.append("</td>");
		}
	}

	
	
	@Override
	protected void tagPre(StringBuilder output) {
		output.append("<tr>");
	}

	@Override
	protected void tagPost() throws IOException {
		out().println("</tr>");
	}

	@Override
	public boolean isEditable() {
		return iteratorAccessor.getIteration(this) == -1;
	}

	public String getWithOp() {
		return withOp;
	}

	public void setWithOp(String withOp) {
		this.withOp = withOp;
	}
		
}
