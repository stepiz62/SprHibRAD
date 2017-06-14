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


package com.sprhibrad.framework.dao;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sprhibrad.framework.common.DataSetClauses;
import com.sprhibrad.framework.common.ShrImage;
import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.common.DataSetClauses.OrderItem;
import com.sprhibrad.framework.configuration.UserManager;
import com.sprhibrad.framework.model.ShrEntity;
import com.sprhibrad.framework.model.VerboseLiteral;

/**
 * The SprHibRAD core class for the Data Access Object layer.
 * Here the framework relies on Hibernate Session to perform the ORM exchange activity and in particular on javax.persistence to build up the criteria for all the inquiries made by the Framework itself.
 * Here, furthermore, the crypto service of Spring Security is used to store the password and the building of an image preview takes place, starting from an image uploaded by the user through 
 * the upload features of the framework, and to be stored in a dedicated field.
 * Here it can be seen the dedicated method to binary data exchange as it occurs, in SprHibRAD, by means of an autonomous and atomic user action. 
 * The management of users by the framework is here directed, as well,  towards the target class of the concrete application that implements the {@code UserManager} interface.
 * {@link update} is the method that by having empty implementation can be implemented with exchange contribution for each desired involved attribute when updating is required. 
 */

public  class ShrDao<T extends ShrEntity> implements IShrDao<T>{
	
	@Autowired
	private SessionFactory sessionFactory;
		
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	UserManager userManager;
	
	@Override
	public List<T> getObjects(Integer iteration, DataSetClauses clauses, Integer pageSize) {
		CriteriaTools tools = getCriteriaQueryTools();
		prepareList(tools, clauses);
		return presentationList(tools, clauses, iteration, pageSize);
	}

	@Override
	public T getObject(Serializable id) {
		return (T) getCurrentSession().get(Utils.typeArgumentClass(getClass(), 0), id);
	}

	@Override
	public void addObject(T entity) {
		manageObject(entity, false);
	}

	@Override
	public void updateObject(T entity) {
		manageObject(entity, true);
	}

	protected void manageObject(T object, boolean update) {
		boolean userOp = userOperation(object);
		T objectInDb = null;
		if (update)
			objectInDb = getObject(object.getId());
		String clearPwd = null;
		if (userOp) {
			if (update)
				dropUser(objectInDb);
			clearPwd = userManager.getPassword(object);
			userManager.setPassword(object, passwordEncoder.encode(clearPwd));
		}
		if (update) {
			update(object, objectInDb);
			getCurrentSession().save(objectInDb);
		} else
			getCurrentSession().save(object);			
		if (userOp)
			createUser(userManager.getUsername(object), clearPwd);
	}

	@Override
	public void deleteObject(Serializable id) {
		T entity = getObject(id);
		if (entity != null) {
			getCurrentSession().delete(entity);
			if (userOperation(entity)) 
				dropUser(entity);			
		}
	}

	@Override
	public void uploadBinary(byte[] bytes, String op, String pp, Serializable id) {
		T objectInDb = getObject(id);
		Utils.setValue(op, objectInDb, bytes);
		if (pp.length() > 0)
			Utils.setValue(pp, objectInDb, getPreviewBytes(bytes, op));
		getCurrentSession().save(objectInDb);
	}

	@Override
	public void deleteBinary(String op, String pp, Serializable id) {
		T objectInDb = getObject(id);
		Utils.setValue(op, objectInDb, null);
		if (pp.length() > 0)
			Utils.setValue(pp, objectInDb, null);
		getCurrentSession().save(objectInDb);
	}
	
	protected void update(T object, T objectInDb) {}
 
	protected void prepareList(ShrDao<T>.CriteriaTools tools, DataSetClauses clauses) {
		String field;
		Object comparingValue = null;
		if (clauses !=  null) {
			ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
			DataSetClauses.Criterion criterion;
			Predicate predicate = null;
			for (Iterator it = clauses.filter.entrySet().iterator(); it.hasNext();) {
				Entry<String, Object> entry =  (Entry<String, Object>) it.next();
				field = entry.getKey();
				criterion = (DataSetClauses.Criterion) entry.getValue();
				comparingValue = criterion.value;
				predicate = criterion.operator == null || criterion.operator.compareTo("")==0 ? 
									predEQ(tools, field, comparingValue) :
									criterion.operator.compareTo(">")==0 ?
											predGT(tools, field, comparingValue) :
											criterion.operator.compareTo("<")==0 ?
													predLT(tools, field, comparingValue) :
													criterion.operator.compareTo("N")==0 ?
														predNull(tools, field, comparingValue) :
														predNotNull(tools, field, comparingValue);
								
				if (predicate != null)
					predicateList.add(predicate);
			}
			Predicate[] predicates = new Predicate[predicateList.size()];
			int index = 0;
			for (Predicate pred : predicateList) {
				predicates[index] = pred;
				index++;
			}				
			tools.criteria.where(predicates);
		}		
	}

	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	protected Predicate predEQ(CriteriaTools tools, String field, Object value) {
		CriteriaBuilder builder = tools.builder;
		Root<T> from = tools.from;
		if (value instanceof String) {
			String text = (String) value;
			return text.compareToIgnoreCase("%") == 0 ? 
					null :
					builder.like(from.<String>get(field), (String) value);
		}
		else if (value instanceof Date)
			return builder.equal(from.<Date>get(field), (Date) value);
		else if (value instanceof Number)
			return builder.equal(from.<Number>get(field), (Number) value);
		else
			return builder.equal(from.<Object>get(field),  value);		 
	}

