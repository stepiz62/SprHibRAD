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

import java.io.IOException;

import javax.servlet.ServletContext;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.stereotype.Component;

import com.sprhibrad.framework.common.Utils;

/**
 * An accessor to the configuration file of the application. 
 */
public class ShrConfigurator {
    ConfigurableEnvironment env;

	public  ShrConfigurator() {
        env = new StandardEnvironment();
        try {
			env.getPropertySources().addFirst(new ResourcePropertySource(Utils.propertiesPathExpr));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getProperty(String name) {
		return env.getProperty(name);
	}

	public void loadSHRparamsIntoContext(ServletContext servletContext, MenuConfig menuConfig) {
        String userprefsmenu = getProperty("sprHibRad.userprefsmenu");        
        servletContext.setAttribute("userprefsmenu", userprefsmenu != null && Boolean.valueOf(userprefsmenu) ? "true" : "false");
        servletContext.setAttribute("menu", menuConfig);
        servletContext.setAttribute("modelPackage", getProperty("entitymanager.packages.to.scan"));
        servletContext.setAttribute("appName", getProperty("sprHibRad.appName"));
	}
}
