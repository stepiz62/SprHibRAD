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

import java.util.Vector;

public class MenuConfig {

	public class Item {
		public String entityOrPath;
		public String labelOrKey;

		public Item(String entityOrPath, String labelOrKey) {
			this.entityOrPath = entityOrPath;
			this.labelOrKey = labelOrKey;
		}

	}

	public Vector<Item> items;

	public void addItem(String entityOrPath, String labelOrKey) {
		if (items == null)
			items = new Vector<Item>();
		items.add(new Item(entityOrPath, labelOrKey));
	}

}
