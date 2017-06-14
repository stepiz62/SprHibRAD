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

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.ShrLocales;
import com.sprhibrad.framework.model.VerboseLiteral;

/**
 * By overriding the {{@link #verboseLiteral(Object)} method this class changes the processing way for the bound value that can have the String type 
 * (see the path attribute of the enclosing tag and look for the corresponding attribute it in the class specified as 'modelAttribute' attribute of the DataForm tag.
 * In particular the {@code VerboseLiteral} object is synthesized from this so typed value that is the locale value.
 * @see ShrSelectTag
 * @see com.sprhibrad.framework.controller.ShrLocale
 * @see com.sprhibrad.framework.configuration.ShrLocales
 * @see com.sprhibrad.framework.model.UserPrefs
 */
public class LocaleSelect extends ShrSelectTag {

	@Override
	protected VerboseLiteral verboseLiteral(Object actualvalue) {
		return ((ShrLocales) pageContext.getSession().getAttribute(Utils.shrLocales)).getMap().get(actualvalue);
	}

}
