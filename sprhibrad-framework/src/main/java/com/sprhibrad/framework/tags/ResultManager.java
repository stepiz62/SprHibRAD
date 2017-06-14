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

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.ShrDateFormatter;
import com.sprhibrad.framework.model.ShrEntity;

/**
 * Acquires, as tag attributes, the fields to be rendered, the default orientation for sorting and the set of row actions. 
 * All the attributes that concur to the dbms inquiry, here can be picked up as tag attributes because the first time the
 * hosting jsp is rendered, the inquiry is still to be launched (that occurs when the search/list page is requested because
 * the navigation came to the entity to inspect the chances to compose the filter (iteration = -1).  
 */
  
public class ResultManager extends ListManagerAccessor implements IteratorDataSetManager {

	private String fields; 
	private String heads;
	private String order;
	private String orientation;
	
	
	@Override
	public int doStartTag() throws JspException {
		DataSetRenderer renderer = new  ResultSetRenderer(this);
		boolean drawContent = getIteration() > -1;

		order = renderer.getOrderAttr(order);
		orientation = renderer.getOrientationAttr(orientation);
		renderer.render(drawContent && fields != null);
		renderer.packOrder(order, orientation);

		return 	drawContent ?  EVAL_BODY_INCLUDE : SKIP_BODY;
	}


	public String orderParamPrefixName() {
		return Utils.iterResult + "-";
	}
	 
	
	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
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

	@Override 
	public String detailRowCommand(String entityName, String detailProperty, String id, Boolean viewProperty, String noDelete, ShrEntity detailObject) {
		return rowCommand(id, detailObject);
	}

	@Override
	public ShrDateFormatter getDateFormatter() {
		return (ShrDateFormatter) pageContext.getSession().getAttribute("dateFormatter");
	}

	@Override
	public String renderAddCommand() {
		return "";
	}


	@Override
	public String noRecords() {
		return msgMgr().msgOrKey("message.noRecords");
	}


	@Override
	public PageContext getPageContext() {
		return pageContext;
	}


	@Override
	public MessageManager getMsgManager() {
		return msgMgr();
	}
}
