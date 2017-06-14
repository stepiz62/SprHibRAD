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
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.web.util.HtmlUtils;

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.EntityRenderer;
import com.sprhibrad.framework.configuration.ShrResourceBundleMessageSource;
import com.sprhibrad.framework.model.ShrEntity;
/** 
 * It is the base class for all the class of the package a part from those ones that extend Spring Form Tags.
 * Beyond providing features such as access to the dictionaries, the class makes available fragment of rendering 
 * that traverse the hierarchy of the package and for that other locations for centralizing the feature would be inappropriate, at least for how the hierarchy was developed: 
 * at the top level, at least, they are sustained by the role of factory. 
 */
public class ShrTagSupport extends TagSupport {
	private String value;
	private String msgKey;

	MessageManager msgManager = new MessageManager();
	
	MessageManager msgMgr() {
		msgManager.setPageContext(pageContext);
		return msgManager;
	}
	
	public String wantToDelQuestion(String whatKey) {
		String verboseTarget = msgMgr().msgOrKey("question.delWhat", new String[] {whatKey == null ?  msgMgr().msgOrKey("enitity.item") : msgMgr().msgOrKey(whatKey)});
		return msgMgr().msgOrKey("question.delete", new String[] {verboseTarget});
	}

	public HttpServletRequest request() {
		return (HttpServletRequest) pageContext.getRequest();
	}

	protected String contextPath() {
		return pageContext.getServletContext().getContextPath();
	}
	
	public JspWriter out() {
		return pageContext.getOut();
	}

	protected Boolean isEdit() {
		return Utils.isEdit(request());
	}
	
	protected Boolean isAdd() {
		return Utils.isAdd(request());
	}
	
	protected String optionTag(String currentVal, String value, String content) {
		return  "<option "
				+ (currentVal != null && currentVal.compareTo(value)== 0 ? "selected='selected'" : "")
				+ "value='"
				+ value
				+ "'>"
				+  HtmlUtils.htmlEscape(content)
				+ "</option>";
	}
		
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMsgKey() {
		return msgKey;
	}

	public void setMsgKey(String msgKey) {
		this.msgKey = msgKey;
	}


	protected void outHref(String subPath, String literal, String entityName, String id) throws IOException {
		out().println(href(subPath, literal, entityName, id));
	}

	protected String href(String subPath, String literal, String entityName, String id) {
		return href(subPath, literal, entityName, id, null, null);
	}

	protected String href(String subPath, String literal, String entityName, String identifier, ShrEntity entity, String verboseEntityName) {
		String langEntity = null;
		langEntity = entityName == null ? null : Utils.escape(msgMgr().msgOrKey("entity." + Utils.upCaseFirstChar(verboseEntityName == null ? entityName : verboseEntityName)));
		String href = getUrl(subPath, identifier, entity, langEntity, entityName);
		String label = literal == null ? msgMgr().msgOrKey(subPath) : literal;
		return "<a href=\"" + href + "\">" + label + "</a>";
	}
	
	protected String  getUrl(String subPath,String identifier, ShrEntity entity, String entityName, String entityPath) {		
		boolean defaultEntity = entityName == null;
		return  "javascript:redir('" + subPath + "'," + (identifier==null ? 0 : identifier) + 
						(entity == null ? (defaultEntity ? "" : ",null") : (",'" + renderEntity(entity) + "'")) + 
						(defaultEntity ? "" : (",'" + entityName + "','" + entityPath + "'")) + 
						");";
	}
	
	protected String renderEntity(ShrEntity entity) {
		return new EntityRenderer(pageContext).render(entity);
	}

	/* used in ELs */
	public static String renderEntity(ShrEntity entity, PageContext pageContext) {
		return new EntityRenderer(pageContext).render(entity) + (Utils.isAMultiModePage(pageContext.getRequest()) ? "" :
				" <<<<<< SprHibRAD: Usage not allowed: make the entity of the atribute used as search criterium to be an instance of the VerboseLiteral class or remove the criterium from the search box ! >>>>>>>");
	}          
	
	public void outHidden(String name, String value) throws IOException {
		Utils.outHidden(name, value, out());
	}

