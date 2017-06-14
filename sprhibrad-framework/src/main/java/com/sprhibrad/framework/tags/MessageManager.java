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

package com.sprhibrad.framework.tags;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.servlet.jsp.PageContext;

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.ShrResourceBundleMessageSource;
import com.sprhibrad.framework.model.ClassSpecificDictionary;

/**
 * Mainly thought for sharing code among tag class having different ancestor
 * paths. The most critical code here is the setter method for the
 * {@code PageContext} that must be invoked at every method invocation: the
 * delegating tag class, living as singleton, has no valid PageContext object
 * available at its construction time, so that it needs to be equipped with some
 * sort of accessor method in which, before returning a reference to this
 * object, the setter method is invoked.
 */
public class MessageManager {
	PageContext pageContext;

	public void setPageContext(PageContext pageContext) {
		this.pageContext = pageContext;
	}

	public ShrResourceBundleMessageSource messageSource() {
		return messageSource(null);
	}

	public ShrResourceBundleMessageSource messageSource(String baseName) {
		return new MessageSourceExtractor(baseName, pageContext).get();
	}

	public String msgOrKey(String key) {
		return messageSource().msgOrKey(key);
	}

	public String msgOrKey(String key, Object[] args) {
		return messageSource().msgOrKey(key, args);
	}

	public String dictionary(String key) {
		return messageSource("dictionary").msgOrKey(key);
	}

	public Class getModelClass(String entityName) {
		Class retVal = getPackageClass(entityName,
				(String) pageContext.getServletContext().getAttribute("modelPackage"), true);
		if (retVal == null)
			retVal = getPackageClass(entityName, "com.sprhibrad.framework.model", false);
		return retVal;
	}

	public Class getPackageClass(String entityName, String packageName, boolean silent) {
		Class retVal = null;
		try {
			retVal = Class.forName(packageName + "." + Utils.upCaseFirstChar(entityName));
		} catch (SecurityException | ClassNotFoundException e) {
			if (!silent)
				e.printStackTrace();
		}
		return retVal;
	}

	public boolean isClassSpecificDictionary(String entityName, String fieldName) {
		Field field;
		Annotation classSpecificDictionaryAnnotation;
		field = null;
		classSpecificDictionaryAnnotation = null;
		try {
			field = getModelClass(entityName).getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		if (field != null)
			classSpecificDictionaryAnnotation = field.getAnnotation(ClassSpecificDictionary.class);
		return classSpecificDictionaryAnnotation != null;
	}
}
