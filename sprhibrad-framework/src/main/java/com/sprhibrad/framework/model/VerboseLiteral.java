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

import java.util.Vector;

import com.sprhibrad.framework.common.Utils;
 
/**
 * The base class for any entity, made essentially by one descriptive text.
 */

public abstract class VerboseLiteral implements ShrEntity {

	@Override
	public boolean equals(Object obj) {
		if (obj==null)
			return super.equals(obj);
		else
			if (obj instanceof Integer)
				return getId().equals((Integer) obj);
			else
				return super.equals(obj) ||
						getLiteral().compareTo(((VerboseLiteral) obj).getLiteral()) == 0 && 
							getId().equals(((VerboseLiteral) obj).getId()) ;
	}
	
	public abstract String literalField() ;

	public  String getLiteral() {
		return (String) Utils.extractValue(literalField(), this);
	}

	@Override
	public Vector<String> render() {
		Vector<String> retVal = new Vector<String>();
		retVal.add(literalField());
		return retVal;
	}

}
