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

import java.beans.PropertyEditor;
import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.springframework.web.servlet.tags.form.TagWriter;
import org.springframework.web.servlet.tags.form.TextareaTag;

import com.sprhibrad.framework.common.Utils;

public class ShrTextAreaTag extends TextareaTag implements InputDelegator {
	
	InputDelegated delegated;

	public ShrTextAreaTag() {
		delegated = new InputDelegated(this, new ShrTextInput() {
			@Override
			public int doEndTag() throws JspException {
				return ShrTextAreaTag.super.doEndTag();
			}});
	}
	
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		this.setReadonly(! delegated.getEditable());
		return super.writeTagContent(tagWriter);
	}
	
	@Override
	public int doEndTag() throws JspException {
		return delegated.doEndTag();
	}

	@Override
	public String shrGetPath() throws JspException {
		return getPath();
	}


}
