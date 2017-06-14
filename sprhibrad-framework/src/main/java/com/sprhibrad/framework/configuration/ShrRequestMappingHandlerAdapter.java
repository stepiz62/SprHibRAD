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

package com.sprhibrad.framework.configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.sprhibrad.framework.controller.ShrBindingErrorProcessor;

/**
 * As object type returned by the override of {link
 * WebMvcConfigurationSupport#createRequestMappingHandlerAdapter} of the
 * '@configuration annotated' class, together with
 * {@code com.sprhibrad.framework.controller.ShrBindingErrorProcessor#} and
 * {@code com.sprhibrad.framework.controller.ShrPropertyAccessException} the
 * class informs Spring binding process to consider SprHibRAD requirements about
 * warning the user in case of wrong format used, customizing the way Spring
 * suggests the user the correct way to do {@link ShrDateFormatter}.
 */
public class ShrRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

	@Autowired
	ShrDateFormatter dateFormatter;

	public ShrRequestMappingHandlerAdapter() {
		super();
	}

	@Autowired
	ShrBindingErrorProcessor shrBindingErrorProcessor;

	@Override
	protected ModelAndView invokeHandlerMethod(HttpServletRequest request, HttpServletResponse response,
			HandlerMethod handlerMethod) throws Exception {
		HttpSession session = request.getSession();
		ShrResourceBundleMessageSource messageSource = (ShrResourceBundleMessageSource) session
				.getAttribute("messageSource");
		ShrDateFormatter dateFormatter = (ShrDateFormatter) session.getAttribute("dateFormatter");
		if (dateFormatter != null) {
			this.dateFormatter.setPattern(dateFormatter.getPattern());
			this.dateFormatter.setLocale(dateFormatter.getLocale());
		}
		shrBindingErrorProcessor.setMessageSource(messageSource, dateFormatter);
		return super.invokeHandlerMethod(request, response, handlerMethod);
	}

}
