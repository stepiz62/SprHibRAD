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

import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.tags.form.InputTag;
import org.springframework.web.servlet.tags.form.TagWriter;

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.ShrDateFormatter;

/**
 * It adds to the Spring features of its ancestor the double behavior (idle/edit) required by SprHibRAD.
 * The class uses {@link ShrTagWriter} object to conditionally render an hidden type of input tag.
 */
public class ShrInputTag extends InputTag implements InputDelegator{

	InputDelegated delegated;
	public ShrInputTag() {
		delegated = new InputDelegated(this, new ShrTextInput() {
			@Override
			public int doEndTag() throws JspException {
				return ShrInputTag.super.doEndTag();
			}});
	}
	
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		ShrTagWriter writer = null;
		int retVal;
		this.getPath();
		boolean editable = delegated.getEditable();
		if (!editable)
			writer = new ShrTagWriter(pageContext);
		retVal = super.writeTagContent(editable ? tagWriter : writer);
		if (!editable) {
			try {
				pageContext.getOut().println(displayString(false));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return retVal;
	}
	
	@Override
	public int doEndTag() throws JspException {
		return delegated.doEndTag();
	}

	String displayString(boolean enabled) throws JspException {
		BindStatus bStatus = getBindStatus();
		Object actualValue = bStatus.getActualValue();
		Boolean isADate = Utils.isADate(actualValue);
		return (isADate
				? ((ShrDateFormatter) pageContext.getSession().getAttribute("dateFormatter"))
						.shrFormat(bStatus)
				: (enabled ? (actualValue == null ? "" : String.valueOf(actualValue)) : bStatus.getDisplayValue()));
	}

	@Override
	public String getDisplayString(Object value, PropertyEditor propertyEditor) {
		String retVal = null;
		try {
			retVal = displayString((delegated.editableBox()).isEditable());
		} catch (JspException e) {
			e.printStackTrace();
		}
		return retVal;
	}

	@Override
	public String shrGetPath() throws JspException {
		return getPath();
	}

}