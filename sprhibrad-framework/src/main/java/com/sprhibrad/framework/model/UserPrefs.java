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

package com.sprhibrad.framework.model;

import java.io.Serializable;
import java.util.Vector;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * It hosts the user preferences :  the 'mode' of the menu (horizontal at the top OR vertical on the left) and the user's Locale 
 * (that drives any format a part from the currency format that is set at application level - see the configuration file of SprHibRAD application)
 */

@Entity
@Table(name="shr_userprefs")
public class UserPrefs implements ShrEntity {
	
	@Id
	private String user;
	
	private String locale;
	
	private Boolean hmenu;
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getLocale() {
		return locale;
	}
	
	public void setLocale(String localeStr) {
		this.locale = localeStr;
	}
	
	public Boolean getHmenu() {
		return hmenu;
	}
	
	public void setHmenu(Boolean hmenu) {
		this.hmenu = hmenu;
	}

	@Override
	public Vector<String> render() {
		return null;
	}
	
	@Override
	public Serializable getId() {
		return user;
	}

}
