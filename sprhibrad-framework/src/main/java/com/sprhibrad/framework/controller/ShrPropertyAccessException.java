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

import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.TypeMismatchException;

import com.sprhibrad.framework.configuration.ShrDateFormatter;
import com.sprhibrad.framework.configuration.ShrResourceBundleMessageSource;

/** see {@link com.sprhibrad.framework.configuration.ShrRequestMappingHandlerAdapter} */

public class ShrPropertyAccessException extends PropertyAccessException {

	private ShrResourceBundleMessageSource messageSource;
	private ShrDateFormatter dateFormatter;

	private PropertyAccessException delegate;

	public ShrPropertyAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ShrPropertyAccessException(PropertyAccessException delegate, ShrResourceBundleMessageSource messageSource, ShrDateFormatter dateFormatter) {
		this(delegate.getMessage(), delegate.getCause());
		this.delegate = delegate;
		this.messageSource = messageSource;
		this.dateFormatter = dateFormatter;
	}

	@Override
	public String getErrorCode() {
		return delegate.getErrorCode();
	}

	@Override
	public String getPropertyName() {
		return delegate.getPropertyName();
	}

	@Override
	public Object getValue() {
		return delegate.getValue();
	}

	@Override
	public String getLocalizedMessage() {
		String msg = null;
		if(getErrorCode().compareTo("typeMismatch")==0) {
			if (getCause() instanceof NumberFormatException)
				msg = messageSource.msgOrKey(((TypeMismatchException) delegate).getRequiredType() == java.math.BigDecimal.class ? 
												"valid.bigDecimal" : "valid.number", new Object[] {getValue()});
			else
				msg = messageSource.msgOrKey("valid.date", new Object[] {getValue(), dateFormatter.toPattern()});
		}
		
		return msg;
	}
	
}
