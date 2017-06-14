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
import javax.servlet.jsp.PageContext;

import org.springframework.web.servlet.tags.form.AbstractHtmlInputElementTag;

import com.sprhibrad.framework.common.Utils;

/**
 * Gets the path from the container tag.
 */
public class InputDelegated  {

	AbstractHtmlInputElementTag delegator;
	PageContext delegatorPageContext;
	ShrTextInput shrTextInput;

	public InputDelegated(AbstractHtmlInputElementTag delegator, ShrTextInput shrTextInput) {
		this.delegator = delegator;
		this.shrTextInput = shrTextInput;
	}

	public boolean getEditable() throws JspException {
		TermTag editableBox = editableBox();
		boolean editable = false;
		if (editableBox != null) {
			editable = editableBox.isEditable();
			String tagPath = ((InputDelegator) delegator).shrGetPath();
			if (tagPath.isEmpty()) // warning Spring singleton implementation
									// (see doEndTag)
				delegator.setPath(editableBox.getPath());
			else if (tagPath.indexOf(".") >= 0)
				editable = false;
		}
		return editable && Utils.isContainerEditable(editableBox);
	}


	public int doEndTag() throws JspException {
		int retVal = shrTextInput.doEndTag();
		delegator.setPath(""); // dealing with singleton mode (al least what is
								// appearing to be)
		return retVal;
	}

	public TermTag editableBox() {
		return (TermTag) delegator.findAncestorWithClass(delegator, TermTag.class);
	}

}
