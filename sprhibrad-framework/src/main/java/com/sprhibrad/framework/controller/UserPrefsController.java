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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.ShrLocales;
import com.sprhibrad.framework.model.UserPrefs;
import com.sprhibrad.framework.service.IShrService;

@Controller
public class UserPrefsController extends ImplShrController<UserPrefs> {

	@Autowired
	ShrLocales shrLocales;
	
	@Override
	protected IShrService<UserPrefs> getService() {
		return userPrefsService;
	}

	@Override
	protected UserPrefs getEntityInstance() {
		return new UserPrefs();
	}
    
    @RequestMapping(value="/userPrefs")
    protected ModelAndView viewObject(HttpServletRequest request, HttpServletResponse response) {
    	request.getSession().setAttribute(Utils.shrLocales, shrLocales); 
		return super.viewObject(getUserPrefs(request, response).getUserPrefs().getUser(), request, null);
	}
 
    @RequestMapping(value="/userPrefs/update/{user}")
	public ModelAndView updateObject(@Valid UserPrefs object, BindingResult result, HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView retVal = super.updateObject(object, result, request);
    	setUserPrefs(request, response, object);
		return retVal;
	}

    @RequestMapping(value="/userPrefs/edit/{user}")
	protected ModelAndView editObject(@PathVariable String user, HttpServletRequest request) {
		return super.editObject(user, request);
 	}

    @Override
	protected void addEditingAttributes(ModelAndView modelAndView, UserPrefs obj) {
    	modelAndView.addObject("locales", shrLocales.getList());
	}

    
}

