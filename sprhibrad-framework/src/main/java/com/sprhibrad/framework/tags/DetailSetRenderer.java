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
 * A concrete implementation dedicated to the rendering of a details grid.
 */

public class DetailSetRenderer extends DataSetRenderer {

	private DetailsDataSetManager detailTag;

	public DetailSetRenderer(DetailsDataSetManager tag) {
		super(tag);
		detailTag = tag;
	}

	@Override
	protected String getEntitysName() {
		return Names.plural(getEntityName());
	}

	@Override
	protected String getEntityName() {
		return detailTag.getEntity();
	}

	@Override
	protected String renderRowCommand(Object object, ShrEntity detailObject) {
		return detailTag.detailRowCommand(detailTag.getEntity(), detailTag.getProperty(), String.valueOf(object),
				detailTag.getViewProperty(), detailTag.getNoDelete(), detailObject);
	}

	@Override
	protected String getDisplayedEntity() {
		return detailTag.getProperty() == null ? detailTag.getEntity()
				: Utils.downCaseFirstChar(getClassName(detailTag.getProperty()));
	}

	@Override
	protected String renderCaption() {
		return tag.getMsgManager().dictionary("entities." + Names.plural(getDisplayedEntity()));
	}

	private String getClassName(String property) {
		try {
			return tag.getMsgManager().getModelClass(detailTag.getEntity()).getDeclaredField(property).getType()
					.getSimpleName();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected ShrEntity displayingObject(ShrEntity detailObject) {
		return detailTag.getProperty() == null ? detailObject
				: (ShrEntity) Utils.extractValue(detailTag.getProperty(), detailObject);
	}

	@Override
	protected String actionParamPrefix() {
		return getEntitysName() + "-";
	}

	@Override
	public String target() {
		return ((DataFormTag) detailTag.getFormTag()).getEntityId();
	}

}
