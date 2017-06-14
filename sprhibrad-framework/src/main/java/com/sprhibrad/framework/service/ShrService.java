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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sprhibrad.framework.common.DataSetClauses;
import com.sprhibrad.framework.dao.IShrDao;
import com.sprhibrad.framework.model.ShrEntity;
/**
 * The SprHibRAD core class for the service layer.
 * It is decoupled from the DAO layer through the {@code getDao} method and does nothing else forwarding 
 * invocations to that layer a part from adding the inherent filter criterium to the {@code DataSetClauses} 
 * parameter as long as the main Object, passed as parameter, is the key to inquiry details in a one-to-many relation;
 * 
 * @see #getDetailObjects(String, ShrEntity, DataSetClauses)
 */

@Transactional
@Service
public abstract class ShrService<T extends ShrEntity> implements IShrService<T>{

	abstract protected IShrDao<T> getDao();

	@Override
	public List<T> getObjects(Integer iteration, DataSetClauses clauses, Integer pageSize) {
		return getDao().getObjects(iteration, clauses, pageSize);
	}

	@Override
	public List<T> getDetailObjects(String mainEntityName, ShrEntity object, DataSetClauses detailClauses) {
		detailClauses.addCriterion(mainEntityName, null, object);
		return  getObjects(null, detailClauses, null);
	}
		
	@Override
	public void addObject(T object) {
		getDao().addObject(object);
	}

	@Override
	public T getObject(Serializable id) {
		return getDao().getObject(id);
	}

	@Override
	public void updateObject(T object) {
		getDao().updateObject(object);
	}

	@Override
	public void deleteObject(Serializable id) {
		getDao().deleteObject(id);
	}
	
	@Override
	public void uploadBinary(byte[] bytes, String op, String pp, Serializable id) {
		getDao().uploadBinary(bytes, op, pp, id);
	}

	@Override
	public void deleteBinary(String op, String pp, Serializable id) {
		getDao().deleteBinary(op, pp, id);
	}

	public List<T> list(String criteria[], String criteria_op[],  String orders, String orientations) {
		return null;		
	};

}
