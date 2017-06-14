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

import com.sprhibrad.framework.common.Utils;
import com.sprhibrad.framework.model.ShrEntity;
import com.sprhibrad.names.Names;
/**
 * A concrete implementation dedicated to the rendering of a search result grid. 
 */
public class ResultSetRenderer extends DataSetRenderer {

	private IteratorDataSetManager resultTag;

	public ResultSetRenderer(IteratorDataSetManager tag) {
		super(tag);
		resultTag = tag;
	}

	@Override
	public String target() {
		return String.valueOf(resultTag.getIteration());
	}

	@Override
	protected String getEntitysName() {
		return Names.plural(getEntityName());
	}

	@Override
	protected String getEntityName() {
		return tag.getEntityName();
	}

	@Override
	protected String renderRowCommand(Object object, ShrEntity detailObject) {
		return tag.detailRowCommand(null, null, String.valueOf(object), false, null, detailObject);
	}

	@Override
	protected String renderCaption() {
		return tag.getMsgManager().msgOrKey("label.result");
	}

	@Override
	protected String actionParamPrefix() {
		return Utils.iterResult + "-";
	}

	@Override
	protected String getDisplayedEntity() {
		return getEntityName();
	}


}
