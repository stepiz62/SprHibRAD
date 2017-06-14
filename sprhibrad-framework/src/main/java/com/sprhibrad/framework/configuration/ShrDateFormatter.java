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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.BindStatus;

/**
 * The class prepares the formatting behavior getting from the configuration
 * data and the resulting local information and it forces a four digits year.
 * The framework lets the formatting behavior be superseded by the possible
 * inherent annotation directive of the model attribute being formatted.
 */
@Component
public class ShrDateFormatter extends DateFormatter {

	private Locale locale;

	public Locale getLocale() {
		return locale;
	}

	private String pattern;
	private String stylePattern;

	public ShrDateFormatter() {
		stylePattern = new ShrConfigurator().getProperty("sprHibRad.dateStyle");
		setStylePattern(stylePattern);
		setLenient(false);
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
		pattern = fourDigitsYearPattern(locale);
		setPattern(pattern);
	}

	public String shrFormat(BindStatus bindingStatus) {
		return stylePattern.length() == 0 ? bindingStatus.getDisplayValue() : shrFormat(bindingStatus.getActualValue());
	}

	public String shrFormat(Object actualvalue) {
		return shrFormat(actualvalue, "", "");
	}

	private String fourDigitsYearPattern(Locale locale) {
		String pattern = ((SimpleDateFormat) getDateFormat(locale)).toPattern();
		return pattern.indexOf("yy") == pattern.lastIndexOf("yy") ? pattern.replace("yy", "yyyy") : pattern;
	}

	public String shrFormat(Object actualvalue, String patternParm, String stylePatternParm) {
		String retVal;
		if (stylePatternParm.length() > 0)
			setStylePattern(stylePatternParm);
		if (patternParm.length() > 0)
			setPattern(patternParm);

		retVal = print((Date) actualvalue, locale);
		if (stylePatternParm.length() > 0)
			setStylePattern(stylePattern);
		if (patternParm.length() > 0)
			setPattern(pattern);
		return retVal;
	}

	public String toPattern() {
		return ((SimpleDateFormat) getDateFormat(locale)).toPattern();
	}

	@Override
	public String print(Date date, Locale locale) {
		return super.print(date, this.locale);
	}

	@Override
	public Date parse(String text, Locale locale) throws ParseException {
		setPattern(pattern);
		return super.parse(text, this.locale);
	}

	public String getPattern() {
		return pattern;
	}

}
