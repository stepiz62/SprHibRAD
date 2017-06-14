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

import javax.servlet.jsp.JspException;
/**
 * gets the iteration index from the request object.
 */
public class ListManager extends FormAccessor {
	private Integer iteration;

	@Override
	public int doStartTag() throws JspException {
		iteration = request().getAttribute("iteration") == null ? -1 : Integer
				.parseInt(request().getAttribute("iteration").toString());
		return EVAL_BODY_INCLUDE;
	}

	public Integer getIteration() {
		return iteration;
	}

}