	protected Predicate predGT(CriteriaTools tools, String field, Object value) {
		CriteriaBuilder builder = tools.builder;
		Root<T> from = tools.from;
		if (value instanceof String)
			return builder.greaterThan(from.<String>get(field), (String) value);
		else if (value instanceof Date)
			return builder.greaterThan(from.<Date>get(field), (Date) value);
		else if (value instanceof VerboseLiteral) {
			Join<T, ShrEntity> join = from.join(field, JoinType.INNER);
			VerboseLiteral lCollection = (VerboseLiteral) value;
			return builder.greaterThan(join.<String>get(lCollection.literalField()), lCollection.getLiteral());
		} else
			return builder.gt(from.<Number>get(field), (Number) value);
	}

	protected Predicate predLT(CriteriaTools tools, String field, Object value) {
		CriteriaBuilder builder = tools.builder;
		Root<T> from = tools.from;
		if (value instanceof String)
			return builder.lessThan(from.<String>get(field), (String) value);			
		else if (value instanceof Date)
			return builder.lessThan(from.<Date>get(field), (Date) value);
		else if (value instanceof VerboseLiteral) {
			Join<T, ShrEntity> join = from.join(field, JoinType.INNER);
			VerboseLiteral lCollection = (VerboseLiteral) value;
			return builder.lessThan(join.<String>get(lCollection.literalField()), lCollection.getLiteral());
		} else
			return builder.lt(from.<Number>get(field), (Number) value);
	}

	protected Predicate predNull(CriteriaTools tools, String field, Object value) {
		CriteriaBuilder builder = tools.builder;
		Root<T> from = tools.from;
		return builder.isNull(from.<Object>get(field));
	}

	protected Predicate predNotNull(CriteriaTools tools, String field, Object value) {
		CriteriaBuilder builder = tools.builder;
		Root<T> from = tools.from;
		return builder.isNotNull(from.<Object>get(field));
	}
	
	protected List<T> presentationList(CriteriaTools tools, DataSetClauses clauses, Integer iteration,  Integer pageSize) {
		if (clauses !=  null)
			setOrders(tools, clauses);
		TypedQuery<T> query = tools.session.createQuery(tools.criteria);
		if (iteration != null) {
			query.setFirstResult((pageSize == null ? 0 : pageSize) * iteration);
			if (pageSize != null && pageSize > 0)
				query.setMaxResults(pageSize + 1);
		}
		List<T> returnedList = query.getResultList();
		if (tools.oldIsDefaultReadOnly != null) 
			tools.session.setDefaultReadOnly(tools.oldIsDefaultReadOnly);
		return returnedList;
	}
	
	protected class CriteriaTools{
		public Session session;
		public Boolean oldIsDefaultReadOnly;
		public CriteriaBuilder builder;
		public CriteriaQuery<T> criteria;
		public Root<T> from;
		public void selectRoot() {
			criteria.select(from);			
		}
	}
	
	protected ShrDao<T>.CriteriaTools getCriteriaQueryTools() {
		Session session = getCurrentSession();
		Class cls = Utils.typeArgumentClass(getClass(), 0);
		CriteriaTools tools = new CriteriaTools();
		tools.session = session;
		tools.builder = session.getCriteriaBuilder();
		tools.criteria = tools.builder.createQuery(cls);
		tools.from = tools.criteria.from(cls);
		return tools;
	}

	protected void setOrders(CriteriaTools tools, DataSetClauses clauses) {
		Class concreteClass = Utils.typeArgumentClass(getClass(), 0);
		Path<T> collector = null;
		if (clauses.propertyToOrder != null) {			
			Join<T, ShrEntity> join = tools.from.join(clauses.propertyToOrder, JoinType.INNER);
			collector = (Path<T>) join;
		} else 
			collector = tools.from;
		tools.selectRoot();
		Vector<Order> orders = new Vector<Order>();
		if (clauses.order != null) 
			for (OrderItem orderItem : clauses.order)
				orders.add(orderItem.orientation.compareToIgnoreCase("asc")==0 ?
						tools.builder.asc(collector.get(orderItem.field)) : tools.builder.desc(collector.get(orderItem.field)));
		tools.criteria.orderBy(orders);
	}
	
	private byte[] getPreviewBytes(byte[] bytes, String rip) {
		BufferedImage image = new ShrImage(bytes, rip, false).getImage();
		Dimension targetDim = new Dimension( image.getWidth(), image.getHeight());
		Utils.insideResize(new Dimension(80,60), image, targetDim);
		int previewWidth = (int) targetDim.getWidth();
		int previewHeight = (int) targetDim.getHeight();
		BufferedImage previewImage = new BufferedImage(previewWidth, previewHeight, BufferedImage.TYPE_INT_RGB);;
		Graphics2D g = previewImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.clearRect(0, 0, previewWidth, previewHeight);
		g.drawImage( image, 0, 0, previewWidth, previewHeight, null);
		g.dispose();
		byte[] previewBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(previewImage, "jpg", baos);
			baos.flush();
			previewBytes = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return previewBytes;
	}

	protected void createUser(String userName, String password) {		
		String database = null;
		try {
			database = jdbcTemplate.getDataSource().getConnection().getCatalog();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (database != null)
			userManager.createUserDDL(jdbcTemplate, database, userName, password);
	}

	protected void dropUser(T entity) {
		jdbcTemplate.execute(new ConnectionCallback() {
			@Override
			public Object doInConnection(Connection con) throws SQLException, DataAccessException  {
				Object retVal = null;
				try {
					retVal = con.createStatement().execute(String.format(userManager.dropUserDDL(), userManager.getUsername(entity)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return retVal;
			}
		});
	}


	private boolean userOperation(T entity) {
		return Utils.downCaseFirstChar(entity.getClass().getSimpleName()).compareTo(userManager.getUserEntityName()) == 0;
	}
}
