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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.springframework.format.annotation.DateTimeFormat;

import com.sprhibrad.framework.common.ShrImage;
import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.configuration.EntityRenderer;
import com.sprhibrad.framework.model.ShrEntity;

/** 
 * This class has the most part of the implementation code that renders a data grid of the framework: either when the data grid is administered by a {@link ResultManager} 
 * or when a {@link DetailManager} is the director tag, that is the grid is the target of listing of main data related to an entity or 
 * when grid data are related to details of a parent entity instance, it makes the rendering.
 * Here, whatever is the nature of the grid, the sorting mechanism takes place with the inherent rendering of the column headings, the rendering of the hidden fields needed 
 * to propagate the grid status along normal navigation the user could perform.  
 * The rendering of the row command, needed for actions on the row data, are left to the descendants of the class.
 */
public abstract class DataSetRenderer  {
	

	protected DataSetManager tag;
	private String orderAttr;
	private String orientationAttr;
	String maleLiteral;
	String femaleLiteral;

	public DataSetRenderer(DataSetManager tag) {
		this.tag = tag;
		maleLiteral = tag.getMsgManager().msgOrKey("label.male");
		femaleLiteral = tag.getMsgManager().msgOrKey("label.female");
	}

	class Format {
		public Format(boolean isAbinary) {
			this.isAbinary = isAbinary;
		}
		String style = "";
		String pattern = "";
		boolean isAbinary = false;
	}
		
	Format getFormat(String fieldName,  ShrEntity object) {
		Format retVal = null;
		try {
			Field field = object.getClass().getDeclaredField(fieldName);
			if (Date.class.isAssignableFrom(field.getType())) {
				Annotation dateTimeFormatAnnotation = field.getAnnotation(DateTimeFormat.class);
				retVal = new Format(false);
				if (dateTimeFormatAnnotation != null) {
					retVal.pattern = (String) Utils.annotationMethod(dateTimeFormatAnnotation, "pattern");
					retVal.style = (String) Utils.annotationMethod(dateTimeFormatAnnotation, "style");
				}
			} else if (byte[].class.isAssignableFrom(field.getType()))
				retVal = new Format(true);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException e1) {
			e1.printStackTrace();
		}
		return retVal;
	}
	
