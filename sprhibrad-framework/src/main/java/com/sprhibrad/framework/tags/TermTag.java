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

/**
 * It is the base class for containers of attribute rendering tags, generally it hosts the {@code path} targeted to them.
 * Prepares, furthermore, the 'editable' status that determines the double behavior of the inner tag.  
 */
public class TermTag extends FormAccessor {
	
	private String label;
	private String path;

	protected void tagPre(StringBuilder output) {}
	protected void tagTop(StringBuilder output) {
		String labelText;
		if (getLabel() == null) {
			String entityQualifier = msgMgr().isClassSpecificDictionary(getEntityName(), dictPath()) ? (getEntityName() + ".") : ""; 
			labelText = msgMgr().dictionary("attr." + entityQualifier + dictPath());
		} else {
			boolean applicationDictionaryKey = getLabel().charAt(0) != '_';
			labelText = applicationDictionaryKey ? msgMgr().dictionary(getLabel()) : msgMgr().msgOrKey(getLabel().substring(1));
		}
		output.append("<td class='label'>" + labelText + "</td>");
	}
	protected void tagMiddle(StringBuilder output) {}
	protected void tagBottom(StringBuilder output) {
		output.append("<td>");
	}
	protected void tagPost() throws IOException {}

	protected String dictPath() {
		return path;
	}
	
	public int termStartTag()  {
		try {
			StringBuilder output = new StringBuilder();
			tagPre(output);
			tagTop(output);
			tagMiddle(output);
			tagBottom(output);
			out().println(output);
			return EVAL_BODY_INCLUDE;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return SKIP_BODY;
	}

	protected void termEndTag() {
		try {
			out().println("</td>");
			tagPost();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isEditable() {
		return false;
	}

	public String renderedValue() {
		return getValue();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
