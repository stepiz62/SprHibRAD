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

/*
 * Renders the links needed to act on the row instance of the detail entity.
 */
public class DetailRowCommand extends ShrTagSupport {
	private String id;
	private String entityName;
	private String noDelete;
	private Boolean viewProperty = false;

	@Override
	public int doStartTag() throws JspException {
		try {
			if (id != null)
				out().println(detailRowCommand(entityName, null, id, viewProperty, noDelete, null));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.doStartTag();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getNoDelete() {
		return noDelete;
	}

	public void setNoDelete(String noDelete) {
		this.noDelete = noDelete;
	}

	public boolean isViewProperty() {
		return viewProperty;
	}

	public void setViewProperty(boolean viewProperty) {
		this.viewProperty = viewProperty;
	}
}
