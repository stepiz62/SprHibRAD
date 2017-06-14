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

import java.util.Locale;

import com.sprhibrad.framework.model.VerboseLiteral;

public class ShrLocale extends VerboseLiteral {

	private String id;
	private String name;

	public ShrLocale(Locale locale) {
		this.id = locale.toString();
		this.name = locale.getDisplayLanguage(locale) + (id.length() == 5 ? (" - " + locale.getDisplayCountry(locale)) : "");
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String literalField() {
		return "name";
	}

}
