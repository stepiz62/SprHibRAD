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
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import com.sprhibrad.framework.configuration.ShrDateFormatter;
import com.sprhibrad.framework.model.ShrEntity;

/**
 * This interface allows the delegated renderer to invoke the methods of the delegating manager of a data set.
 */
public interface DataSetManager {

	public String getOrder();

	public String getOrientation() ;

	public String getFields();

	public String getHeads();
	
	public String getEntityName();

	public String detailRowCommand(String entityName, String detailProperty, String id, Boolean viewProperty, String noDelete, ShrEntity detailObject);

	public MessageManager getMsgManager();

	public HttpServletRequest request();

	public JspWriter out();

	public String targetCommand(String caption, String target, String cssClass, String actionParams);
	
	public String orderParamPrefixName();
	
	public void outHidden(String name, String value) throws IOException;

	public ShrDateFormatter getDateFormatter();

	public String imageTag(String previewManagerEntity, String targetImageEntity, Serializable id, String fieldName, String targetImageField, String format);

	public String renderAddCommand();

	public String noRecords();
	
	public PageContext getPageContext();


}
