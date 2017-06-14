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

package com.sprhibrad.generator;

import java.util.Vector;

/**
 * Identifies the subdomain of the json tree which the gui selector object moves
 * along. Then identifies the dependent objects that need to be involved in
 * jsonTree-Gui data exchange basing on the selected value.
 */
public class Selector {
	ShrgGuiObject guiObj;
	String attr;
	String domain;

	Vector<Dependent> dependents = new Vector<Dependent>();
	class Dependent {
		ShrgGuiObject guiObj;
		boolean forFeeding;
		public Dependent(ShrgGuiObject guiObj, boolean forFeeding) {
			super();
			this.guiObj = guiObj;
			this.forFeeding = forFeeding;
		}
	}
	
	public void addDependent(ShrgGuiObject guiObject, boolean toGuiOnly) {
		dependents.add(new Dependent(guiObject, toGuiOnly));
	}
	
	public String getDomain() {
		return domain;
	}

	public Selector(Object object, String domain) {
		guiObj = new ShrgGuiObject(object);
		this.domain = domain;
		guiObj.addSelector(this);
	}
		 
	public String getAttr() {
		return attr;
	}
	
	public void setAttr(String attr) {
		boolean doEnable;
		this.attr = attr;
		for(Dependent dependent : dependents) {
			if (attr == null) 
				dependent.guiObj.clear();
			else
				dependent.guiObj.getShrgObject(dependent.forFeeding).set(true);
			doEnable = attr != null;
			if (doEnable && dependent.guiObj.enableOnFieldInterface != null)
				doEnable &= dependent.guiObj.enableOnFieldInterface.isToEnable(attr);
			dependent.guiObj.enable(doEnable);
		}
	}
	
	public ShrgGuiObject getGuiObj() {
		return guiObj;
	}


}
