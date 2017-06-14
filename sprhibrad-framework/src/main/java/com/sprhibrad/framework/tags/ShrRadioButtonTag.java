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

import org.springframework.web.servlet.tags.form.RadioButtonTag;
import org.springframework.web.servlet.tags.form.TagWriter;

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.ShrResourceBundleMessageSource;

/**
 * It adds to the Spring features of its ancestor the double behavior (idle/edit) required by SprHibRAD (for a radio item).
 * The labeling is got from the 'dictionary' (only). 
 * 
 */
public class ShrRadioButtonTag extends RadioButtonTag {
	String valueLabel;
	
	MessageManager msgManager = new MessageManager();
	
	MessageManager msgMgr() {
		msgManager.setPageContext(pageContext);
		return msgManager;
	}

	public String getValueLabel() {
		return valueLabel;
	}

	public void setValueLabel(String valueLabel) {
		this.valueLabel = valueLabel;
	}

	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		TermTag editableBox = (TermTag) findAncestorWithClass(this, TermTag.class);
		boolean editable = editableBox.isEditable() && Utils.isContainerEditable(editableBox);
		if (editableBox != null)
			setPath(editableBox.getPath());
		setDisabled(!editable);
		if (Integer.valueOf(String.valueOf(getValue())) == 0 && ! editable)
			Utils.outHidden(getPath(), String.valueOf(getBindStatus().getActualValue()), pageContext.getOut());			
		return super.writeTagContent(tagWriter);
	}

	@Override
	public int doEndTag() throws JspException {
		int retVal = super.doEndTag();
		try {
			pageContext.getOut().append(msgMgr().messageSource("dictionary").msgOrKey(valueLabel));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retVal;
	}


}
