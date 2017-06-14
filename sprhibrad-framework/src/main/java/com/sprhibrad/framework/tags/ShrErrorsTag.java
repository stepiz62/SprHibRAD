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

import org.springframework.web.servlet.tags.form.ErrorsTag;
import org.springframework.web.servlet.tags.form.TagWriter;
/**
 * To be encapsulated within a {@code TermTag} tag, together with an actual entity's attribute hosting box, 
 * the class gets attribute name form its container, just like the attribute rendering tag, so that it is associated to the latter.   
 */
public class ShrErrorsTag extends ErrorsTag {

	public ShrErrorsTag() {
		super();
		super.setElement("td");
	}

	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		TermTag pathableParent = (TermTag) findAncestorWithClass(this, TermTag.class);
		if (pathableParent != null)
			setPath(pathableParent.getPath());
		if (shouldRender())
			try {
				pageContext.getOut().append("<br><table class='SHError'><tr>");
			} catch (IOException e) {
				e.printStackTrace();
			}
		return super.writeTagContent(tagWriter);
	}

	@Override
	public int doEndTag() throws JspException {
		int retVal = super.doEndTag();
		if (shouldRender())
			try {
				pageContext.getOut().println("</tr></table>");
			} catch (IOException e) {
				e.printStackTrace();
			}
		return retVal;
	}

	
}
