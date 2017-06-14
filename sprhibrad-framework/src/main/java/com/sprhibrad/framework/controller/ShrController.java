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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import com.sprhibrad.framework.common.DataSetClauses;
import com.sprhibrad.framework.common.ShrImage;
import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.ShrDateFormatter;
import com.sprhibrad.framework.configuration.ShrResourceBundleMessageSource;
import com.sprhibrad.framework.model.ShrEntity;
import com.sprhibrad.framework.model.UserPrefs;
import com.sprhibrad.framework.model.UserTable;
import com.sprhibrad.framework.model.VerboseLiteral;
import com.sprhibrad.framework.service.IShrService;
import com.sprhibrad.names.Names;

/**
 * The SprHibRAD core class for the Controller activity.
 * Beyond invoking the service layer it manages the navigation using a stack of {@code NavigatorNode}. 
 * The latter will be then used by the view helper classes (SprHibRAD custom tag classes) for the rendering of the navigation bar.
 * In SprHibRAD the controller layer is kept at the default Spring configuration for efficiency, that is any controller object lives 
 * as singleton: this is why the http session object comes into play providing status for user preferences and navigation stack.
 * The member of this class, indeed, are injected by Spring get their current value from the session object.
 * It was chosen to use the http session object through the classical api instead of the Spring directives (@Session etc..) because I have actually not understood their use.  
 *          
 * @param <T> any class (of the model) that implements {@link ShrEntity} or its descendant.
 */
public abstract class ShrController<T extends ShrEntity> {

	private  Stack<NavigatorNode> navigator;
	private HashMap<String, HashMap<String, ResultProperties>> resultsProperties;	
	
	HashMap<String, byte[]> binariesMap = new HashMap<String, byte[]>();
		
	@Autowired
	protected IShrService<UserPrefs> userPrefsService;
	
	@Autowired
	protected LocaleResolver localeResolver;

	@Autowired
	FormattingConversionService conversionService;

	@Autowired
	ShrBindingErrorProcessor shBindingErrorProcessor;
	
	@Autowired
	UrlBasedViewResolver viewResolver;

	@Autowired
	BirtViewFactory birtViewFactory;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public UserPreferences getUserPrefs(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		UserPrefs userPrefs = (UserPrefs) session.getAttribute("userPrefs");
		if (userPrefs == null) {
			String userName = request.getUserPrincipal().getName();
			userPrefs = userPrefsService.getObject(userName);
			if (userPrefs == null) {
				userPrefs = new UserPrefs();
				userPrefs.setUser(userName);
				userPrefs.setHmenu(true);
				userPrefs.setLocale(Locale.getDefault().toString());
				userPrefsService.addObject(userPrefs);
			}
			setUserPrefs(request, response, userPrefs);
		}		
		ShrResourceBundleMessageSource messageSource = (ShrResourceBundleMessageSource) session.getAttribute("messageSource");
		localeResolver.setLocale(request, response, messageSource.getCurrentUserLocale());
		return new UserPreferences(userPrefs,  messageSource,
								(ShrDateFormatter) session.getAttribute("dateFormatter"));
	}
	
	private Builder localeBuilder;
	
	protected Builder getBuilder() {
		if (localeBuilder == null)
			localeBuilder = new Builder();
		return localeBuilder;
	}

	private Locale getUserLocale(UserPrefs userPrefs) {
		String userLocStr = userPrefs.getLocale();
		String userLanguage = userLocStr.substring(0,2);
		String userRegion = userLocStr.length() == 5 ? userLocStr.substring(3) : null;
		return getBuilder().setLanguage(userLanguage).setRegion(userRegion).build();
	}
	
	public void setUserPrefs(HttpServletRequest request, HttpServletResponse response, UserPrefs userPrefs) {
		HttpSession session = request.getSession(false);
		session.setAttribute("userPrefs", userPrefs);
		Locale locale = getUserLocale(userPrefs);
		localeResolver.setLocale(request, response, locale);
		checkMessageSource(null, locale, session);
		checkMessageSource("dictionary", locale, session);
		ShrDateFormatter dateFormatter =  (ShrDateFormatter) session.getAttribute("dateFormatter");
		if (dateFormatter == null || dateFormatter.getLocale() != locale) 
			dateFormatter = new ShrDateFormatter();
		dateFormatter.setLocale(locale);
		session.setAttribute("dateFormatter", dateFormatter);
	}

