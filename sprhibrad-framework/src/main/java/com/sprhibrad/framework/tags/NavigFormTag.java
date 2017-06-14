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
import java.util.Stack;

import javax.servlet.ServletContext;

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.EntityRenderer;
import com.sprhibrad.framework.configuration.MenuConfig;
import com.sprhibrad.framework.controller.ShrController.NavigatorNode;
import com.sprhibrad.framework.model.ShrEntity;
import com.sprhibrad.framework.model.UserPrefs;
import com.sprhibrad.names.Names;

/**
 * Does the most part of the rendering for the generic page: Relying on the
 * Navigator object stack, it draws the application menu or the navigation bar.
 * Furthermore the class provides just a little of javascript to keep the row
 * commands links short.
 */
public class NavigFormTag extends ShrFormTag {

	private String caption;
	private String innerCaption;

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getInnerCaption() {
		return innerCaption;
	}

	public void setInnerCaption(String innerCaption) {
		this.innerCaption = innerCaption;
	}

	public Boolean getHmenu() {
		UserPrefs userPrefs = ((UserPrefs) pageContext.getSession().getAttribute("userPrefs"));
		return userPrefs == null ? true : userPrefs.getHmenu();
	}

	@Override
	protected String outsidePreRenderedHtml() {
		String entityName = extractModelAttribute();
		String defaultEntity = Utils.escape(msgMgr().msgOrKey("entity." + Utils.upCaseFirstChar(entityName)));
		String targetMember = (String) request().getAttribute("targetMember");
		String scriptTag = "<script type='text/javascript' >" + "function add() { redir('add', 0); }"
				+ "function list(iter) { redir('list', iter); }" + "function choices(iter)  { redir('choices', iter); }"
				+ "function view(id) { redir('view', id); }" + "function select(id) { redir('select', id); }"
				+ "function del(id, renderedEntity) { redir('del', id, renderedEntity); }" + ""
				+ "function redir(action, identifier, renderedEntity, entityName, entityPath) {"
				+ "	var doIt = true;" + "	if (action == 'delete') {"
				+ "		var entityTarget = entityName == null ? '" + defaultEntity + "' : entityName;"
				+ "		var verboseTarget = renderedEntity == null ? '" + msgMgr().msgOrKey("question.delWhat")
				+ "'.replace('{0}', entityTarget) : renderedEntity;" + "		var msg = '"
				+ msgMgr().msgOrKey("question.deleteFrom") + "'.replace('{0}', verboseTarget);"
				+ "		doIt = confirm(msg);" + "	}" + "	var url = '" + contextPathTheme()
				+ "' + (entityPath == null ? '" + entityName + "' : entityPath) + '/' + action + "
				+ "				(identifier == 0 ? '' : ('/' + identifier + '.html')) + '"
				+ (targetMember == null ? "" : ("?targetMember=" + targetMember)) + "';" + "	if (doIt)"
				+ "		location.href = url;" + "}" + "</script>";

		return heading() + "<body onload=\"javascript:{" + jsOnLoad() + "}\" >" + scriptTag
				+ (caption == null ? "" : ("<h1>" + msgMgr().msgOrKey(caption, new Object[] { appName() }) + "</h1>"));
	}

	protected String jsOnLoad() {
		return getHmenu() ? ""
				: "var elem = document.getElementById('menu'); elem.scrollTop=(elem.scrollHeight - elem.clientHeight);";
	}

	@Override
	protected String caption() {
		return caption;
	}

	protected String builtTitle() {
		String retVal = null;
		if (innerCaption == null) {
			String modelAttribute = extractModelAttribute();
			retVal = modelAttribute.compareTo("command") == 0 ? ""
					: msgMgr().dictionary("entities." + Names.plural(modelAttribute));
		} else {
			String target = request().getParameter("op");
			String entityName = (String) request().getAttribute("entityName");
			String qualifier = msgMgr().isClassSpecificDictionary(entityName, target) ? (entityName + ".") : "";
			retVal = msgMgr().msgOrKey(innerCaption,
					new Object[] { msgMgr().dictionary("attr." + qualifier + target) });
		}
		return retVal;
	}

	@Override
	protected String insidePreRenderedHtml() {
		return "<div class='container'><div class='" + hmenuDependentClass("menu") + "' id='menu'>" + navigator()
				+ "</div>" + "<div class='" + hmenuDependentClass("page") + "'>" + "<h1>" + builtTitle() + "</h1>";
	}