	protected String inputTag(String caption, String actionAttr, String cssClass) {
		String cssClassAttr = cssClass == null ? "" : ("class='" + cssClass + "'");
		return "<input " + cssClassAttr + " value='" + caption + "' type='submit'" + actionAttr + " />";
	}

	protected String inputTag(String caption, String actionTheme, String action, String cssClass, String confirmMsg) {
		return inputTag(caption, actionTheme, action, cssClass, null, confirmMsg);
	}
	
	protected String inputTag(String caption, String actionTheme, String action, String cssClass, String actionParams, String confirmMsg) {
		StringBuilder actionAttr = new StringBuilder("");
		if (action != null) {
			actionAttr.append(" formaction=\"");
			String url = actionTheme + action + (actionParams == null ? "" : ("?" + actionParams)) ;
			actionAttr.append(confirmMsg == null ?  url : "javascript:askAct('" + confirmMsg + "','" + url + "');");
			actionAttr.append("\"");
		}		
		return inputTag(caption, actionAttr.toString(), cssClass);
	}
	
	protected void outButton(String caption, String actionTheme, String action, String confirmMsg, String cssClass) throws IOException {
		outInputTag(caption, actionTheme, action, true, confirmMsg, cssClass);
	}

	protected void outInputTag(String caption, String actionTheme, String action, boolean cellWrap, String confirmMsg, String cssClass) throws IOException {
		String tag = inputTag(caption, actionTheme, action, cssClass, confirmMsg);
		out().println(cellWrap ? Utils.cellWrap(tag) : tag);
	}
	
	public  String styleWrap(String content, String cssClass) {
		return 	"<b class='" + cssClass + "'>" + content + "</b>";
	}

	protected String getText() {
		return msgMgr().msgOrKey(getMsgKey());
	}

	public String detailRowCommand(String entityName, String detailProperty, String id, Boolean viewProperty, String noDelete, ShrEntity childObject) {
		String html = "";
		ShrEntity detailObject = detailProperty == null ? childObject :  (ShrEntity) Utils.extractValue(detailProperty, childObject);
		String viewId = viewProperty ? String.valueOf(detailObject.getId()) : id;
		String childEntityName = Utils.lowCaseName(childObject);
		String detailEntityName = Utils.lowCaseName(detailObject);
		String viewEntityName = viewProperty ? detailEntityName : childEntityName;
		html += styleWrap(href("view", msgMgr().msgOrKey("command.view"), viewEntityName, viewId, null, entityName), "linkWrap");
		if (noDelete == null || noDelete.compareTo("true") != 0 )
			html += styleWrap(href("delete", msgMgr().msgOrKey("command.delete"), childEntityName, id, childObject, entityName), "linkWrap");
		return html;
	}

	public String imageTag(String entityName, String targetImageEntity, Serializable id, String fieldName, String targetImageField, String format) {
		return imageTag(entityName, targetImageEntity, id,  fieldName, targetImageField, format, null, null, false);
	}
		
	public String imageTag(String previewManagerEntity, String targetImageEntity, Serializable id, String fieldName, String targetImageField, String format, String width, String height, boolean fromObjectView) {
		StringBuilder labString = new StringBuilder("");
		String prefixForTarget = contextPath() + "/";
		String prefixForPreview = prefixForTarget;
		if (format != null) {
			prefixForTarget += (targetImageEntity == null ? previewManagerEntity : targetImageEntity) + "/";
			prefixForPreview += previewManagerEntity + "/";
		}
		if (targetImageField != null && format != null) {
			labString.append("<a href='" + prefixForTarget + "viewImage?target=" + targetImageField);
			labString.append("&" + "id=" + (fromObjectView ? "" : id));
			labString.append("' target='_blank'>");
		}
		String source = format == null ? "noImg.jpg" : (Utils.getImageKey(request().getUserPrincipal().getName(), previewManagerEntity, id, fieldName) +  "." + format);
		labString.append("<img src='" + prefixForPreview + source + "'" + attribute("width", width) + attribute("height", height) + " />");
		if (targetImageField != null && format != null)
			labString.append("</a>");
		return labString.toString();
	}

	String attribute(String name, String value) {
		return value == null ? "" : (name + "='" + value + "' ");
	}
	

}