	void checkMessageSource(String baseName, Locale locale, HttpSession session) {
		String sessionParam = baseName == null ? "messageSource" : baseName;
		ShrResourceBundleMessageSource messageSource =  (ShrResourceBundleMessageSource) session.getAttribute(sessionParam);
		if (messageSource == null) 
			messageSource = baseName == null ? new ShrResourceBundleMessageSource() : new ShrResourceBundleMessageSource(baseName);
		messageSource.setCurrentUserLocale(locale);
		session.setAttribute(sessionParam, messageSource);
	}
	
	public HashMap<String, ResultProperties> getResultsProperties(String user) {
		if (resultsProperties == null)
			resultsProperties = new HashMap<String, HashMap<String, ResultProperties>>();
		if (resultsProperties.get(user) == null)
			resultsProperties.put(user,  new HashMap<String, ResultProperties>());		
		return resultsProperties.get(user);
	}

	class ResultProperties {		
		public DataSetClauses clauses;
		public String orderAttr;
		public String orientationAttr;
		public ResultProperties() {
			clauses = new DataSetClauses();
		}
		public void loadOrderClauses() {
			clauses.loadOrderClauses(orderAttr, orientationAttr);
		}
	}

	/** A tool of the "back to list" management. 
	 * it allows the reconstruction of the request parameter hosting the criterion operator */
	class CriterionOperator {
		String tagId;
		String value;
		public CriterionOperator(String tagId, String value) {
			this.tagId = tagId;
			this.value = value;
		}
	}
	Vector<CriterionOperator> operators;
	Vector<CriterionOperator> getOperators() {
		if (operators==null)
			operators = new Vector<CriterionOperator>();
		return operators;
	}

	@Resource
	private Environment env;
	
	public Stack<NavigatorNode> getNavigator(HttpServletRequest request) {
		navigator = (Stack<NavigatorNode>) request.getSession(false).getAttribute("navigator");
		if (navigator == null) {
			navigator = new Stack<NavigatorNode>();
			request.getSession(false).setAttribute("navigator", navigator);
		}
		return navigator;
	}

	public class NavigatorNode {
		public String url; 
		public Map<String, Object> attributes;
		public String entityName;
		public String message;
		public boolean obsolete;
		public String viewName;	
		public Boolean isEdit;
		public Integer iteration;
		public Object listMode;

		public NavigatorNode(String url, String entityName) {
			this.url = url;
			this.entityName = entityName;
			attributes = new HashMap<String, Object>();
			obsolete = false;
		}
		public ShrEntity getObject() {
			return (ShrEntity) attributes.get(entityName);
		}
	}
	
	public class MViewAndObj {
		ModelAndView mav;
		ShrEntity obj;
		public MViewAndObj(ModelAndView mav, ShrEntity obj) {
			this.mav = mav;
			this.obj = obj;
		}
	}
	
	public void pushNode(NavigatorNode node, HttpServletRequest request) {
		getNavigator(request).push(node);
	}
	
	public NavigatorNode popNode(HttpServletRequest request) {
		return getNavigator(request).pop();
	}
	

	public Object getObjectFromNode(NavigatorNode node) {
		return getObjectFromNode(node, node.entityName);
	}

	public Object getObjectFromNode(NavigatorNode node, String objName) {
		return  node.attributes.get(objName);
	}
	
	public void putObjectIntoNode(NavigatorNode node, ShrEntity object) {
		putObjectIntoNode(node, node.entityName, object);
	}

	public void putObjectIntoNode(NavigatorNode node, String objName, Object object) {
		 node.attributes.put(objName, object);
	}
	
	public NavigatorNode backTrackAndPeekNode(HttpServletRequest request) {
		NavigatorNode retVal = null;
		Stack<NavigatorNode> navigator = getNavigator(request);
		if (navigator.size() > 0) {
			int index = navigator.size() - 1;
			NavigatorNode node = null;
			do {
				node = navigator.get(index);
				if ((node.url.compareTo(request.getServletPath()) == 0))
					retVal = node;
				else
					index--;
			} while (retVal == null && index >= 0);
			if (retVal != null) {
				int index2 = navigator.size() - 1;
				while (index2 > index) {
					navigator.pop();
					index2--;
				}
			}
		}
		return retVal;
	}
	
	public ShrEntity getParentObjectFromNavigator(HttpServletRequest request, String entityName) {
		NavigatorNode node = null;
		Stack<NavigatorNode> navigator = getNavigator(request);
		ShrEntity object = null;
		if (navigator.size() >= 2) { // home page holds no object
			int index = navigator.size();
			do {
				index--;
				node = navigator.get(index);
				if (node != null)
					object = (ShrEntity) getObjectFromNode(node);
			} while (node != null && object != null && entityName.compareTo(node.entityName)==0);
		}
		return object;
	}

	
	protected abstract IShrService<T> getService();
	protected abstract T getEntityInstance();

