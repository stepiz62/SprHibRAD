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

import java.io.IOException;

/**
 * Provides availability of the iteration index, picking it up from the {@code ListManager}
 */
public class ListIteratorAccessor extends ShrTagSupport {

	public Integer getIteration(FormAccessor formAccessor) {
		Integer iteration = null;
		ListManager listManager = (ListManager) findAncestorWithClass(formAccessor, ListManager.class);
		if (listManager != null)
			iteration = (listManager).getIteration();
		if (iteration==null)
			try {
				out().println("iteration == null");
			} catch (IOException e) {
				e.printStackTrace();
			}
		return iteration;
	}
}
