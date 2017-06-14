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

import com.sprhibrad.framework.common.Utils;

/**
 * It provides the handle to go to the other side of a many-to-many relation, or
 * in other words, it lets the user to view or to manage the 'foreign' entity
 * from the context of a 'relation' entity.
 */
public class DetailProvider extends FormAccessor {
	private String detailMember;
	private String verb;
	private String targetId;
	private boolean readOnly;

	@Override
	public int doStartTag() {
		try {
			out().println(render(detailMember, verb, targetId));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return EVAL_BODY_INCLUDE;
	}

	public String getDetailMember() {
		return detailMember;
	}

	public void setDetailMember(String detailMember) {
		this.detailMember = detailMember;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	protected String render(String memberName, String verb, String targetId) {
		String retVal = "";
		String targetEntity = null;
		try {
			targetEntity = Utils.downCaseFirstChar(
					getEntityObj().getClass().getDeclaredField(memberName).getType().getSimpleName());
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		if (verb == null || verb.compareToIgnoreCase("select") == 0) {
			retVal += "<td class='detailProvider'>";
			if (Utils.isEditing(request()) && !readOnly)
				retVal += "<input type='submit' formaction='" + contextPath() + "/" + getEntityName() + "/freeze?redir="
						+ targetEntity + (memberName == null ? "" : ("&targetMember=" + memberName))
						+ "&action=choices'" + " value='"
						+ msgMgr().msgOrKey("command." + (isEdit() ? "change" : "select")) + "' />";
			else if (Utils.extractValue(memberName, getEntityObj()) != null)
				retVal += styleWrap(href("view", msgMgr().msgOrKey("label.inspect"), targetEntity, targetId),
						"linkWrap");
			retVal += "</td>";
		}
		return retVal;
	}

	public boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

}