	void render(boolean condition) {
		if ( ! condition)
			return;
		Vector<String> fieldsVector = new Vector<String>();
		Vector<String> headsVector = null;
		HashMap<String, String> ImagePreviewTargetMap = new HashMap<String, String>();
		{
			String fieldName = null;
			Utils.comaSplitAndTrim(tag.getFields(), fieldsVector);
			for (int i = 1; i < fieldsVector.size(); i++) {
				fieldName = fieldsVector.get(i);
				if (fieldName.charAt(0)=='[' && fieldName.charAt(fieldName.length()-1)==']') {
					ImagePreviewTargetMap.put(fieldsVector.get(i - 1), fieldName.substring(1,fieldName.length()-1));
					fieldsVector.remove(i);
				}
			}
		}
		if (tag.getHeads() == null)
			headsVector = fieldsVector;
		else {
			headsVector = new Vector<String>();
			Utils.comaSplitAndTrim(tag.getHeads(), headsVector);
		}
		List<ShrEntity> detailObjects = (List<ShrEntity>) tag.request().getAttribute(getEntitysName());
		ShrEntity object = null;
		Object value = null;
		String inTbody = "", inThead = "";
		HashMap<String, Format> formatMap = null;
		for (ShrEntity detailObject : detailObjects) {
			object = displayingObject(detailObject);
			inTbody += "<tr>";
			value = null;
			if (formatMap == null) {
				formatMap = new HashMap<String, Format>();
				for (String fieldName : fieldsVector)
					formatMap.put(fieldName, getFormat(fieldName, object));
				inThead += renderHeads(headsVector, formatMap);
				
			}
			for (String fieldName : fieldsVector) {
				fieldName = fieldName.trim();
				value = Utils.extractValue(fieldName, object);
				inTbody += "<td>" + valueRender(fieldName, value, formatMap, detailObject, object, ImagePreviewTargetMap) + "</td>";
			}
			value = Utils.extractValue("id", detailObject);
			inTbody += Utils.cellWrap(renderRowCommand(value, detailObject));
			inTbody += "</tr>";
		}
		if (inTbody.isEmpty())
			inTbody = tag.noRecords();
		String html = "";
		html += "<table><tr><td class='label'>" + renderCaption() + "</td><td>" + renderAddCommand() + "</tr></table>";
		html += "<table class='mainList'>"
				+ "			<thead>\r\n"
				+ 				Utils.rowWrap(inThead)
				+ "			</thead>"
				+ "			<tbody>" + inTbody + "</tbody></table>";
		try {
			tag.out().println(html);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected ShrEntity displayingObject(ShrEntity detailObject) {
		return detailObject;
	}

	protected String renderCaption() { return ""; }

	protected abstract String renderRowCommand(Object object, ShrEntity detailObject);

	protected abstract String getEntitysName();
	
	protected abstract String getEntityName();
	
	protected abstract String getDisplayedEntity();

	protected String renderAddCommand() {
		return tag.renderAddCommand();
	}

	private String renderSortSymbol(HashMap<String, Integer> orderMap, String colName) {
		Integer number = orderMap.get(colName);
		if (number==null)
			return "";
		else {
			String symbol;
			if (number > 0)
				symbol = "v";
			else {
					symbol = "^";
					number = -number;
			}
			return "<b class='sortArrow'>" + " " + symbol + " " + number + "</b>";
		}
	}

	protected String renderHeads(Vector<String> headsVector, HashMap<String, Format> formatMap) {
		String html = "";
		Vector<String> fieldVector = new Vector<String>();
		Vector<String> orientationVector = new Vector<String>();
		Utils.comaSplitAndTrim(tag.getOrder(), fieldVector, tag.getOrientation(), orientationVector);
		int index = 0;
		HashMap<String, Integer> orderMap = new HashMap<String, Integer>();
		String gotOrientation;
		for (String field : fieldVector) {
			gotOrientation = orientationVector.size() > index ? orientationVector.get(index) : "asc";
			orderMap.put(field, 
						(gotOrientation.compareTo("asc") == 0 ? 1 : -1) * (index + 1) );
			index++;
		}
		String actionParamTheme = Utils.orderActionParam + "=";
		String label = null;
		Format format = null;
		String targetMember = tag.request().getParameter("targetMember");
		String displayedEntity = getDisplayedEntity();
		for (String head : headsVector) {
			label = tag.getMsgManager().dictionary("attr." + (tag.getMsgManager().isClassSpecificDictionary(displayedEntity, head) ? (displayedEntity + ".") : "") + head);
			format = formatMap.get(head);
			html += "<th >" + (format != null && formatMap.get(head).isAbinary ? label : 
								(tag.targetCommand(label, target(), "btnaslnk", actionParamTheme + actionParamPrefix() + head + (targetMember==null ? "" : ("&targetMember=" + targetMember))) + 
									(orderMap.get(head) == null ? "" : renderSortSymbol(orderMap, head)))
							) +
					"</th>";	
		}
		html += "<th >" + 
				(orderMap.size() == 0 ? "" :  tag.targetCommand(tag.getMsgManager().msgOrKey("command.clearOrder"),  target(), "reset", actionParamTheme + actionParamPrefix() + "_none" )) + 
				"</th>";	
		return html;
	}
	

	protected abstract String actionParamPrefix();

	public String target() { return null;}
	

	private String sexRender(Object value) {
		return value.toString().equalsIgnoreCase("true") ? maleLiteral : femaleLiteral;
	}

	private String valueRender(String fieldName, Object value, HashMap<String, Format> formatMap, ShrEntity childObject, ShrEntity detailObject, HashMap<String, String> imagePreviewTargetMap) {
		if (value instanceof Date) {
			Format format =  formatMap.get(fieldName);
			return tag.getDateFormatter().shrFormat(value, format.pattern, format.style);
		} 
		else if (value == null && formatMap != null && formatMap.get(fieldName) != null && formatMap.get(fieldName).isAbinary || value instanceof byte[])
			return tag.imageTag(tag.getEntityName(), Utils.downCaseFirstChar(detailObject.getClass().getSimpleName()), detailObject.getId(), fieldName, imagePreviewTargetMap.get(fieldName), 
								new ShrImage((byte[]) value, fieldName, true).getFormatName());
		else if (value instanceof ShrEntity)
			return new EntityRenderer(tag.getPageContext()).render((ShrEntity) value);
		else if (fieldName.compareTo("sex") == 0)
			return sexRender(value);
		else 
			return value == null ? "" : String.valueOf(value);
	}
	
	public void packOrder(String order, String orientation) {
		if (order != null) {
			try {
				tag.out().println("<table><tr><td>");
				tag.outHidden(tag.orderParamPrefixName() + "order", order);
				if (orientation != null)
					tag.outHidden(tag.orderParamPrefixName() + "orientation", orientation);
				tag.out().println("</td></tr></table>");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void checkOrderAttrs() {
		orderAttr = (String) tag.request().getAttribute(tag.orderParamPrefixName() + "order");
		orientationAttr = (String) tag.request().getAttribute(tag.orderParamPrefixName() + "orientation");
	}
	
	public String getOrderAttr(String order) {
		checkOrderAttrs();
		return orderAttr == null ? order : orderAttr;
	}

	public String getOrientationAttr(String orientation) {
		return orientationAttr == null ? orientation : orientationAttr;
	}

}
