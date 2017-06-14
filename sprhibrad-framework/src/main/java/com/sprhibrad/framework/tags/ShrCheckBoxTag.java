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

import org.springframework.web.servlet.tags.form.CheckboxTag;
import org.springframework.web.servlet.tags.form.TagWriter;

import com.sprhibrad.framework.common.Utils;
/**
 * The class adds to the Spring data-binding features of its ancestor, the double behavior (idle/edit) required by SprHibRAD (for a checkBox item) .
 */
public class ShrCheckBoxTag extends CheckboxTag {
	
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {

		TermTag editableBox = (TermTag) findAncestorWithClass(this, TermTag.class);
		boolean editable = editableBox.isEditable() && Utils.isContainerEditable(editableBox);;
		if (editableBox != null)
			setPath(editableBox.getPath());

		setDisabled(!editable);
		Object actualvalue = getBindStatus().getActualValue();

		if (! editable)
			Utils.outHidden(getPath(), String.valueOf(actualvalue), pageContext.getOut());			
		return super.writeTagContent(tagWriter);
	}
}
