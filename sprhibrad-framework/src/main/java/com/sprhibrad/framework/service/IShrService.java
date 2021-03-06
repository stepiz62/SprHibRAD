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

package com.sprhibrad.framework.service;

import java.io.Serializable;
import java.util.List;

import com.sprhibrad.framework.common.DataSetClauses;
import com.sprhibrad.framework.model.ShrEntity;

public interface IShrService<T> {
	
	public List<T> getObjects(Integer iteration, DataSetClauses clauses, Integer pageSize);

	public List<T> getDetailObjects(String mainEntityName, ShrEntity object, DataSetClauses detailClauses);

	public void addObject(T object);

	public T getObject(Serializable id);

	public void updateObject(T object);

	public void deleteObject(Serializable id);

	public void uploadBinary(byte[] bytes, String op, String pp, Serializable id);

	public void deleteBinary(String op, String pp, Serializable id);
	
	public List<T> list(String criteria[], String criteria_op[],  String orders, String orientations);

}
