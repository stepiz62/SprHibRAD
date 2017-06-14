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

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.controller.ShrController.NavigatorNode;

/**
 * The buttons for acting on a data form.
 */
public class FormButtons extends FormAccessor {
	private String actCaption;

	@Override
	public int doStartTag() throws JspException {
		try {
			out().println("<br><table class='formButtons'><tr>");
			ShrFormTag formTag = getFormTag();
			if (actCaption == null && !(formTag instanceof DataFormTag))
				actCaption = "";
			outButton(
					msgMgr().msgOrKey("command."
							+ (actCaption == null ? (isAdd() ? "add" : (isEdit() ? "save" : "edit")) : actCaption)),
					formTag.actionTheme(), null, null, null);
			if (actCaption != null || Utils.isEditing(request()))
				outButton(msgMgr().msgOrKey("command." + "cancel"), formTag.contextPathTheme(), "cancel", null, null);
			if (formTag instanceof DataFormTag && !Utils.isEditing(request())) {
				NavigatorNode node = getNavigatorNode(1);
				if (node.iteration != null)
					outButton(msgMgr().msgOrKey("command.backToList"), contextPath() + "/",
							node.entityName + "/" + node.listMode + "/" + node.iteration + ".html", null, null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Utils.isEditing(request()) ? super.doStartTag() : EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			out().println("</tr></table><br>");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.doEndTag();
	}

	public String getActCaption() {
		return actCaption;
	}

	public void setActCaption(String actCaption) {
		this.actCaption = actCaption;
	}
}
