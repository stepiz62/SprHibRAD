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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import com.sprhibrad.framework.controller.ShrController.NavigatorNode;
import com.sprhibrad.framework.model.ShrEntity;
import com.sprhibrad.framework.tags.DataItem;
import com.sprhibrad.framework.tags.TermTag;

public class Utils {
	 
	public static final String toParentFKslotPrefix = "SHR_ParentFK_";
	public static final String orderActionParam = "orderCol";
	public static final String operatorIdSuffix = "_SHRop";
	public static final String iterResult = "_iterResult";
	public static final String shrLocales = "shrLocales";
	public static final String propertiesPathExpr = "classpath:application.properties";
	public static final String shRepArgNull = "_SHR_NULL_";
	
	public static void outHidden(String name, String value, JspWriter out)  {
		try {
			out.println(hidden(name, value));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String hidden(String name, String value) {
		return "<input type='hidden' " + idAndNameAttrs(name) + " value='" + value + "' />";
	}

	public static String idAndNameAttrs(String valueLiteral) {
		return writeAttr("id", valueLiteral) + writeAttr("name", valueLiteral);
	}

	public static String writeAttr(String attr, String valueLiteral) {
		return " " + attr +"='" + valueLiteral + "'";
	}
	
	
	public static String escape(String input)  {
		return input.replace("'", "\\'");
	}
	
	public static Date getDateFromDbTimestamp(String input) throws ParseException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
		return format.parse(input);
	}
			
	static public String downCaseFirstChar(String literal) {
		return literal.substring(0,1).toLowerCase() + literal.substring(1);
	}
	
	static public String lowCaseName(Object obj) {
		return downCaseFirstChar(obj.getClass().getSimpleName());
	}
	
	static public String upCaseFirstChar(String literal) {
		return literal.substring(0,1).toUpperCase() + literal.substring(1);
	}

	static public Boolean isEdit(ServletRequest servletRequest) {
		return isMode("edit", servletRequest);
	}

	static public Boolean isAdd(ServletRequest servletRequest) {
		return isMode("add", servletRequest);
	}

	static public Boolean isEditing(ServletRequest servletRequest) {
		return isAdd(servletRequest) ||  isEdit(servletRequest);
	}
	
	static public Boolean isMode(String modeName, ServletRequest servletRequest) {
		String mode = (String) servletRequest.getAttribute("mode");
		return mode != null && mode.compareToIgnoreCase(modeName) == 0;
	}

	static public Boolean isAMultiModePage(ServletRequest servletRequest) {
		return servletRequest.getAttribute("mode") != null;
	}

	static public Stack<NavigatorNode> getNavigator (HttpServletRequest request) {
		return (Stack<NavigatorNode>) request.getSession(false).getAttribute("navigator");
	}
	
	public static <T> Type[] typeArgumentClasses(Class<T> genericInstance) {
		return ((java.lang.reflect.ParameterizedType) genericInstance.getGenericSuperclass()).getActualTypeArguments();
	}
	
	public static <T> Class typeArgumentClass(Class<T> genericInstance, int typeArgumentIndex) {
		return (Class) Utils.typeArgumentClasses(genericInstance)[typeArgumentIndex];
	}
	
	public static void comaSplitAndTrim(String sourceString, Vector<String> targetVector) {
        targetVector.removeAllElements();
        StringTokenizer tokenizer = new StringTokenizer(sourceString, ",");
        int tokenizerSize = tokenizer.countTokens();
        for (int i = 0; i < tokenizerSize; i++)
            targetVector.add(tokenizer.nextToken());
		for (String string : targetVector)
			string.trim();
	}
	
	public static void comaSplitAndTrim(String sourceString1, Vector<String> targetVector1, String sourceString2, Vector<String> targetVector2) {
		Utils.comaSplitAndTrim(sourceString1, targetVector1);
		if (sourceString1 != null)
			Utils.comaSplitAndTrim(sourceString2, targetVector2);
	}

	public static void insideResize(Dimension constrainer, BufferedImage srcImg, Dimension outputDimension) {
		if (srcImg == null)
			return;
		boolean horizontalConstraint = ((double) outputDimension.width / (double) outputDimension.height) > ((double) constrainer.getWidth() / (double) constrainer.getHeight());
		resize(outputDimension, horizontalConstraint ? (double) constrainer.getWidth() / (double) outputDimension.width : (double) constrainer.getHeight() / (double) outputDimension.height);
	}

	private static void resize(Dimension target, double factor) {
		target.height = (int) (target.height * factor);
		target.width = (int) (target.width * factor);
	}

	public static Object extractValue(String fieldName, ShrEntity object) {
		Method accessor = null;
		Object value = null;
		try {			
			accessor = object.getClass().getDeclaredMethod(accessorName(false, fieldName), null);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			value = accessor.invoke(object, null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return value;
	}

	private static String accessorName(boolean set, String fieldName) {
		return (set ? "set" : "get") + Utils.upCaseFirstChar(fieldName);
	}
	
	public static void setValue(String fieldName, ShrEntity object, Object value) {
		Method accessor = null;
		try {			
			Field field = object.getClass().getDeclaredField(fieldName);
			accessor = object.getClass().getDeclaredMethod(accessorName(true, fieldName), new Class[] {field.getType()});
		} catch (NoSuchMethodException | SecurityException | NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			accessor.invoke(object, new Object[] {value});
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static String getImageKey(String user, String entityName, Serializable id, String imageField) {
		return user + "_" + entityName + "_" + id + "_" + imageField;
	}

	public static String cellWrap(String content) {
		return 	"<td>" + content + "</td>";
	}
	
	public static String rowWrap(String content) {
		return 	content.isEmpty() ? "" : ("<tr>" + content + "</tr>");
	}

	public static boolean isContainerEditable(TermTag editableBox) {
		return (! (editableBox instanceof DataItem) || ! ((DataItem) editableBox).getReadOnly());
	}

	public static Boolean isADate(Object value) {
		return value instanceof java.sql.Timestamp || value instanceof java.util.Date;
	}

	public static Object annotationMethod(Annotation annotation, String methodName) {
		Method method;
		Object retVal = null;
		try {
			method = annotation.getClass().getDeclaredMethod(methodName);
			retVal = method.invoke(annotation, null);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return retVal;
	}

}
