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

importPackage(Packages.java.lang);
importPackage(Packages.java.text);
importPackage(Packages.java.util);
importPackage(Packages.com.sprhibrad.framework.configuration);

function log(msg) {
	System.out.println(msg);
}

function locale() {
	var loc = new Locale(params["loc_lang"].value, params["loc_country"].value);
	return loc;
}

function cyFormatByPrm(value) {
	if (value == null)
		return "";
	else {
		var formatter = new ShrCurrencyFormatter();
		return formatter.getNumberFormat(locale()).format(value);
	}
}

function dateFormatByPrm(value) {
	return dateFormatByPatterns(value, "", "");
}

function dateFormatByPatterns(value, patternParm, stylePatternParm) {
	if (value == null)
		return "";
	else {
		var formatter = new ShrDateFormatter();
		formatter.setLocale(locale());
		return formatter.shrFormat(value, patternParm, stylePatternParm);
	}
}

function numFormatByPrm(value) {
	if (value == null)
		return "";
	else {
		var frmt = NumberFormat.getCurrencyInstance(locale());
		return frmt.format(value);
	}
}

function dictionary_(item) {
	return vars["dictionary"].msgOrKey(item);
}

function msgOrKey(item) {
	return vars["messageSource"].msgOrKey(item);
}

function msgOrKeyWithArgs(item, args) {
	return vars["messageSource"].msgOrKey(item, args);
}

function askAndPost(msg, formId, formaction) {
	if (confirm(msg)) {
		var form = document.getElementById(formId);
		form.action = formaction;
		form.submit();		
	}
}

function askAct(msg, url) {
	if (confirm(msg))  
		location.href = url;
}