	protected String getEntityName() {
		return Utils.typeArgumentClass(getClass(), 0).getSimpleName();
	}

	protected String getEntityPropertyName() {
		return Utils.downCaseFirstChar(getEntityName());
	}

	protected String getEntitiesPropertyName() {
		return Names.plural(getEntityPropertyName());
	}
	
	protected String getEntityViewName() {
		return getEntityPropertyName() + "_form";
	}

	protected boolean validate(T object, BindingResult result) {
		return ! result.hasErrors();
	}

	protected void setListFilter(T object, HttpServletRequest request) {}

	protected ResultProperties resultProperties(String user, String key) {
		if (key==null)
			key = Utils.iterResult;
		if (getResultsProperties(user).get(key) == null)
			getResultsProperties(user).put(key, new ResultProperties());
		return getResultsProperties(user).get(key);
	}
	
	public String orderAttr(String user, String key) {
		return resultProperties(user, key).orderAttr;
	}

	public void setOrderAttr(String user, String key, String value) {
		resultProperties(user, key).orderAttr = value;
	}

	public void appendOrderAttr(String user, String key, String value) {
		resultProperties(user, key).orderAttr += value;
	}

	public String orientationAttr(String user, String key) {
		return resultProperties(user, key).orientationAttr;
	}

	public void setOrientationAttr(String user, String key, String value) {
		resultProperties(user, key).orientationAttr = value;
	}

	public void appendOrientationAttr(String user, String key, String value) {
		resultProperties(user, key).orientationAttr += value;
	}

	public DataSetClauses getClauses(String user, String key) {
		return resultProperties(user, key).clauses;
	}
	
	NavigatorNode urlMatchingNode(HttpServletRequest request) {
		NavigatorNode node = peekNode(request);
		if(node != null && !node.obsolete)
			node = this.backTrackAndPeekNode(request);
		return node;
	}

	protected ShrEntity manageNode(Serializable id, HttpServletRequest request, String entityName, ModelAndView modelAndView, 
			boolean forEdit, ShrEntity availableObject) {
		return manageNode(id, request, entityName, modelAndView, forEdit, null, null, null, availableObject);
	}
	
	protected ShrEntity manageNode(Serializable id, HttpServletRequest request, String entityName, ModelAndView modelAndView, 
							boolean forEdit, Integer iteration, String listMode, T searchEntity, ShrEntity availableObject) {
		NavigatorNode node = urlMatchingNode(request);
		boolean obsoleteNode = node != null && node.obsolete;
		String url = null;
		String messageFromObsolete = null;
		if (obsoleteNode) {
			url = node.url;
			messageFromObsolete = node.message;
			popNode(request);
		}
		ShrEntity object = availableObject != null ? availableObject : 
								(entityName == null ? null :
									node == null || obsoleteNode ? (id == null ? getEntityInstance() : getService().getObject(id)) : 
										(ShrEntity) getObjectFromNode(node));
		if (node == null || obsoleteNode) {
			node = new NavigatorNode(url == null ? request.getServletPath() : url, entityName); 
			node.viewName = modelAndView.getViewName();
			if (messageFromObsolete != null)
				node.message = messageFromObsolete;
			node.isEdit = forEdit;
			if (object != null) {
				putObjectIntoNode(node, object);
				if (id != null) {
					ShrEntity parentObject = getParentObjectFromNavigator(request, entityName);
					ShrEntity propertyObject;
					int propertyId, parentObjId;
					boolean storeInNode;
					for (Field field : object.getClass().getDeclaredFields()) 
						if (ShrEntity.class.isAssignableFrom(field.getType())) {
							propertyObject = ((ShrEntity)Utils.extractValue(field.getName(),  object));
							storeInNode = true;							
							if (propertyObject != null) {
								propertyId = (int) propertyObject.getId();
								if (parentObject != null) {
									parentObjId = (int) parentObject.getId();
									if (field.getType() == parentObject.getClass())
										if (propertyId == parentObjId) {
											storeInNode = false;
											putObjectIntoNode(node, Utils.toParentFKslotPrefix + Names.plural(entityName), field.getName());
										}
								}
							}
							if (storeInNode)
								putObjectIntoNode(node, field.getName(), propertyObject);
						}
				}
			}
			if (searchEntity != null && iteration != null) {
				NavigatorNode currentTopNode = peekNode(request); /* possibly not null due to a "previous" action (urlMatchingNode 
																	in this case does not find a match because only one node is
																	 created for any iteration >= 0 and this implies that during
																	  pagination navigator does not build url history*/  
				if (iteration > 0 || iteration == 0 && currentTopNode.iteration != null &&  currentTopNode.iteration > 0) 
					popNode(request);
				node.iteration = iteration;
				node.listMode = listMode;
				node.entityName = Utils.lowCaseName(searchEntity);
				if (iteration > -1)
					putObjectIntoNode(node, searchEntity);
			}
			pushNode(node, request);
		}
		modelAndView.addObject("message", node.message);
		node.message = "";
		return object;
	}


