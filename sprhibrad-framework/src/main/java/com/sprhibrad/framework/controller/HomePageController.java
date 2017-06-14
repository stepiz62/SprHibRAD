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

package com.sprhibrad.framework.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sprhibrad.framework.common.DataSetClauses;
import com.sprhibrad.framework.configuration.UserManager;
import com.sprhibrad.framework.model.None;
import com.sprhibrad.framework.model.ShrEntity;
import com.sprhibrad.framework.service.ShrService;

@Controller
public class HomePageController extends ShrController<None> {

	@Autowired
	UserManager userManager;

	@RequestMapping(value = "/")
	public ModelAndView mainPage(HttpServletRequest request, HttpServletResponse response) {
		return goHome(request, response);
	}

	@RequestMapping(value = "/index")
	public ModelAndView indexPage(HttpServletRequest request, HttpServletResponse response) {
		return goHome(request, response);
	}

	private ModelAndView goHome(HttpServletRequest request, HttpServletResponse response) {
		getUserPrefs(request, response);
		return managedGo("home", request);
	}

	private ModelAndView managedGo(String viewName, HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView(viewName);
		manageNode(null, request, null, modelAndView, false, null);
		return modelAndView;
	}

	@RequestMapping(value = "/sessionexp")
	public ModelAndView sessionEspired(HttpServletRequest request) {
		return new ModelAndView("sessionexp");
	}

	@RequestMapping(value = "/noaccess")
	public ModelAndView noaccess(HttpServletRequest request) {
		return managedGo("noaccess", request);
	}

	@RequestMapping(value = "/changePwd")
	public ModelAndView changePwd(HttpServletRequest request) {
		String userName = request.getUserPrincipal().getName();
		DataSetClauses detailClauses = new DataSetClauses();
		detailClauses.addCriterion(userManager.getUserNameField(), null, userName);
		List<ShrEntity> objects = userManager.getUserService().getObjects(null, detailClauses, null);
		ModelAndView mav = loadObject(true, null, "changePwd_form", userManager.getUserEntityName(), request,
				objects.get(0)).mav;
		return mav;
	}

	@RequestMapping(value = "/cancel")
	protected String cancel(HttpServletRequest request) {
		return super.cancel(request);
	}

	@Override
	protected ShrService<None> getService() {
		return null;
	}

	@Override
	protected None getEntityInstance() {
		return null;
	}

}
