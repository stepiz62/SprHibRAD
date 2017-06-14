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

import com.sprhibrad.framework.common.Utils;

/** 
 * A {@link NavigFormTag} that is the handler class for the "dataForm" tag. It manages the behavior of the data form built by a set of dataItem tag ({@link DataItem}) and possible details data grids.
 */
public class DataFormTag extends NavigFormTag {
	
	private String entityId;
	private String onLoadJs = "";
	
	@Override
	protected String getAction() {		
		return actionTheme() + (Utils.isAdd(request()) ? "save" : 
									((isEdit() ? "update" : "edit") + "/" + getEntityId() + ".html"));
	}

	
	@Override
	protected String jsOnLoad() {
		return super.jsOnLoad() + " " + onLoadJs;
	}


	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
	
	protected Boolean isEdit() {
		return Utils.isEdit(request());
	}
	
	@Override
	public String actionTheme() {
		return super.actionTheme() + getModelAttribute() + "/";
	}


	@Override
	protected String builtTitle() {
		return  msgMgr().msgOrKey("label." + (String) request().getAttribute("mode")) + " " + msgMgr().dictionary("entity." + extractModelAttribute());
	}


	public String getOnLoadJs() {
		return onLoadJs;
	}


	public void setOnLoadJs(String onLoadJs) {
		this.onLoadJs = onLoadJs;
	}

	
}