	public ModelAndView addObject(String viewName, String entityName, HttpServletRequest request, 
									String fk) {
		ModelAndView modelAndView = new ModelAndView(viewName);
		ShrEntity object = manageNode(null, request, entityName, modelAndView, false, null);
		NavigatorNode node = peekNode(request);
		if (fk != null)
			putObjectIntoNode(node, Utils.toParentFKslotPrefix + Names.plural(entityName), fk);
		modelAndView.addObject(entityName, object);
		modelAndView.addObject("mode", "add");
		return modelAndView;
	}

	
	protected String freezeObject(T object, HttpServletRequest request, String redir, String targetMember,
			String action) {
		NavigatorNode node = peekNode(request);
		if (node != null) {
			Object propertyObject;
			// puts non ShrEntity properties of the bound object into the "Object" in the node
			T objectInNode = (T) getObjectFromNode(node);
			for (Field field : object.getClass().getDeclaredFields())
				if (!ShrEntity.class.isAssignableFrom(field.getType()) && field.getName().compareTo("id") != 0) {
					propertyObject = Utils.extractValue(field.getName(), object);
					setObjectMember(objectInNode, propertyObject, field.getName());
				}
		}
		return "redirect:/" + redir + "/" + action + "/-1.html" + "?targetMember=" + targetMember;
	}
	
	public MViewAndObj editObject(Serializable id, String viewName, String entityName, HttpServletRequest request) {
		return loadObject(true, id, viewName, entityName, request, null);
    }
	
	public MViewAndObj viewObject(Serializable id, String viewName, String entityName, HttpServletRequest request, String[] imageFields) {
		viewResolver.clearCache();
		MViewAndObj mavAndObj = loadObject(false, id, viewName, entityName, request, null);
		if (imageFields != null)
			makeImagesAvailable(mavAndObj.obj, request, imageFields);
		return mavAndObj;
	}


	public MViewAndObj loadObject(Boolean forEdit, Serializable id, String viewName, String entityName, HttpServletRequest request, ShrEntity availableObject) {
		ModelAndView modelAndView = new ModelAndView(viewName);
		ShrEntity object = manageNode(id, request, entityName, modelAndView, forEdit, availableObject);
		modelAndView.addObject(entityName, object);
		modelAndView.addObject("mode", forEdit ? "edit" : "view");
		return new MViewAndObj(modelAndView, object);
	}
	
	protected String getUser(HttpServletRequest request) {
		return  request.getUserPrincipal().getName();
	}
	
	protected void addDetailsAttribute(String entitiesName, IShrService detailService, String property, 
										T masterObject, ModelAndView modelAndView, HttpServletRequest request, 
										String orders, String orientations, String detailFkMember, String[] previewFields) {
		checkOrderAttrs(request, entitiesName, orders, orientations);
		String user = getUser(request);
		request.setAttribute(entitiesName + "-order", orderAttr(user, entitiesName));
		request.setAttribute(entitiesName + "-orientation", orientationAttr(user, entitiesName));
		ShrEntity entity = getEntityInstance();
		getClauses(user, entitiesName).filter.clear();
		resultProperties(user, entitiesName).loadOrderClauses();
		resultProperties(user, entitiesName).clauses.propertyToOrder = property;		
		String fkMemberName = detailFkMember == null ? Utils.lowCaseName(masterObject) : detailFkMember;
		List<ShrEntity> objects = detailService.getDetailObjects(fkMemberName, masterObject,  resultProperties(user, entitiesName).clauses);
		if (previewFields != null)
			for (ShrEntity object : objects)
				makeImagesAvailable(property == null ? object : (ShrEntity) Utils.extractValue(property, object), request, previewFields);
		modelAndView.addObject(Utils.toParentFKslotPrefix + entitiesName, fkMemberName);
		modelAndView.addObject(entitiesName, objects);
	}

