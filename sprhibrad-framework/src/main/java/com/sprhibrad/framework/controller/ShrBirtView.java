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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.web.servlet.view.AbstractView;

import com.sprhibrad.framework.common.Utils;

/**
 * This class is an helper for interfacing with BIRT reporting tools.
 * Is is built with the goal to keep SprHibRAD away to have BIRT in its class path.
 * The application code, instead, must have it, because is the application code that will invoke the BIRT APIs.
 */
public abstract class ShrBirtView  extends AbstractView {

    @Resource
    private Environment env;

    protected HashMap<String, HashMap<String, Object>> reportParamsMap = new HashMap<String, HashMap<String, Object>>();
	
	public void render(Map<String, String> map, HttpServletRequest request,
						HttpServletResponse response, HashMap<String, Object> params) throws Exception {
		makeReportParamAvailable(request, map, params);
		render(map, request, response);
	}

	private void makeReportParamAvailable(HttpServletRequest request, Map<String, String> map, HashMap<String, Object> params) {
		String reportKey = getReportKey(request, map);
		reportParamsMap.put(reportKey, params);
	}
 	
	public String getReportKey(HttpServletRequest request, Map<String, String> map) {
		return request.getUserPrincipal().getName() + "_" + shr_getName(map) + "_" + shr_getFormat(map);
	}
	/** it needs to be called for the memory be disposed and after the call to getReportsPath */
	protected void shr_setParameters(final Object task, HttpServletRequest request, Map<String, String> map) {
		HashMap<String, Object> params = reportParamsMap.remove(getReportKey(request, map));
		params.forEach(new BiConsumer<String, Object>() {
			@Override
			public void accept(String name, Object value) {
				shr_setParameter(task, name, String.valueOf(value).isEmpty() ? Utils.shRepArgNull : value);
			}});		
	}

	protected String shr_getFormat(Map<String, String> map) {
		return map.get("format");
	}

	protected String shr_getName(Map<String, String> map) {
		return map.get("name");
	}
	
	/** it needs to be called before the call to shr_setParameters  */
	protected String getReportsPath(Map map, HttpServletRequest request) {
		String langParameterizedReports = env.getProperty("sprHibRad.langParameterizedReports");
		HashMap<String, Object> params = reportParamsMap.get(getReportKey(request, map));
			String subPath = langParameterizedReports != null && langParameterizedReports.compareToIgnoreCase("true")==0 ? "" :
								("/" + new Locale((String) params.get("loc_lang"), (String) params.get("loc_country")).toString()); 
		return "/reports" + subPath;
	}

	abstract protected void shr_setParameter(Object task, String name, Object value);
}
