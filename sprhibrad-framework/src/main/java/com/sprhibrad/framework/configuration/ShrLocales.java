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

import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import org.springframework.stereotype.Component;

import com.sprhibrad.framework.controller.ShrLocale;

/**
 * The class detects the locales, among all ones available at the platform level, for which a dictionary file does exist. 
 * and accommodates them in commonly useful data-structure made of a dedicated descendant of {@code VerboseLiteral} class, 
 * useful to expand a {@code java.util.Locale} object into a VerboseLiteral object.
 * The so built data structures serves to resolve the bounded value ({@code map} member) in the userPrefs_form.jsp and to load 
 * the list ({@code list} member) of locales provided for the application.
 *
 * see {@link ShrLocale}
 */
@Component
public class ShrLocales {

	private HashMap<String, ShrLocale> map;
	private Vector<ShrLocale> list;

	public HashMap<String, ShrLocale> getMap() {
		check();
		return map;
	}

	public Vector<ShrLocale> getList() {
		check();
		return list;
	}

	public void check() {
		if (map == null) {
			map = new HashMap<String, ShrLocale>();
			list = new Vector<ShrLocale>();
			addLocale(Locale.getDefault());
			ShrResourceBundleMessageSource messageSource = new ShrResourceBundleMessageSource();
			boolean oldIsFallbackToSystemLocale = messageSource
					.shrIsFallbackToSystemLocale();
			messageSource.setFallbackToSystemLocale(false);
			for (Locale loc : Locale.getAvailableLocales())
				if (messageSource.shrGetResourceBundle(loc) != null)
					addLocale(loc);
			messageSource.setFallbackToSystemLocale(oldIsFallbackToSystemLocale);
		}
	}

	private void addLocale(Locale loc) {
		if (map.get(loc.toString()) == null) {
			ShrLocale shrLocale = new ShrLocale(loc);
			list.add(shrLocale);
			map.put(loc.toString(), shrLocale);
		}
	}

}
