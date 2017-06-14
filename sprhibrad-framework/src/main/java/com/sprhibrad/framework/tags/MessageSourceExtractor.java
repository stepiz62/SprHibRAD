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

import javax.servlet.jsp.PageContext;

import com.sprhibrad.framework.configuration.ShrResourceBundleMessageSource;

/**
 * Used by the framework to define/access the message sources used.
 *
 */
public class MessageSourceExtractor {
	String baseName;
	PageContext pageContext;

	public MessageSourceExtractor(String baseName, PageContext pageContext) {
		this.baseName = baseName;
		this.pageContext = pageContext;
	}

	public ShrResourceBundleMessageSource get() {
		String sessionParam = baseName == null ? "messageSource" : baseName;
		ShrResourceBundleMessageSource messageSource = (ShrResourceBundleMessageSource) pageContext.getSession()
				.getAttribute(sessionParam);
		return messageSource == null
				? (baseName == null ? new ShrResourceBundleMessageSource()
						: new ShrResourceBundleMessageSource(baseName))
				: messageSource;
	}

}