	protected ModelAndView redirectMavByNode(NavigatorNode node) {
		return redirectMavByNode(node, false);
	}
	
	protected ModelAndView redirectMavByNode(NavigatorNode node, boolean makeNodeObsolete) {
		if (makeNodeObsolete)
			node.obsolete = true;
		RedirectView redirectView = new RedirectView();
		redirectView.setContextRelative(true);
		redirectView.setUrl(node.url);
		ModelAndView mav = new ModelAndView();
		mav.setView(redirectView);
		return mav;
	}
	
	protected void addEditingAttributes(ModelAndView modelAndView, T obj) {}
	
	public ModelAndView saveObject( T object, String entityName, BindingResult result, HttpServletRequest request) {
		manageExtraViewAttr(object, peekNode(request), request, entityName);
		getService().addObject(object);
		popNode(request);
		prepareTopNode(request, entityName, "added");
		return redirectMavByNode(peekNode(request));
	}
	
	protected String cancel(HttpServletRequest request) {
		popNode(request);
		return goBackWithEffects(request, null, null);
	}

    public ModelAndView updateObject(T object, String entityName, BindingResult result, HttpServletRequest request) {
		manageExtraViewAttr(object, peekNode(request), request, entityName);
		getService().updateObject(object);
		popNode(request);
		peekNode(request).obsolete = true;
		return redirectMavByNode(peekNode(request));
    }
    
	public ModelAndView renewModelAndView(HttpServletRequest request, String entityName, String effect, T object) {
		NavigatorNode node = peekNode(request);
		ModelAndView modelAndView;
		modelAndView = new ModelAndView(node.viewName);
		modelAndView.addObject(node.entityName == null ? entityName : node.entityName, object);	
		modelAndView.addObject("mode", effect.compareTo("stayAdd")==0 ? "add" : "edit");
		return modelAndView;
	}

	public String deleteObject(Integer id, String entityName, HttpServletRequest request) {
		getService().deleteObject(id);
		return goBackWithEffects(request, entityName, "deleted");
	}

	/** possible preparation of the target node will be sensed by the redirect that perfoms the selective scanning  !?!*/
	public String goBackWithEffects(HttpServletRequest request, String entityName, String effect) {
		NavigatorNode node = prepareTopNode(request, entityName, effect);
		return "redirect:" + (node == null ? "/index.html" : node.url);
	}
	
	
	public NavigatorNode prepareTopNode(HttpServletRequest request, String entityName, String effect) {
		NavigatorNode node = peekNode(request);
		if (effect == null)
			node.message = "";
		else {
			node.obsolete = effect.compareTo("updated")==0;
			ShrResourceBundleMessageSource base = new ShrResourceBundleMessageSource();
			ShrResourceBundleMessageSource dictio = new ShrResourceBundleMessageSource("dictionary");
			String subject = dictio.msgOrKey("entity." + entityName);
			String action = base.msgOrKey("action." + effect);
			node.message = base.msgOrKey("message.success", new String[] {subject, action});
		}
		return node;
	}
	
	private void manageExtraViewAttr(T object, NavigatorNode node, HttpServletRequest request, String entityName) {
		ShrEntity parentObjectCandidate = getParentObjectFromNavigator(request, entityName);
		if (parentObjectCandidate != null) {
			String candidateMemberToParent = (String) getObjectFromNode(node, Utils.toParentFKslotPrefix + Names.plural(entityName));
			if (candidateMemberToParent != null)
				setObjectMember(object, parentObjectCandidate, candidateMemberToParent);
		}
		int propertyId;
		ShrEntity propertyObject;
		// extracts ShrEntity properties from "Object" in the node and puts them in the bound object
		T objectInNode = (T) getObjectFromNode(node);
		for (Field field : object.getClass().getDeclaredFields()) 
			if (ShrEntity.class.isAssignableFrom(field.getType()) && ! VerboseLiteral.class.isAssignableFrom(field.getType())) {
				propertyObject = (ShrEntity) Utils.extractValue(field.getName(), objectInNode);
				if (propertyObject != null) {
					propertyId = (int) propertyObject.getId();
					if (parentObjectCandidate == null || field.getType() != parentObjectCandidate.getClass() || propertyId != (int) parentObjectCandidate.getId())
						setObjectMember(object, propertyObject, field.getName());
				}
			}
	}
	
	protected void setObjectMember(T object, Object sourceObject, String memberName ) {
		Utils.setValue(memberName == null ? Utils.lowCaseName(sourceObject) : memberName, object, sourceObject);
	}

