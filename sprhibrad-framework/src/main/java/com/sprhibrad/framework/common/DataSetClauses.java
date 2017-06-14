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

package com.sprhibrad.framework.common;

import java.util.HashMap;
import java.util.Vector;

public class DataSetClauses {

	public class Criterion {
		public String operator;
		public Object value;
		public Criterion(String operator, Object value) {
			this.operator = operator;
			this.value = value;
		}
	}

	public class OrderItem {
		public String field;
		public String orientation;
		public OrderItem(String field, String orientation) {
			this.field = field;
			this.orientation = orientation;
		}
	}

	public HashMap<String, Criterion> filter;
	public Vector<OrderItem> order;
	public String propertyToOrder;

	public DataSetClauses() {
		filter = new HashMap<String, Criterion>();
		order = new Vector<OrderItem>();
	}
	
	public void addCriterion(String key, String operator, Object object) {
		filter.put(key, new Criterion(operator, likeValue(object)));
	}

	private Object likeValue(Object object) {
		return object instanceof String ? (object + "%") : object;		
	}
	
	private Object  objectFromReport(Object input) {
		return String.valueOf(input).compareTo(Utils.shRepArgNull) == 0 ? "" : input;
	}
	
	public void addCriterionForReport(String key, String operator, Object object) {		
		filter.put(key, new Criterion((String) objectFromReport(operator), likeValue(objectFromReport(object))));
	}

	public void addOrderItem(String field, String orientation) {
		order.add(new OrderItem(field, orientation));
	}
	
	public void loadOrderClauses(String commaSeparatedFields, String commaSeparatedOrientations) {
		order.clear();
		if ( commaSeparatedFields != null ) {
			Vector<String> fieldVector = new Vector<String>();
			Vector<String> orientationVector = new Vector<String>();
			Utils.comaSplitAndTrim(commaSeparatedFields, fieldVector, commaSeparatedOrientations, orientationVector);
			int index = 0;
			for (String field : fieldVector) {
				addOrderItem(field, orientationVector.size() > index ? orientationVector.get(index) : "asc");
				index++;
			}
		}
	}
}
