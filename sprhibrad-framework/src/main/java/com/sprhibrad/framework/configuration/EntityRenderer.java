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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.jsp.PageContext;

import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;
import org.springframework.format.number.NumberStyleFormatter;
import org.springframework.format.number.PercentStyleFormatter;

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.model.ShrEntity;

/** 
 * 
 * It renders, as text, the entity instance basing on the attribute the entity exposes for that purpose.
 *
 */
public class EntityRenderer {
	PageContext pageContext;

	public EntityRenderer(PageContext pageContext) {
		this.pageContext = pageContext;
	}

	public String render(ShrEntity entity) {
		StringBuilder retVal = new StringBuilder("");
		Vector<String> verbosFields = null;
		if (entity != null)
			verbosFields = entity.render();
		boolean first = true;
		if (verbosFields == null)
			retVal.append("- - -");
		else
			for (String fieldName : verbosFields) {
				if (!first)
					retVal.append(" ");
				retVal.append(verboseFieldRendering(fieldName, entity));
				if (first)
					first = false;
			}
		return retVal.toString();
	}

	private String verboseFieldRendering(String fieldName, ShrEntity entity) {
		Field field = null;
		try {
			field = entity.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		Object fieldValue = Utils.extractValue(fieldName, entity);
		if (fieldValue == null)
			return "";
		else {
			ShrDateFormatter dateFormatter = (ShrDateFormatter) pageContext.getSession().getAttribute("dateFormatter");
			if (fieldValue instanceof String)
				return (String) fieldValue;
			else if (Utils.isADate(fieldValue)) {
				return dateFormatter.shrFormat(fieldValue);
			} else if (fieldValue instanceof Number) {
				Locale locale = dateFormatter.getLocale();
				if (fieldValue instanceof BigDecimal)
					return new ShrCurrencyFormatter().getNumberFormat(locale).format(fieldValue);
				else {
					Annotation numberFormatAnnotation = field.getAnnotation(NumberFormat.class);
					boolean isPercent = false;
					if (numberFormatAnnotation != null) {
						Style style = (Style) Utils.annotationMethod(numberFormatAnnotation, "style");
						isPercent = style == Style.PERCENT;
					}
					if (isPercent)
						return new PercentStyleFormatter().print((Number) fieldValue, locale);
					else
						return new NumberStyleFormatter().getNumberFormat(locale).format(fieldValue);
				}
			} else
				return String.valueOf(fieldValue == null ? "" : fieldValue);
		}
	}
}