	protected ModelAndView objectList(Integer iteration, T object, BindingResult result, HttpServletRequest request, String[] imageFields) {
		return getList(new ListParams(getEntitiesPropertyName(), getEntitiesPropertyName(), iteration, request, imageFields, null), object, false, result);
	}

	public ModelAndView getList(ListParams listParams) {
		return getList(listParams, Integer.parseInt(env.getRequiredProperty("sprHibRad.pagesize")));
	}
	
	class ListParams{
		public ListParams(String viewName, String attributeName, Integer iteration, HttpServletRequest request,
				String[] imageFields, String targetMember) {
			this.viewName = viewName;
			this.attributeName = attributeName;
			this.iteration = iteration;
			this.request = request;
			this.imageFields = imageFields;
			this.targetMember = targetMember;
		}
		String viewName;
		String attributeName;
		Integer iteration;
		HttpServletRequest request;
		String[] imageFields;
		String targetMember;
	}
	
	public ModelAndView getList(ListParams listParams, T searchEntity, boolean forSelection, BindingResult result) {
		HttpServletRequest request = listParams.request;
		NavigatorNode node = urlMatchingNode(request);
		Boolean backTracking = node != null && node.iteration > -1;
		checkOrderAttrs(request, null, null, null);
		if (backTracking) {
			searchEntity = (T) node.getObject();
			for (CriterionOperator op : getOperators())
				request.setAttribute(op.tagId, op.value);
		}
		String user = getUser(request);
		request.setAttribute(Utils.iterResult + "-order", orderAttr(user, null));
		request.setAttribute(Utils.iterResult + "-orientation", orientationAttr(user, null));
		if (listParams.iteration > -1) {
			getClauses(user, null).filter.clear();
			getOperators().clear();
			setListFilter(searchEntity, request);
			resultProperties(user, null).loadOrderClauses();
		}
		String listMode = forSelection ? "choices" : "list";
		ModelAndView modelAndView = getList(listParams, searchEntity, 
										Integer.parseInt(env.getRequiredProperty("sprHibRad.pagesize")), listMode, backTracking ? null : result);
		if ( ! backTracking)
			manageNode(null, request, null, modelAndView, false, listParams.iteration, listMode, searchEntity, null);
		if (listParams.iteration == -1)
			addEditingAttributes(modelAndView, searchEntity);
		return modelAndView;
	}
	
	private void checkOrderAttrs( HttpServletRequest request, String nameParam, String orders, String orientations) {
		String name = nameParam == null ? Utils.iterResult : nameParam;
		String user = getUser(request);
		if (orderAttr(user, name) == null && orders != null) {
			setOrderAttr(user, name, orders);
			setOrientationAttr(user, name, orientations);
		}
		String orderCol = request.getParameter(Utils.orderActionParam);
		boolean isAnOrderCommadForThisList = orderCol != null && (nameParam == null || orderCol.indexOf(nameParam + "-") == 0);
		if (isAnOrderCommadForThisList && orderCol.compareTo(name + "-_none") == 0) {
			setOrderAttr(user, name, "");
			setOrientationAttr(user, name, "");
		} else {			
			if (orderAttr(user, name) == null)
				setOrderAttr(user, name, (String) request.getAttribute(name + "-order"));
			if (orientationAttr(user, name) == null)
				setOrientationAttr(user, name, (String) request.getAttribute(name + "-orientation"));
			if (orderAttr(user, name) == null)
				setOrderAttr(user, name, (String) request.getParameter(name + "-order"));
			if (orientationAttr(user, name) == null)
				setOrientationAttr(user, name, (String) request.getParameter(name + "-orientation"));
			if (isAnOrderCommadForThisList) {
				Vector<String> orderVector = new Vector<String>();
				Vector<String> orientationVector = new Vector<String>();
				Utils.comaSplitAndTrim(orderAttr(user, name), orderVector, orientationAttr(user, name), orientationVector);
				int index = 0;
				String orientationItem = null;
				setOrientationAttr(user, name, "");
				boolean preAsc, colOderIsToBeChanged, colOrderCHanged = false;
				String orderColField = orderCol.substring(name.length() + 1);
				for (String field : orderVector) {
					if (orientationVector.size() > index ) {
						preAsc = orientationVector.get(index).compareTo("asc")==0;
						colOderIsToBeChanged = field.compareTo(orderColField)==0;
						orientationItem = preAsc && ! colOderIsToBeChanged || ! preAsc && colOderIsToBeChanged ? "asc" : "desc";
						if ( ! colOrderCHanged )
							colOrderCHanged = colOderIsToBeChanged;
					} else
						orientationItem = "asc";
					appendOrientationAttr(user, name, (index == 0 ? "" : ",") + orientationItem);
					index++;
				}
				if (! colOrderCHanged) {
					appendOrderAttr(user, name, (index == 0 ? "" : ",") + orderColField);
					appendOrientationAttr(user, name, (index == 0 ? "" : ",") + "asc");
				}
			}
		}
	}
	
