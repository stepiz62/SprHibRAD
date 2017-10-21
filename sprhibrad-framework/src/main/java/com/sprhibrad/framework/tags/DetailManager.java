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

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.ShrDateFormatter;
import com.sprhibrad.framework.model.ShrEntity;
import com.sprhibrad.names.Names;

/**
 * Differently from its sibling {@code ResultManager}, this class doesn't
 * collect column orders and orientations as attributes because at the time of
 * the first processing by the jsp container the dbms inquiry has been already
 * done and there the order must have been caught by the code: (the application
 * must invoke {@link com.sprhibrad.framework.controller.ShrController#addDetailsAttribute}
 * in its override of {@link com.sprhibrad.framework.controller.ImplShrController#addDetailsAttributes}),
 * so that, here, those two attribute are already known and the delegated
 * renderer gets them from the request object.
 * 
 * An important attribute is {@code viewProperty} that, when the details entity
 * is a 'relation' entity, states that the {@code property} attribute represents
 * the target entity for the row command of the grid.
 */

public class DetailManager extends FormAccessor  implements DetailsDataSetManager {

	private String fields; 
	private String heads;
	private String order;
	private Boolean noAdd=false;
	private Boolean viewProperty = false;
	
	public String getOrder() {
		return order;
	}

	public String getOrientation() {
		return orientation;
	}

	private String orientation;
	private String entity;
	private String noDelete;
	private String property;

	@Override
	public int doStartTag() throws JspException {
		DataSetRenderer renderer = new  DetailSetRenderer(this);
		boolean drawContent = ! Utils.isEditing(request());

		order = renderer.getOrderAttr(order);
		orientation = renderer.getOrientationAttr(orientation);
		renderer.render(drawContent && fields != null);
		renderer.packOrder(order, orientation);
		
		return 	drawContent ?  EVAL_BODY_INCLUDE : SKIP_BODY;
	}	

	@Override
	public String orderParamPrefixName() {
		return entity + "s-";
	}
	
	@Override
	protected String action() {
		return "view";
	}

	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}

	public String getHeads() {
		return heads;
	}

	public void setHeads(String heads) {
		this.heads = heads;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getNoDelete() {
		return noDelete;
	}

	public void setNoDelete(String noDelete) {
		this.noDelete = noDelete;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	@Override
	public ShrDateFormatter getDateFormatter() {
		return (ShrDateFormatter) pageContext.getSession().getAttribute("dateFormatter");
	}

	@Override
	public ShrEntity getEntityObj() {
		return null;
	}

	@Override
	public String renderAddCommand() {
		return noAdd ? "" : Utils.isEditing(request()) ? "" :
								"<td>"
								+ styleWrap("<a href='" + contextPath() + "/" + entity + "/add" + "?fk=" + 
											request().getAttribute(Utils.toParentFKslotPrefix + Names.plural(entity)) + "' >" +  msgMgr().msgOrKey("command.add")
											+ "</a>", "linkWrap")
								+ "</td>";
	}

	public Boolean getNoAdd() {
		return noAdd;
	}

	public void setNoAdd(Boolean noAdd) {
		this.noAdd = noAdd;
	}

	public Boolean getViewProperty() {
		return viewProperty;
	}

	public void setViewProperty(Boolean viewProperty) {
		this.viewProperty = viewProperty;
	}

	@Override
	protected String renderEntity(ShrEntity entity) {
		return super.renderEntity((ShrEntity) (property == null ? entity : Utils.extractValue(property, entity)));
	}

	@Override
	public String noRecords() {
		return "";
	}


	@Override
	public MessageManager getMsgManager() {
		return msgMgr();
	}

	
}
