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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.springframework.web.servlet.tags.form.FormTag;
import org.springframework.web.servlet.tags.form.TagWriter;

import com.sprhibrad.framework.configuration.ShrResourceBundleMessageSource;

/**
 * The SprHibRAD bas class for rendering the html form.
 * It adds to the Spring features of its ancestor the tools useful to render the most part of aspects and behavior desired from the framework.
 * Exposes as public item the Spring modelAttribute member so that it is reachable from any contained SpriHibRAD tag that collaborates to the rendering of the form content .
 */
public class ShrFormTag extends FormTag {
	
	MessageManager msgManager = new MessageManager();
	
	MessageManager msgMgr() {
		msgManager.setPageContext(pageContext);
		return msgManager;
	}

	@Override
	protected String getMethod() {
		return "POST";
	}

	@Override
	protected String getAction() {
		return actionTheme() + super.getAction();
	}


	public String extractModelAttribute() {
		return getModelAttribute();
	}

	public String contextPathTheme() {
		return pageContext.getServletContext().getContextPath() + "/";
	}
	
	public String actionTheme() {
		return contextPathTheme();
	}

	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		try {
			pageContext.getOut().append(outsidePreRenderedHtml());
		} catch (IOException e) {
			e.printStackTrace();
		}
		int retVal = super.writeTagContent(tagWriter);
		try {
			pageContext.getOut().append(insidePreRenderedHtml());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retVal;
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().append(postRenderedHtml());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.doEndTag();
	}
	
	protected String appName() {
		return (String) pageContext.getServletContext().getAttribute("appName"); 
	}

	protected String heading() {
		String contextPath = pageContext.getServletContext().getContextPath();
		return "<head>"
						+ "<meta charset=\"UTF-8\">"
						+ "<link href='" + contextPath + "/shr.css" + "' rel='stylesheet' />"
						+ "<script type='text/javascript'  src='"  + contextPath + "/SprHibRAD.js' ></script>"
						+ "<title> " + (caption() == null ? builtTitle() : msgMgr().msgOrKey(caption(), new Object[] {appName()})) + "</title>"
						+ "</head>";
	}
	
	protected String outsidePreRenderedHtml() {
		return heading() + "<body>";
	}
	
	protected String builtTitle() {
		return super.getAction();
	}

	protected String caption() {
		return null;
	}

	protected String insidePreRenderedHtml() {
		return "<div class='container'>";
	}
	
	protected String postRenderedHtml() {
		return "</div>";
	}
	
	protected HttpServletRequest request() {
		return ( HttpServletRequest) pageContext.getRequest();
	}
	


}