	protected void addToFilter(String key, Object object, HttpServletRequest request) {
		String operatorTagId = key + Utils.operatorIdSuffix;
		String operator = (String) request.getParameter(operatorTagId);
		if (operator==null)  /* "back to list" management */
			operator = (String) request.getAttribute(operatorTagId);
		if (object != null || operator.compareTo("N") == 0 || operator.compareTo("NN") == 0) {
			if (operator==null)
				operator = "";
			String user = getUser(request);
	    	getClauses(user, null).addCriterion(key, operator, object);
			getOperators().add(new CriterionOperator(operatorTagId, operator));
		}
    }
	
	public ModelAndView getList(ListParams listParams, Integer pageSize) {
		ModelAndView modelAndView = new ModelAndView(listParams.viewName);
		List<T> objects = getService().getObjects(listParams.iteration, getClauses(getUser(listParams.request), null), pageSize);
		if (listParams.imageFields != null)
			for (T object : objects)
				makeImagesAvailable(object, listParams.request, listParams.imageFields);
		int gotSize = objects.size();
		boolean furtherRecords = gotSize == pageSize + 1;
		if (listParams.iteration != null)
			modelAndView.addObject("more", listParams.iteration >= 0 && furtherRecords);
		if (furtherRecords)
			objects.remove(gotSize-1);
		modelAndView.addObject(listParams.attributeName, objects);
		return modelAndView;
	}

	public ModelAndView getList(ListParams listParams, T searchEntity, Integer pageSize, String listMode, BindingResult result) {
		int iteration = listParams.iteration;
		ModelAndView modelAndView;
		Object searchEntityObject = null;
		boolean inputValidated = true;
		if (result != null)
			inputValidated = iteration != 0 || validate(searchEntity, result);
		if (iteration == -1 || ! inputValidated) {
			modelAndView = new ModelAndView(listParams.viewName);
			if (iteration == -1)
				searchEntityObject = getEntityInstance();
			iteration = -1;
		} else
			modelAndView = getList(listParams);
		if (searchEntityObject == null)
			searchEntityObject = searchEntity;
		modelAndView.addObject(Utils.lowCaseName(searchEntityObject), searchEntityObject);	
		modelAndView.addObject("iteration", iteration);
		modelAndView.addObject("listmode", listMode);
		modelAndView.addObject("initial", "empty");
		modelAndView.addObject("targetMember", listParams.targetMember);
		return modelAndView;
	}
	    	
	public NavigatorNode peekNode(HttpServletRequest request) {
		return peekNode(request, null);
	}

	public NavigatorNode peekNode(HttpServletRequest request, Integer backStep) {
		Stack<NavigatorNode> navigator = getNavigator(request);
		if (backStep == null)
			return navigator.size() > 0 ? navigator.peek() : null;
		else
			return navigator.get(navigator.size() - 1 - backStep);
	}

    protected String selectObject(Integer id, HttpServletRequest request, String member) {
    	T object = getService().getObject(id);
		NavigatorNode node =  peekNode(request, 2); /* back to the context where selection originated */
		setObjectMember((T) getObjectFromNode(node), object, member);
		return "redirect:" + node.url;
	}
    
    @ExceptionHandler
    public ModelAndView exceptionHandler(Exception exception) {
    	exception.printStackTrace();
    	Logger.getLogger(getClass()).error(null, exception);    	 
     	ModelAndView modelAndView = new ModelAndView("exception");
    	modelAndView.addObject("message", new ShrResourceBundleMessageSource().msgOrKey("exception.msg"));
    	return modelAndView;
    } 
    
	public byte[] getImage(String key, String ext) {
		return binariesMap.remove(key + "." + ext);
	}

	public String viewBinary(Integer id, String target, HttpServletRequest request, boolean isImage) {
		T object = (T) (id == null ?  getObjectFromNode(peekNode(request)) : getService().getObject(id));
		String imageKey = makeBinaryAvailable(object, request, target, isImage);
		return "redirect:" + imageKey;
	}
	