	@Override
	protected String postRenderedHtml() {
		return "</div></div>";
	}

	private String hmenuDependentClass(String className) {
		return getHmenu() ? className : (className + "2");
	}

	private String navigator() {
		Stack<NavigatorNode> navigator = Utils.getNavigator(request());
		String retVal = "<table>";
		if (navigator == null || navigator.size() == 1)
			retVal += getMenu();
		else {
			String itemSet = "";
			int index = 0;
			NavigatorNode node = null;
			while (index < navigator.size() - 1) {
				node = navigator.get(index);
				if (node.iteration == null && (!navigator.get(index + 1).isEdit // listing
																				// but
																				// navigation
																				// exited
																				// the
																				// list
																				// session
						|| index == 0 && navigator.get(index + 1).url.compareTo("/changePwd") == 0) || // changePwd
																										// fired
																										// from
																										// the
																										// application
																										// menu
						node.iteration != null && node.iteration == -1 && // navigating
																			// iteration
																			// ....
								navigator.size() - index > 2 && navigator.get(index + 2).iteration == null)
					itemSet += navItem(node);
				index++;
			}
			retVal += (getHmenu() ? Utils.rowWrap(itemSet) : itemSet);
		}
		retVal += "</table>";
		return retVal;
	} // - , -1 , 1 , 0, null

	private String navItem(NavigatorNode node) {
		ShrEntity entity = node.getObject();
		String retVal = "<td class='" + hmenuDependentClass("navItem1") + "'>";
		if (entity != null)
			retVal += msgMgr().messageSource().getMessage("entity." + entity.getClass().getSimpleName(), null, "",
					null);
		retVal += "</td><td class='" + hmenuDependentClass("navItem2") + "'>";
		retVal += "<a href='" + request().getContextPath() + node.url + "'>"
				+ (entity == null
						? (node.entityName == null ? msgMgr().msgOrKey("view." + node.viewName)
								: msgMgr().dictionary("entities." + Names.plural(node.entityName)))
						: (entity.render() == null || entity.render().size() == 0
								? msgMgr().dictionary("entity." + node.entityName)
								: new EntityRenderer(pageContext).render(entity)))
				+ "</a>";
		retVal += "</td>";
		return getHmenu() ? retVal : Utils.rowWrap(retVal);
	}

	private void write(String text) {
		try {
			pageContext.getOut().append(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String menuItem(String path, String label, String postQuestion) {
		String url = request().getContextPath() + path;
		String retVal = "<td " + "class='" + hmenuDependentClass("menuItem") + "'><a href=\""
				+ (postQuestion == null ? url : "javascript:askAndPost('" + postQuestion + "','shr','" + url + "');")
				+ "\">" + label + "</a></td>  ";
		return getHmenu() ? retVal : Utils.rowWrap(retVal);
	}

	private String entityMenuItem(String entityOrPath) {
		boolean directPage = entityOrPath.charAt(0) == '/';
		String entity = directPage ? entityOrPath.substring(1) : entityOrPath;
		return menuItem("/" + entity + (directPage ? "" : "/list/-1.html"),
				msgMgr().dictionary(directPage ? "entity." + entity : "entities." + Names.plural(entity)), null);
	}

	private String getMenu() {
		ServletContext context = pageContext.getServletContext();
		MenuConfig menuConfig = (MenuConfig) context.getAttribute("menu");
		String retVal = "";
		for (MenuConfig.Item item : menuConfig.items)
			retVal += item.labelOrKey == null ? entityMenuItem(item.entityOrPath)
					: menuItem(item.entityOrPath, msgMgr().msgOrKey(item.labelOrKey), null);
		if (((String) context.getAttribute("userprefsmenu")).compareTo("true") == 0)
			retVal += menuItem("/userPrefs", msgMgr().msgOrKey("menu.userPrefs"), null);
		else
			retVal += menuItem("/changePwd", msgMgr().msgOrKey("menu.changePwd"), null);
		retVal += menuItem("/logout", msgMgr().msgOrKey("menu.logout"), msgMgr().msgOrKey("question.logout"));
		return getHmenu() ? Utils.rowWrap(retVal) : retVal;
	}

}
