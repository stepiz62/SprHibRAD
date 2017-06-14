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

import org.springframework.web.servlet.tags.form.SelectTag;
import org.springframework.web.servlet.tags.form.TagWriter;

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.model.VerboseLiteral;
/**
 * It adds to the Spring features of its ancestor the double behavior (idle/edit) required by SprHibRAD (for a select item).
 * In the framework a particular class layout has provided for populating a Select tag, any class that extends the {@link VerboseLiteral} class. 
 * It is worth noting that the class allows either a {@code VerboseLiteral} or a simple String as binding type: in both cases it is 
 * {{@link #verboseLiteral(Object)} method responsibilities to return a {@code VerboseLiteral} object. 
 */

public class ShrSelectTag extends SelectTag {
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		int retVal = 0;

		TermTag editableBox = (TermTag) findAncestorWithClass(this, TermTag.class);
		boolean editable = editableBox.isEditable() && Utils.isContainerEditable(editableBox);
		if (editableBox != null)
			setPath(editableBox.getPath());

		if (editable)
			retVal = super.writeTagContent(tagWriter);
		else {
			Object actualvalue = getBindStatus().getActualValue();
			if (actualvalue != null)
				Utils.outHidden(getPath(), (String) (actualvalue instanceof String ? actualvalue : String.valueOf(((VerboseLiteral) actualvalue).getId())), pageContext.getOut());			
			try {
				VerboseLiteral verboseLiteralObject = actualvalue == null ? null : verboseLiteral(actualvalue);
				pageContext.getOut().println(actualvalue == null || verboseLiteralObject == null ? "" : verboseLiteralObject.getLiteral());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return retVal;
	}

	protected VerboseLiteral verboseLiteral(Object actualvalue) {
		return ((VerboseLiteral) actualvalue);
	}
}