	public ModelAndView uploadBinary(T object, String op, String pp, HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView("uploadBinary");
		manageNode(null, request, null, modelAndView, false, null);
		modelAndView.addObject("op", op);
		String entityName = Utils.downCaseFirstChar(getEntityName());
		if (pp.length() > 0) {
			modelAndView.addObject("pp", pp);	
			modelAndView.addObject("accept", "accept='.jpg,.jpeg,.png,.bmp,.gif'");
			modelAndView.addObject("entityName", entityName);
		}
		modelAndView.addObject("action", entityName + "/doUploadBinary");
		return modelAndView;
	}
	
	public ModelAndView deleteBinary(String op, String pp, HttpServletRequest request) {
		NavigatorNode node = peekNode(request);
		T object = (T) node.getObject();
		getService().deleteBinary(op, pp, object.getId());
		return redirectMavByNode(node, true);
	}	

	public ModelAndView doUploadBinary(MultipartFile file, String op, String pp, HttpServletRequest request) {
		if (!file.isEmpty()) {
			byte[] bytes = null;
			try {
				bytes = file.getBytes();
				NavigatorNode node = peekNode(request, 1);
				T object = (T) node.getObject(); // must be castable that way
				getService().uploadBinary(bytes, op, pp, object.getId());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		popNode(request);
		return redirectMavByNode(peekNode(request), true);
	}
	
	public void checkPasswords(T object, BindingResult result, String oldPwd, String currPwd, String pwd2, HttpServletRequest request) {
		if (pwd2 != null && ((UserTable)object).getPassword().compareTo(pwd2) != 0)
			result.rejectValue("password", "message.pwdsDontMatch");
		if (! passwordEncoder.matches(oldPwd, currPwd))
			result.rejectValue("password", "message.wrongOldPwd");
		int minPwdSize = Integer.parseInt(env.getRequiredProperty("sprHibRad.minPwdSize"));
		if (((UserTable)object).getPassword().length() < minPwdSize)
			result.rejectValue("password", "message.tooShortPwd", new String[] {String.valueOf(minPwdSize)}, null);
		if (result.hasErrors())
			((UserTable)object).setPassword(currPwd);
		else {
			NavigatorNode node = peekNode(request, 1);
			ShrResourceBundleMessageSource messageSource = (ShrResourceBundleMessageSource) request.getSession().getAttribute("messageSource");
			node.message = messageSource.msgOrKey("message.pwdChanged");
		}		
	}

	private void makeImagesAvailable(ShrEntity object, HttpServletRequest request, String[] imageFields) {
		Object value;
		Field field = null;
		for (String fieldName : imageFields) {
			try {
				field = object.getClass().getDeclaredField(fieldName);
			} catch (NoSuchFieldException | SecurityException e1) {
				e1.printStackTrace();
			}
			if (field != null && field.getType() == byte[].class) 
				makeBinaryAvailable(object, request, field.getName(), true);
		}
	}

	private String makeBinaryAvailable(ShrEntity object, HttpServletRequest request, String imageField, boolean isImage) {
		String imageKey = null;
		Object value;
		try {
			value = Utils.extractValue(imageField, object);
			if (value != null)	{	
				String format = (isImage ? (new ShrImage((byte[]) value, imageField, true).getFormatName()) : 
		 			getFormatName((byte[]) value));
				if (format != null) {
					imageKey = Utils.getImageKey(request.getUserPrincipal().getName(), 							
											Utils.downCaseFirstChar(getEntityName()),
											object.getId(), imageField) +
							 "." + format;
					binariesMap.put(imageKey, (byte[]) value);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return imageKey;
	}

	protected void reportHandler(Map<String, String> map, HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
		try {
			ShrResourceBundleMessageSource messageSource = (ShrResourceBundleMessageSource) request.getSession().getAttribute("messageSource");
			Locale locale = messageSource.getCurrentUserLocale();
			params.put("loc_country", locale.getCountry());
			params.put("loc_lang", locale.getLanguage());
			birtViewFactory.create().render(map, request, response, params);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
    private String getFormatName(byte[] value) {
    	String contentType = new Tika().detect(value);
		return contentType.compareTo("text/plain") == 0 ? "txt" : contentType.substring(contentType.indexOf('/') + 1);
	}

	@InitBinder
    protected void initBinder(WebDataBinder binder) {
         binder.setBindingErrorProcessor(shBindingErrorProcessor);
         shrInitBinder(binder);
    }

	protected void shrInitBinder(WebDataBinder binder) { }

}
