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

import java.util.Stack;

import javax.servlet.jsp.PageContext;

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.controller.ShrController.NavigatorNode;
import com.sprhibrad.framework.model.ShrEntity;

/**
 * Substantially a class that goes up along the containment chain. to find the
 * SprHibRAD tag whose class is a child of
 * {@code org.springframework.web.servlet.tags.form.FormTag} and makes its
 * significant attributes available to the tag class that extends it.
 */

public class FormAccessor extends ShrTagSupport {

	public String getEntityName() {
		return getFormTag().extractModelAttribute();
	}

	public String rowCommand(String id, ShrEntity detailObject) {
		String entityName = getEntityName();
		if (request().getAttribute("listmode") == "choices")
			return styleWrap(href("select", msgMgr().msgOrKey("command.select"), entityName, id), "linkWrap");
		else {
			return styleWrap(href("view", msgMgr().msgOrKey("command.view"), entityName, id), "linkWrap")
					+ styleWrap(href("delete", msgMgr().msgOrKey("command.delete"), entityName, id, detailObject, null),
							"linkWrap");
		}
	}

	public ShrFormTag getFormTag() {
		return (ShrFormTag) findAncestorWithClass(this, ShrFormTag.class);
	}

	public String targetCommand(String caption, String target, String cssClass, String actionParams) {
		return inputTag(caption, contextPath() + "/" + getEntityName() + "/", action() + "/" + target + ".html",
				cssClass, actionParams, null);
	}

	protected String action() {
		return null;
	}

	public ShrEntity getEntityObj() {
		return (ShrEntity) request().getAttribute(getEntityName());
	}

	public NavigatorNode getNavigatorNode(int topRelativeIndex) {
		Stack<NavigatorNode> navigator = Utils.getNavigator(request());
		return navigator.get(navigator.size() - 1 - topRelativeIndex);
	}

	public PageContext getPageContext() {
		return pageContext;
	}

}
