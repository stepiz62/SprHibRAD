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

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.sprhibrad.framework.model.ShrEntity;

/**
 * This class is the driver for overriding during the writing of a SprHibRAD based application controller class.
 * It provides the main signatures that the ide shows the developer. It can be see as an invocation adapter of the {@link ShrController} methods.
 */
public abstract class ImplShrController<T extends ShrEntity> extends ShrController<T> {

    protected void addEditingAttributes(ModelAndView modelAndView, T obj) {}
    protected void addDetailsAttributes(ModelAndView modelAndView, T obj, HttpServletRequest request) {}
    
	public ModelAndView newObject(HttpServletRequest request) {
		return newObject(request, null);
	}
	
	public ModelAndView newObject(HttpServletRequest request, String fk) {
		ModelAndView modelAndView = addObject(getEntityViewName(), getEntityPropertyName(), request, fk);
		addEditingAttributes(modelAndView, null);
		return modelAndView;
	}
	
	protected ModelAndView saveObject(T object, BindingResult result, HttpServletRequest request) {
		if (validate(object, result))
			return saveObject(object, getEntityPropertyName(), result, request);
		else
			return stayBecauseOfErrors(request, object, "stayAdd");
	}
	 
	protected ModelAndView objectList(Integer iteration, T object, BindingResult result, HttpServletRequest request, String[] previewFields) {
		return super.objectList(iteration, object, result, request, previewFields);
	}
	
	protected ModelAndView objectSelectionList(Integer iteration, T object, BindingResult result, HttpServletRequest request, String[] imageFields, String targetMember) {
		return getList(new ListParams(getEntitiesPropertyName(), getEntitiesPropertyName(), iteration, request, imageFields, targetMember), object, true, result);
	}
	    
	protected ModelAndView editObject(Serializable id, HttpServletRequest request) {
		MViewAndObj mviewAndObj = editObject(id, getEntityViewName(), getEntityPropertyName(), request);
		addEditingAttributes(mviewAndObj.mav, (T) mviewAndObj.obj);
		return mviewAndObj.mav;
	}
	
	protected ModelAndView viewObject(Serializable id, HttpServletRequest request, String[] imageFields) {
		MViewAndObj mviewAndObj = viewObject(id, getEntityViewName(), getEntityPropertyName(), request, imageFields);
		addDetailsAttributes(mviewAndObj.mav, (T) mviewAndObj.obj, request);
		return mviewAndObj.mav;
	}
  
    public ModelAndView updateObject(T object, BindingResult result, HttpServletRequest request) {
		if (validate(object, result))
			return updateObject(object, getEntityPropertyName(), result, request);
		else
			return stayBecauseOfErrors(request, object, "stayEdit");
    }

    private ModelAndView stayBecauseOfErrors(HttpServletRequest request, T object, String effect) {
    	ModelAndView modelAndView = renewModelAndView(request, null, effect, object);
		addEditingAttributes(modelAndView, object);
		return modelAndView;
	}
   
    public String deleteObject(Integer id, HttpServletRequest request) {
		return deleteObject(id, getEntityPropertyName(), request);
    }

    public String selectObject(@PathVariable Integer id, HttpServletRequest request, String member) {
		return super.selectObject(id, request, member);
    }


}
