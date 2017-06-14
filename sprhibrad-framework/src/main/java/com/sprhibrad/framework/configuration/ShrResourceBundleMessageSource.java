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

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.context.support.ResourceBundleMessageSource;

public class ShrResourceBundleMessageSource extends ResourceBundleMessageSource {

	public ShrResourceBundleMessageSource() {
		super();
		setBasename("messages");
	}

	public ShrResourceBundleMessageSource(String baseName) {
		super();
		setBasename(baseName);
	}

	private Locale currentUserLocale;

	public Locale getCurrentUserLocale() {
		return currentUserLocale;
	}

	public void setCurrentUserLocale(Locale locale) {
		this.currentUserLocale = locale;
	}

	public ResourceBundle shrGetResourceBundle(Locale locale) {
		return super.getResourceBundle((String) getBasenameSet().toArray()[0], locale);
	}

	public boolean shrIsFallbackToSystemLocale() {
		return super.isFallbackToSystemLocale();
	}

	public String msgOrKey(String key) {
		return msgOrKey(key, null);
	}

	public String msgOrKey(String key, Object[] args) {
		return getMessage(key, args, key, currentUserLocale);
	}

}
